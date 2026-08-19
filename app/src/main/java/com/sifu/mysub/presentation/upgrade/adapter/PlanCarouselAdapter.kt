package com.sifu.mysub.presentation.upgrade.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sifu.mysub.databinding.ItemPlanCardBinding
import com.sifu.mysub.domain.model.UpgradePlan

class PlanCarouselAdapter(
    private val onPlanClick: (Int) -> Unit
) : ListAdapter<UpgradePlan, PlanCarouselAdapter.PlanViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PlanViewHolder(
        ItemPlanCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onPlanClick
    )

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) =
        holder.bind(getItem(position))

    class PlanViewHolder(
        private val binding: ItemPlanCardBinding,
        private val onPlanClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // The card face is the pinbase artwork, set once in the layout and the
        // same for every plan, so binding only fills in the price pill.
        fun bind(plan: UpgradePlan) = with(binding) {
            tvPlanTitle.text = plan.title
            tvPlanPrice.text = plan.price

            // Tapping an off-centre card scrolls it into place.
            root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onPlanClick(bindingAdapterPosition)
                }
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<UpgradePlan>() {
            override fun areItemsTheSame(oldItem: UpgradePlan, newItem: UpgradePlan) =
                oldItem.code == newItem.code

            override fun areContentsTheSame(oldItem: UpgradePlan, newItem: UpgradePlan) =
                oldItem == newItem
        }
    }
}
