package com.example.bhandara.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.bhandara.data.models.api.LocalShopRequest
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.ui.screens.addshop.Step1ShopTypeStep
import com.example.bhandara.ui.screens.addshop.Step2BasicDetailsStep
import com.example.bhandara.ui.screens.addshop.Step2GoogleSignInStep
import com.example.bhandara.ui.screens.addshop.Step3DetailedMenuStep
import com.example.bhandara.ui.screens.addshop.Step4OptionalDetailsStep
import com.example.bhandara.utils.ImageUploadHelper
import com.example.bhandara.utils.LocationHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocalShopScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    // Repositories
    val backendRepository = remember { BackendRepository() }
    val userRepository = remember { UserRepository() }
    val locationHelper = remember { LocationHelper(context) }
    val imageUploadHelper = remember { ImageUploadHelper(context) }

    // Step navigation
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 4

    // ── Google Sign-In interstitial ─────────────────────────────────────────
    // Shown as a full-screen overlay between Step 1 → Step 2 when user is anonymous
    var showGoogleSignIn by remember { mutableStateOf(false) }
    var signInLoading by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        scope.launch {
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken == null) {
                    signInError = "Google Sign-In failed. Please try again."
                    signInLoading = false
                    return@launch
                }

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val currentUser = auth.currentUser

                try {
                    // Try to link anonymous account with Google credential
                    currentUser?.linkWithCredential(credential)?.await()
                    Log.d("AddLocalShopScreen", "Linked anonymous account with Google.")
                } catch (e: FirebaseAuthUserCollisionException) {
                    // Google account already exists — sign into the existing account
                    Log.d("AddLocalShopScreen", "Collision: signing into existing Google account.")
                    auth.signInWithCredential(credential).await()
                }

                // Sign-in successful — dismiss overlay and advance to Step 2
                signInLoading = false
                signInError = null
                showGoogleSignIn = false
                currentStep = 2

            } catch (e: ApiException) {
                Log.e("AddLocalShopScreen", "Google Sign-In failed", e)
                signInError = "Sign-in failed (code ${e.statusCode}). Please try again."
                signInLoading = false
            } catch (e: Exception) {
                Log.e("AddLocalShopScreen", "Sign-in or linking error", e)
                signInError = "Sign-in error: ${e.message}"
                signInLoading = false
            }
        }
    }

    fun launchGoogleSignIn() {
        signInLoading = true
        signInError = null
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.bhandara.R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    // Step 1 - Shop Type
    var isMovingCart by remember { mutableStateOf<Boolean?>(null) }

    // Step 2 - Basic Details
    var shopName by remember { mutableStateOf("") }
    var shopType by remember { mutableStateOf("") }
    var menuItems by remember { mutableStateOf(listOf<String>()) }
    var currentMenuItem by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Step 3 - Detailed Menu
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

    // Step 4 - Optional Details
    var ownerPhone by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var cuisineType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var averageCostForTwo by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var homeDelivery by remember { mutableStateOf(false) }
    var takeaway by remember { mutableStateOf(true) }
    var hasSeating by remember { mutableStateOf(true) }
    var wifiAvailable by remember { mutableStateOf(false) }

    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Camera/Image logic (must be at top-level composable, not inside AnimatedContent)
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

    // Clear All logic
    var showClearAllDialog by remember { mutableStateOf(false) }

    val clearAll: () -> Unit = {
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
        isMovingCart = null
        draftDetailedMenuItems = emptyList()
        menuItemPrefs.edit().remove("draft_items").apply()
        errorMessage = null
        currentStep = 1
    }

    // Save shop logic
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
                currentStep = 2
                return@launch
            }
            if (shopType.isBlank()) {
                errorMessage = "Shop type is required"
                currentStep = 2
                return@launch
            }
            val allMenuItems = (finalMenuItems + draftDetailedMenuItems.map { it.name }).distinct()
            if (allMenuItems.isEmpty()) {
                errorMessage = "At least one menu item is required"
                currentStep = 2
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
                    isMovingCart = isMovingCart ?: false,
                    mapIcon = "default"
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
                    if (isMovingCart == true) {
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

    // Show Google Sign-In interstitial (full-screen overlay, same pattern as detailed menu)
    if (showGoogleSignIn) {
        Step2GoogleSignInStep(
            isLoading = signInLoading,
            errorMessage = signInError,
            onGoogleSignInClick = { launchGoogleSignIn() },
            onNavigateBack = {
                showGoogleSignIn = false
                signInError = null
                signInLoading = false
            }
        )
        return
    }

    // Show the detailed menu local form (full-screen overlay)
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

    // Clear All dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear Entire Form?") },
            text = { Text("This will clear all fields — shop name, type, menu items, images, and all other details. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearAll()
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

    // Step validation
    val canProceed = when (currentStep) {
            1 -> isMovingCart != null
            2 -> shopName.isNotBlank() && shopType.isNotBlank()
        3 -> true // optional step
        4 -> true // optional step
            else -> false
        }

    // Step labels for progress
    val stepLabels = listOf("Type", "Details", "Menu", "More")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Local Shop") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            currentStep--
                        } else {
                            onNavigateBack()
                        }
                    }) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back button (steps 2-4)
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("Back")
                        }
                    }

                        if (currentStep < totalSteps) {
                        // Next button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    // Intercept Step 1 → Step 2: if anonymous, show Google Sign-In
                                    if (currentStep == 1 && (auth.currentUser?.isAnonymous != false)) {
                                        showGoogleSignIn = true
                                    } else {
                                        currentStep++
                                    }
                                },
                                modifier = Modifier.weight(if (currentStep > 1) 2f else 1f).height(48.dp),
                                enabled = canProceed
                            ) {
                            Text(if (currentStep == 3) "Skip / Next" else "Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                            }
                        } else {
                        // Final step - Add Shop button
                            Button(
                                onClick = { saveShop() },
                                modifier = Modifier.weight(2f).height(48.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Default.Storefront, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Shop")
                                }
                            }
                        }
                    }
                }
            }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Progress Indicator ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Step labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    stepLabels.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (index + 1 == currentStep) FontWeight.Bold else FontWeight.Normal,
                            color = if (index + 1 <= currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Linear progress bar
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {}
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Error Message ---
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // --- Step Content ---
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "stepTransition"
            ) { step ->
                    when (step) {
                        1 -> Step1ShopTypeStep(
                            isMovingCart = isMovingCart,
                            onMovingCartSelected = { isMovingCart = it }
                        )

                    2 -> Step2BasicDetailsStep(
                            selectedImages = selectedImages,
                            onAddPhotosClick = { showImageSourceSheet = true },
                            onRemoveImage = { uri -> selectedImages = selectedImages - uri },
                            shopName = shopName,
                            onShopNameChange = { shopName = it },
                            shopType = shopType,
                            onShopTypeChange = { shopType = it },
                            menuItems = menuItems,
                            onMenuItemsChange = { menuItems = it },
                            currentMenuItem = currentMenuItem,
                            onCurrentMenuItemChange = { currentMenuItem = it }
                        )

                    3 -> Step3DetailedMenuStep(
                            menuItems = menuItems,
                            draftDetailedMenuItems = draftDetailedMenuItems,
                            onOpenDetailedMenu = { showDetailedMenuScreen = true }
                        )

                    4 -> Step4OptionalDetailsStep(
                            description = description,
                            onDescriptionChange = { description = it },
                            ownerPhone = ownerPhone,
                            onOwnerPhoneChange = { ownerPhone = it },
                            fullAddress = fullAddress,
                            onFullAddressChange = { fullAddress = it },
                            landmark = landmark,
                            onLandmarkChange = { landmark = it },
                            cuisineType = cuisineType,
                            onCuisineTypeChange = { cuisineType = it },
                            averageCostForTwo = averageCostForTwo,
                            onAverageCostForTwoChange = { averageCostForTwo = it },
                            takeaway = takeaway,
                            onTakeawayChange = { takeaway = it },
                            homeDelivery = homeDelivery,
                            onHomeDeliveryChange = { homeDelivery = it },
                            hasSeating = hasSeating,
                            onHasSeatingChange = { hasSeating = it },
                            wifiAvailable = wifiAvailable,
                            onWifiAvailableChange = { wifiAvailable = it }
                        )
                    }
                }
            }

        // Image Selection Bottom Sheet
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
