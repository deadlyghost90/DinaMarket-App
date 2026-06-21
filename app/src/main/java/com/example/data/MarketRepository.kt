package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

class MarketRepository(private val context: Context) {
    private var database: FirebaseDatabase? = null
    private var auth: FirebaseAuth? = null
    private var isFirebaseAvailable = false

    private val _localProducts = MutableStateFlow<List<Product>>(emptyList())
    val localProducts: StateFlow<List<Product>> = _localProducts

    private val _localSellers = MutableStateFlow<Map<String, Seller>>(emptyMap())
    private val _localReviews = MutableStateFlow<List<Review>>(emptyList())
    private val _localOrders = MutableStateFlow<List<Order>>(emptyList())
    private val _localNotifications = MutableStateFlow<List<Notification>>(emptyList())

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1004354649208:android:6d8449fca17822152a818d")
                    .setApiKey("AIzaSyBRrdnBcZZi0TmH_ZXtoSXIo5eheoP-RKk")
                    .setDatabaseUrl("https://dinamarket-pakistan-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .setProjectId("dinamarket-pakistan")
                    .setStorageBucket("dinamarket-pakistan.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            database = FirebaseDatabase.getInstance()
            auth = FirebaseAuth.getInstance()
            isFirebaseAvailable = true
            Log.d("DinaMarket", "Firebase Programmatic Initialization Succeeded")
        } catch (e: Exception) {
            Log.e("DinaMarket", "Firebase program init error", e)
            isFirebaseAvailable = false
        }
    }

    // AUTHENTICATION UTILS
    fun getAuthUserId(): String? {
        return if (isFirebaseAvailable) auth?.currentUser?.uid else null
    }

    fun isUserLoggedIn(): Boolean {
        return getAuthUserId() != null
    }

    fun getAuthUserEmail(): String {
        return if (isFirebaseAvailable) auth?.currentUser?.email ?: "" else ""
    }

    fun getAuthUserName(): String {
        val email = getAuthUserEmail()
        return email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    fun login(email: String, selectPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            if (isFirebaseAvailable && auth != null) {
                auth!!.signInWithEmailAndPassword(email, selectPass)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        auth!!.createUserWithEmailAndPassword(email, selectPass)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { err ->
                                onError(err.message ?: "Authentication failed") 
                            }
                    }
            } else {
                onError("Firebase Auth is not available")
            }
        } catch (e: Exception) {
            Log.e("DinaMarket", "Auth error caught", e)
            onError(e.message ?: "Unknown auth error")
        }
    }

    fun logout() {
        if (isFirebaseAvailable) auth?.signOut()
    }

    // SEARCH & FETCH PRODUCTS WITH SMART BOOST SORTING
    fun getProducts(): Flow<List<Product>> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(emptyList())
                awaitClose {}
            }
        }

        return callbackFlow {
            val ref = database!!.getReference("products")
            // Sort database elements via 'boost_score'
            val query = ref.orderByChild("boostScore")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Product>()
                    snapshot.children.forEach { child ->
                        child.getValue(Product::class.java)?.let { prod ->
                            // Dynamic enforcement of Smart Boosting
                            // If order is zero, upgrade boost score instantly
                            var finalProd = prod
                            if (prod.monthlyOrders == 0 && prod.boostScore != 100) {
                                finalProd = prod.copy(boostScore = 100)
                                ref.child(prod.id).child("boostScore").setValue(100)
                            } else if (prod.monthlyOrders >= 5 && prod.boostScore == 100) {
                                finalProd = prod.copy(boostScore = 1)
                                ref.child(prod.id).child("boostScore").setValue(1)
                            }
                            list.add(finalProd)
                        }
                    }
                    // Sort descending since higher boost score belongs on top
                    trySend(list.sortedByDescending { it.boostScore })
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            query.addValueEventListener(listener)
            awaitClose { query.removeEventListener(listener) }
        }
    }

    fun getReviews(productId: String): Flow<List<Review>> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(emptyList())
                awaitClose {}
            }
        }
        return callbackFlow {
            val ref = database!!.getReference("reviews").orderByChild("productId").equalTo(productId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Review>()
                    snapshot.children.forEach { child ->
                        child.getValue(Review::class.java)?.let { list.add(it) }
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    fun addReview(productId: String, rating: Int, comment: String, onSuccess: () -> Unit) {
        val reviewerName = getAuthUserName()
        val reviewId = "rev_" + System.currentTimeMillis()
        val newReview = Review(
            id = reviewId,
            productId = productId,
            userName = reviewerName,
            userRating = rating,
            comment = comment,
            timestamp = System.currentTimeMillis(),
            isVerifiedPurchaser = true
        )

        if (isFirebaseAvailable && database != null) {
            val ref = database!!.getReference("reviews").child(reviewId)
            ref.setValue(newReview).addOnSuccessListener {
                updateProductRatingMetrics(productId, rating)
                onSuccess()
            }
        }
    }

    private fun updateProductRatingMetrics(productId: String, newRating: Int) {
        if (isFirebaseAvailable && database != null) {
            val productRef = database!!.getReference("products").child(productId)
            productRef.get().addOnSuccessListener { snapshot ->
                val prod = snapshot.getValue(Product::class.java) ?: return@addOnSuccessListener
                val count = prod.reviewCount + 1
                val calculated = ((prod.rating * prod.reviewCount) + newRating) / count
                productRef.child("reviewCount").setValue(count)
                productRef.child("rating").setValue(calculated)
            }
        }
    }

    // CHECKOUT IMPLEMENTATION
    fun placeOrder(
        name: String,
        phone: String,
        address: String,
        paymentMethod: String,
        tid: String,
        cartItems: List<CartItem>,
        onSuccess: (Order) -> Unit
    ) {
        val groupOrders = cartItems.groupBy { it.product.sellerId }
        groupOrders.forEach { (sellerId, items) ->
            val orderId = "order_" + System.currentTimeMillis() + "_" + (1000..9999).random()
            val totalAmount = items.sumOf { it.totalItemPrice }
            val newOrder = Order(
                orderId = orderId,
                buyerUid = getAuthUserId() ?: "guest_uid",
                buyerName = name,
                buyerPhone = phone,
                deliveryLocation = address,
                items = items,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                transactionId = tid,
                status = "Pending Verification",
                sellerId = sellerId,
                timestamp = System.currentTimeMillis()
            )

            // Decrement inventory stock on products and implement Smart Boost order updates
            items.forEach { cartItem ->
                decrementInventoryAndIncrementOrders(cartItem.product.id, cartItem.quantity)
            }

            if (isFirebaseAvailable && database != null) {
                database!!.getReference("orders").child(orderId).setValue(newOrder)
            }

            onSuccess(newOrder)
        }
    }

    private fun decrementInventoryAndIncrementOrders(productId: String, purchaseQty: Int) {
        if (isFirebaseAvailable && database != null) {
            val ref = database!!.getReference("products").child(productId)
            ref.get().addOnSuccessListener { snapshot ->
                val prod = snapshot.getValue(Product::class.java) ?: return@addOnSuccessListener
                val newStock = (prod.stockCount - purchaseQty).coerceAtLeast(0)
                val newOrders = prod.monthlyOrders + purchaseQty
                
                // Smart boosting score logic
                // Boost score drops back to organic baseline (1) if they reach 5 orders
                val finalBoost = if (newOrders >= 5) 1 else prod.boostScore

                val updates = mapOf(
                    "stockCount" to newStock,
                    "monthlyOrders" to newOrders,
                    "boostScore" to finalBoost
                )
                ref.updateChildren(updates)
            }
        }
    }

    // SELLER DASHBOARD & REGISTER
    fun registerSeller(seller: Seller, onSuccess: () -> Unit) {
        if (isFirebaseAvailable && database != null) {
            database!!.getReference("sellers").child(seller.uid).setValue(seller)
                .addOnSuccessListener { onSuccess() }
        }
    }

    fun getSellerProfile(uid: String): Flow<Seller?> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(null)
                awaitClose {}
            }
        }
        return callbackFlow {
            val ref = database!!.getReference("sellers").child(uid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(snapshot.getValue(Seller::class.java))
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    // MERCHANT INVENTORY ADD / EDIT
    fun addProduct(product: Product, onSuccess: () -> Unit) {
        // Enforce the Traffic Booster instantly on new items!
        val newProd = product.copy(
            id = "p_" + System.currentTimeMillis() + "_" + (100..999).random(),
            boostScore = 100, // Smart Boost level for initial recommended tier
            monthlyOrders = 0
        )

        if (isFirebaseAvailable && database != null) {
            database!!.getReference("products").child(newProd.id).setValue(newProd)
                .addOnSuccessListener { onSuccess() }
        }
    }

    fun updateProductStock(productId: String, newStock: Int, onSuccess: () -> Unit) {
        if (isFirebaseAvailable && database != null) {
            database!!.getReference("products").child(productId).child("stockCount").setValue(newStock)
                .addOnSuccessListener { onSuccess() }
        }
    }

    // ORDER TRACKING FOR BUYER
    fun getBuyerOrders(buyerUid: String): Flow<List<Order>> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(emptyList())
                awaitClose {}
            }
        }
        return callbackFlow {
            val ref = database!!.getReference("orders").orderByChild("buyerUid").equalTo(buyerUid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Order>()
                    snapshot.children.forEach { child ->
                        child.getValue(Order::class.java)?.let { list.add(it) }
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    // ORDER TRACKING FOR SELLER
    fun getSellerOrders(sellerId: String): Flow<List<Order>> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(emptyList())
                awaitClose {}
            }
        }
        return callbackFlow {
            val ref = database!!.getReference("orders").orderByChild("sellerId").equalTo(sellerId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Order>()
                    snapshot.children.forEach { child ->
                        child.getValue(Order::class.java)?.let { list.add(it) }
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }

    // ORDER VERIFICATION & CUSTOM NOTIFICATION SYSTEM
    fun updateOrderStatus(orderId: String, buyerUid: String, status: String, sellerNote: String, onSuccess: () -> Unit) {
        if (isFirebaseAvailable && database != null) {
            val updates = mapOf(
                "status" to status,
                "sellerNote" to sellerNote
            )
            database!!.getReference("orders").child(orderId).updateChildren(updates)
                .addOnSuccessListener {
                    pushNotification(buyerUid, "Order Status Updated!", sellerNote)
                    onSuccess()
                }
        }
    }

    // PUSH IN-APP NOTIFICATIONS
    fun pushNotification(recipientUid: String, title: String, body: String) {
        val notId = "notif_" + System.currentTimeMillis()
        val notif = Notification(notId, recipientUid, title, body, System.currentTimeMillis(), false)
        
        if (isFirebaseAvailable && database != null) {
            database!!.getReference("notifications").child(notId).setValue(notif)
        }
    }

    fun getNotifications(recipientUid: String): Flow<List<Notification>> {
        if (!isFirebaseAvailable || database == null) {
            return callbackFlow {
                trySend(emptyList())
                awaitClose {}
            }
        }
        return callbackFlow {
            val ref = database!!.getReference("notifications").orderByChild("recipientUid").equalTo(recipientUid)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Notification>()
                    snapshot.children.forEach { child ->
                        child.getValue(Notification::class.java)?.let { list.add(it) }
                    }
                    trySend(list.sortedByDescending { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }
            ref.addValueEventListener(listener)
            awaitClose { ref.removeEventListener(listener) }
        }
    }
}
