package com.yourcompany.loanrisk.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.yourcompany.loanrisk.BuildConfig
import com.yourcompany.loanrisk.databinding.FragmentSettingsBinding
import com.yourcompany.loanrisk.presentation.MainActivity

/**
 * Settings fragment
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displaySettings()
        setupClickListeners()
    }

    private fun displaySettings() {
        binding.tvAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"

        val billingManager = (requireActivity() as MainActivity).billingManager
        if (billingManager.isPro()) {
            binding.tvProStatus.text = "✅ PRO Unlocked"
            binding.btnRestorePurchase.visibility = View.GONE
        } else {
            binding.tvProStatus.text = "Free Version"
            binding.btnRestorePurchase.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        val billingManager = (requireActivity() as MainActivity).billingManager

        binding.btnRestorePurchase.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            billingManager.restorePurchases { success ->
                binding.progressBar.visibility = View.GONE
                if (success) {
                    Toast.makeText(requireContext(), "Purchase restored! ✅", Toast.LENGTH_SHORT).show()
                    displaySettings()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No previous purchase found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnAbout.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Loan Agreement Risk Analyzer\nAnalyze loan agreements and detect hidden risks",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy policy link", Toast.LENGTH_SHORT).show()
        }

        binding.btnTerms.setOnClickListener {
            Toast.makeText(requireContext(), "Terms of service link", Toast.LENGTH_SHORT).show()
        }

        binding.btnContact.setOnClickListener {
            Toast.makeText(requireContext(), "Contact: support@loanrisk.com", Toast.LENGTH_SHORT).show()
        }

        binding.btnClearCache.setOnClickListener {
            // Clear app cache
            requireContext().cacheDir.deleteRecursively()
            Toast.makeText(requireContext(), "Cache cleared", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
