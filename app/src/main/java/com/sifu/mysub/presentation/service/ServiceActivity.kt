package com.sifu.mysub.presentation.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
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
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ActivityServiceBinding
import com.sifu.mysub.presentation.detail.SubscriptionDetailActivity
import com.sifu.mysub.presentation.main.CardTarget
import com.sifu.mysub.presentation.main.Header
import com.sifu.mysub.presentation.main.adapter.ScreenRowAdapter
import kotlinx.coroutines.launch

/**
 * One service, opened by tapping its card on the browse screen.
 *
 * Shows the subscription the user holds for it, or the plans it offers — which
 * one is decided in the ViewModel and arrives as state.
 */
class ServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceBinding

    private val serviceCode: String
        get() = intent.getStringExtra(EXTRA_SERVICE_CODE).orEmpty()

    private val viewModel: ServiceViewModel by viewModels {
        (application as MySubApplication).container.serviceViewModelFactory(serviceCode)
    }

    private val rowAdapter = ScreenRowAdapter(
        onCardClick = { card ->
            if (card.target == CardTarget.SubscriptionDetail) {
                startActivity(SubscriptionDetailActivity.newInstance(this))
            }
        },
        onPlanClick = { plan ->
            // TODO: hand the chosen plan to the subscribe flow.
            Toast.makeText(
                this,
                getString(R.string.plan_selected_format, plan.name, plan.price),
                Toast.LENGTH_SHORT
            ).show()
        }
    )

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

    private fun render(state: ServiceUiState) = with(binding) {
        progress.isVisible = state.isLoading
        errorGroup.isVisible = state.errorMessage != null
        tvError.text = state.errorMessage.orEmpty()

        tvTitle.text = state.title

        val header = state.header
        brandHeader.isVisible = header is Header.Brand
        if (header is Header.Brand) {
            tvHeaderLogoCode.text = header.logoText
            tvHeaderBrandName.text = header.name
        }

        rvContent.isVisible = state.isContentVisible
        rowAdapter.setData(state.rows)
    }

    companion object {
        private const val EXTRA_SERVICE_CODE = "extra_service_code"

        /**
         * Carries the service code, not the serialized row: the screen reloads
         * from the same source the menu used, so nothing stale crosses the
         * Intent and the payload stays far below the Binder transaction limit.
         */
        fun newInstance(context: Context, serviceCode: String): Intent =
            Intent(context, ServiceActivity::class.java)
                .putExtra(EXTRA_SERVICE_CODE, serviceCode)
    }
}
