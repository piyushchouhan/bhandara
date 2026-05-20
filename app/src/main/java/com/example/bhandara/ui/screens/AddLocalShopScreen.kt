package com.example.bhandara.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bhandara.R
import com.example.bhandara.data.models.api.LocalShopRequest
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.utils.ImageUploadHelper
import com.example.bhandara.utils.LocationHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddLocalShopScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Repositories
    val backendRepository = remember { BackendRepository() }
    val userRepository = remember { UserRepository() }
    val locationHelper = remember { LocationHelper(context) }
    val imageUploadHelper = remember { ImageUploadHelper(context) }

    // Required Fields State
    var shopName by remember { mutableStateOf("") }
    var shopType by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf(listOf<String>()) }
    var currentMenuItem by remember { mutableStateOf("") }

    // Optional Fields State
    var showOptionalDetails by remember { mutableStateOf(false) }
    
    var ownerPhone by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var averageCostForTwo by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Checkboxes / Toggles
    var homeDelivery by remember { mutableStateOf(false) }
    var takeaway by remember { mutableStateOf(true) }
    var hasSeating by remember { mutableStateOf(true) }
    var wifiAvailable by remember { mutableStateOf(false) }

    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMovingCartDialog by remember { mutableStateOf(true) }
    var isMovingCart by remember { mutableStateOf(false) }
    var selectedMapIcon by remember { mutableStateOf("default") }
    
    // Dropdowns
    var shopTypeExpanded by remember { mutableStateOf(false) }
    val shopTypes = listOf("Restaurant", "Food Truck", "Street Food", "Cafe", "Fast Food", "Bakery", "Sweet Shop")
    
    var priceRangeExpanded by remember { mutableStateOf(false) }
    val priceRanges = listOf("$", "$$", "$$$", "$$$$")

    // Detailed menu items (from AddMenuItemsScreen, saved locally before shop is created)
    val menuItemPrefs = remember { context.getSharedPreferences("DraftMenuItems", android.content.Context.MODE_PRIVATE) }
    val initialDraftItems: List<com.example.bhandara.data.models.api.MenuItemRequest> = remember {
        val json = menuItemPrefs.getString("draft_items", null)
        if (json != null) {
            try {
                val arr = org.json.JSONArray(json)
                List(arr.length()) { i ->
                    val obj = arr.getJSONObject(i)
                    com.example.bhandara.data.models.api.MenuItemRequest(
                        name = obj.getString("name"),
                        foodType = obj.getString("foodType"),
                        price = if (obj.has("price") && !obj.isNull("price")) obj.getDouble("price") else null
                    )
                }
            } catch (e: Exception) { listOf() }
        } else listOf()
    }
    var draftDetailedMenuItems by remember { mutableStateOf(initialDraftItems) }
    var showDetailedMenuScreen by remember { mutableStateOf(false) }

    // Camera/Image logic
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedImages = selectedImages + tempPhotoUri!!
        }
        showImageSourceSheet = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri = createTempImageUri()
            cameraLauncher.launch(tempPhotoUri!!)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        selectedImages = selectedImages + uris
        showImageSourceSheet = false
    }

    val saveShop: () -> Unit = {
        scope.launch {
            val finalMenuItems = if (currentMenuItem.isNotBlank()) {
                menuItems + currentMenuItem.trim()
            } else {
                menuItems
            }

            // Validation
            if (shopName.isBlank()) {
                errorMessage = "Shop name is required"
                return@launch
            }
            if (shopType.isBlank()) {
                errorMessage = "Shop type is required"
                return@launch
            }
            val allMenuItems = (finalMenuItems + draftDetailedMenuItems.map { it.name }).distinct()
            if (allMenuItems.isEmpty()) {
                errorMessage = "At least one menu item is required"
                return@launch
            }

            isLoading = true
            errorMessage = null

            try {
                val firebaseUid = userRepository.getCurrentUserId()
                if (firebaseUid == null) {
                    errorMessage = "User not authenticated"
                    isLoading = false
                    return@launch
                }

                val location = locationHelper.getCurrentLocation()
                if (location == null) {
                    errorMessage = "Could not get your location. Please enable GPS."
                    isLoading = false
                    return@launch
                }

                // Upload images if any
                var imageUrls: List<String>? = null
                if (selectedImages.isNotEmpty()) {
                    imageUrls = imageUploadHelper.uploadImages(selectedImages) { progress ->
                        uploadProgress = progress
                    }
                }

                val request = LocalShopRequest(
                    ownerUid = firebaseUid,
                    ownerPhone = ownerPhone.ifBlank { null },
                    ownerEmail = ownerEmail.ifBlank { null },
                    shopName = shopName.trim(),
                    shopType = shopType,
                    cuisineType = cuisineType.ifBlank { null },
                    description = description.ifBlank { null },
                    menuItems = allMenuItems,
                    priceRange = priceRange.ifBlank { null },
                    averageCostForTwo = averageCostForTwo.toIntOrNull(),
                    imageUrls = imageUrls,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    fullAddress = fullAddress.ifBlank { null },
                    landmark = landmark.ifBlank { null },
                    homeDelivery = homeDelivery,
                    takeaway = takeaway,
                    hasSeating = hasSeating,
                    wifiAvailable = wifiAvailable,
                    isMovingCart = isMovingCart,
                    mapIcon = selectedMapIcon
                )

                val response = backendRepository.createLocalShop(request)
                if (response != null) {
                    // Now save the detailed menu items if any
                    if (draftDetailedMenuItems.isNotEmpty()) {
                        backendRepository.addMenuItemsManual(
                            response.id,
                            com.example.bhandara.data.models.api.ManualMenuItemsRequest(draftDetailedMenuItems)
                        )
                    }
                    // Save vendor info if moving cart
                    if (isMovingCart) {
                        val vendorPrefs = context.getSharedPreferences("VendorPrefs", android.content.Context.MODE_PRIVATE)
                        vendorPrefs.edit()
                            .putLong("vendor_shop_id", response.id.toLongOrNull() ?: -1L)
                            .putString("vendor_owner_uid", firebaseUid)
                            .apply()
                    }
                    // Clear local draft
                    menuItemPrefs.edit().remove("draft_items").apply()
                    onNavigateBack()
                } else {
                    errorMessage = "Failed to add shop. Please try again."
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
                uploadProgress = 0
            }
        }
    }

    // Show the detailed menu local form (no API calls, just local state)
    if (showDetailedMenuScreen) {
        AddMenuItemsScreen(
            initialItems = draftDetailedMenuItems,
            onSaveItems = { items ->
                draftDetailedMenuItems = items
                // Persist to SharedPreferences
                val json = org.json.JSONArray().apply {
                    items.forEach { item ->
                        put(org.json.JSONObject().apply {
                            put("name", item.name)
                            put("foodType", item.foodType)
                            if (item.price != null) put("price", item.price) else put("price", org.json.JSONObject.NULL)
                        })
                    }
                }.toString()
                menuItemPrefs.edit().putString("draft_items", json).apply()
                showDetailedMenuScreen = false
            },
            onClearAll = {
                draftDetailedMenuItems = emptyList()
                menuItemPrefs.edit().remove("draft_items").apply()
                showDetailedMenuScreen = false
            },
            onNavigateBack = { showDetailedMenuScreen = false }
        )
        return
    }

    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear Entire Form?") },
            text = { Text("This will clear all fields — shop name, type, menu items, images, and all other details. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Clear all form fields
                        shopName = ""
                        shopType = ""
                        menuItems = listOf()
                        currentMenuItem = ""
                        ownerPhone = ""
                        ownerEmail = ""
                        cuisineType = ""
                        description = ""
                        averageCostForTwo = ""
                        priceRange = ""
                        fullAddress = ""
                        landmark = ""
                        selectedImages = listOf()
                        homeDelivery = false
                        takeaway = true
                        hasSeating = true
                        wifiAvailable = false
                        draftDetailedMenuItems = emptyList()
                        menuItemPrefs.edit().remove("draft_items").apply()
                        errorMessage = null
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Moving Cart Dialog
    if (showMovingCartDialog) {
        AlertDialog(
            onDismissRequest = { showMovingCartDialog = false },
            title = { Text(stringResource(R.string.is_moving_cart_title)) },
            text = { Text(stringResource(R.string.is_moving_cart_message)) },
            confirmButton = {
                TextButton(onClick = {
                    isMovingCart = true
                    showMovingCartDialog = false
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    isMovingCart = false
                    showMovingCartDialog = false
                }) { Text(stringResource(R.string.no)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Local Shop") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showClearAllDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear All")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = { saveShop() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Shop")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Images Section
            Text(
                text = "Photos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (selectedImages.isEmpty()) {
                OutlinedCard(
                    onClick = { showImageSourceSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add Photos (Camera or Gallery)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImages) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImages = selectedImages - uri },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha=0.6f), RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                    
                    if (selectedImages.size < 10) {
                        item {
                            OutlinedCard(
                                onClick = { showImageSourceSheet = true },
                                modifier = Modifier.size(100.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, "Add more")
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Basic Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Shop Name
            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Shop Type
            ExposedDropdownMenuBox(
                expanded = shopTypeExpanded,
                onExpandedChange = { shopTypeExpanded = !shopTypeExpanded }
            ) {
                OutlinedTextField(
                    value = shopType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Shop Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shopTypeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = shopTypeExpanded,
                    onDismissRequest = { shopTypeExpanded = false }
                ) {
                    shopTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                shopType = selectionOption
                                shopTypeExpanded = false
                            }
                        )
                    }
                }
            }

            // Menu Items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Menu Items *", style = MaterialTheme.typography.labelLarge)
                
                if (menuItems.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        menuItems.forEach { item ->
                            InputChip(
                                selected = true,
                                onClick = { menuItems = menuItems - item },
                                label = { Text(item) },
                                trailingIcon = { Icon(Icons.Default.Close, "Remove", Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = currentMenuItem,
                    onValueChange = { 
                        if (it.endsWith(",") || it.endsWith("\n")) {
                            val newItem = it.trim().dropLast(1)
                            if (newItem.isNotBlank() && !menuItems.contains(newItem)) {
                                menuItems = menuItems + newItem
                                currentMenuItem = ""
                            }
                        } else {
                            currentMenuItem = it 
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Add Top Dishes") },
                    placeholder = { Text("e.g. Samosa, Chai, Vada Pav") },
                    supportingText = { Text("Type and press comma or done to add") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (currentMenuItem.isNotBlank() && !menuItems.contains(currentMenuItem.trim())) {
                                menuItems = menuItems + currentMenuItem.trim()
                                currentMenuItem = ""
                            }
                        }
                    )
                )

                Text(
                    text = "Want to add detailed menu with pricing and types? Use the button below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (draftDetailedMenuItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${draftDetailedMenuItems.size} detailed item(s) added",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showDetailedMenuScreen = true }) {
                                Text("Edit")
                            }
                        }
                    }
                }
                
                OutlinedButton(
                    onClick = { showDetailedMenuScreen = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (draftDetailedMenuItems.isEmpty()) "Add Detailed Menu Items" else "Update Detailed Menu Items")
                }

                // Map Icon Picker
                val mapIconOptions = listOf(
                    "chai_stall" to "☕ Chai Stall",
                    "veg_cart"   to "🥗 Veg Cart",
                    "juice_cart" to "🍹 Juice Cart",
                    "snacks_cart" to "🍿 Snacks Cart",
                    "biryani_cart" to "🍛 Biryani Cart",
                    "fruit_cart" to "🍎 Fruit Cart",
                    "restaurant" to "🍽️ Restaurant",
                    "cafe"       to "☕ Cafe",
                    "bakery"     to "🥐 Bakery",
                    "default"    to "🛒 Other"
                )
                Text(
                    text = "Map Icon",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Choose how your shop appears on the map",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mapIconOptions.forEach { (slug, label) ->
                        FilterChip(
                            selected = selectedMapIcon == slug,
                            onClick = { selectedMapIcon = slug },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Expandable Additional Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOptionalDetails = !showOptionalDetails }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add More Details (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (showOptionalDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Details"
                )
            }

            AnimatedVisibility(visible = showOptionalDetails) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    // Contact
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ownerPhone,
                            onValueChange = { ownerPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }

                    // Address
                    OutlinedTextField(
                        value = fullAddress,
                        onValueChange = { fullAddress = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Cuisine & Cost
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cuisineType,
                            onValueChange = { cuisineType = it },
                            label = { Text("Cuisine Type") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = averageCostForTwo,
                            onValueChange = { averageCostForTwo = it.filter { char -> char.isDigit() } },
                            label = { Text("Cost for 2 (₹)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Toggles
                    Text("Facilities", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = takeaway, onCheckedChange = { takeaway = it })
                            Text("Takeaway")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = homeDelivery, onCheckedChange = { homeDelivery = it })
                            Text("Delivery")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasSeating, onCheckedChange = { hasSeating = it })
                            Text("Seating")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = wifiAvailable, onCheckedChange = { wifiAvailable = it })
                            Text("WiFi")
                        }
                    }


                }
            }
            
            // Padding for scroll and bottom button
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Image Selection Sheet
        if (showImageSourceSheet) {
            ModalBottomSheet(onDismissRequest = { showImageSourceSheet = false }) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text(
                        "Add Photos",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    ListItem(
                        headlineContent = { Text("Take Photo") },
                        leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                        modifier = Modifier.clickable {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                tempPhotoUri = createTempImageUri()
                                cameraLauncher.launch(tempPhotoUri!!)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Choose from Gallery") },
                        leadingContent = { Icon(Icons.Default.Image, null) },
                        modifier = Modifier.clickable {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }
}
