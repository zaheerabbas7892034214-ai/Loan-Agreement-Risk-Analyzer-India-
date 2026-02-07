package com.yourcompany.loanrisk.presentation.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yourcompany.loanrisk.R
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.databinding.FragmentSummaryBinding
import com.yourcompany.loanrisk.presentation.MainActivity

/**
 * Clause summary fragment (FREE version with limited preview)
 */
class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
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
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displaySummary()
        setupClickListeners()
    }

    private fun displaySummary() {
        // Display basic extracted information
        with(binding) {
            tvFileName.text = loanData.fileName

            // Show limited data for free users
            loanData.interestRate?.let {
                tvInterestRate.text = "$it% per annum"
                tvInterestRate.visibility = View.VISIBLE
            }

            loanData.loanTenure?.let {
                tvLoanTenure.text = it
                tvLoanTenure.visibility = View.VISIBLE
            }

            loanData.emiAmount?.let {
                tvEmiAmount.text = "₹${String.format("%.2f", it)}"
                tvEmiAmount.visibility = View.VISIBLE
            }

            // Show risk score but blur details
            tvRiskScore.text = "${riskResult.riskScore}/100"
            tvRiskLevel.text = "Risk Level: ${riskResult.riskLevel.name}"

            // Show preview message
            tvPreviewMessage.text = "🔒 Unlock PRO to see:\n" +
                    "• Full clause-by-clause breakdown\n" +
                    "• Hidden fee detection\n" +
                    "• Prepayment penalty analysis\n" +
                    "• Effective APR calculator\n" +
                    "• Export PDF & Excel reports"
        }
    }

    private fun setupClickListeners() {
        val billingManager = (requireActivity() as MainActivity).billingManager

        binding.btnUnlockPro.setOnClickListener {
            if (billingManager.isPro()) {
                // Already PRO, navigate to full analysis
                navigateToFullAnalysis()
            } else {
                // Show paywall
                findNavController().navigate(R.id.action_summaryFragment_to_paywallFragment)
            }
        }

        binding.btnViewFullAnalysis.setOnClickListener {
            if (billingManager.isPro()) {
                navigateToFullAnalysis()
            } else {
                findNavController().navigate(R.id.action_summaryFragment_to_paywallFragment)
            }
        }
    }

    private fun navigateToFullAnalysis() {
        val bundle = bundleOf(
            "loanData" to loanData,
            "riskResult" to riskResult
        )
        findNavController().navigate(
            R.id.action_summaryFragment_to_riskFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
