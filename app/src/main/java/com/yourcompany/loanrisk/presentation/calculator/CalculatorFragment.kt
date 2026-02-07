package com.yourcompany.loanrisk.presentation.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.databinding.FragmentCalculatorBinding

/**
 * Cost calculator fragment (PRO feature)
 */
class CalculatorFragment : Fragment() {

    private var _binding: FragmentCalculatorBinding? = null
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
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayCostBreakdown()
    }

    private fun displayCostBreakdown() {
        with(binding) {
            // Principal
            loanData.principalAmount?.let {
                tvPrincipal.text = "₹${String.format("%.2f", it)}"
            } ?: run {
                tvPrincipal.text = "N/A"
            }

            // Processing Fee
            val processingFee = calculateProcessingFee()
            tvProcessingFee.text = "₹${String.format("%.2f", processingFee)}"

            // Interest Rate
            loanData.interestRate?.let {
                tvInterestRate.text = "$it% p.a."
            } ?: run {
                tvInterestRate.text = "N/A"
            }

            // Effective APR
            riskResult.effectiveApr?.let {
                tvEffectiveApr.text = "${String.format("%.2f", it)}% p.a."
            } ?: run {
                tvEffectiveApr.text = "N/A"
            }

            // EMI
            loanData.emiAmount?.let {
                tvEmi.text = "₹${String.format("%.2f", it)}"
            } ?: run {
                tvEmi.text = "N/A"
            }

            // Total Cost
            riskResult.totalCostOfLoan?.let {
                tvTotalCost.text = "₹${String.format("%.2f", it)}"
            } ?: run {
                tvTotalCost.text = "N/A"
            }

            // Hidden Fees
            if (riskResult.hiddenFees.isNotEmpty()) {
                tvHiddenFees.text = riskResult.hiddenFees.joinToString("\n") { "• $it" }
                tvHiddenFees.visibility = View.VISIBLE
            } else {
                tvHiddenFeesLabel.visibility = View.GONE
                tvHiddenFees.visibility = View.GONE
            }

            // Comparison with standard rates
            val comparisonText = buildString {
                append("💡 Rate Comparison\n\n")
                
                loanData.interestRate?.let { rate ->
                    when {
                        rate > 15 -> append("Your rate ($rate%) is HIGHER than typical bank rates (9-12%)")
                        rate > 12 -> append("Your rate ($rate%) is slightly HIGHER than typical bank rates (9-12%)")
                        else -> append("Your rate ($rate%) is competitive with bank rates (9-12%)")
                    }
                }
            }
            tvComparison.text = comparisonText
        }
    }

    private fun calculateProcessingFee(): Double {
        val principal = loanData.principalAmount ?: 100000.0
        val feeStr = loanData.processingFee ?: return 0.0

        return when {
            feeStr.contains("%") -> {
                val percentage = feeStr.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                principal * (percentage / 100.0)
            }
            else -> feeStr.toDoubleOrNull() ?: 0.0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
