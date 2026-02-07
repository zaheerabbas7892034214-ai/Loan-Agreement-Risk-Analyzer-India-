package com.yourcompany.loanrisk.presentation.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourcompany.loanrisk.analytics.RiskAnalyzer
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.pdf.PdfExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for upload screen
 */
class UploadViewModel(private val context: Context) : ViewModel() {

    private val pdfExtractor = PdfExtractor(context)
    private val riskAnalyzer = RiskAnalyzer()

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private var selectedUri: Uri? = null
    private var selectedFileName: String? = null

    fun onPdfSelected(uri: Uri) {
        selectedUri = uri
        selectedFileName = getFileName(uri)
        _uiState.value = UploadUiState.FileSelected(selectedFileName ?: "Unknown file")
    }

    fun analyzePdf() {
        val uri = selectedUri ?: return
        val fileName = selectedFileName ?: "document.pdf"

        viewModelScope.launch {
            try {
                _uiState.value = UploadUiState.Processing("Extracting text from PDF...")
                
                // Extract PDF data
                val loanData = pdfExtractor.extractFromPdf(uri, fileName)
                
                _uiState.value = UploadUiState.Processing("Analyzing clauses and risks...")
                
                // Analyze risks
                val riskResult = riskAnalyzer.analyze(loanData)
                
                _uiState.value = UploadUiState.Success(loanData, riskResult)
            } catch (e: Exception) {
                _uiState.value = UploadUiState.Error("Failed to process PDF: ${e.message}")
            }
        }
    }

    fun clearSelection() {
        selectedUri = null
        selectedFileName = null
        _uiState.value = UploadUiState.Idle
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}

/**
 * UI states for upload screen
 */
sealed class UploadUiState {
    object Idle : UploadUiState()
    data class FileSelected(val fileName: String) : UploadUiState()
    data class Processing(val message: String) : UploadUiState()
    data class Success(
        val loanData: LoanAgreementData,
        val riskResult: RiskAnalysisResult
    ) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

/**
 * ViewModel Factory
 */
class UploadViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadViewModel::class.java)) {
            return UploadViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
