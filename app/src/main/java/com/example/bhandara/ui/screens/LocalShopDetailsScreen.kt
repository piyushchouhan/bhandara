package com.example.bhandara.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.ui.components.ReviewsSection
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalShopDetailsScreen(
    shopId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BackendRepository() }
    
    var shop by remember { mutableStateOf<LocalShopResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showSuggestDeleteDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedFullScreenImageIndex by remember { mutableStateOf<Int?>(null) }
    val currentUserUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    
    val prefs = context.getSharedPreferences("ShopActionsPrefs", android.content.Context.MODE_PRIVATE)
    var hasReported by remember { mutableStateOf(prefs.getBoolean("reported_shop_$shopId", false)) }
    var hasSuggestedDelete by remember { mutableStateOf(prefs.getBoolean("suggested_delete_shop_$shopId", false)) }
    
    // Fetch shop details from backend
    LaunchedEffect(shopId) {
        isLoading = true
        val result = repository.getLocalShopById(shopId)
        if (result != null) {
            shop = result
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (shop != null) {
            val shopImages = shop!!.imageUrls ?: emptyList()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Carousel
                if (shopImages.isNotEmpty()) {
                    val carouselState = rememberCarouselState(
                        initialItem = 0,
                        itemCount = { shopImages.size }
                    )
                    
                    HorizontalUncontainedCarousel(
                        state = carouselState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        itemWidth = 340.dp,
                        itemSpacing = 12.dp
                    ) { index ->
                        Card(
                            modifier = Modifier
                                .height(260.dp)
                                .maskClip(RoundedCornerShape(16.dp))
                                .clickable { selectedFullScreenImageIndex = index }
                        ) {
                            AsyncImage(
                                model = shopImages[index],
                                contentDescription = "Shop image ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // Placeholder if no images
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs
                val tabs = listOf("Overview", "Reviews")
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            ) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedTabIndex == 0) {
                    // Overview Content
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Shop info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = shop!!.shopName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (shop!!.isVerified == true) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Verified Shop",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (shop!!.isActive != false) {
                                val isOwner = currentUserUid != null && shop!!.ownerUid == currentUserUid
                                IconButton(
                                    onClick = {
                                        if (isOwner) {
                                            showDeactivateDialog = true
                                        } else {
                                            showSuggestDeleteDialog = true
                                        }
                                    },
                                    enabled = isOwner || !hasSuggestedDelete,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.DeleteForever,
                                        contentDescription = if (isOwner) "Deactivate Shop" else "Suggest Delete"
                                    )
                                }
                            }

                            if (shop!!.isActive == false) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Inactive") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Cancel,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            } else if (shop!!.isCurrentlyOpen == true) {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Open Now") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                    
                    // Time, Location
                    InfoCard(shop!!)
                    
                    // Menu Items
                    if (!shop!!.menuItems.isNullOrEmpty()) {
                        MenuItemsSection(shop!!.menuItems!!)
                    }
                    
                    // Description
                    if (!shop!!.description.isNullOrBlank()) {
                        DescriptionSection(shop!!.description!!)
                    }
                    
                    // Additional Info
                    AdditionalInfoSection(shop!!)
                    
                    // Report Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { showReportDialog = true },
                            enabled = !hasReported,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (hasReported) "Reported" else "Report this shop")
                        }
                    }
                    
                    // Action Buttons
                    ActionButtons(
                        shop = shop!!,
                        onGetDirections = {
                            val uri = Uri.parse("geo:${shop!!.latitude},${shop!!.longitude}?q=${shop!!.latitude},${shop!!.longitude}(${shop!!.shopName})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        onCall = {
                            if (!shop!!.ownerPhone.isNullOrBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop!!.ownerPhone}"))
                                context.startActivity(intent)
                            }
                        }
                    )

                    
                    Spacer(modifier = Modifier.height(32.dp))
                    }
                } else if (selectedTabIndex == 1) {
                    // Reviews Content
                    ReviewsSection(
                        targetId = shopId,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Failed to load shop details")
            }
        }
    }

    if (selectedFullScreenImageIndex != null && shop?.imageUrls?.isNotEmpty() == true) {
        com.example.bhandara.ui.components.FullScreenImageCarousel(
            imageUrls = shop!!.imageUrls!!,
            initialIndex = selectedFullScreenImageIndex!!,
            onDismissRequest = { selectedFullScreenImageIndex = null }
        )
    }

    if (showReportDialog) {
        var selectedReason by remember { mutableStateOf("") }
        val reasons = listOf("Not a food shop", "Incorrect location", "Inappropriate content", "Shop is permanently closed")
        
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Shop") },
            text = {
                Column {
                    Text("Please select a reason for reporting:")
                    Spacer(modifier = Modifier.height(16.dp))
                    reasons.forEach { reason ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (reason == selectedReason),
                                onClick = { selectedReason = reason }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reason)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReportDialog = false
                        scope.launch {
                            val result = repository.reportLocalShop(shopId)
                            if (result != null) {
                                Toast.makeText(context, "Shop reported successfully", Toast.LENGTH_SHORT).show()
                                prefs.edit().putBoolean("reported_shop_$shopId", true).apply()
                                hasReported = true
                                shop = result
                            } else {
                                Toast.makeText(context, "Failed to report shop", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = selectedReason.isNotEmpty()
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Deactivate Shop") },
            text = { Text("Are you sure you want to deactivate this shop? It will no longer be visible to users.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeactivateDialog = false
                        scope.launch {
                            val result = repository.deactivateLocalShop(shopId)
                            if (result != null) {
                                Toast.makeText(context, "Shop deactivated successfully", Toast.LENGTH_SHORT).show()
                                shop = result
                            } else {
                                Toast.makeText(context, "Failed to deactivate shop", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Deactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuggestDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showSuggestDeleteDialog = false },
            title = { Text("Suggest Deletion") },
            text = { Text("Are you sure you want to suggest deleting this shop? If multiple users report it, the shop will be reviewed and removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuggestDeleteDialog = false
                        scope.launch {
                            val result = repository.suggestDeleteLocalShop(shopId)
                            if (result != null) {
                                Toast.makeText(context, "Deletion suggested successfully", Toast.LENGTH_SHORT).show()
                                prefs.edit().putBoolean("suggested_delete_shop_$shopId", true).apply()
                                hasSuggestedDelete = true
                                shop = result
                            } else {
                                Toast.makeText(context, "Failed to suggest deletion", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Suggest Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuggestDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoCard(shop: LocalShopResponse) {
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    
    val startTime = if (!shop.openingTime.isNullOrBlank()) {
        try {
            LocalTime.parse(shop.openingTime).format(timeFormatter)
        } catch (e: Exception) {
            shop.openingTime
        }
    } else null
    
    val endTime = if (!shop.closingTime.isNullOrBlank()) {
        try {
            LocalTime.parse(shop.closingTime).format(timeFormatter)
        } catch (e: Exception) {
            shop.closingTime
        }
    } else null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Time
            if (startTime != null && endTime != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$startTime - $endTime",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Location
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Column {
                    Text(
                        text = shop.fullAddress ?: shop.area ?: shop.city ?: "Location not provided",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (!shop.landmark.isNullOrBlank()) {
                        Text(
                            text = "Near: ${shop.landmark}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    if (shop.distance != null) {
                        val distanceKm = (shop.distance / 1000.0).roundToInt()
                        Text(
                            text = "$distanceKm km away",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MenuItemsSection(menuItems: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Menu Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                menuItems.forEach { item ->
                    AssistChip(
                        onClick = { },
                        label = { Text(item) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdditionalInfoSection(shop: LocalShopResponse) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (!shop.shopType.isNullOrBlank() || !shop.cuisineType.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Store,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Shop Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val typeText = listOfNotNull(shop.shopType, shop.cuisineType).joinToString(" • ")
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (shop.averageCostForTwo != null && shop.averageCostForTwo > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Average Cost",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${shop.averageCostForTwo} for two (approx.)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Facilities Row
            val facilities = mutableListOf<String>()
            if (shop.hasSeating == true) facilities.add("Seating Available")
            if (shop.homeDelivery == true) facilities.add("Home Delivery")
            if (shop.takeaway == true) facilities.add("Takeaway")
            if (shop.wifiAvailable == true) facilities.add("Wi-Fi")
            if (shop.onlinePayment == true) facilities.add("Online Payment")

            if (facilities.isNotEmpty()) {
                Text(
                    text = "Facilities",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    facilities.forEach { facility ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(facility) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    shop: LocalShopResponse,
    onGetDirections: () -> Unit,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onGetDirections,
            modifier = Modifier.weight(1f),
            enabled = shop.isActive != false
        ) {
            Icon(
                Icons.Default.Directions,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Directions")
        }
        
        if (!shop.ownerPhone.isNullOrBlank()) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.weight(1f),
                enabled = shop.isActive != false
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call")
            }
        }
    }
}
