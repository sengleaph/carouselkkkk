package com.sifu.mysub.presentation.upgrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sifu.mysub.MySubApplication
import com.sifu.mysub.R
import com.sifu.mysub.core.util.tryThis
import com.sifu.mysub.databinding.SheetUpgradePlanBinding
import com.sifu.mysub.presentation.common.HorizontalMarginItemDecoration
import com.sifu.mysub.presentation.upgrade.adapter.PlanCarouselAdapter
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * The upgrade plan carousel, as a modal bottom sheet.
 *
 * A [BottomSheetDialogFragment] rather than a translucent Activity: the dim, the
 * outside-tap dismiss, the swipe-down gesture and the slide animation all come
 * for free, and the flow stays inside the host Activity.
 */
class UpgradePlanBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetUpgradePlanBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: UpgradePlanViewModel by lazy {
        val factory = (requireActivity().application as MySubApplication)
            .container
            .upgradePlanViewModelFactory()
        ViewModelProvider(this, factory)[UpgradePlanViewModel::class.java]
    }

    private val adapter by lazy {
        PlanCarouselAdapter { position -> binding.planCarousel.currentItem = position }
    }

    /** Guards the one-shot jump to the recommended card. */
    private var hasSettledOnRecommended = false

    /** Guards the carousel wiring, which is per-view and must not stack up. */
    private var isCarouselInitialized = false

    /** Kept so a re-run swaps the decoration instead of adding a second one. */
    private var currentItemDecoration: RecyclerView.ItemDecoration? = null

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.onIntent(UpgradePlanIntent.PageSelected(position))
        }
    }

    /** Transparent sheet background so the layout supplies the rounded panel. */
    override fun getTheme() = R.style.ThemeOverlay_MySub_BottomSheet

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetUpgradePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyInsets()
        setUpCarousel()
        setupClicks()
        observeState()
        observeEvents()
    }

    override fun onStart() {
        super.onStart()
        // The panel is short, so a collapsed state would only clip it: open
        // fully, and let a drag down dismiss rather than collapse.
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    /**
     * Keeps the white panel running all the way to the bottom edge: the sheet
     * itself takes the navigation-bar inset as padding, instead of the dialog
     * container being padded and leaving a gap under the panel.
     */
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.sheet) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    /**
     * Attaches the pager and sizes the card for this screen.
     *
     * The geometry is proportional, not a table of pixel constants: the card is
     * a fraction of the screen width, and its height follows from the artwork's
     * own 180:241 ratio. Raw-pixel sizing would hold the card at one physical
     * width, which reads as a normal card at 1080p and as a postage stamp at
     * 1440p; a fraction keeps it the same share of the screen on both.
     */
    private fun setUpCarousel() {
        if (isCarouselInitialized) return
        tryThis(TAG) {
            binding.planCarousel.adapter = adapter
            binding.planCarousel.registerOnPageChangeCallback(pageChangeCallback)

            val screenWidthInPx = resources.displayMetrics.widthPixels

            // The cap is what a fraction alone gets wrong: 42% of a tablet is a
            // poster, so past ~200dp the card stops growing and the stack simply
            // sits in the middle of the wider panel.
            val maxCardWidthInPx = MAX_CARD_WIDTH_DP * resources.displayMetrics.density
            val cardWidthInPx = (screenWidthInPx * CARD_WIDTH_RATIO)
                .coerceAtMost(maxCardWidthInPx)
                .toInt()
            val cardHeightInPx = (cardWidthInPx / CARD_ASPECT_RATIO).toInt()

            transformEffect(screenWidthInPx, cardWidthInPx, cardHeightInPx)

            if (screenWidthInPx >= WIDE_SCREEN_PX) showEdgeMasks()

            isCarouselInitialized = true
        }
    }

    /**
     * The overlapping-stack effect. Four pieces do the work:
     *  1. the item decoration narrows each page to card width, leaving the rest
     *     of the pager width as the space neighbours peek into;
     *  2. clipToPadding/clipChildren=false let them actually draw there;
     *  3. translationX pulls the neighbours inwards so the centre card covers
     *     their outer edges -- that is the stacked look in the design;
     *  4. translationZ lifts the centre card above them, since without it the
     *     later page would draw on top of the one being centred.
     *
     * offscreenPageLimit=4 keeps the outer cards of the stack alive; without it
     * the pager tears them down and the stack looks two cards deep.
     */
    private fun transformEffect(
        screenWidthInPx: Int,
        cardWidthInPx: Int,
        cardHeightInPx: Int
    ) {
        tryThis(TAG) {
            binding.planCarousel.offscreenPageLimit = 4
            binding.planCarousel.clipChildren = false
            binding.planCarousel.clipToPadding = false
            val recycler = binding.planCarousel.getChildAt(0) as? RecyclerView ?: return@tryThis
            recycler.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val sideInsetInPx = (screenWidthInPx - cardWidthInPx) / 2

            // Pages are laid out a whole pager width apart. Pulling each one back
            // by this much leaves neighbouring card centres NEIGHBOUR_STEP_RATIO
            // of a card apart, which is the overlap the design shows -- roughly
            // two thirds of the next card visible, not a sliver.
            val pageTranslationX = screenWidthInPx - cardWidthInPx * NEIGHBOUR_STEP_RATIO

            val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                page.apply {
                    val absPos = abs(position)
                    val scale = 1f - (1f - NEIGHBOUR_SCALE) * absPos

                    scaleX = scale
                    scaleY = scale
                    translationX = -pageTranslationX * position
                    translationZ = (1f - absPos) * CENTRE_LIFT_PX
                }
            }

            setHeight(cardHeightInPx)
            binding.planCarousel.setPageTransformer(pageTransformer)
            currentItemDecoration?.let {
                recycler.removeItemDecoration(it)
                currentItemDecoration = null
            }
            val itemDecoration = HorizontalMarginItemDecoration(sideInsetInPx)
            binding.planCarousel.addItemDecoration(itemDecoration)
            currentItemDecoration = itemDecoration
        }
    }

    /** Pager height in raw pixels, so the page box keeps the artwork's ratio. */
    private fun setHeight(heightInPx: Int) {
        binding.planCarousel.updateLayoutParams { height = heightInPx }
    }

    /** Covers the outermost strip on folds and tablets; width lives in the layout. */
    private fun showEdgeMasks() = with(binding) {
        hideLeft.isVisible = true
        hideRight.isVisible = true
    }

    private fun setupClicks() = with(binding) {
        btnClose.setOnClickListener { viewModel.onIntent(UpgradePlanIntent.Close) }
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
            //    has to run in the submitList commit callback;
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
                        requireContext(),
                        getString(
                            R.string.upgrade_selected_format,
                            event.plan.title,
                            event.plan.price
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }

                UpgradePlanUiEvent.Dismiss -> dismiss()
            }
        }
    }

    override fun onDestroyView() {
        binding.planCarousel.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
        super.onDestroyView()
    }

    /** Bound to the view lifecycle: collection stops when the sheet view goes. */
    private inline fun launchWhenStarted(crossinline block: suspend () -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
        }
    }

    companion object {
        const val TAG = "UpgradePlanBottomSheet"

        fun newInstance() = UpgradePlanBottomSheet()

        /** Card width as a share of the screen, and the cap that keeps tablets sane. */
        private const val CARD_WIDTH_RATIO = 0.42f
        private const val MAX_CARD_WIDTH_DP = 200f

        /** pinbase.png is 180x241; the page box holds that so the art is not stretched. */
        private const val CARD_ASPECT_RATIO = 180f / 241f

        /** Gap between neighbouring card centres, as a share of one card. */
        private const val NEIGHBOUR_STEP_RATIO = 0.66f

        /** Scale of a card one page out from the centre. */
        private const val NEIGHBOUR_SCALE = 0.88f

        /** Lift of the centred card over its neighbours, in raw pixels. */
        private const val CENTRE_LIFT_PX = 20f

        /** Fold/tablet width past which the panel is wide enough to need the masks. */
        private const val WIDE_SCREEN_PX = 1768
    }
}
