package com.example.bhandara.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.utils.ImageUploadHelper
import com.example.bhandara.utils.LocationHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBhandaraScreen(
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
    
    // Form state
    var organizerName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    // Menu items as a list of chips
    var menuItems by remember { mutableStateOf(listOf<String>()) }
    var currentMenuItem by remember { mutableStateOf("") }
    
    var foodType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var feastDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var address by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var estimatedCapacity by remember { mutableStateOf("") }
    
    // UI state
    var isLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    
    // Camera-related
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Helper to create a temp file for camera image
    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedImages = selectedImages + tempPhotoUri!!
        }
        showImageSourceSheet = false
    }
    
    // Camera permission launcher
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
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        selectedImages = selectedImages + uris
        showImageSourceSheet = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Bhandara") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Fixed bottom bar with submit button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            // Validation
                            val finalMenuItems = if (currentMenuItem.isNotBlank()) {
                                menuItems + currentMenuItem.trim()
                            } else {
                                menuItems
                            }

                            if (finalMenuItems.isEmpty()) {
                                errorMessage = "Please enter menu items"
                                return@launch
                            }
                            if (feastDate == null) {
                                errorMessage = "Please select a date"
                                return@launch
                            }
                            if (startTime == null) {
                                errorMessage = "Please select start time"
                                return@launch
                            }
                            if (endTime == null) {
                                errorMessage = "Please select end time"
                                return@launch
                            }
                            
                            isLoading = true
                            errorMessage = null
                            
                            try {
                                // Hardcoded placeholder
                                val imageUrls = listOf("https://placehold.co/600x400/orange/white?text=Bhandara+Feast")
                                
                                // Get current location
                                val location = locationHelper.getCurrentLocation()
                                if (location == null) {
                                    errorMessage = "Could not get your location. Please enable GPS."
                                    isLoading = false
                                    return@launch
                                }
                                
                                // Get Firebase UID
                                val firebaseUid = userRepository.getCurrentUserId()
                                if (firebaseUid == null) {
                                    errorMessage = "User not authenticated"
                                    isLoading = false
                                    return@launch
                                }
                                
                                // Create feast request
                                val request = FeastRequest(
                                    firebaseUid = firebaseUid,
                                    organizerName = organizerName.ifBlank { null },
                                    contactPhone = contactPhone.ifBlank { null },
                                    menuItems = finalMenuItems,
                                    foodType = foodType.ifBlank { null },
                                    description = description.ifBlank { null },
                                    imageUrls = imageUrls,
                                    feastDate = feastDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    startTime = startTime!!.format(DateTimeFormatter.ISO_LOCAL_TIME),
                                    endTime = endTime!!.format(DateTimeFormatter.ISO_LOCAL_TIME),
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    address = address.ifBlank { null },
                                    landmark = landmark.ifBlank { null },
                                    estimatedCapacity = estimatedCapacity.toIntOrNull()
                                )
                                
                                // Submit to backend
                                val response = backendRepository.createFeast(request)
                                
                                if (response != null) {
                                    // Success - navigate back
                                    onNavigateBack()
                                } else {
                                    errorMessage = "Failed to submit bhandara. Please try again."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error: ${e.message}"
                            } finally {
                                isLoading = false
                                uploadProgress = 0
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Bhandara")
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
            // Images Section (Required)
            Text(
                text = "Photos *",
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
            
            // Menu Items (Required)
            // Menu Items (Required)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Menu Items *",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                @OptIn(ExperimentalLayoutApi::class)
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
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
                    label = { Text("Add Item") },
                    placeholder = { Text("Type and press comma or Done") },
                    supportingText = { Text("Separate items with commas") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (currentMenuItem.isNotBlank()) {
                                if (!menuItems.contains(currentMenuItem.trim())) {
                                    menuItems = menuItems + currentMenuItem.trim()
                                }
                                currentMenuItem = ""
                            }
                        }
                    )
                )
            }
            
            // Food Type
            OutlinedTextField(
                value = foodType,
                onValueChange = { foodType = it },
                label = { Text("Food Type") },
                placeholder = { Text("Vegetarian, Vegan, etc.") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 1000) description = it },
                label = { Text("Description") },
                placeholder = { Text("Tell us about this bhandara...") },
                supportingText = { Text("${description.length}/1000") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Divider()
            
            // Date & Time Section
            Text(
                text = "When",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Feast Date (Required)
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Date *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            feastDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "Select date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Icon(Icons.Default.CalendarToday, null)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Start Time (Required)
                OutlinedCard(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Start Time *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                startTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Select",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(20.dp))
                    }
                }
                
                // End Time (Required)
                OutlinedCard(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "End Time *",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                endTime?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Select",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            Divider()
            
            // Contact Info
            Text(
                text = "Contact Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = organizerName,
                onValueChange = { organizerName = it },
                label = { Text("Organizer Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )
            
            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Contact Phone") },
                placeholder = { Text("+919876543210") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )
            
            Divider()
            
            // Location
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { if (it.length <= 500) address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                leadingIcon = { Icon(Icons.Default.LocationOn, null) }
            )
            
            OutlinedTextField(
                value = landmark,
                onValueChange = { if (it.length <= 255) landmark = it },
                label = { Text("Landmark") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, null) }
            )
            
            Divider()
            
            // Capacity
            OutlinedTextField(
                value = estimatedCapacity,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) estimatedCapacity = it },
                label = { Text("Estimated Capacity") },
                placeholder = { Text("Number of people") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.People, null) }
            )
            
            // Error message
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Upload progress
            if (isLoading && uploadProgress > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Uploading images... $uploadProgress%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = feastDate?.toEpochDay()?.times(86400000L) ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        feastDate = LocalDate.ofEpochDay(millis / 86400000L)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
    
    // Start Time Picker
    if (showStartTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = startTime?.hour ?: LocalTime.now().hour,
            initialMinute = startTime?.minute ?: LocalTime.now().minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timeState.hour, timeState.minute)
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }

    // End Time Picker
    if (showEndTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = endTime?.hour ?: ((startTime?.hour ?: LocalTime.now().hour) + 1) % 24,
            initialMinute = endTime?.minute ?: startTime?.minute ?: LocalTime.now().minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timeState.hour, timeState.minute)
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }

    // Image Source Selection Sheet
    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Add Photos",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                    modifier = Modifier.clickable {
                        // Check camera permission
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
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }
}
