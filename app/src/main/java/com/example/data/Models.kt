package com.example.data

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val images: List<String> = emptyList(),
    val category: String = "",
    val stockCount: Int = 0,
    val monthlyOrders: Int = 0,
    val boostScore: Int = 1,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val sellerId: String = "",
    val sellerName: String = ""
) {
    fun isOutOfStock(): Boolean = stockCount <= 0
}

data class Seller(
    val uid: String = "",
    val businessName: String = "",
    val bio: String = "",
    val storeLogo: String = "",
    val physicalLocation: String = "",
    val contactEmail: String = "",
    val whatsappNumber: String = "",
    val discordLink: String = ""
)

data class Review(
    val id: String = "",
    val productId: String = "",
    val userName: String = "",
    val userRating: Int = 5,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isVerifiedPurchaser: Boolean = false
)

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalItemPrice: Double
        get() = product.price * quantity
}

data class Order(
    val orderId: String = "",
    val buyerUid: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val deliveryLocation: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "", // "EasyPaisa" or "JazzCash"
    val transactionId: String = "", // 12-digit TID
    val transactionImage: String? = null,
    val status: String = "Pending Verification", // "Pending Verification", "Confirmed"
    val sellerId: String = "",
    val sellerNote: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Notification(
    val id: String = "",
    val recipientUid: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
