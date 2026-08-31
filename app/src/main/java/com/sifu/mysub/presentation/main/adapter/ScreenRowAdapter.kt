package com.sifu.mysub.presentation.main.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ItemCardBinding
import com.sifu.mysub.databinding.ItemDetailRowBinding
import com.sifu.mysub.databinding.ItemPlanBinding
import com.sifu.mysub.presentation.main.ScreenRow
import com.sifu.mysub.presentation.main.ValueEmphasis

/**
 * Renders whichever rows the state supplied: cards, subscription detail rows,
 * or plan rows.
 *
 * A plain [RecyclerView.Adapter] holding its own list. It decides nothing —
 * every value arrives final from the ViewModel, so each bind is a straight
 * assignment. Clicks are handed up to the screen.
 */
class ScreenRowAdapter(
    private val onCardClick: (ScreenRow.Card) -> Unit,
    private val onPlanClick: (ScreenRow.Plan) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ScreenRow> = emptyList()

    /**
     * Replaces the whole list.
     *
     * The screen re-renders from a fresh state object rather than mutating rows
     * in place, so the whole list is always what changed. A full invalidation is
     * the honest match for that; the lists here are a handful of rows, and the
     * screens set `itemAnimator = null`, so there is no animation to preserve.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(rows: List<ScreenRow>) {
        items = rows
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ScreenRow.Card -> VIEW_TYPE_CARD
        is ScreenRow.Detail -> VIEW_TYPE_DETAIL
        is ScreenRow.Plan -> VIEW_TYPE_PLAN
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CARD ->
                CardViewHolder(ItemCardBinding.inflate(inflater, parent, false), onCardClick)

            VIEW_TYPE_DETAIL ->
                DetailViewHolder(ItemDetailRowBinding.inflate(inflater, parent, false))

            else ->
                PlanViewHolder(ItemPlanBinding.inflate(inflater, parent, false), onPlanClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = items[position]) {
            is ScreenRow.Card -> (holder as CardViewHolder).bind(row)
            is ScreenRow.Detail -> (holder as DetailViewHolder).bind(row)
            is ScreenRow.Plan -> (holder as PlanViewHolder).bind(row)
        }
    }

    class CardViewHolder(
        private val binding: ItemCardBinding,
        private val onCardClick: (ScreenRow.Card) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ScreenRow.Card) = with(binding) {
            tvCardTitle.text = row.title
            tvCardDescription.text = row.description
            tvCardDescription.isVisible = row.isDescriptionVisible

            tvLogoCode.text = row.logoText
            tvCardTrailing.text = row.trailing
            tvCardTrailing.isVisible = row.trailing.isNotEmpty()

            // Artwork sits over the code; without a URL the code stays visible.
            ivLogo.isVisible = row.imageUrl.isNotEmpty()
            if (row.imageUrl.isNotEmpty()) {
                ivLogo.load(row.imageUrl) { crossfade(true) }
            }

            // A card with nowhere to go should not look or behave like a button.
            val isTappable = row.target != null
            ivChevron.isVisible = isTappable
            root.isClickable = isTappable
            root.setOnClickListener(if (isTappable) { _ -> onCardClick(row) } else null)
        }
    }

    class DetailViewHolder(
        private val binding: ItemDetailRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ScreenRow.Detail) = with(binding) {
            tvTitle.text = row.title
            tvValue.text = row.value
            divider.isVisible = row.hasDivider

            tvValue.setTextColor(ContextCompat.getColor(root.context, row.emphasis.colorRes()))
            tvValue.typeface = if (row.isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        /** A lookup, not a decision: the meaning was chosen in the ViewModel. */
        private fun ValueEmphasis.colorRes(): Int = when (this) {
            ValueEmphasis.NEGATIVE -> R.color.value_red
            ValueEmphasis.COPYABLE -> R.color.copy_gold
            ValueEmphasis.LINK -> R.color.link_blue
            ValueEmphasis.PLAIN -> R.color.text_primary
        }
    }

    class PlanViewHolder(
        private val binding: ItemPlanBinding,
        private val onPlanClick: (ScreenRow.Plan) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: ScreenRow.Plan) = with(binding) {
            tvPlanName.text = row.name
            tvPlanPrice.text = row.price
            root.setOnClickListener { onPlanClick(row) }
        }
    }

    private companion object {
        const val VIEW_TYPE_CARD = 0
        const val VIEW_TYPE_DETAIL = 1
        const val VIEW_TYPE_PLAN = 2
    }
}
