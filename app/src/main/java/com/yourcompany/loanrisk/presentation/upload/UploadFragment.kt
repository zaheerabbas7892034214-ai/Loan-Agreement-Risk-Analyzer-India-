package com.yourcompany.loanrisk.presentation.upload

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.yourcompany.loanrisk.R
import com.yourcompany.loanrisk.databinding.FragmentUploadBinding
import kotlinx.coroutines.launch

/**
 * Upload PDF fragment
 */
class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels {
        UploadViewModelFactory(requireContext())
    }

    private val selectPdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.onPdfSelected(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UploadUiState.Idle -> {
                        binding.tvSelectedFile.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = false
                        binding.btnClearFile.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                    }
                    is UploadUiState.FileSelected -> {
                        binding.tvSelectedFile.visibility = View.VISIBLE
                        binding.tvSelectedFile.text = state.fileName
                        binding.btnAnalyze.isEnabled = true
                        binding.btnClearFile.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                    is UploadUiState.Processing -> {
                        binding.btnAnalyze.isEnabled = false
                        binding.btnChoosePdf.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvStatus.text = state.message
                    }
                    is UploadUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        // Navigate to summary screen
                        val bundle = bundleOf(
                            "loanData" to state.loanData,
                            "riskResult" to state.riskResult
                        )
                        findNavController().navigate(
                            R.id.action_uploadFragment_to_summaryFragment,
                            bundle
                        )
                    }
                    is UploadUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = true
                        binding.btnChoosePdf.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnChoosePdf.setOnClickListener {
            openPdfPicker()
        }

        binding.btnAnalyze.setOnClickListener {
            viewModel.analyzePdf()
        }

        binding.btnClearFile.setOnClickListener {
            viewModel.clearSelection()
        }
    }

    private fun openPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        selectPdfLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
