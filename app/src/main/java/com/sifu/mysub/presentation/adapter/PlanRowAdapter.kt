package com.sifu.mysub.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sifu.mysub.databinding.ItemPlanRowBinding
import com.sifu.mysub.presentation.PlanRow

/**
 * The upgrade plans from `res/raw/upgrade_plans.json`, as subscription-style
 * cards under the subscription card on Home.
 *
 * Takes [PlanRow] rather than the domain `UpgradePlan`: the brand code shown in
 * the logo pill belongs to the offer, not to any one plan, and folding it into
 * the row keeps the adapter free of adapter-wide mutable state that DiffUtil
 * would not know to rebind on.
 */
class PlanRowAdapter(
    private val onPlanClick: (PlanRow) -> Unit
) : ListAdapter<PlanRow, PlanRowAdapter.PlanRowViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PlanRowViewHolder(
        ItemPlanRowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onPlanClick
    )

    override fun onBindViewHolder(holder: PlanRowViewHolder, position: Int) =
        holder.bind(getItem(position))

    class PlanRowViewHolder(
        private val binding: ItemPlanRowBinding,
        private val onPlanClick: (PlanRow) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: PlanRow) = with(binding) {
            tvLogoCode.text = row.brandCode
            tvPlanTitle.text = row.brandCode
            tvPlanPrice.text = row.price
            // Caption text is the fixed "Recommended" label from the layout, so
            // only its visibility varies.
            tvPlanCaption.isVisible = row.isRecommended

            root.setOnClickListener { onPlanClick(row) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<PlanRow>() {
            override fun areItemsTheSame(oldItem: PlanRow, newItem: PlanRow) =
                oldItem.code == newItem.code

            override fun areContentsTheSame(oldItem: PlanRow, newItem: PlanRow) =
                oldItem == newItem
        }
    }
}
