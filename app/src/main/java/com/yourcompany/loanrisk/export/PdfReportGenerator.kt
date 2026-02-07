package com.yourcompany.loanrisk.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF report generator
 */
class PdfReportGenerator(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40
    }

    /**
     * Generate PDF report
     */
    suspend fun generateReport(
        loanData: LoanAgreementData,
        riskResult: RiskAnalysisResult
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        var pageNumber = 1
        var yPosition = MARGIN + 20

        // Page 1: Summary
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Title
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Loan Agreement Risk Analysis Report", MARGIN.toFloat(), yPosition.toFloat(), titlePaint)
        yPosition += 40

        // Date
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", MARGIN.toFloat(), yPosition.toFloat(), normalPaint)
        yPosition += 30

        // File name
        val boldPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("File: ${loanData.fileName}", MARGIN.toFloat(), yPosition.toFloat(), boldPaint)
        yPosition += 40

        // Risk Score
        canvas.drawText("Risk Score: ${riskResult.riskScore}/100", MARGIN.toFloat(), yPosition.toFloat(), boldPaint)
        yPosition += 20
        
        val riskLevelPaint = Paint().apply {
            color = when (riskResult.riskLevel.name) {
                "CRITICAL" -> Color.RED
                "HIGH" -> Color.rgb(255, 140, 0)
                "MEDIUM" -> Color.rgb(255, 200, 0)
                else -> Color.GREEN
            }
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Risk Level: ${riskResult.riskLevel.name}", MARGIN.toFloat(), yPosition.toFloat(), riskLevelPaint)
        yPosition += 40

        // Loan Details Section
        canvas.drawText("Loan Details", MARGIN.toFloat(), yPosition.toFloat(), boldPaint)
        yPosition += 25

        loanData.interestRate?.let {
            canvas.drawText("Interest Rate: $it% per annum", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        loanData.processingFee?.let {
            canvas.drawText("Processing Fee: $it", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        loanData.loanTenure?.let {
            canvas.drawText("Loan Tenure: $it", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        loanData.emiAmount?.let {
            canvas.drawText("EMI Amount: ₹${String.format("%.2f", it)}", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        riskResult.effectiveApr?.let {
            canvas.drawText("Effective APR: ${String.format("%.2f", it)}%", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        riskResult.totalCostOfLoan?.let {
            canvas.drawText("Total Cost: ₹${String.format("%.2f", it)}", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        yPosition += 20

        // Risk Factors
        canvas.drawText("Risk Factors", MARGIN.toFloat(), yPosition.toFloat(), boldPaint)
        yPosition += 25

        if (riskResult.hasArbitrationClause) {
            canvas.drawText("• Contains arbitration clause", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        if (riskResult.hasVaguePenaltyWording) {
            canvas.drawText("• Contains vague penalty wording", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        riskResult.prepaymentPenaltyPercentage?.let {
            if (it > 0) {
                canvas.drawText("• Prepayment penalty: $it%", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
                yPosition += 20
            }
        }

        riskResult.foreclosureLockInPeriod?.let {
            canvas.drawText("• Lock-in period: $it", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
        }

        if (riskResult.hiddenFees.isNotEmpty()) {
            canvas.drawText("• Hidden fees detected: ${riskResult.hiddenFees.size}", MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
            yPosition += 20
            riskResult.hiddenFees.take(3).forEach { fee ->
                if (yPosition < PAGE_HEIGHT - MARGIN) {
                    canvas.drawText("  - $fee", MARGIN.toFloat() + 40, yPosition.toFloat(), normalPaint)
                    yPosition += 18
                }
            }
        }

        document.finishPage(page)

        // Page 2: Recommendations
        if (riskResult.recommendations.isNotEmpty()) {
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = MARGIN + 20

            canvas.drawText("Recommendations", MARGIN.toFloat(), yPosition.toFloat(), boldPaint)
            yPosition += 25

            riskResult.recommendations.forEach { recommendation ->
                if (yPosition < PAGE_HEIGHT - MARGIN - 40) {
                    // Word wrap for long recommendations
                    val words = recommendation.split(" ")
                    var line = ""
                    words.forEach { word ->
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        val width = normalPaint.measureText(testLine)
                        if (width < PAGE_WIDTH - MARGIN * 2 - 40) {
                            line = testLine
                        } else {
                            canvas.drawText(line, MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
                            yPosition += 18
                            line = word
                        }
                    }
                    if (line.isNotEmpty()) {
                        canvas.drawText(line, MARGIN.toFloat() + 20, yPosition.toFloat(), normalPaint)
                        yPosition += 25
                    }
                }
            }

            document.finishPage(page)
        }

        // Save to file
        val outputDir = File(context.getExternalFilesDir(null), "reports")
        outputDir.mkdirs()
        val outputFile = File(outputDir, "loan_analysis_${System.currentTimeMillis()}.pdf")
        
        FileOutputStream(outputFile).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()

        outputFile
    }
}
