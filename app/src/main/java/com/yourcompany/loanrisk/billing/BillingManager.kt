package com.yourcompany.loanrisk.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.yourcompany.loanrisk.data.model.ProductDetails
import com.yourcompany.loanrisk.data.model.PurchaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Manages Google Play Billing operations
 */
class BillingManager(private val context: Context) {

    companion object {
        const val PRODUCT_ID_PRO = "loanrisk_pro_unlock"
        private const val TAG = "BillingManager"
    }

    private var billingClient: BillingClient? = null
    private val _purchaseState = MutableStateFlow(PurchaseState())
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    /**
     * Initialize billing client and connect
     */
    fun initialize(onReady: () -> Unit = {}) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    queryPurchases()
                    queryProductDetails()
                    onReady()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
                // Try to restart the connection on the next request
            }
        })
    }

    /**
     * Query existing purchases (for restore)
     */
    private fun queryPurchases() {
        if (!isReady()) return

        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val proPurchase = purchases.find { it.products.contains(PRODUCT_ID_PRO) }
                if (proPurchase != null && proPurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    handlePurchase(proPurchase)
                } else {
                    _purchaseState.value = PurchaseState(isPro = false)
                }
            }
        }
    }

    /**
     * Query product details for display
     */
    private fun queryProductDetails() {
        if (!isReady()) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.firstOrNull()?.let { details ->
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: "₹999"
                    val priceMicros = details.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 999000000L
                    
                    _productDetails.value = ProductDetails(
                        productId = details.productId,
                        title = details.title,
                        description = details.description,
                        price = price,
                        priceAmountMicros = priceMicros
                    )
                    Log.d(TAG, "Product details loaded: $price")
                }
            } else {
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch purchase flow
     */
    suspend fun launchPurchaseFlow(activity: Activity): Boolean = withContext(Dispatchers.Main) {
        if (!isReady()) return@withContext false

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(getProductDetailsForPurchase() ?: return@withContext false)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient?.launchBillingFlow(activity, billingFlowParams)
        result?.responseCode == BillingClient.BillingResponseCode.OK
    }

    /**
     * Get product details object for purchase
     */
    private suspend fun getProductDetailsForPurchase(): com.android.billingclient.api.ProductDetails? =
        withContext(Dispatchers.IO) {
            if (!isReady()) return@withContext null

            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_PRO)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            var result: com.android.billingclient.api.ProductDetails? = null
            billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    result = productDetailsList.firstOrNull()
                }
            }
            
            // Wait a bit for the async call
            kotlinx.coroutines.delay(500)
            result
        }

    /**
     * Handle purchase verification and acknowledgment
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                // Already acknowledged, grant access
                _purchaseState.value = PurchaseState(
                    isPro = true,
                    purchaseToken = purchase.purchaseToken,
                    purchaseTime = purchase.purchaseTime,
                    productId = PRODUCT_ID_PRO
                )
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase is pending")
        }
    }

    /**
     * Acknowledge purchase
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
                _purchaseState.value = PurchaseState(
                    isPro = true,
                    purchaseToken = purchase.purchaseToken,
                    purchaseTime = purchase.purchaseTime,
                    productId = PRODUCT_ID_PRO
                )
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Restore purchases
     */
    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        if (!isReady()) {
            onComplete(false)
            return
        }

        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val proPurchase = purchases.find { 
                    it.products.contains(PRODUCT_ID_PRO) && 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED 
                }
                
                if (proPurchase != null) {
                    handlePurchase(proPurchase)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    /**
     * Check if billing client is ready
     */
    private fun isReady(): Boolean {
        return billingClient?.isReady == true
    }

    /**
     * Clean up billing client
     */
    fun destroy() {
        billingClient?.endConnection()
        billingClient = null
    }

    /**
     * Check if user is PRO
     */
    fun isPro(): Boolean = _purchaseState.value.isPro
}
