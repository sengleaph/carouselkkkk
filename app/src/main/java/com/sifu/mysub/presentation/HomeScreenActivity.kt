package com.sifu.mysub.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sifu.mysub.MySubApplication
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ActivityHomeScreenBinding
import com.sifu.mysub.presentation.subscription.SubscriptionActivity
import kotlinx.coroutines.launch

/**
 * Dumb View: renders [HomeUiState] and calls plain functions on the ViewModel.
 * No intent type and no event channel — navigation is the screen's only side
 * effect and it happens inline, on the tap that caused it.
 */
class HomeScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding

    private val viewModel: HomeViewModel by viewModels {
        (application as MySubApplication).container.homeViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue header behind the status bar, so force light (white) status icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupViews()
        observeState()
    }

    /** The blue header runs under the status bar; only its title is pushed down. */
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.homeRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.tvHomeTitle.updatePadding(top = bars.top + TITLE_TOP_PADDING_DP.dp)
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun setupViews() = with(binding) {
        cardSubscription.setOnClickListener { openSubscription() }
        btnRetry.setOnClickListener { viewModel.load() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) = with(binding) {
        progress.isVisible = state.isLoading
        errorGroup.isVisible = state.errorMessage != null
        tvError.text = state.errorMessage.orEmpty()

        cardSubscription.isVisible = state.isCardVisible
        tvEmpty.isVisible = state.isEmptyVisible

        tvLogoCode.text = state.brandCode
        tvBrandName.text = state.brandName
        tvPlanName.text = state.planName
        tvAmount.text = state.amount
        tvRenew.text = if (state.renew.isBlank()) {
            ""
        } else {
            getString(R.string.home_renew_format, state.renew)
        }
    }

    private fun openSubscription() {
        if (!viewModel.canOpenSubscription()) return
        startActivity(Intent(this, SubscriptionActivity::class.java))
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private companion object {
        const val TITLE_TOP_PADDING_DP = 14
    }
}
