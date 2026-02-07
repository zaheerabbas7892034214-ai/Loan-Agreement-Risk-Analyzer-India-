package com.yourcompany.loanrisk.pdf

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * PDF extraction and parsing engine
 */
class PdfExtractor(private val context: Context) {

    companion object {
        private const val TAG = "PdfExtractor"
    }

    init {
        // Initialize PDFBox
        PDFBoxResourceLoader.init(context)
    }

    /**
     * Extract text and data from PDF
     */
    suspend fun extractFromPdf(uri: Uri, fileName: String): LoanAgreementData = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open PDF file")

            val document = PDDocument.load(inputStream)
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            inputStream.close()

            Log.d(TAG, "Extracted text length: ${text.length}")

            // Parse the extracted text
            parseExtractedText(text, fileName)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting PDF", e)
            throw e
        }
    }

    /**
     * Parse extracted text to find loan details
     */
    private fun parseExtractedText(text: String, fileName: String): LoanAgreementData {
        val lowerText = text.lowercase(Locale.getDefault())

        return LoanAgreementData(
            id = UUID.randomUUID().toString(),
            fileName = fileName,
            uploadDate = System.currentTimeMillis(),
            interestRate = extractInterestRate(lowerText),
            processingFee = extractProcessingFee(text),
            prepaymentClause = extractPrepaymentClause(text),
            penalInterestClause = extractPenalInterestClause(text),
            loanTenure = extractLoanTenure(text),
            emiAmount = extractEmiAmount(lowerText),
            interestType = extractInterestType(lowerText),
            principalAmount = extractPrincipalAmount(lowerText),
            rawText = text
        )
    }

    /**
     * Extract interest rate from text
     */
    private fun extractInterestRate(text: String): Double? {
        // Patterns: "12% per annum", "12 p.a.", "interest rate: 12%", "@ 12%"
        val patterns = listOf(
            Regex("""(\d+\.?\d*)\s*%\s*(per\s+annum|p\.a\.|p\.a|pa)"""),
            Regex("""interest\s+rate[:\s]+(\d+\.?\d*)\s*%"""),
            Regex("""@\s*(\d+\.?\d*)\s*%"""),
            Regex("""rate\s+of\s+interest[:\s]+(\d+\.?\d*)\s*%""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val rate = match.groupValues[1].toDoubleOrNull()
                if (rate != null && rate > 0 && rate < 50) { // Sanity check
                    return rate
                }
            }
        }
        return null
    }

    /**
     * Extract processing fee
     */
    private fun extractProcessingFee(text: String): String? {
        val patterns = listOf(
            Regex("""processing\s+fee[:\s]+₹?\s*(\d+[\d,]*\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""processing\s+charges[:\s]+₹?\s*(\d+[\d,]*\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""processing\s+fee[:\s]+(\d+\.?\d*)\s*%""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].replace(",", "")
            }
        }
        return null
    }

    /**
     * Extract prepayment clause
     */
    private fun extractPrepaymentClause(text: String): String? {
        val keywords = listOf("prepayment", "pre-payment", "foreclosure", "pre-closure")
        
        for (keyword in keywords) {
            val index = text.indexOf(keyword, ignoreCase = true)
            if (index != -1) {
                // Extract surrounding context (200 chars)
                val start = maxOf(0, index - 50)
                val end = minOf(text.length, index + 200)
                return text.substring(start, end).trim()
            }
        }
        return null
    }

    /**
     * Extract penal interest clause
     */
    private fun extractPenalInterestClause(text: String): String? {
        val keywords = listOf("penal", "penalty", "default", "late payment")
        
        for (keyword in keywords) {
            val index = text.indexOf(keyword, ignoreCase = true)
            if (index != -1) {
                val start = maxOf(0, index - 50)
                val end = minOf(text.length, index + 200)
                val clause = text.substring(start, end).trim()
                
                // Check if it mentions interest or charges
                if (clause.contains("interest", ignoreCase = true) || 
                    clause.contains("charge", ignoreCase = true)) {
                    return clause
                }
            }
        }
        return null
    }

    /**
     * Extract loan tenure
     */
    private fun extractLoanTenure(text: String): String? {
        val patterns = listOf(
            Regex("""tenure[:\s]+(\d+)\s*(months?|years?)""", RegexOption.IGNORE_CASE),
            Regex("""period[:\s]+(\d+)\s*(months?|years?)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)\s*(months?|years?)\s+loan""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return "${match.groupValues[1]} ${match.groupValues[2]}"
            }
        }
        return null
    }

    /**
     * Extract EMI amount
     */
    private fun extractEmiAmount(text: String): Double? {
        val patterns = listOf(
            Regex("""emi[:\s]+₹?\s*(\d+[\d,]*\.?\d*)"""),
            Regex("""monthly\s+installment[:\s]+₹?\s*(\d+[\d,]*\.?\d*)"""),
            Regex("""equated\s+monthly\s+installment[:\s]+₹?\s*(\d+[\d,]*\.?\d*)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }

    /**
     * Extract interest type (Fixed/Floating)
     */
    private fun extractInterestType(text: String): String? {
        return when {
            text.contains("fixed rate", ignoreCase = true) -> "Fixed"
            text.contains("floating rate", ignoreCase = true) -> "Floating"
            text.contains("variable rate", ignoreCase = true) -> "Floating"
            else -> null
        }
    }

    /**
     * Extract principal amount
     */
    private fun extractPrincipalAmount(text: String): Double? {
        val patterns = listOf(
            Regex("""loan\s+amount[:\s]+₹?\s*(\d+[\d,]*\.?\d*)"""),
            Regex("""principal[:\s]+₹?\s*(\d+[\d,]*\.?\d*)"""),
            Regex("""sanctioned\s+amount[:\s]+₹?\s*(\d+[\d,]*\.?\d*)""")
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].replace(",", "").toDoubleOrNull()
            }
        }
        return null
    }
}
