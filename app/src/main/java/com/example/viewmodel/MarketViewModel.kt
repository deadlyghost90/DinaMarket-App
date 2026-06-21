package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.MarketRepository
import com.example.data.Notification
import com.example.data.Order
import com.example.data.Product
import com.example.data.Review
import com.example.data.Seller
import com.example.ui.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MarketViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MarketRepository(application.applicationContext)

    // UI States
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected product detailed screen reviews & updates
    private val _selectedProductReviews = MutableStateFlow<List<Review>>(emptyList())
    val selectedProductReviews: StateFlow<List<Review>> = _selectedProductReviews.asStateFlow()

    // Shopping Cart (Reactive Session)
    val cartItems = mutableStateListOf<CartItem>()

    // Auth flows
    val authEmailInput = mutableStateOf("")
    val authPasswordInput = mutableStateOf("")
    val authError = mutableStateOf<String?>(null)
    val userLoggedIn = mutableStateOf(repository.isUserLoggedIn())
    val loggedInEmail = mutableStateOf(repository.getAuthUserEmail())
    val loggedInName = mutableStateOf(repository.getAuthUserName())

    // Role state ("Buyer" or "Seller")
    val currentUserRole = mutableStateOf("Buyer") // "Buyer" or "Seller"

    // Seller State
    val sellerProfile = mutableStateOf<Seller?>(null)
    private val _sellerOrders = MutableStateFlow<List<Order>>(emptyList())
    val sellerOrders: StateFlow<List<Order>> = _sellerOrders.asStateFlow()

    // Notifications Inbox state
    private val _buyerNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val buyerNotifications: StateFlow<List<Notification>> = _buyerNotifications.asStateFlow()

    // Buyer Orders state tracker
    private val _buyerOrders = MutableStateFlow<List<Order>>(emptyList())
    val buyerOrders: StateFlow<List<Order>> = _buyerOrders.asStateFlow()

    init {
        // Collect database products
        viewModelScope.launch {
            repository.getProducts().collectLatest { list ->
                _products.value = list
                applyCategoryAndFilters()
            }
        }

        // Collect notifications and orders if user is logged in
        refreshUserSessionData()
    }

    fun refreshUserSessionData() {
        val uid = repository.getAuthUserId() ?: return
        viewModelScope.launch {
            repository.getNotifications(uid).collectLatest { list ->
                _buyerNotifications.value = list
            }
        }
        viewModelScope.launch {
            repository.getBuyerOrders(uid).collectLatest { list ->
                _buyerOrders.value = list
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        applyCategoryAndFilters()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyCategoryAndFilters()
    }

    private fun applyCategoryAndFilters() {
        var list = _products.value
        val cat = _selectedCategory.value
        val search = _searchQuery.value.trim()

        if (cat != "All") {
            // Translate the category comparison
            list = list.filter { it.category.equals(cat, ignoreCase = true) }
        }

        if (search.isNotEmpty()) {
            list = list.filter {
                it.title.contains(search, ignoreCase = true) ||
                        it.description.contains(search, ignoreCase = true) ||
                        it.sellerName.contains(search, ignoreCase = true)
            }
        }
        _filteredProducts.value = list
    }

    // CART MANAGEMENT
    fun addToCart(product: Product) {
        val existingIndex = cartItems.indexOfFirst { it.product.id == product.id }
        if (existingIndex != -1) {
            val item = cartItems[existingIndex]
            if (product.stockCount > item.quantity) {
                cartItems[existingIndex] = item.copy(quantity = item.quantity + 1)
            }
        } else {
            if (product.stockCount > 0) {
                cartItems.add(CartItem(product, 1))
            }
        }
    }

    fun updateCartQuantity(productId: String, newQty: Int) {
        val index = cartItems.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            if (newQty <= 0) {
                cartItems.removeAt(index)
            } else {
                val item = cartItems[index]
                if (item.product.stockCount >= newQty) {
                    cartItems[index] = item.copy(quantity = newQty)
                }
            }
        }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getCartTotal(): Double {
        return cartItems.sumOf { it.totalItemPrice }
    }

    // PRODUCT DETAILS REVIEWS COMPONENT
    fun fetchProductReviews(productId: String) {
        viewModelScope.launch {
            repository.getReviews(productId).collectLatest { list ->
                _selectedProductReviews.value = list
            }
        }
    }

    fun submitProductReview(productId: String, rating: Int, comment: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addReview(productId, rating, comment) {
                fetchProductReviews(productId)
                onSuccess()
            }
        }
    }

    // CHECKOUT LOGISTICS
    fun performCheckout(
        fullName: String,
        phoneInput: String,
        deliveryAddr: String,
        method: String,
        tid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (fullName.isEmpty() || phoneInput.isEmpty() || deliveryAddr.isEmpty()) {
            onError(LanguageManager.translate("error_fields_required"))
            return
        }
        if (tid.length != 12 || !tid.all { it.isDigit() }) {
            onError("Transaction ID (TID) must be a valid 12-digit numeric code.")
            return
        }

        viewModelScope.launch {
            repository.placeOrder(
                name = fullName,
                phone = phoneInput,
                address = deliveryAddr,
                paymentMethod = method,
                tid = tid,
                cartItems = cartItems.toList()
            ) { _ ->
                clearCart()
                onSuccess()
            }
        }
    }

    fun updateProductStock(productId: String, newStock: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock) {
                onSuccess()
            }
        }
    }

    // FIREBASE AUTH VERIFIERS
    fun authenticateUser(onSuccess: () -> Unit) {
        val email = authEmailInput.value.trim()
        val pwd = authPasswordInput.value
        if (email.isEmpty() || pwd.isEmpty()) {
            authError.value = "All fields are strictly required."
            return
        }
        viewModelScope.launch {
            repository.login(email, pwd, {
                userLoggedIn.value = true
                loggedInEmail.value = repository.getAuthUserEmail()
                loggedInName.value = repository.getAuthUserName()
                authError.value = null
                refreshUserSessionData()
                onSuccess()
            }, { err ->
                authError.value = err
            })
        }
    }

    fun performLogout() {
        repository.logout()
        userLoggedIn.value = false
        currentUserRole.value = "Buyer"
        loggedInEmail.value = ""
        loggedInName.value = ""
    }

    // SELLER DASHBOARD CONTROLS
    fun fetchSellerProfileAndOrders() {
        val uid = repository.getAuthUserId() ?: return
        viewModelScope.launch {
            repository.getSellerProfile(uid).collectLatest { profile ->
                sellerProfile.value = profile
                if (profile != null) {
                    repository.getSellerOrders(profile.uid).collectLatest { orders ->
                        _sellerOrders.value = orders
                    }
                }
            }
        }
    }

    fun registerNewSeller(
        bizName: String,
        bio: String,
        logo: String,
        location: String,
        email: String,
        whatsapp: String,
        onSuccess: () -> Unit
    ) {
        val uid = repository.getAuthUserId() ?: return
        val seller = Seller(
            uid = uid,
            businessName = bizName,
            bio = bio,
            storeLogo = logo,
            physicalLocation = location,
            contactEmail = email,
            whatsappNumber = whatsapp,
            discordLink = "https://discord.gg/dinamarketpk"
        )
        viewModelScope.launch {
            repository.registerSeller(seller) {
                sellerProfile.value = seller
                fetchSellerProfileAndOrders()
                onSuccess()
            }
        }
    }

    fun publishNewProduct(
        title: String,
        desc: String,
        priceVal: Double,
        stockVal: Int,
        imgUrl: String,
        category: String,
        onSuccess: () -> Unit
    ) {
        val uid = repository.getAuthUserId() ?: return
        val fallbackName = loggedInName.value.ifEmpty { "Guest Mode" }
        val shopName = sellerProfile.value?.businessName ?: (if (fallbackName.contains("Guest")) "Dina Local Bazaar" else "$fallbackName Store")
        
        val productImages = if (imgUrl.isNotEmpty()) listOf(imgUrl) else listOf("https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=600")
        
        val newProduct = Product(
            title = title,
            description = desc,
            price = priceVal,
            stockCount = stockVal,
            images = productImages,
            category = category,
            sellerId = uid,
            sellerName = shopName
        )
        viewModelScope.launch {
            repository.addProduct(newProduct) {
                applyCategoryAndFilters()
                onSuccess()
            }
        }
    }

    fun verifyOrderAndNotify(order: Order, customNote: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateOrderStatus(
                orderId = order.orderId,
                buyerUid = order.buyerUid,
                status = "Confirmed & Settled",
                sellerNote = customNote
            ) {
                fetchSellerProfileAndOrders()
                onSuccess()
            }
        }
    }
}
