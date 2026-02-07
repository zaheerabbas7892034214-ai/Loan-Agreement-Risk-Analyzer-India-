package com.yourcompany.loanrisk.presentation.export

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.databinding.FragmentExportBinding
import com.yourcompany.loanrisk.export.ExcelReportGenerator
import com.yourcompany.loanrisk.export.PdfReportGenerator
import kotlinx.coroutines.launch
import java.io.File

/**
 * Export fragment (PRO feature)
 */
class ExportFragment : Fragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    private lateinit var loanData: LoanAgreementData
    private lateinit var riskResult: RiskAnalysisResult
    private lateinit var pdfGenerator: PdfReportGenerator
    private lateinit var excelGenerator: ExcelReportGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            loanData = it.getParcelable("loanData") ?: return
            riskResult = it.getParcelable("riskResult") ?: return
        }
        pdfGenerator = PdfReportGenerator(requireContext())
        excelGenerator = ExcelReportGenerator(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnExportPdf.setOnClickListener {
            exportPdf()
        }

        binding.btnExportExcel.setOnClickListener {
            exportExcel()
        }
    }

    private fun exportPdf() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnExportPdf.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file = pdfGenerator.generateReport(loanData, riskResult)
                binding.progressBar.visibility = View.GONE
                binding.btnExportPdf.isEnabled = true
                
                Toast.makeText(requireContext(), "PDF generated successfully!", Toast.LENGTH_SHORT).show()
                shareFile(file, "application/pdf")
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnExportPdf.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Failed to generate PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun exportExcel() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnExportExcel.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file = excelGenerator.generateReport(loanData, riskResult)
                binding.progressBar.visibility = View.GONE
                binding.btnExportExcel.isEnabled = true
                
                Toast.makeText(requireContext(), "Excel generated successfully!", Toast.LENGTH_SHORT).show()
                shareFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnExportExcel.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Failed to generate Excel: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(intent, "Share Report"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
