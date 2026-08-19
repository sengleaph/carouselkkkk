package com.sifu.mysub.presentation.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Insets every page by [horizontalMarginInPx] on both sides.
 *
 * This is what narrows a ViewPager2 page from the full pager width down to card
 * width: the pager still steps a whole width per page, but the visible card is
 * `pagerWidth - 2 * margin`, which leaves room for the neighbours to peek.
 */
class HorizontalMarginItemDecoration(
    private val horizontalMarginInPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = horizontalMarginInPx
        outRect.right = horizontalMarginInPx
    }
}
