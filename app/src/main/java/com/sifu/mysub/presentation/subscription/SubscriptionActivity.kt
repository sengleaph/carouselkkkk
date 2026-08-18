package com.sifu.mysub.presentation.subscription

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sifu.mysub.MySubApplication
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ActivityMainBinding
import com.sifu.mysub.presentation.subscription.adapter.DetailRowAdapter
import com.sifu.mysub.presentation.upgrade.UpgradePlanBottomSheet
import kotlinx.coroutines.launch

/**
 * Dumb View: renders [SubscriptionUiState], forwards [SubscriptionIntent]s,
 * and performs the Android-only side effects the ViewModel asks for.
 */
class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: SubscriptionViewModel by viewModels {
        (application as MySubApplication).container.subscriptionViewModelFactory()
    }

    private val adapter by lazy {
        DetailRowAdapter { row -> viewModel.onIntent(SubscriptionIntent.RowClicked(row)) }
    }

    private val overflowMenu by lazy {
        SubscriptionMenuPopup(
            context = this,
            onEditAccount = { viewModel.onIntent(SubscriptionIntent.EditAccountClicked) },
            onUpgradePlan = { viewModel.onIntent(SubscriptionIntent.UpgradePlanClicked) }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue header behind the status bar, so force light (white) status icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupViews()
        observeState()
        observeEvents()
    }

    /** The blue header runs under the status bar; only its content is pushed down. */
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarRow.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = bars.top
            }
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun setupViews() = with(binding) {
        rvDetail.adapter = adapter
        rvDetail.itemAnimator = null

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnRetry.setOnClickListener { viewModel.onIntent(SubscriptionIntent.Retry) }
        btnMore.setOnClickListener { overflowMenu.show(it) }
        btnCancel.setOnClickListener {
            toast(getString(R.string.action_cancel_subscription))
        }
    }

    private fun observeState() = launchWhenStarted {
        viewModel.uiState.collect(::render)
    }

    private fun observeEvents() = launchWhenStarted {
        viewModel.events.collect { event ->
            when (event) {
                is SubscriptionUiEvent.CopyToClipboard ->
                    copyToClipboard(event.label, event.value)

                is SubscriptionUiEvent.OpenUrl -> openUrl(event.url)

                SubscriptionUiEvent.NavigateToUpgradePlan -> showUpgradeSheet()

                // TODO: replace with real navigation once that screen exists.
                SubscriptionUiEvent.NavigateToEditAccount ->
                    toast(getString(R.string.menu_edit_account))
            }
        }
    }

    /**
     * Opens the upgrade carousel as a modal bottom sheet.
     *
     * The tag lookup makes this idempotent: a second event (say a double tap on
     * the menu item) would otherwise stack a second sheet on top of the first.
     */
    private fun showUpgradeSheet() {
        if (supportFragmentManager.findFragmentByTag(UpgradePlanBottomSheet.TAG) != null) return
        UpgradePlanBottomSheet.newInstance()
            .show(supportFragmentManager, UpgradePlanBottomSheet.TAG)
    }

    private fun render(state: SubscriptionUiState) = with(binding) {
        progress.isVisible = state.isLoading
        errorGroup.isVisible = state.errorMessage != null
        tvError.text = state.errorMessage.orEmpty()

        scrollContent.isInvisible = !state.isContentVisible
        btnCancel.isVisible = state.isCancelVisible
        btnMore.isVisible = state.isContentVisible
        tvEmpty.isVisible = state.isEmpty

        tvScreenTitle.text = state.categoryName.ifBlank {
            getString(R.string.title_category_default)
        }
        tvLogoCode.text = state.brandCode
        tvBrandName.text = state.brandName

        adapter.submitList(state.rows)
    }

    private fun copyToClipboard(label: String, value: String) {
        getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(label, value))
        toast(getString(R.string.copied_format, label))
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private inline fun launchWhenStarted(crossinline block: suspend () -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) { block() }
        }
    }
}
