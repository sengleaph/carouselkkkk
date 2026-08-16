package com.sifu.mysub.presentation.upgrade.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sifu.mysub.R
import com.sifu.mysub.databinding.ItemPlanCardBinding
import com.sifu.mysub.domain.model.PlanTheme
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

        fun bind(plan: UpgradePlan) = with(binding) {
            tvPlanTitle.text = plan.title
            tvPlanPrice.text = plan.price

            planArtwork.background = ContextCompat.getDrawable(
                root.context,
                plan.theme.artworkRes()
            )

            // Tapping an off-centre card scrolls it into place.
            root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onPlanClick(bindingAdapterPosition)
                }
            }
        }

        /** Domain theme -> drawable. Resource IDs stay out of the domain layer. */
        private fun PlanTheme.artworkRes(): Int = when (this) {
            PlanTheme.PURPLE -> R.drawable.bg_plan_purple
            PlanTheme.BLUE -> R.drawable.bg_plan_blue
            PlanTheme.PINK -> R.drawable.bg_plan_pink
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
