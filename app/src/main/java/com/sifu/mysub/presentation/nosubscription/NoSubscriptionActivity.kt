package com.sifu.mysub.presentation.nosubscription

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ActivityNoSubscriptionBinding

/**
 * Shown when a plan is tapped and `subscription.json` reports `"haveSub": false`.
 *
 * Deliberately stateless: there is no ViewModel because there is nothing to
 * load. Home has already made the only decision that matters — which of the two
 * destinations to open — so this screen just says so and offers the next step.
 */
class NoSubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue header behind the status bar, so force light (white) status icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        binding = ActivityNoSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        setupViews()
    }

    /** The blue header runs under the status bar; only its content is pushed down. */
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.noSubRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarRow.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = bars.top
            }
            view.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun setupViews() = with(binding) {
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnSubscribe.setOnClickListener {
            // TODO: hand off to the purchase flow once that exists.
            Toast.makeText(
                this@NoSubscriptionActivity,
                getString(R.string.action_subscribe),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
