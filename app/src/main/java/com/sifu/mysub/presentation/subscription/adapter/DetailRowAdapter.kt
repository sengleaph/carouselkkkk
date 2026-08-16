package com.sifu.mysub.presentation.subscription.adapter

import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ItemDetailRowBinding
import com.sifu.mysub.domain.model.DetailRow
import com.sifu.mysub.domain.model.RowEmphasis
import com.sifu.mysub.domain.model.RowStyle

/**
 * Renders domain [DetailRow]s. It reads only typed properties — no string
 * parsing survives this far up the stack.
 */
class DetailRowAdapter(
    private val onRowClick: (DetailRow) -> Unit
) : ListAdapter<DetailRow, DetailRowAdapter.RowViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RowViewHolder(
        ItemDetailRowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onRowClick
    )

    override fun onBindViewHolder(holder: RowViewHolder, position: Int) =
        holder.bind(getItem(position))

    class RowViewHolder(
        private val binding: ItemDetailRowBinding,
        private val onRowClick: (DetailRow) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: DetailRow) = with(binding) {
            tvTitle.text = row.title
            tvValue.text = row.value

            tvValue.setTextColor(
                ContextCompat.getColor(root.context, row.valueColorRes())
            )
            tvValue.setTypeface(null, if (row.bold) Typeface.BOLD else Typeface.NORMAL)
            tvValue.underline(row.style == RowStyle.LINK)

            ivCopy.isVisible = row.style == RowStyle.COPYABLE

            tvRemark.text = row.remark.orEmpty()
            tvRemark.isVisible = row.remark != null

            divider.isVisible = row.showDivider

            val interactive = row.style != RowStyle.PLAIN
            root.isClickable = interactive
            root.isFocusable = interactive
            root.setOnClickListener(
                if (interactive) View.OnClickListener { onRowClick(row) } else null
            )
        }

        private fun DetailRow.valueColorRes(): Int = when {
            emphasis == RowEmphasis.NEGATIVE -> R.color.value_red
            style == RowStyle.LINK -> R.color.link_blue
            else -> R.color.text_primary
        }

        private fun android.widget.TextView.underline(enabled: Boolean) {
            paintFlags = if (enabled) {
                paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<DetailRow>() {
            override fun areItemsTheSame(oldItem: DetailRow, newItem: DetailRow) =
                oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: DetailRow, newItem: DetailRow) =
                oldItem == newItem
        }
    }
}
