package com.yourcompany.loanrisk.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data model representing extracted loan agreement data
 */
@Parcelize
data class LoanAgreementData(
    val id: String = "",
    val fileName: String = "",
    val uploadDate: Long = System.currentTimeMillis(),
    val interestRate: Double? = null,
    val processingFee: String? = null,
    val prepaymentClause: String? = null,
    val penalInterestClause: String? = null,
    val loanTenure: String? = null,
    val emiAmount: Double? = null,
    val interestType: String? = null, // "Fixed" or "Floating"
    val principalAmount: Double? = null,
    val rawText: String = ""
) : Parcelable
