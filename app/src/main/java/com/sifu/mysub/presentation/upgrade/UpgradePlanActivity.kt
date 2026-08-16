package com.sifu.mysub.presentation.upgrade

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sifu.mysub.MySubApplication
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ActivityUpgradePlanBinding
import com.sifu.mysub.presentation.upgrade.adapter.PlanCarouselAdapter
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * The upgrade sheet. Runs as a translucent Activity so the subscription screen
 * stays visible behind the scrim, and is reached with a plain Intent.
 */
class UpgradePlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpgradePlanBinding

    private val viewModel: UpgradePlanViewModel by viewModels {
        (application as MySubApplication).container.upgradePlanViewModelFactory()
    }

    private val adapter by lazy {
        PlanCarouselAdapter { position -> binding.planCarousel.currentItem = position }
    }

    /** Guards the one-shot jump to the recommended card. */
    private var hasSettledOnRecommended = false

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.onIntent(UpgradePlanIntent.PageSelected(position))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpgradePlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupCarousel()
        setupClicks()
        observeState()
        observeEvents()
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.upgradeRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.sheet.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    /**
     * Overlapping-carousel setup. Four pieces do the work:
     *  1. horizontal padding on the pager reserves the space neighbours peek into;
     *  2. clipToPadding/clipChildren=false let them actually draw there;
     *  3. translationX pulls the neighbours inwards so the centre card covers
     *     their inner edges — that is the stacked look in the design;
     *  4. translationZ lifts the centre card above them, since without it the
     *     later page would draw on top of the one being centred.
     */
    private fun setupCarousel() = with(binding.planCarousel) {
        adapter = this@UpgradePlanActivity.adapter
        offscreenPageLimit = 3

        val sidePadding = (resources.displayMetrics.widthPixels * SIDE_PADDING_RATIO).toInt()

        // The padding goes on the inner RecyclerView, NOT on ViewPager2: padding
        // on ViewPager2 shrinks the RecyclerView's bounds, and a child only gets
        // touch events inside its own bounds — the side cards would still draw
        // (clipChildren=false) but swipes starting on them would be ignored.
        (getChildAt(0) as? RecyclerView)?.apply {
            setPadding(sidePadding, 0, sidePadding, 0)
            clipToPadding = false
            clipChildren = false
        }

        val liftPx = LIFT_DP * resources.displayMetrics.density

        setPageTransformer { page, position ->
            val centreness = 1f - abs(position).coerceAtMost(1f)

            page.translationX = -position * page.width * OVERLAP_RATIO
            page.translationZ = centreness * liftPx

            val scale = MIN_SCALE + centreness * (1f - MIN_SCALE)
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = MIN_ALPHA + centreness * (1f - MIN_ALPHA)

            // Gradient border belongs to whichever card is centred.
            page.findViewById<View>(R.id.selectionGlow)?.alpha = centreness
        }

        registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun setupClicks() = with(binding) {
        btnClose.setOnClickListener { viewModel.onIntent(UpgradePlanIntent.Close) }
        scrimTouch.setOnClickListener { viewModel.onIntent(UpgradePlanIntent.Close) }
        btnOk.setOnClickListener { viewModel.onIntent(UpgradePlanIntent.Confirm) }
    }

    private fun observeState() = launchWhenStarted {
        viewModel.uiState.collect { state ->
            with(binding) {
                progress.isVisible = state.isLoading
                tvError.isVisible = state.errorMessage != null
                tvError.text = state.errorMessage.orEmpty()
                planCarousel.isVisible = state.isContentVisible
                btnOk.isEnabled = state.selectedPlan != null

                tvTitle.text = state.title
                tvLogoCode.text = state.brandCode
            }
            // Two ordering hazards here:
            //  - setCurrentItem on a not-yet-populated adapter is dropped, so it
            //    has to run in submitList's commit callback;
            //  - that callback runs synchronously on the empty -> populated fast
            //    path, i.e. before the pager has laid out, where the scroll is
            //    dropped again. post() defers it past that first layout pass.
            adapter.submitList(state.plans) {
                if (!hasSettledOnRecommended && state.plans.isNotEmpty()) {
                    hasSettledOnRecommended = true
                    settleOn(state.selectedIndex)
                }
            }
        }
    }

    /**
     * Jumps the carousel to [index] on first load.
     *
     * `post` clears the pending layout pass — before it, the scroll is dropped.
     * `doOnPreDraw` + [ViewPager2.requestTransform] then re-runs the page
     * transformer against the new scroll offset; without it the pages keep the
     * scale/alpha/glow they were given at the old position.
     */
    private fun settleOn(index: Int) = with(binding.planCarousel) {
        post {
            setCurrentItem(index, false)
            doOnPreDraw { requestTransform() }
        }
    }

    private fun observeEvents() = launchWhenStarted {
        viewModel.events.collect { event ->
            when (event) {
                is UpgradePlanUiEvent.Confirmed -> {
                    // TODO: hand the chosen plan to the purchase flow.
                    Toast.makeText(
                        this@UpgradePlanActivity,
                        getString(
                            R.string.upgrade_selected_format,
                            event.plan.title,
                            event.plan.price
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                UpgradePlanUiEvent.Dismiss -> finish()
            }
        }
    }

    override fun onDestroy() {
        binding.planCarousel.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onDestroy()
    }

    private inline fun launchWhenStarted(crossinline block: suspend () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
        }
    }

    private companion object {
        /** Fraction of screen width reserved on each side for the peeking cards. */
        const val SIDE_PADDING_RATIO = 0.30f

        /** How far neighbours slide towards the centre, as a fraction of page width. */
        const val OVERLAP_RATIO = 0.20f

        const val MIN_SCALE = 0.80f
        const val MIN_ALPHA = 0.65f
        const val LIFT_DP = 8f
    }
}
