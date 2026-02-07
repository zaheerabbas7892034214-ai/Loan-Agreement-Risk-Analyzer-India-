package com.yourcompany.loanrisk.presentation.risk

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourcompany.loanrisk.data.model.ProblematicClause
import com.yourcompany.loanrisk.databinding.ItemProblematicClauseBinding

/**
 * Adapter for problematic clauses
 */
class ProblematicClauseAdapter(
    private val clauses: List<ProblematicClause>
) : RecyclerView.Adapter<ProblematicClauseAdapter.ClauseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClauseViewHolder {
        val binding = ItemProblematicClauseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClauseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClauseViewHolder, position: Int) {
        holder.bind(clauses[position])
    }

    override fun getItemCount() = clauses.size

    class ClauseViewHolder(
        private val binding: ItemProblematicClauseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(clause: ProblematicClause) {
            with(binding) {
                tvTitle.text = clause.title
                tvSeverity.text = clause.severity
                tvContent.text = clause.content
                tvExplanation.text = clause.explanation

                // Set severity color
                val color = when (clause.severity) {
                    "High" -> android.graphics.Color.RED
                    "Medium" -> android.graphics.Color.rgb(255, 140, 0)
                    else -> android.graphics.Color.GRAY
                }
                tvSeverity.setTextColor(color)
            }
        }
    }
}
