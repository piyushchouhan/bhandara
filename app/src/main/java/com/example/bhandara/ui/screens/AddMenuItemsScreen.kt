package com.example.bhandara.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.repository.BackendRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A local-first menu item form.
 * When [shopId] is null: works as a local draft (no API calls). Items are returned via [onSaveItems].
 * When [shopId] is provided: posts items directly to API (used from LocalShopDetailsScreen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemsScreen(
    shopId: String? = null,
    initialItems: List<MenuItemRequest> = emptyList(),
    onSaveItems: ((List<MenuItemRequest>) -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BackendRepository() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Manual Entry") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Image Scan") }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (selectedTab == 0) {
                    ManualEntrySection(
                        shopId = shopId,
                        repository = repository,
                        initialItems = initialItems,
                        isLoading = isLoading,
                        onLoadingChange = { isLoading = it },
                        onError = { errorMessage = it },
                        onSaveItems = onSaveItems,
                        onSuccess = onNavigateBack
                    )
                } else {
                    AiImageScanSection(
                        shopId = shopId,
                        repository = repository,
                        isLoading = isLoading,
                        onLoadingChange = { isLoading = it },
                        onError = { errorMessage = it },
                        onSaveItems = onSaveItems,
                        onSuccess = onNavigateBack
                    )
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManualEntrySection(
    shopId: String?,
    repository: BackendRepository,
    initialItems: List<MenuItemRequest>,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSaveItems: ((List<MenuItemRequest>) -> Unit)?,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Pre-populate with existing draft items, or start with one blank item
    var items by remember {
        mutableStateOf(
            if (initialItems.isNotEmpty()) initialItems
            else listOf(MenuItemRequest("", "VEG"))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEachIndexed { index, item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Item ${index + 1}", fontWeight = FontWeight.Bold)
                        if (items.size > 1) {
                            IconButton(onClick = {
                                items = items.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    "Remove item",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { newName ->
                                items = items.toMutableList()
                                    .apply { this[index] = item.copy(name = newName) }
                            },
                            label = { Text("Dish Name") },
                            modifier = Modifier.weight(2f)
                        )
                        OutlinedTextField(
                            value = item.price?.let {
                                if (it % 1 == 0.0) it.toInt().toString() else it.toString()
                            } ?: "",
                            onValueChange = { newPrice ->
                                val priceValue = newPrice.toDoubleOrNull()
                                if (newPrice.isEmpty() || priceValue != null) {
                                    items = items.toMutableList()
                                        .apply { this[index] = item.copy(price = priceValue) }
                                }
                            },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = item.foodType == "VEG",
                                onClick = {
                                    items = items.toMutableList()
                                        .apply { this[index] = item.copy(foodType = "VEG") }
                                }
                            )
                            Text("Veg")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = item.foodType == "NON_VEG",
                                onClick = {
                                    items = items.toMutableList()
                                        .apply { this[index] = item.copy(foodType = "NON_VEG") }
                                }
                            )
                            Text("Non-Veg")
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { items = items + MenuItemRequest("", "VEG") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add Another Item")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val validItems = items.filter { it.name.isNotBlank() }
                if (validItems.isEmpty()) {
                    onError("Please add at least one item name.")
                    return@Button
                }

                if (onSaveItems != null) {
                    // Local draft mode — just return items to parent, no API call
                    onSaveItems(validItems)
                } else if (shopId != null) {
                    // Direct API mode (used from LocalShopDetailsScreen)
                    scope.launch {
                        onLoadingChange(true)
                        onError(null)
                        val response = repository.addMenuItemsManual(
                            shopId,
                            com.example.bhandara.data.models.api.ManualMenuItemsRequest(validItems)
                        )
                        if (response != null) {
                            onSuccess()
                        } else {
                            onError("Failed to add menu items.")
                        }
                        onLoadingChange(false)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(if (onSaveItems != null) "Save & Go Back" else "Save Menu Items")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiImageScanSection(
    shopId: String?,
    repository: BackendRepository,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    onSaveItems: ((List<MenuItemRequest>) -> Unit)?,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) selectedImages = selectedImages + tempPhotoUri!!
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

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (shopId == null)
                "Upload photos of your menu. After scanning, items will be saved as a draft and submitted when you create the shop."
            else
                "Upload photos of the menu. Our AI will automatically extract the dish names and types!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedImages.isEmpty()) {
            OutlinedCard(
                onClick = { showImageSourceSheet = true },
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera, null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Menu Photos")
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedImages) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
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
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                            )
                        }
                    }
                }
                item {
                    OutlinedCard(
                        onClick = { showImageSourceSheet = true },
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, "Add more")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (selectedImages.isEmpty()) {
                    onError("Please add at least one photo of the menu.")
                    return@Button
                }

                scope.launch {
                    onLoadingChange(true)
                    onError(null)

                    val parts = withContext(Dispatchers.IO) {
                        selectedImages.mapNotNull { uri ->
                            val inputStream =
                                context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
                            val bytes = inputStream.readBytes()
                            val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData(
                                "images",
                                "menu_${System.currentTimeMillis()}.jpg",
                                requestBody
                            )
                        }
                    }

                    if (parts.isEmpty()) {
                        onError("Failed to read selected images.")
                        onLoadingChange(false)
                        return@launch
                    }

                    if (shopId != null) {
                        // Direct API mode
                        val response = repository.addMenuItemsFromImage(shopId, parts)
                        if (response != null) {
                            onSuccess()
                        } else {
                            onError("Failed to analyze menu images.")
                        }
                    } else if (onSaveItems != null) {
                        // Draft mode: scan images but save result as local draft
                        // We still need a real shopId to call the API, so inform the user
                        onError("AI scan requires the shop to be created first. Use Manual Entry to add items now, or create the shop and then use AI scan from the shop details page.")
                    }
                    onLoadingChange(false)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing with AI...")
            } else {
                Text("Scan and Add Items")
            }
        }
    }

    if (showImageSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showImageSourceSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Add Menu Photos",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                    modifier = Modifier.clickable {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
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
