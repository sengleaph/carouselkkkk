package com.sifu.mysub.presentation.subscription

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.sifu.mysub.databinding.PopupSubscriptionMenuBinding

/**
 * The anchored "⋯" menu from the design.
 *
 * A [PopupWindow] rather than a `PopupMenu` because the design needs rounded
 * corners *and* leading icons — `PopupMenu.setForceShowIcon` is API 29+, and
 * theming its background to a rounded shape fights the platform style.
 */
class SubscriptionMenuPopup(
    context: Context,
    private val onEditAccount: () -> Unit,
    private val onUpgradePlan: () -> Unit
) {

    private val binding = PopupSubscriptionMenuBinding.inflate(LayoutInflater.from(context))

    private val window = PopupWindow(
        binding.root,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        /* focusable = */ true
    ).apply {
        // Transparent — the MaterialCardView inside supplies the shape and shadow.
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
    }

    init {
        binding.itemEditAccount.setOnClickListener { dismissThen(onEditAccount) }
        binding.itemUpgradePlan.setOnClickListener { dismissThen(onUpgradePlan) }
    }

    /** Drops the card below [anchor], right edge aligned with the anchor's. */
    fun show(anchor: View) {
        val content = window.contentView
        content.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        // Cancel the shadow padding so the card lines up with the anchor, not the padding box.
        val shadowPad = content.paddingEnd
        val xOffset = anchor.width - content.measuredWidth + shadowPad
        window.showAsDropDown(anchor, xOffset, -shadowPad)
    }

    fun dismiss() = window.dismiss()

    private fun dismissThen(action: () -> Unit) {
        window.dismiss()
        action()
    }
}
