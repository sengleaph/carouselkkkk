package com.sifu.mysub

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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sifu.mysub.databinding.ActivityMainBinding
import com.sifu.mysub.presentation.main.CardTarget
import com.sifu.mysub.presentation.main.Header
import com.sifu.mysub.presentation.main.MainUiState
import com.sifu.mysub.presentation.main.MainViewModel
import com.sifu.mysub.presentation.main.adapter.ScreenRowAdapter
import com.sifu.mysub.presentation.service.ServiceActivity
import kotlinx.coroutines.launch

/**
 * Reads `subscription.json` and shows one of two faces of the same screen.
 *
 * `haveSub = true` puts the brand on the header with a single card beneath it;
 * `haveSub = false` puts the category tile up top and the searchable service
 * list below. Which one is a value in the state, so this class never branches
 * on `haveSub` itself.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        (application as MySubApplication).container.mainViewModelFactory()
    }

    // Only cards appear on this screen; plans live behind their own.
    private val rowAdapter = ScreenRowAdapter(
        onCardClick = { card -> openCard(card.target) },
        onPlanClick = { }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue header behind the status bar, so force light (white) status icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupViews()
        observeState()

        // The ViewModel no longer loads itself, so the screen asks. Guarded on
        // the current state because the ViewModel outlives a rotation: without
        // it, every configuration change would re-read and blink the list.
        if (viewModel.uiState.value.rows.isEmpty()) viewModel.load()
    }

    /** The blue header runs under the status bar; only the toolbar is pushed down. */
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
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
        btnClearSearch.setOnClickListener { edtSearch.text.clear() }

        // The ViewModel owns the filtering; this only reports what was typed.
        edtSearch.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: MainUiState) = with(binding) {
        progress.isVisible = state.isLoading
        errorGroup.isVisible = state.errorMessage != null
        tvError.text = state.errorMessage.orEmpty()

        tvTitle.text = state.title
        renderHeader(state.header)

        // The white panel exists to hold the search field, so they share a fate.
        panelBg.isVisible = state.isSearchVisible
        searchField.isVisible = state.isSearchVisible
        btnClearSearch.isVisible = state.searchQuery.isNotEmpty()

        rvContent.isVisible = state.isContentVisible
        tvEmpty.isVisible = state.isEmptyResult
        rowAdapter.setData(state.rows)
    }

    private fun renderHeader(header: Header) = with(binding) {
        categoryIcon.isVisible = header is Header.Category
        brandHeader.isVisible = header is Header.Brand

        if (header is Header.Brand) {
            tvHeaderLogoCode.text = header.logoText
            tvHeaderBrandName.text = header.name
        }
    }

    /**
     * Navigation happens on the tap that caused it, never from a value parked in
     * state -- a gesture cannot replay, so there is nothing to consume or guard.
     */
    private fun openCard(target: CardTarget?) {
        val intent = when (target) {
            is CardTarget.Service -> ServiceActivity.newInstance(this, target.serviceCode)
            // Nothing on the browse screen opens the detail directly.
            CardTarget.SubscriptionDetail, null -> return
        }
        startActivity(intent)
    }
}
