package com.yourcompany.loanrisk.presentation.risk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourcompany.loanrisk.R
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.databinding.FragmentRiskBinding

/**
 * Risk breakdown fragment (PRO feature)
 */
class RiskFragment : Fragment() {

    private var _binding: FragmentRiskBinding? = null
    private val binding get() = _binding!!

    private lateinit var loanData: LoanAgreementData
    private lateinit var riskResult: RiskAnalysisResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            loanData = it.getParcelable("loanData") ?: return
            riskResult = it.getParcelable("riskResult") ?: return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayRiskAnalysis()
        setupClickListeners()
    }

    private fun displayRiskAnalysis() {
        with(binding) {
            // Risk score and level
            tvRiskScore.text = "${riskResult.riskScore}/100"
            tvRiskLevel.text = riskResult.riskLevel.name

            // Set color based on risk level
            val color = when (riskResult.riskLevel.name) {
                "CRITICAL" -> android.graphics.Color.RED
                "HIGH" -> android.graphics.Color.rgb(255, 140, 0)
                "MEDIUM" -> android.graphics.Color.rgb(255, 200, 0)
                else -> android.graphics.Color.GREEN
            }
            tvRiskLevel.setTextColor(color)

            // Detailed information
            val details = buildString {
                append("📊 Detailed Analysis\n\n")

                riskResult.effectiveApr?.let {
                    append("Effective APR: ${String.format("%.2f", it)}%\n")
                }

                riskResult.totalCostOfLoan?.let {
                    append("Total Cost: ₹${String.format("%.2f", it)}\n")
                }

                riskResult.prepaymentPenaltyPercentage?.let {
                    if (it > 0) {
                        append("Prepayment Penalty: $it%\n")
                    } else {
                        append("Prepayment Penalty: None ✅\n")
                    }
                }

                if (riskResult.hasArbitrationClause) {
                    append("Contains Arbitration Clause ⚠️\n")
                }

                if (riskResult.hasVaguePenaltyWording) {
                    append("Vague Penalty Terms Detected ⚠️\n")
                }

                riskResult.foreclosureLockInPeriod?.let {
                    append("Lock-in Period: $it\n")
                }

                if (riskResult.hiddenFees.isNotEmpty()) {
                    append("\n🔍 Hidden Fees Detected:\n")
                    riskResult.hiddenFees.forEach { fee ->
                        append("  • $fee\n")
                    }
                }
            }
            tvDetails.text = details

            // Problematic clauses
            if (riskResult.problematicClauses.isNotEmpty()) {
                rvProblematicClauses.layoutManager = LinearLayoutManager(requireContext())
                rvProblematicClauses.adapter = ProblematicClauseAdapter(riskResult.problematicClauses)
            }

            // Recommendations
            val recommendations = riskResult.recommendations.joinToString("\n\n") { "• $it" }
            tvRecommendations.text = recommendations
        }
    }

    private fun setupClickListeners() {
        binding.btnViewCalculator.setOnClickListener {
            val bundle = bundleOf(
                "loanData" to loanData,
                "riskResult" to riskResult
            )
            findNavController().navigate(
                R.id.action_riskFragment_to_calculatorFragment,
                bundle
            )
        }

        binding.btnExportReport.setOnClickListener {
            val bundle = bundleOf(
                "loanData" to loanData,
                "riskResult" to riskResult
            )
            findNavController().navigate(
                R.id.action_riskFragment_to_exportFragment,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
