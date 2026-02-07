package com.yourcompany.loanrisk.export

import android.content.Context
import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

/**
 * Excel report generator
 */
class ExcelReportGenerator(private val context: Context) {

    /**
     * Generate Excel report with cost comparison
     */
    suspend fun generateReport(
        loanData: LoanAgreementData,
        riskResult: RiskAnalysisResult
    ): File = withContext(Dispatchers.IO) {
        val workbook = XSSFWorkbook()

        // Create styles
        val headerStyle = createHeaderStyle(workbook)
        val currencyStyle = createCurrencyStyle(workbook)

        // Sheet 1: Loan Summary
        createSummarySheet(workbook, loanData, riskResult, headerStyle)

        // Sheet 2: Cost Breakdown
        createCostBreakdownSheet(workbook, loanData, riskResult, headerStyle, currencyStyle)

        // Sheet 3: Payment Schedule
        createPaymentScheduleSheet(workbook, loanData, headerStyle, currencyStyle)

        // Save to file
        val outputDir = File(context.getExternalFilesDir(null), "reports")
        outputDir.mkdirs()
        val outputFile = File(outputDir, "loan_analysis_${System.currentTimeMillis()}.xlsx")

        FileOutputStream(outputFile).use { outputStream ->
            workbook.write(outputStream)
        }
        workbook.close()

        outputFile
    }

    private fun createSummarySheet(
        workbook: Workbook,
        loanData: LoanAgreementData,
        riskResult: RiskAnalysisResult,
        headerStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("Loan Summary")
        var rowNum = 0

        // Header
        var row = sheet.createRow(rowNum++)
        var cell = row.createCell(0)
        cell.setCellValue("Loan Agreement Analysis")
        cell.cellStyle = headerStyle
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 1))
        rowNum++

        // Loan details
        addRow(sheet, rowNum++, "File Name", loanData.fileName)
        addRow(sheet, rowNum++, "Interest Rate", loanData.interestRate?.let { "$it%" } ?: "N/A")
        addRow(sheet, rowNum++, "Processing Fee", loanData.processingFee ?: "N/A")
        addRow(sheet, rowNum++, "Loan Tenure", loanData.loanTenure ?: "N/A")
        addRow(sheet, rowNum++, "EMI Amount", loanData.emiAmount?.let { "₹$it" } ?: "N/A")
        addRow(sheet, rowNum++, "Interest Type", loanData.interestType ?: "N/A")
        rowNum++

        // Risk analysis
        row = sheet.createRow(rowNum++)
        cell = row.createCell(0)
        cell.setCellValue("Risk Analysis")
        cell.cellStyle = headerStyle
        sheet.addMergedRegion(CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1))

        addRow(sheet, rowNum++, "Risk Score", "${riskResult.riskScore}/100")
        addRow(sheet, rowNum++, "Risk Level", riskResult.riskLevel.name)
        addRow(sheet, rowNum++, "Effective APR", riskResult.effectiveApr?.let { String.format("%.2f%%", it) } ?: "N/A")
        addRow(sheet, rowNum++, "Total Cost", riskResult.totalCostOfLoan?.let { String.format("₹%.2f", it) } ?: "N/A")
        addRow(sheet, rowNum++, "Arbitration Clause", if (riskResult.hasArbitrationClause) "Yes" else "No")
        addRow(sheet, rowNum++, "Vague Penalty Wording", if (riskResult.hasVaguePenaltyWording) "Yes" else "No")

        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createCostBreakdownSheet(
        workbook: Workbook,
        loanData: LoanAgreementData,
        riskResult: RiskAnalysisResult,
        headerStyle: CellStyle,
        currencyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("Cost Breakdown")
        var rowNum = 0

        // Header
        var row = sheet.createRow(rowNum++)
        row.createCell(0).apply {
            setCellValue("Cost Component")
            cellStyle = headerStyle
        }
        row.createCell(1).apply {
            setCellValue("Amount (₹)")
            cellStyle = headerStyle
        }
        rowNum++

        // Calculate costs
        val principal = loanData.principalAmount ?: 0.0
        val processingFee = calculateProcessingFee(loanData.processingFee, principal)
        val totalInterest = (riskResult.totalCostOfLoan ?: 0.0) - principal - processingFee
        val totalCost = riskResult.totalCostOfLoan ?: 0.0

        // Add rows
        addCostRow(sheet, rowNum++, "Principal Amount", principal, currencyStyle)
        addCostRow(sheet, rowNum++, "Processing Fee", processingFee, currencyStyle)
        addCostRow(sheet, rowNum++, "Total Interest", totalInterest, currencyStyle)
        
        if (riskResult.hiddenFees.isNotEmpty()) {
            addCostRow(sheet, rowNum++, "Other Fees (estimated)", 5000.0, currencyStyle)
        }
        
        rowNum++
        addCostRow(sheet, rowNum++, "Total Cost of Loan", totalCost, currencyStyle)

        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createPaymentScheduleSheet(
        workbook: Workbook,
        loanData: LoanAgreementData,
        headerStyle: CellStyle,
        currencyStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("Payment Schedule")
        var rowNum = 0

        // Header
        var row = sheet.createRow(rowNum++)
        row.createCell(0).apply {
            setCellValue("Month")
            cellStyle = headerStyle
        }
        row.createCell(1).apply {
            setCellValue("EMI (₹)")
            cellStyle = headerStyle
        }
        row.createCell(2).apply {
            setCellValue("Principal (₹)")
            cellStyle = headerStyle
        }
        row.createCell(3).apply {
            setCellValue("Interest (₹)")
            cellStyle = headerStyle
        }
        row.createCell(4).apply {
            setCellValue("Balance (₹)")
            cellStyle = headerStyle
        }

        // Generate payment schedule
        val emi = loanData.emiAmount ?: return
        val interestRate = loanData.interestRate ?: return
        val monthlyRate = interestRate / 12.0 / 100.0
        var balance = loanData.principalAmount ?: 100000.0
        val tenure = parseTenure(loanData.loanTenure) ?: 12

        for (month in 1..tenure.coerceAtMost(12)) { // Show first 12 months
            if (balance <= 0) break
            
            val interest = balance * monthlyRate
            val principal = emi - interest
            balance -= principal

            row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(month.toDouble())
            row.createCell(1).apply {
                setCellValue(emi)
                cellStyle = currencyStyle
            }
            row.createCell(2).apply {
                setCellValue(principal)
                cellStyle = currencyStyle
            }
            row.createCell(3).apply {
                setCellValue(interest)
                cellStyle = currencyStyle
            }
            row.createCell(4).apply {
                setCellValue(maxOf(0.0, balance))
                cellStyle = currencyStyle
            }
        }

        // Auto-size columns
        for (i in 0..4) {
            sheet.autoSizeColumn(i)
        }
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.fontHeightInPoints = 12
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        return style
    }

    private fun createCurrencyStyle(workbook: Workbook): CellStyle {
        val style = workbook.createCellStyle()
        style.dataFormat = workbook.createDataFormat().getFormat("₹#,##0.00")
        return style
    }

    private fun addRow(sheet: Sheet, rowNum: Int, label: String, value: String) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(label)
        row.createCell(1).setCellValue(value)
    }

    private fun addCostRow(sheet: Sheet, rowNum: Int, label: String, value: Double, style: CellStyle) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(label)
        row.createCell(1).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun calculateProcessingFee(feeStr: String?, principal: Double): Double {
        return when {
            feeStr == null -> 0.0
            feeStr.contains("%") -> {
                val percentage = feeStr.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                principal * (percentage / 100.0)
            }
            else -> feeStr.toDoubleOrNull() ?: 0.0
        }
    }

    private fun parseTenure(tenure: String?): Int? {
        if (tenure == null) return null
        val match = Regex("""(\d+)\s*(month|year)""", RegexOption.IGNORE_CASE).find(tenure)
        if (match != null) {
            val value = match.groupValues[1].toIntOrNull() ?: return null
            val unit = match.groupValues[2].lowercase()
            return if (unit.startsWith("year")) value * 12 else value
        }
        return null
    }
}
