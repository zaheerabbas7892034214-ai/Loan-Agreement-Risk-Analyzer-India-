package com.yourcompany.loanrisk.presentation.paywall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yourcompany.loanrisk.databinding.FragmentPaywallBinding
import com.yourcompany.loanrisk.presentation.MainActivity
import kotlinx.coroutines.launch

/**
 * Paywall fragment for PRO unlock
 */
class PaywallFragment : Fragment() {

    private var _binding: FragmentPaywallBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaywallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val billingManager = (requireActivity() as MainActivity).billingManager

        // Observe product details
        viewLifecycleOwner.lifecycleScope.launch {
            billingManager.productDetails.collect { productDetails ->
                productDetails?.let {
                    binding.tvPrice.text = it.price
                }
            }
        }

        // Observe purchase state
        viewLifecycleOwner.lifecycleScope.launch {
            billingManager.purchaseState.collect { purchaseState ->
                if (purchaseState.isPro) {
                    Toast.makeText(requireContext(), "PRO unlocked! ✅", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        setupClickListeners(billingManager)
    }

    private fun setupClickListeners(billingManager: com.yourcompany.loanrisk.billing.BillingManager) {
        binding.btnUnlockPro.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnUnlockPro.isEnabled = false

                val success = billingManager.launchPurchaseFlow(requireActivity())
                
                if (!success) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to start purchase. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                binding.progressBar.visibility = View.GONE
                binding.btnUnlockPro.isEnabled = true
            }
        }

        binding.btnRestorePurchase.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            billingManager.restorePurchases { success ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    Toast.makeText(requireContext(), "Purchase restored! ✅", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No previous purchase found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
