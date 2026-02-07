package com.yourcompany.loanrisk.data.model

/**
 * Model representing billing purchase state
 */
data class PurchaseState(
    val isPro: Boolean = false,
    val purchaseToken: String? = null,
    val purchaseTime: Long? = null,
    val productId: String? = null
)

/**
 * Product details for display
 */
data class ProductDetails(
    val productId: String,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long
)
