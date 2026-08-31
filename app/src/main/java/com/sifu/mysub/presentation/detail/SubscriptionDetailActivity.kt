package com.sifu.mysub.presentation.detail

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sifu.mysub.MySubApplication
import com.sifu.mysub.databinding.ActivityServiceBinding
import com.sifu.mysub.presentation.main.adapter.ScreenRowAdapter
import kotlinx.coroutines.launch

/**
 * The subscription's detail rows, opened by tapping its card on the main screen.
 *
 * Shares [ActivityServiceBinding] with the plans screen: both are a toolbar over a
 * list of [com.sifu.mysub.presentation.main.ScreenRow]s, so a second identical
 * layout would only be a copy to keep in step.
 */
class SubscriptionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceBinding

    private val viewModel: SubscriptionDetailViewModel by viewModels {
        (application as MySubApplication).container.subscriptionDetailViewModelFactory()
    }

    // Neither row type on this screen is tappable yet.
    private val rowAdapter = ScreenRowAdapter(onCardClick = {}, onPlanClick = {})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue header behind the status bar, so force light (white) status icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupViews()
        observeState()

        // The ViewModel no longer loads itself, so the screen asks. Guarded on
        // the current state because the ViewModel outlives a rotation: without
        // it, every configuration change would re-read and blink the list.
        if (viewModel.uiState.value.rows.isEmpty()) viewModel.load()
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.serviceRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarRow.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = bars.top
            }
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun setupViews() = with(binding) {
        rvContent.adapter = rowAdapter
        rvContent.itemAnimator = null
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnRetry.setOnClickListener { viewModel.load() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: SubscriptionDetailUiState) = with(binding) {
        progress.isVisible = state.isLoading
        errorGroup.isVisible = state.errorMessage != null
        tvError.text = state.errorMessage.orEmpty()

        tvTitle.text = state.title
        rvContent.isVisible = state.isContentVisible
        rowAdapter.setData(state.rows)
    }

    companion object {
        fun newInstance(context: Context): Intent =
            Intent(context, SubscriptionDetailActivity::class.java)
    }
}
