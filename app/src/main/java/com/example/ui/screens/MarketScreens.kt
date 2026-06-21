package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.PrivacyPolicyActivity
import com.example.TermsAndConditionsActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.data.Seller
import com.example.data.Review
import com.example.data.CartItem
import com.example.data.Order
import com.example.data.Product
import com.example.ui.LanguageManager
import com.example.viewmodel.MarketViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMarketAppFrame(viewModel: MarketViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("home") } // home, details, cart, checkout, seller_suite, notifications
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var showAuthPopup by remember { mutableStateOf(false) }
    var pendingActionAfterAuth by remember { mutableStateOf<(() -> Unit)?>(null) }

    val userLoggedIn by viewModel.userLoggedIn
    val cartSize = viewModel.cartItems.sumOf { it.quantity }
    val isUrdu = LanguageManager.isUrdu.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { currentScreen = "home" }
                    ) {
                        // Luxurious gold and green circular logo emblem helper
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E5D3E))
                                .border(1.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "D",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24),
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DinaMarket",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "Pakistan (Jhelum district)",
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 9.sp,
                                lineHeight = 10.sp
                            )
                        }
                    }
                },
                 actions = {
                    // Language Toggle
                    IconButton(
                        onClick = { LanguageManager.toggleLanguage() },
                        modifier = Modifier.testTag("lang_toggle_btn")
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isUrdu) "EN" else "اردو",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Notification Bell Icon above
                    IconButton(
                        onClick = { currentScreen = "notifications" },
                        modifier = Modifier.testTag("notif_bell_btn")
                    ) {
                        val notifs by viewModel.buyerNotifications.collectAsState()
                        val unreadCount = notifs.filter { !it.isRead }.size
                        if (unreadCount > 0) {
                            BadgedBox(
                                badge = { Badge { Text(unreadCount.toString(), fontSize = 9.sp) } }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = borderGray(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // HOME Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { currentScreen = "home" }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("bottom_nav_home")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (currentScreen == "home") MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LanguageManager.translate("bottom_nav_home"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentScreen == "home") MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // SEARCH Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { 
                                currentScreen = "home"
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("bottom_nav_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LanguageManager.translate("bottom_nav_search"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    // CART Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { currentScreen = "cart" }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("bottom_nav_cart")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            if (cartSize > 0) {
                                BadgedBox(
                                    badge = { Badge { Text(cartSize.toString(), fontSize = 9.sp) } }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = if (currentScreen == "cart" || currentScreen == "checkout") MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = if (currentScreen == "cart" || currentScreen == "checkout") MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LanguageManager.translate("bottom_nav_cart"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentScreen == "cart" || currentScreen == "checkout") MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // ACCOUNT Tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { currentScreen = "account" }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("bottom_nav_account")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Account",
                            tint = if (currentScreen == "account" || currentScreen == "seller_suite") MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LanguageManager.translate("bottom_nav_account"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentScreen == "account" || currentScreen == "seller_suite") MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> {
                    HomeScreenLayout(
                        viewModel = viewModel,
                        onProductClick = { prod ->
                            selectedProductForDetail = prod
                            viewModel.fetchProductReviews(prod.id)
                            currentScreen = "details"
                        },
                        onGoToCart = { currentScreen = "cart" }
                    )
                }
                "details" -> {
                    selectedProductForDetail?.let { prod ->
                        ProductDetailScreenLayout(
                            product = prod,
                            viewModel = viewModel,
                            onGoBack = { currentScreen = "home" },
                            onWriteReview = { rating, cmd ->
                                if (!userLoggedIn) {
                                    pendingActionAfterAuth = {
                                        viewModel.submitProductReview(prod.id, rating, cmd) {
                                            Toast.makeText(context, "Review Published!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    showAuthPopup = true
                                } else {
                                    viewModel.submitProductReview(prod.id, rating, cmd) {
                                        Toast.makeText(context, "Review Published!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
                "cart" -> {
                    ShoppingCartScreenLayout(
                        viewModel = viewModel,
                        onGoBack = { currentScreen = "home" },
                        onProceedCheckout = {
                            if (!userLoggedIn) {
                                pendingActionAfterAuth = {
                                    currentScreen = "checkout"
                                }
                                showAuthPopup = true
                            } else {
                                currentScreen = "checkout"
                            }
                        }
                    )
                }
                "checkout" -> {
                    CheckoutScreenLayout(
                        viewModel = viewModel,
                        onGoBack = { currentScreen = "cart" },
                        onComplete = {
                            currentScreen = "home"
                            Toast.makeText(context, "Order Submitted! Waiting Verification.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                "seller_suite" -> {
                    SellerSuiteScreenLayout(
                        viewModel = viewModel,
                        onGoBack = {
                            viewModel.currentUserRole.value = "Buyer"
                            currentScreen = "home"
                        }
                    )
                }
                "notifications" -> {
                    BuyerNotificationsScreenLayout(
                        viewModel = viewModel,
                        onGoBack = { currentScreen = "home" }
                    )
                }
                "account" -> {
                    AccountScreenLayout(
                        viewModel = viewModel,
                        onGoBack = { currentScreen = "home" },
                        onNavigateToSeller = { currentScreen = "seller_suite" },
                        onNavigateToNotifications = { currentScreen = "notifications" }
                    )
                }
            }

            // Auth Popup Form overlay
            if (showAuthPopup) {
                AuthPopupOverlay(
                    viewModel = viewModel,
                    onDismiss = { showAuthPopup = false },
                    onAuthSuccess = {
                        showAuthPopup = false
                        pendingActionAfterAuth?.invoke()
                        pendingActionAfterAuth = null
                    }
                )
            }
        }
    }
}

// HOME LANDING
@Composable
fun HomeScreenLayout(
    viewModel: MarketViewModel,
    onProductClick: (Product) -> Unit,
    onGoToCart: () -> Unit
) {
    val context = LocalContext.current
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchVal by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()
    val cartSize = viewModel.cartItems.sumOf { it.quantity }

    val categories = listOf("All", "Fast Food", "Grocery", "Clothes", "Cosmetics", "Electronics", "Rent-a-Car")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchVal,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(LanguageManager.translate("search_hint"), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2D3748),
                    unfocusedTextColor = Color(0xFF2D3748)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Floating Cart button
            IconButton(
                onClick = onGoToCart,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("floating_cart_btn")
            ) {
                BadgedBox(
                    badge = { if (cartSize > 0) Badge { Text(cartSize.toString()) } }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart Menu",
                        tint = Color.White
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Shopify style slider banner at the top
            item {
                ShopifyStyleHeroBanner()
            }

            // Daraz Circular Shortcuts row
            item {
                DarazShortcutRow(
                    onSelectCategory = { cat ->
                        viewModel.selectCategory(cat)
                        viewModel.updateSearchQuery("")
                    },
                    onSearchRecommended = {
                        viewModel.selectCategory("All")
                        viewModel.updateSearchQuery("Special")
                        Toast.makeText(context, "Featured Punjab Deals Active! 🔥", Toast.LENGTH_SHORT).show()
                    },
                    onContactSupport = {
                        Toast.makeText(context, "DinaMarket PK helpline: +92 (300) 123-4567 📞", Toast.LENGTH_LONG).show()
                    }
                )
            }

            // Categories horizontal bar
            item {
                Text(
                    text = LanguageManager.translate("category_all"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = activeCategory == cat
                        val translateKey = when (cat) {
                            "Fast Food" -> "category_fastfood"
                            "Grocery" -> "category_grocery"
                            "Clothes" -> "category_clothes"
                            "Cosmetics" -> "category_cosmetics"
                            "Electronics" -> "category_electronics"
                            "Rent-a-Car" -> "category_rentacar"
                            else -> "category_all"
                        }
                        Card(
                            modifier = Modifier
                                .clickable { viewModel.selectCategory(cat) }
                                .testTag("cat_pill_$cat"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                            ),
                            border = borderGray(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = LanguageManager.translate(translateKey),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Product Grid
            item {
                Text(
                    text = "Recommended Vendors Grid",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            item {
                if (filteredProducts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderGray()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No products match your current search constraints.", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                } else {
                    // Compose grid emulation for lists elements safely
                    val chunked = filteredProducts.chunked(2)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        chunked.forEach { rowPairs ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowPairs.forEach { prod ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp)
                                    ) {
                                        ProductCardItem(prod, onProductClick, onAddToCartClick = { viewModel.addToCart(prod) })
                                    }
                                }
                                if (rowPairs.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// SHOPIFY BANNER SLIDER
@Composable
fun ShopifyStyleHeroBanner() {
    val slides = listOf(
        Triple(
            "Jhelum HQ Digital Bazaar",
            "Serving residents all over Jhelum, Dina, Sohawa, and Pind Dadan Khan with direct home delivery.",
            "https://images.unsplash.com/photo-1588096344314-7cc3419bb69e?q=80&w=800"
        ),
        Triple(
            "Organic Punjab Farm Groceries",
            "Crisp vegetables and sweet orchard fruits sourced directly from Punjab's green crops.",
            "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=800"
        ),
        Triple(
            "Peshawari Custom Sandals",
            "Authentic leather footwear from master artisans of Peshawar, delivered straight to your door.",
            "https://images.unsplash.com/photo-1549298916-b41d501d3772?q=80&w=800"
        ),
        Triple(
            "Local Pakistan Boutique Wear",
            "Stunning custom kurtas, festive shalwar kameez, and daily-wear lawn at Jhelum's fairest prices.",
            "https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?q=80&w=800"
        ),
        Triple(
            "Smart Electronics & Gadgets",
            "Power banks and handy tech elements tailored to beat load-shedding demands in Jhelum district.",
            "https://images.unsplash.com/photo-1609592424085-f0bf476f7572?q=80&w=800"
        ),
        Triple(
            "Rent-a-Car & Self-Drives",
            "Rent premium Honda and Toyota models to get around Dina, Sohawa, or Khewra Salt Mines with ease.",
            "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=800"
        ),
        Triple(
            "Safe EasyPaisa payments",
            "Submit purchases securely and verify instantly using EasyPaisa or JazzCash 12-digit transaction checks.",
            "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?q=80&w=800"
        )
    )

    var currentSlide by remember { mutableIntStateOf(0) }

    // Cyclic auto-run slider loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentSlide = (currentSlide + 1) % slides.size
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // background visual layout loaded dynamically via Coil with overlay tint
            Image(
                painter = rememberAsyncImagePainter(model = slides[currentSlide].third),
                contentDescription = slides[currentSlide].first,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Overlap dark gradient for absolute maximum contrast readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Slide text contents
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "DinaMarket PK - what we offer",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24), // Gold accent
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = slides[currentSlide].first,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = slides[currentSlide].second,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Dot indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                slides.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (i == currentSlide) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

// PRODUCT CARD GRID COMPONENT
@Composable
fun ProductCardItem(
    product: Product,
    onProductClick: (Product) -> Unit,
    onAddToCartClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) }
            .testTag("prod_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderGray(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFEDF2F7))
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = if (product.images.isNotEmpty()) product.images[0] else "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=600"
                    ),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Boosting algorithm tag overlay
                if (product.boostScore == 100) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "RECOMMENDED",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Out of stock overlay
                if (product.isOutOfStock()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OUT OF STOCK",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Shop",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = product.sellerName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB000),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", product.rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ordersText = LanguageManager.translate("order_counter")
                    Text(
                        text = String.format(ordersText, product.monthlyOrders),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rs. ${product.price}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            if (product.isOutOfStock()) {
                                Toast.makeText(context, "Item is out of stock!", Toast.LENGTH_SHORT).show()
                            } else {
                                onAddToCartClick()
                                Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (product.isOutOfStock()) Color.LightGray else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .testTag("add_cart_quick_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Quick",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// PRODUCT DETAIL SCREEN
@Composable
fun ProductDetailScreenLayout(
    product: Product,
    viewModel: MarketViewModel,
    onGoBack: () -> Unit,
    onWriteReview: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val reviews by viewModel.selectedProductReviews.collectAsState()
    var ratingInput by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var carouselIndex by remember { mutableIntStateOf(0) }

    val images = product.images.ifEmpty { listOf("https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=600") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("detail_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(product.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Sliding carousel layout up to 3 models
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = borderGray()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = rememberAsyncImagePainter(images[carouselIndex]),
                            contentDescription = product.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (images.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        carouselIndex = if (carouselIndex == 0) images.size - 1 else carouselIndex - 1
                                    },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", tint = Color.White)
                                }
                                IconButton(
                                    onClick = {
                                        carouselIndex = (carouselIndex + 1) % images.size
                                    },
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = Color.White)
                                }
                            }
                        }

                        // Bottom Dot Indicators
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            images.forEachIndexed { i, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (i == carouselIndex) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }

            // Description details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderGray(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rs. ${product.price}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Stock status
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (product.isOutOfStock()) Color(0xFFFED7D7) else Color(0xFFC6F6D5)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (product.isOutOfStock()) LanguageManager.translate("out_of_stock") else "In Stock",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (product.isOutOfStock()) Color(0xFFC53030) else Color(0xFF22543D),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val leftKey = LanguageManager.translate("only_left")
                        Text(
                            text = if (product.isOutOfStock()) "Out of stock! Please come back later" else String.format(leftKey, product.stockCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (product.isOutOfStock()) Color.Red else MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Category: ${product.category}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(product.description, fontSize = 13.sp, color = Color(0xFF2D3748), lineHeight = 18.sp)
                    }
                }
            }

            // Button add to cart
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (product.isOutOfStock()) {
                            Toast.makeText(context, "Cannot purchase out of stock product!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addToCart(product)
                            Toast.makeText(context, "Added to shopping cart!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_add_cart_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (product.isOutOfStock()) Color.DarkGray else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !product.isOutOfStock()
                ) {
                    Text(
                        text = LanguageManager.translate("add_to_cart"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Realtime Review System
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = LanguageManager.translate("reviews_header"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Add reviews form
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderGray(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Write your Verified Review", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Star Selector
                        Row {
                            (1..5).forEach { star ->
                                IconButton(onClick = { ratingInput = star }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star $star",
                                        tint = if (star <= ratingInput) Color(0xFFFFB000) else Color.LightGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            placeholder = { Text("Share your purchase experience with local buyers...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("detail_review_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (reviewComment.trim().isEmpty()) {
                                    Toast.makeText(context, "Review comment cannot be empty!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onWriteReview(ratingInput, reviewComment)
                                    reviewComment = ""
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("detail_submit_review_btn"),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Post Review")
                        }
                    }
                }
            }

            // Reviews list
            if (reviews.isEmpty()) {
                item {
                    Text(
                        "No reviews posted yet. Be the first verification buyer!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(reviews) { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderGray()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.userName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row {
                                    (1..r.userRating).forEach {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Rating Star",
                                            tint = Color(0xFFFFB000),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LanguageManager.translate("verified_buyer"), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(r.comment, fontSize = 12.sp, color = Color(0xFF2D3748))
                        }
                    }
                }
            }
        }
    }
}

// SHOPPING CART SCREEN
@Composable
fun ShoppingCartScreenLayout(
    viewModel: MarketViewModel,
    onGoBack: () -> Unit,
    onProceedCheckout: () -> Unit
) {
    val cartItems = viewModel.cartItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple back headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("cart_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(LanguageManager.translate("cart_heading"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Empty", modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text(LanguageManager.translate("cart_empty"), color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoBack, shape = RoundedCornerShape(8.dp)) {
                    Text("Explore Marketplace")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cartItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderGray()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = if (item.product.images.isNotEmpty()) item.product.images[0] else ""
                                    ),
                                    contentDescription = item.product.title,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                    Text("Rs. ${item.product.price}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Total item: Rs. ${item.totalItemPrice}", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(item.product.id, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp).testTag("qty_minus_${item.product.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Minus", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = item.quantity.toString(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(item.product.id, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp).testTag("qty_plus_${item.product.id}")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(LanguageManager.translate("total"), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Rs. ${viewModel.getCartTotal()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onProceedCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // min 48dp touch target
                        .testTag("cart_checkout_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(LanguageManager.translate("proceed_checkout"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// LOCALIZED CHECKOUT SCREEN WITH EASYPAISA / JAZZCASH PREFS
@Composable
fun CheckoutScreenLayout(
    viewModel: MarketViewModel,
    onGoBack: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(viewModel.loggedInName.value) }
    var phoneNumber by remember { mutableStateOf("") }
    var addressDetails by remember { mutableStateOf("") }
    var selectedWallet by remember { mutableStateOf("EasyPaisa") } // "EasyPaisa" or "JazzCash"
    var transactionId by remember { mutableStateOf("") } // 12-digit TID

    var checkoutError by remember { mutableStateOf<String?>(null) }
    var processingOrderState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("checkout_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(LanguageManager.translate("checkout_heading"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Error panel
            checkoutError?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFED7D7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = err,
                        color = Color(0xFF9B2C2C),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text("1. Customer Delivery Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(LanguageManager.translate("full_name")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_name_field"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text(LanguageManager.translate("phone_number")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_phone_field"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = addressDetails,
                onValueChange = { addressDetails = it },
                label = { Text(LanguageManager.translate("exact_location")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("checkout_address_field"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("2. " + LanguageManager.translate("wallet_heading"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                LanguageManager.translate("wallet_sub"),
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // EasyPaisa / JazzCash design toggle buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedWallet = "EasyPaisa" }
                        .testTag("checkout_wallet_easypaisa"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedWallet == "EasyPaisa") MaterialTheme.colorScheme.primary else Color.White
                    ),
                    border = borderGray()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "EasyPaisa",
                            tint = if (selectedWallet == "EasyPaisa") Color.White else Color.Green,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            LanguageManager.translate("easypaisa_pay"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedWallet == "EasyPaisa") Color.White else Color.DarkGray
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedWallet = "JazzCash" }
                        .testTag("checkout_wallet_jazzcash"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedWallet == "JazzCash") MaterialTheme.colorScheme.primary else Color.White
                    ),
                    border = borderGray()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "JazzCash",
                            tint = if (selectedWallet == "JazzCash") Color.White else Color.Blue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            LanguageManager.translate("jazzcash_pay"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedWallet == "JazzCash") Color.White else Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Merchant mockup details info panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                border = borderGray(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        LanguageManager.translate("seller_wallet_details") + ":",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text("Name: DinaMarket Verification Wallet", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Number: 03001234567 (EasyPaisa) / 03129876543 (JazzCash)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = transactionId,
                onValueChange = { transactionId = it },
                label = { Text(LanguageManager.translate("tid_verification")) },
                placeholder = { Text("12-digit number of receipt", fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkout_tid_field"),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                LanguageManager.translate("self_pickup_delivery"),
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                LanguageManager.translate("success_charge_msg"),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val lowerAddress = addressDetails.lowercase()
                    val isJhelumAddress = lowerAddress.contains("jhelum") || 
                                          lowerAddress.contains("dina") || 
                                          lowerAddress.contains("sohawa") || 
                                          lowerAddress.contains("khewra") ||
                                          lowerAddress.contains("punjab") || 
                                          lowerAddress.contains("pind dadan")
                    if (!isJhelumAddress && addressDetails.isNotEmpty()) {
                        checkoutError = "DinaMarket PK delivery operations are strictly limited to Jhelum District area (Jhelum City, Dina, Sohawa, Khewra). Please specify Jhelum region in your delivery address."
                        return@Button
                    }
                    processingOrderState = true
                    viewModel.performCheckout(
                        fullName = fullName,
                        phoneInput = phoneNumber,
                        deliveryAddr = addressDetails,
                        method = selectedWallet,
                        tid = transactionId,
                        onSuccess = {
                            processingOrderState = false
                            onComplete()
                        },
                        onError = { errorText ->
                            processingOrderState = false
                            checkoutError = errorText
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("checkout_submit_btn"),
                shape = RoundedCornerShape(8.dp),
                enabled = !processingOrderState
            ) {
                Text(
                    text = if (processingOrderState) "Submitting verified checkout..." else LanguageManager.translate("submit_order"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// SELLER SUITE CENTRAL DISPATCH (Setup + Cockpit Lists)
@Composable
fun SellerSuiteScreenLayout(
    viewModel: MarketViewModel,
    onGoBack: () -> Unit
) {
    val context = LocalContext.current
    val sellerProf by viewModel.sellerProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple back navbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("seller_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (sellerProf != null) "${sellerProf?.businessName} - Cockpit" else LanguageManager.translate("seller_suite_reg"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (sellerProf == null) {
            // Require shop registration setup
            SellerSetupRegistrationForm(viewModel)
        } else {
            // Main business suite dashboards
            SellerSuiteDashboardCockpit(viewModel)
        }
    }
}

// SELLER REGISTRATION LAYOUT
@Composable
fun SellerSetupRegistrationForm(viewModel: MarketViewModel) {
    var bName by remember { mutableStateOf("") }
    var bBio by remember { mutableStateOf("") }
    var bLogo by remember { mutableStateOf("") }
    var bLoc by remember { mutableStateOf("") }
    var bEmail by remember { mutableStateOf("") }
    var bWhatsApp by remember { mutableStateOf("") }

    var formErr by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Configure Your Local Shop Front",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        formErr?.let { err ->
            Text(err, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = bName,
            onValueChange = { bName = it },
            label = { Text(LanguageManager.translate("business_name")) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_reg_bizname"),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bBio,
            onValueChange = { bBio = it },
            label = { Text(LanguageManager.translate("bio_desc")) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .testTag("seller_reg_bio"),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        SmartImageUploadWidget(
            label = LanguageManager.translate("store_logo"),
            currentValue = bLogo,
            onValueChange = { bLogo = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bLoc,
            onValueChange = { bLoc = it },
            label = { Text(LanguageManager.translate("physical_loc")) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_reg_location"),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bEmail,
            onValueChange = { bEmail = it },
            label = { Text(LanguageManager.translate("contact_email")) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_reg_email"),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bWhatsApp,
            onValueChange = { bWhatsApp = it },
            label = { Text(LanguageManager.translate("whatsapp_num")) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("seller_reg_whatsapp"),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            LanguageManager.translate("legal_notice") + " " + LanguageManager.translate("success_charge_msg"),
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (bName.isEmpty() || bLoc.isEmpty() || bWhatsApp.isEmpty()) {
                    formErr = "Shop Name, physical Stall Location, and WhatsApp details are strictly required."
                } else {
                    viewModel.registerNewSeller(
                        bizName = bName,
                        bio = bBio,
                        logo = bLogo,
                        location = bLoc,
                        email = bEmail,
                        whatsapp = bWhatsApp,
                        onSuccess = {
                            formErr = null
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("seller_reg_submit_btn"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(LanguageManager.translate("register_business"), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// SELLER MAIN BUSINESS SUITE COCKPIT
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerSuiteDashboardCockpit(viewModel: MarketViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("inventory") } // inventory, orders
    val sellerOrders by viewModel.sellerOrders.collectAsState()

    // Forms states for publishing new product
    var showAddProductDialog by remember { mutableStateOf(false) }
    var pTitle by remember { mutableStateOf("") }
    var pDesc by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pStock by remember { mutableStateOf("") }
    var pImgUrl by remember { mutableStateOf("") }
    var pCategory by remember { mutableStateOf("Fast Food") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers using premium Primary color setup
        TabRow(
            selectedTabIndex = if (activeTab == "inventory") 0 else 1,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "inventory") 0 else 1]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = activeTab == "inventory",
                onClick = { activeTab = "inventory" },
                text = { Text("Inventory Catalog", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("seller_tab_inventory")
            )
            Tab(
                selected = activeTab == "orders",
                onClick = { activeTab = "orders" },
                text = { Text("Customer Orders", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("seller_tab_orders")
            )
        }

        if (activeTab == "inventory") {
            // Inventory page layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        LanguageManager.translate("inventory_title"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Button(
                        onClick = { showAddProductDialog = !showAddProductDialog },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("seller_add_product_toggle")
                    ) {
                        Text(
                            text = if (showAddProductDialog) "Minimize" else LanguageManager.translate("add_item"),
                            fontSize = 12.sp
                        )
                    }
                }

                if (showAddProductDialog) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderGray(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("New Product Specifications", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = pTitle,
                                onValueChange = { pTitle = it },
                                label = { Text(LanguageManager.translate("product_title")) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seller_add_title"),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = pDesc,
                                onValueChange = { pDesc = it },
                                label = { Text(LanguageManager.translate("product_desc")) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .testTag("seller_add_desc"),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row {
                                OutlinedTextField(
                                    value = pPrice,
                                    onValueChange = { pPrice = it },
                                    label = { Text(LanguageManager.translate("base_price")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("seller_add_price"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = pStock,
                                    onValueChange = { pStock = it },
                                    label = { Text(LanguageManager.translate("total_stock")) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("seller_add_stock"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            SmartImageUploadWidget(
                                label = "Product Picture (Upload or URL)",
                                currentValue = pImgUrl,
                                onValueChange = { pImgUrl = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Select Item Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            val catOptions = listOf("Fast Food", "Grocery", "Clothes", "Cosmetics", "Electronics", "Rent-a-Car")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(catOptions) { itemCat ->
                                    val isCurrent = pCategory == itemCat
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFFEDF2F7)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .clickable { pCategory = itemCat }
                                            .testTag("seller_add_cat_pill_$itemCat")
                                    ) {
                                        Text(
                                            itemCat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) Color.White else Color.DarkGray,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val pr = pPrice.toDoubleOrNull()
                                    val st = pStock.toIntOrNull()
                                    if (pTitle.isEmpty() || pr == null || st == null) {
                                        Toast.makeText(context, "Fill all product details correctly", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.publishNewProduct(
                                            title = pTitle,
                                            desc = pDesc,
                                            priceVal = pr,
                                            stockVal = st,
                                            imgUrl = pImgUrl,
                                            category = pCategory,
                                            onSuccess = {
                                                Toast.makeText(context, "Product Published with Recommended Boost!", Toast.LENGTH_SHORT).show()
                                                showAddProductDialog = false
                                                pTitle = ""
                                                pDesc = ""
                                                pPrice = ""
                                                pStock = ""
                                                pImgUrl = ""
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("seller_publish_submit_btn")
                            ) {
                                Text(LanguageManager.translate("add_product_btn"), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Showing current products of that seller
                val currentProducts by viewModel.products.collectAsState()
                val sellerProfile by viewModel.sellerProfile
                val filterSellerProds = currentProducts.filter { it.sellerId == sellerProfile?.uid }

                if (filterSellerProds.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active catalog listings found.", fontSize = 13.sp, color = Color.LightGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filterSellerProds) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                border = borderGray(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = if (prod.images.isNotEmpty()) prod.images[0] else ""
                                        ),
                                        contentDescription = prod.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        Text("Rs. ${prod.price} | Stock: ${prod.stockCount}", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    // Direct stock replenishment editor
                                    var quickStockInput by remember { mutableStateOf(prod.stockCount.toString()) }
                                    OutlinedTextField(
                                        value = quickStockInput,
                                        onValueChange = {
                                            quickStockInput = it
                                            val valid = it.toIntOrNull()
                                            if (valid != null) {
                                                viewModel.updateProductStock(prod.id, valid) {}
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .width(70.dp)
                                            .height(50.dp)
                                            .testTag("quick_stock_input_${prod.id}"),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Orders tab divided into "Pending Verification" and "Confirmed Orders"
            var activeOrderSection by remember { mutableStateOf("Pending Verification") }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeOrderSection = "Pending Verification" }
                        .testTag("order_section_pending"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeOrderSection == "Pending Verification") MaterialTheme.colorScheme.primary else Color(0xFFEDF2F7)
                    )
                ) {
                    Text(
                        LanguageManager.translate("orders_tab_pending"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeOrderSection == "Pending Verification") Color.White else Color.DarkGray,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeOrderSection = "Confirmed Orders" }
                        .testTag("order_section_confirmed"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeOrderSection == "Confirmed Orders") MaterialTheme.colorScheme.primary else Color(0xFFEDF2F7)
                    )
                ) {
                    Text(
                        LanguageManager.translate("orders_tab_confirmed"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeOrderSection == "Confirmed Orders") Color.White else Color.DarkGray,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            val filterOrders = sellerOrders.filter { it.status == activeOrderSection }

            if (filterOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records in this tracking console.", fontSize = 13.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filterOrders) { order ->
                        SellerOrderCardItem(order = order, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// SELLER INDIVIDUAL ORDER CARD ITEM
@Composable
fun SellerOrderCardItem(order: Order, viewModel: MarketViewModel) {
    var sellerNoteInput by remember { mutableStateOf("Your package is ready for self-pickup at stall 4:00 PM.") }
    var processingSettledState by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("order_card_${order.orderId}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = borderGray()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order: ${order.orderId.takeLast(6)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(order.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Buyer: ${order.buyerName} (${order.buyerPhone})", fontSize = 12.sp, color = Color.DarkGray)
            Text("Address: ${order.deliveryLocation}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(6.dp))

            Text("Receipt Wallets Check:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Text("Method: ${order.paymentMethod} | TID: ${order.transactionId}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(6.dp))

            // Cart Items list inside the order
            order.items.forEach { cartItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("- ${cartItem.product.title} (x${cartItem.quantity})", fontSize = 11.sp, color = Color.DarkGray)
                    Text("Rs. ${cartItem.totalItemPrice}", fontSize = 11.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total amount client paid:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Rs. ${order.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            if (order.status == "Pending Verification") {
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))

                Text(LanguageManager.translate("custom_msg_lbl"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = sellerNoteInput,
                    onValueChange = { sellerNoteInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("verified_note_${order.orderId}"),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        processingSettledState = true
                        viewModel.verifyOrderAndNotify(order, sellerNoteInput) {
                            processingSettledState = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_confirm_btn_${order.orderId}"),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !processingSettledState
                ) {
                    Text(
                        text = if (processingSettledState) "Updating order..." else LanguageManager.translate("verify_confirm_btn"),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            } else {
                order.sellerNote?.let { note ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                        border = borderGray(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Sent notification body:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(note, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// IN-APP SYSTEM NOTIFICATIONS HUB
@Composable
fun BuyerNotificationsScreenLayout(viewModel: MarketViewModel, onGoBack: () -> Unit) {
    val list by viewModel.buyerNotifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("notif_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(LanguageManager.translate("buyer_notification_inbox"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (list.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "No notifications", modifier = Modifier.size(54.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Your system notification inbox is empty.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(list) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderGray()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Icon(Icons.Default.Check, contentDescription = "Check", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(notif.body, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }
    }
}

// ACCOUNT & BUYER CORE HUB + SELLER ENTRY PORTAL
@Composable
fun AccountScreenLayout(
    viewModel: MarketViewModel,
    onGoBack: () -> Unit,
    onNavigateToSeller: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val userLoggedIn by viewModel.userLoggedIn
    val loggedInEmail by viewModel.loggedInEmail
    val loggedInName by viewModel.loggedInName
    val buyerOrders by viewModel.buyerOrders.collectAsState()
    
    // Auth inputs for local layout signup
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var registrationNameInput by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple elegant top bar for Account Screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                modifier = Modifier.testTag("account_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("User Account Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!userLoggedIn) {
                // NOT LOGGED IN MODE: Elegant local Sign-In / Sign-Up system
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("guest_account_prompt_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderGray(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isSignUpMode) "Register your Jhelum Resident Profile" else "Access Jhelum Digital Marketplace",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isSignUpMode) {
                                "Create a free DinaMarket PK profile to buy high-quality items, review local seller stores, and toggle your own vendor setup."
                            } else {
                                "Kindly log-in to trace live orders, publish store items, rate local Punjabi goods, and secure fast digital transaction confirmations."
                            },
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        if (statusMessage != null) {
                            Text(statusMessage!!, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = registrationNameInput,
                                onValueChange = { registrationNameInput = it },
                                label = { Text("Your Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person", tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_name_field"),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Your Registered Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Gray) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_field"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Secure Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("reg_pass_field"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    val name = registrationNameInput.trim()
                                    val email = emailInput.trim()
                                    val pwd = passwordInput
                                    if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                                        errorMessage = "All registration credentials are required!"
                                        return@Button
                                    }
                                    errorMessage = null
                                    statusMessage = "Authenticating secure setup..."
                                    
                                    // Set ViewModel credentials values
                                    viewModel.authEmailInput.value = email
                                    viewModel.authPasswordInput.value = pwd
                                    
                                    // Call authenticate with custom signup bypass trigger 
                                    viewModel.authenticateUser {
                                        Toast.makeText(context, "$name signed up successfully! ✨", Toast.LENGTH_SHORT).show()
                                        statusMessage = null
                                    }
                                } else {
                                    val email = emailInput.trim()
                                    val pwd = passwordInput
                                    if (email.isEmpty() || pwd.isEmpty()) {
                                        errorMessage = "Please enter valid email and password fields."
                                        return@Button
                                    }
                                    errorMessage = null
                                    statusMessage = "Sign-in in progress..."
                                    
                                    viewModel.authEmailInput.value = email
                                    viewModel.authPasswordInput.value = pwd
                                    
                                    viewModel.authenticateUser {
                                        Toast.makeText(context, "Welcome back to DinaMarket! 👋", Toast.LENGTH_SHORT).show()
                                        statusMessage = null
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isSignUpMode) "Sign Up & Register" else "Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Google Sign-In ready for configuration.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = borderGray(),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Simple Google 'G' icon representation using shape
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color(0xFFDB4437), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Continue with Google", color = Color.DarkGray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isSignUpMode) "Already have an account? Sign In" else "New to Dinamarket Pakistan? Sign Up",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    isSignUpMode = !isSignUpMode
                                    errorMessage = null
                                    statusMessage = null
                                }
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            } else {
                // LOGGED IN MODULE
                // 1. Profile information header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = borderGray(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loggedInName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(loggedInName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                            Text(loggedInEmail, fontSize = 12.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            // Verification Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified Location Badge", tint = Color(0xFF00A86B), modifier = Modifier.size(15.dp))
                                Text("Jhelum Area Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00A86B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. MERCHANT HUB ACCESS DIRECT ROUTER (CRITICAL SATISFIER)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDFBF4)), // soft emerald light green accent
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1FAE5)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00A86B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("V", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "💼 Switch to Seller Suite Dashboard",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E5D3E)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create your physical store listing for Dina, Sohawa, Jhelum City, or Khewra. List Lahore styled dresses, Punjab farm items, and accept direct easy EasyPaisa/JazzCash digital payments.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E5D3E).copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                viewModel.currentUserRole.value = "Seller"
                                viewModel.fetchSellerProfileAndOrders()
                                onNavigateToSeller()
                            },
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("account_to_seller_transition_btn"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A86B))
                        ) {
                            Text("Switch to Seller Dashboard View", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. ACTUAL LIVE ORDER LIST TRACING (REALTIME VERIFIER)
                Text(
                    text = "📦 Your Purchases & Direct Verification status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (buyerOrders.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderGray(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No catalog items purchased yet.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Browse Punjab farm fresh mangoes, clothing items, and Peshawari shoes on our Jhelum feeds, select EasyPaisa as payment, and check progress right here in real time!",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    }
                } else {
                    buyerOrders.forEach { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = borderGray(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ID: ${order.orderId.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                    // Verification status label
                                    val isApproved = order.status == "Confirmed & Settled"
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isApproved) Color(0xFFEDFBF4) else Color(0xFFFEF3C7)
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isApproved) Color(0xFF00A86B) else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total Paid: RS ${order.totalAmount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Method: ${order.paymentMethod} | Direct TID: ${order.transactionId}",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )

                                order.sellerNote?.let { note ->
                                    if (note.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Merchant Note: $note",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.DarkGray,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.height(24.dp))

                // Logout button
                Button(
                    onClick = {
                        viewModel.performLogout()
                        Toast.makeText(context, "Log-out complete.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("account_logoff_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)), // soft reddish white
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Direct Sign Out / Logoff Profile", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Compliance info & Links Segment
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("compliance_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = borderGray(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LanguageManager.translate("terms_legal"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        context.startActivity(Intent(context, TermsAndConditionsActivity::class.java))
                                    }
                                    .testTag("footer_terms_link")
                            )
                            Text(
                                text = "|",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = LanguageManager.translate("privacy_legal"),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                                    }
                                    .testTag("footer_privacy_link")
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = LanguageManager.translate("legal_notice"),
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "DinaMarket is dedicated strictly to residents of Jhelum district (Dina, Sohawa, Jhelum City, Khewra, Pind Dadan Khan, etc.), Punjab, Pakistan.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// SECURE FIREBASE AUTH OVERLAY
@Composable
fun AuthPopupOverlay(
    viewModel: MarketViewModel,
    onDismiss: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    var email by viewModel.authEmailInput
    var password by viewModel.authPasswordInput
    val error by viewModel.authError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() } // Dim dismiss tap
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
                .clickable(enabled = false) { /* prevent block propagation */ }
                .testTag("auth_popup_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        LanguageManager.translate("login_required"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("auth_dismiss_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error indicator
                error?.let { err ->
                    Text(
                        text = err,
                        color = Color.Red,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(LanguageManager.translate("login_email")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(LanguageManager.translate("login_pwd")) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.authenticateUser {
                            onAuthSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_login_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(LanguageManager.translate("login_btn"), color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// BORDER HELPER
@Composable
fun borderGray(): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
}

// REAL IMGBB UPLOADER METHOD USING RECIPIENT ENDPOINT
suspend fun uploadImageToImgBB(imageBytes: ByteArray, filename: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    filename,
                    imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload?key=58a94c30abc6fe26c5a4d1c4658e2156")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    android.util.Log.e("ImgBB", "HTTP Failed: ${response.code} - ${response.message}")
                    return@withContext null
                }
                val bodyString = response.body?.string() ?: return@withContext null
                android.util.Log.d("ImgBB", "Response: $bodyString")
                // Extract public hosted "url" from the json string safely via regex
                val regex = """\"url\":\"([^\"]+)\"""".toRegex()
                val match = regex.find(bodyString)
                val url = match?.groupValues?.get(1)?.replace("\\/", "/")
                url
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// SMART SYSTEM TO CONVERT LOCAL OR PRESET IMAGES TO CDN CHANNELS
@Composable
fun SmartImageUploadWidget(
    label: String,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf("") }
    var showPresetsDialog by remember { mutableStateOf(false) }

    // Multi-contract media picker launcher with ImgBB integration
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                isUploading = true
                uploadStatusMessage = "Reading local image bytes..."
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bytes = stream?.readBytes()
                    stream?.close()
                    if (bytes != null) {
                        uploadStatusMessage = "Hosting live on ImgBB Server... 🚀"
                        val uploadedUrl = uploadImageToImgBB(bytes, "dinamarket_item.jpg")
                        if (uploadedUrl != null) {
                            onValueChange(uploadedUrl)
                            uploadStatusMessage = "ImgBB URL generated!"
                            Toast.makeText(context, "Hosted on ImgBB: $uploadedUrl ✨", Toast.LENGTH_LONG).show()
                        } else {
                            uploadStatusMessage = "ImgBB error. Saved local uri path..."
                            onValueChange(uri.toString())
                            Toast.makeText(context, "Network error. Used local URI path preview.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onValueChange(uri.toString())
                    }
                } catch (e: Exception) {
                    onValueChange(uri.toString())
                } finally {
                    delay(500)
                    isUploading = false
                    uploadStatusMessage = ""
                }
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = borderGray(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image previews block
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    if (currentValue.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = currentValue),
                            contentDescription = "Image preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No Image",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Upload button
                    Button(
                        onClick = { pickerLauncher.launch("image/*") },
                        enabled = !isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Upload from Device", fontSize = 11.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset assets button
                    Button(
                        onClick = { showPresetsDialog = true },
                        enabled = !isUploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Select Pakistani Preset", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uploadStatusMessage,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Or manual input field
            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                placeholder = { Text("Or paste any custom picture URL link here", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }
    }

    // presets dialogue picker
    if (showPresetsDialog) {
        val presets = listOf(
            "Karachi Special Biryani" to "https://images.unsplash.com/photo-1633945274405-b6c8069047b0?q=80&w=600",
            "Peshawari Leather Sandals" to "https://images.unsplash.com/photo-1549298916-b41d501d3772?q=80&w=600",
            "Pure Organic Zafran Saffron" to "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?q=80&w=600",
            "White Jasmine Essence (Itar)" to "https://images.unsplash.com/photo-1528740564265-2d82f3788da4?q=80&w=600",
            "Lahore Tech Accessories" to "https://images.unsplash.com/photo-1609592424085-f0bf476f7572?q=80&w=600",
            "Civic Self-Drive Car Rental" to "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?q=80&w=600",
            "Market Grocery Shopfront Logo" to "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=600",
            "Traditional Jhelum Khas Sweets" to "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?q=80&w=600"
        )

        androidx.compose.ui.window.Dialog(onDismissRequest = { showPresetsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(340.dp)
                ) {
                    Text(
                        "DinaMarket Premium Presets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(presets) { (title, url) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(url)
                                        showPresetsDialog = false
                                    }
                                    .border(1.dp, Color(0xFFEDF2F7), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = url),
                                    contentDescription = title,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showPresetsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

// DARAZ STYLE CIRCULAR SHORTCUTS ROW
data class DarazShortcutItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val action: () -> Unit
)

@Composable
fun DarazShortcutRow(
    onSelectCategory: (String) -> Unit,
    onSearchRecommended: () -> Unit,
    onContactSupport: () -> Unit
) {
    val shortcuts = listOf(
        DarazShortcutItem("Verified Mall", Icons.Default.CheckCircle, Color(0xFF00A86B)) { onSelectCategory("All") },
        DarazShortcutItem("Punjab Mart", Icons.Default.ShoppingCart, Color(0xFFE11D48)) { onSelectCategory("Grocery") },
        DarazShortcutItem("Dina Fleet", Icons.Default.LocationOn, Color(0xFF3B82F6)) { onSelectCategory("Rent-a-Car") },
        DarazShortcutItem("Hot Deals", Icons.Default.Star, Color(0xFFFFB000)) { onSearchRecommended() },
        DarazShortcutItem("Helpline", Icons.Default.Call, Color(0xFF059669)) { onContactSupport() },
        DarazShortcutItem("Info Hub", Icons.Default.Info, Color(0xFF7C3AED)) { onContactSupport() }
    )

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = "⚡ QUICK SHORTCUTS (DARAZ STYLE)",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shortcuts) { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { item.action() }
                        .width(74.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(item.color.copy(alpha = 0.12f))
                            .border(1.5.dp, item.color.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = item.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

