package com.yourcompany.loanrisk.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Risk analysis result model
 */
@Parcelize
data class RiskAnalysisResult(
    val agreementId: String = "",
    val riskScore: Int = 0, // 0-100
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val prepaymentPenaltyPercentage: Double? = null,
    val totalCostOfLoan: Double? = null,
    val hasArbitrationClause: Boolean = false,
    val foreclosureLockInPeriod: String? = null,
    val hasVaguePenaltyWording: Boolean = false,
    val hiddenFees: List<String> = emptyList(),
    val effectiveApr: Double? = null,
    val problematicClauses: List<ProblematicClause> = emptyList(),
    val recommendations: List<String> = emptyList()
) : Parcelable

@Parcelize
data class ProblematicClause(
    val title: String,
    val content: String,
    val severity: String, // "High", "Medium", "Low"
    val explanation: String
) : Parcelable

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}
