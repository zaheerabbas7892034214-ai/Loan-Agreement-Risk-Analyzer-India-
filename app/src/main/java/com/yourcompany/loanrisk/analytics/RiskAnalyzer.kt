package com.yourcompany.loanrisk.analytics

import com.yourcompany.loanrisk.data.model.LoanAgreementData
import com.yourcompany.loanrisk.data.model.ProblematicClause
import com.yourcompany.loanrisk.data.model.RiskAnalysisResult
import com.yourcompany.loanrisk.data.model.RiskLevel
import java.util.*
import kotlin.math.pow

/**
 * Risk analysis engine for loan agreements
 */
class RiskAnalyzer {

    /**
     * Analyze loan agreement and generate risk report
     */
    fun analyze(loanData: LoanAgreementData): RiskAnalysisResult {
        val text = loanData.rawText.lowercase(Locale.getDefault())
        
        // Detect various risk factors
        val prepaymentPenalty = detectPrepaymentPenalty(text, loanData.prepaymentClause)
        val arbitrationClause = detectArbitrationClause(text)
        val lockInPeriod = detectForeclosureLockIn(text)
        val vaguePenalty = detectVaguePenaltyWording(text)
        val hiddenFees = detectHiddenFees(text)
        val effectiveApr = calculateEffectiveAPR(loanData)
        val totalCost = calculateTotalCost(loanData)
        val problematicClauses = identifyProblematicClauses(loanData, text)
        
        // Calculate risk score
        val riskScore = calculateRiskScore(
            prepaymentPenalty,
            arbitrationClause,
            vaguePenalty,
            hiddenFees.size,
            loanData.interestRate,
            effectiveApr
        )
        
        val riskLevel = determineRiskLevel(riskScore)
        val recommendations = generateRecommendations(riskScore, problematicClauses, loanData)

        return RiskAnalysisResult(
            agreementId = loanData.id,
            riskScore = riskScore,
            riskLevel = riskLevel,
            prepaymentPenaltyPercentage = prepaymentPenalty,
            totalCostOfLoan = totalCost,
            hasArbitrationClause = arbitrationClause,
            foreclosureLockInPeriod = lockInPeriod,
            hasVaguePenaltyWording = vaguePenalty,
            hiddenFees = hiddenFees,
            effectiveApr = effectiveApr,
            problematicClauses = problematicClauses,
            recommendations = recommendations
        )
    }

    /**
     * Detect prepayment penalty percentage
     */
    private fun detectPrepaymentPenalty(text: String, clause: String?): Double? {
        val searchText = clause?.lowercase() ?: text
        
        val patterns = listOf(
            Regex("""(\d+\.?\d*)\s*%.*?(prepayment|foreclosure|pre-closure)"""),
            Regex("""(prepayment|foreclosure).*?(\d+\.?\d*)\s*%"""),
            Regex("""penalty.*?(\d+\.?\d*)\s*%""")
        )

        for (pattern in patterns) {
            val match = pattern.find(searchText)
            if (match != null) {
                val percentage = if (match.groupValues.size > 2) {
                    match.groupValues[1].toDoubleOrNull() ?: match.groupValues[2].toDoubleOrNull()
                } else {
                    match.groupValues[1].toDoubleOrNull()
                }
                
                if (percentage != null && percentage > 0 && percentage < 20) {
                    return percentage
                }
            }
        }

        // Check for "no prepayment penalty" wording
        if (searchText.contains("no prepayment", ignoreCase = true) ||
            searchText.contains("nil", ignoreCase = true) && searchText.contains("prepayment")) {
            return 0.0
        }

        return null
    }

    /**
     * Detect arbitration clause
     */
    private fun detectArbitrationClause(text: String): Boolean {
        val keywords = listOf("arbitration", "arbitrator", "dispute resolution")
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    /**
     * Detect foreclosure lock-in period
     */
    private fun detectForeclosureLockIn(text: String): String? {
        val patterns = listOf(
            Regex("""lock-in.*?(\d+)\s*(months?|years?)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)\s*(months?|years?).*?lock-in""", RegexOption.IGNORE_CASE),
            Regex("""foreclosure.*?after\s+(\d+)\s*(months?|years?)""", RegexOption.IGNORE_CASE)
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
     * Detect vague penalty wording
     */
    private fun detectVaguePenaltyWording(text: String): Boolean {
        val vagueTerms = listOf(
            "as deemed appropriate",
            "at the discretion",
            "may be charged",
            "subject to change",
            "without notice",
            "other charges as applicable"
        )
        return vagueTerms.any { text.contains(it, ignoreCase = true) }
    }

    /**
     * Detect hidden fees
     */
    private fun detectHiddenFees(text: String): List<String> {
        val fees = mutableListOf<String>()
        
        val feePatterns = mapOf(
            "documentation charges" to Regex("""documentation\s+(charges?|fee)""", RegexOption.IGNORE_CASE),
            "legal charges" to Regex("""legal\s+(charges?|fee)""", RegexOption.IGNORE_CASE),
            "administrative charges" to Regex("""administrative\s+(charges?|fee)""", RegexOption.IGNORE_CASE),
            "stamp duty" to Regex("""stamp\s+duty""", RegexOption.IGNORE_CASE),
            "insurance charges" to Regex("""insurance\s+(charges?|fee|premium)""", RegexOption.IGNORE_CASE),
            "valuation charges" to Regex("""valuation\s+(charges?|fee)""", RegexOption.IGNORE_CASE),
            "conversion charges" to Regex("""conversion\s+(charges?|fee)""", RegexOption.IGNORE_CASE),
            "other miscellaneous charges" to Regex("""miscellaneous\s+(charges?|fee)""", RegexOption.IGNORE_CASE)
        )

        for ((feeName, pattern) in feePatterns) {
            if (pattern.find(text) != null) {
                fees.add(feeName)
            }
        }

        return fees
    }

    /**
     * Calculate effective APR including all fees
     */
    private fun calculateEffectiveAPR(loanData: LoanAgreementData): Double? {
        val interestRate = loanData.interestRate ?: return null
        val processingFeeStr = loanData.processingFee
        val principal = loanData.principalAmount ?: 100000.0 // Default for calculation
        
        // Parse processing fee
        val processingFee = when {
            processingFeeStr == null -> 0.0
            processingFeeStr.contains("%") -> {
                val percentage = processingFeeStr.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                principal * (percentage / 100.0)
            }
            else -> processingFeeStr.toDoubleOrNull() ?: 0.0
        }

        // Simple effective APR calculation
        // Effective APR = Interest Rate + (Processing Fee / Principal) * (12 / Tenure in months)
        val tenureMonths = parseTenureToMonths(loanData.loanTenure) ?: 12
        val feeImpact = if (tenureMonths > 0) {
            (processingFee / principal) * (12.0 / tenureMonths) * 100.0
        } else 0.0

        return interestRate + feeImpact
    }

    /**
     * Calculate total cost of loan
     */
    private fun calculateTotalCost(loanData: LoanAgreementData): Double? {
        val emi = loanData.emiAmount ?: return null
        val tenureMonths = parseTenureToMonths(loanData.loanTenure) ?: return null
        val principal = loanData.principalAmount ?: return null
        val processingFeeStr = loanData.processingFee
        
        val processingFee = when {
            processingFeeStr == null -> 0.0
            processingFeeStr.contains("%") -> {
                val percentage = processingFeeStr.replace("%", "").trim().toDoubleOrNull() ?: 0.0
                principal * (percentage / 100.0)
            }
            else -> processingFeeStr.toDoubleOrNull() ?: 0.0
        }

        return (emi * tenureMonths) + processingFee
    }

    /**
     * Parse tenure string to months
     */
    private fun parseTenureToMonths(tenure: String?): Int? {
        if (tenure == null) return null
        
        val match = Regex("""(\d+)\s*(month|year)""", RegexOption.IGNORE_CASE).find(tenure)
        if (match != null) {
            val value = match.groupValues[1].toIntOrNull() ?: return null
            val unit = match.groupValues[2].lowercase()
            return if (unit.startsWith("year")) value * 12 else value
        }
        return null
    }

    /**
     * Identify problematic clauses
     */
    private fun identifyProblematicClauses(
        loanData: LoanAgreementData,
        text: String
    ): List<ProblematicClause> {
        val clauses = mutableListOf<ProblematicClause>()

        // High interest rate
        if (loanData.interestRate != null && loanData.interestRate > 15.0) {
            clauses.add(
                ProblematicClause(
                    title = "High Interest Rate",
                    content = "Interest rate: ${loanData.interestRate}% per annum",
                    severity = "High",
                    explanation = "The interest rate exceeds 15% p.a., which is higher than typical bank rates."
                )
            )
        }

        // Prepayment penalty
        if (loanData.prepaymentClause != null && !loanData.prepaymentClause.contains("no", ignoreCase = true)) {
            clauses.add(
                ProblematicClause(
                    title = "Prepayment Penalty",
                    content = loanData.prepaymentClause.take(150),
                    severity = "Medium",
                    explanation = "This clause may charge you a penalty for paying off the loan early."
                )
            )
        }

        // Vague penalty terms
        if (detectVaguePenaltyWording(text)) {
            clauses.add(
                ProblematicClause(
                    title = "Vague Penalty Terms",
                    content = "Contains vague terms like 'at discretion' or 'may be charged'",
                    severity = "Medium",
                    explanation = "Ambiguous wording allows lender to charge arbitrary fees."
                )
            )
        }

        // Penal interest
        if (loanData.penalInterestClause != null) {
            clauses.add(
                ProblematicClause(
                    title = "Penal Interest Clause",
                    content = loanData.penalInterestClause.take(150),
                    severity = "Medium",
                    explanation = "You will be charged additional interest on late payments."
                )
            )
        }

        return clauses
    }

    /**
     * Calculate overall risk score (0-100)
     */
    private fun calculateRiskScore(
        prepaymentPenalty: Double?,
        hasArbitration: Boolean,
        hasVaguePenalty: Boolean,
        hiddenFeesCount: Int,
        interestRate: Double?,
        effectiveApr: Double?
    ): Int {
        var score = 0

        // Base score from interest rate
        if (interestRate != null) {
            score += when {
                interestRate > 20 -> 30
                interestRate > 15 -> 20
                interestRate > 12 -> 10
                else -> 0
            }
        }

        // Prepayment penalty
        if (prepaymentPenalty != null && prepaymentPenalty > 0) {
            score += (prepaymentPenalty * 3).toInt().coerceAtMost(20)
        }

        // Arbitration clause
        if (hasArbitration) score += 10

        // Vague penalty wording
        if (hasVaguePenalty) score += 15

        // Hidden fees
        score += (hiddenFeesCount * 5).coerceAtMost(20)

        // Effective APR vs stated rate
        if (effectiveApr != null && interestRate != null) {
            val difference = effectiveApr - interestRate
            if (difference > 2) {
                score += 10
            }
        }

        return score.coerceIn(0, 100)
    }

    /**
     * Determine risk level from score
     */
    private fun determineRiskLevel(score: Int): RiskLevel {
        return when {
            score >= 70 -> RiskLevel.CRITICAL
            score >= 50 -> RiskLevel.HIGH
            score >= 30 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    /**
     * Generate recommendations
     */
    private fun generateRecommendations(
        riskScore: Int,
        problematicClauses: List<ProblematicClause>,
        loanData: LoanAgreementData
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (riskScore >= 70) {
            recommendations.add("⚠️ This loan has significant risks. Consider seeking alternative lenders.")
        }

        if (loanData.interestRate != null && loanData.interestRate > 15.0) {
            recommendations.add("💡 Compare with other banks - RBI repo rate-linked loans typically offer lower rates.")
        }

        if (problematicClauses.any { it.title.contains("Prepayment") }) {
            recommendations.add("💡 Negotiate to remove or reduce prepayment penalties.")
        }

        if (problematicClauses.any { it.title.contains("Vague") }) {
            recommendations.add("⚠️ Request clarification on all fee structures in writing before signing.")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ This loan agreement appears to have standard terms.")
        }

        recommendations.add("📋 Always read the fine print and understand all clauses before signing.")

        return recommendations
    }
}
