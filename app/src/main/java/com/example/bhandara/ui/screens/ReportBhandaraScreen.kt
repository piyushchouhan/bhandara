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
import com.example.bhandara.data.models.api.FeastRequest
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.data.repository.UserRepository
import com.example.bhandara.ui.screens.reportbhandara.Step1FoodDetailsStep
import com.example.bhandara.ui.screens.reportbhandara.Step2WhenContactStep
import com.example.bhandara.ui.screens.reportbhandara.Step3LocationCapacityStep
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
    
    // Repositories
    val backendRepository = remember { BackendRepository() }
    val userRepository = remember { UserRepository() }
    val locationHelper = remember { LocationHelper(context) }
    val imageUploadHelper = remember { ImageUploadHelper(context) }
    
    // Step navigation
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 3
    
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
    
    // Clear all confirmation dialog
    var showClearAllDialog by remember { mutableStateOf(false) }
    
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

    val clearAll: () -> Unit = {
        organizerName = ""
        contactPhone = ""
        menuItems = listOf()
        currentMenuItem = ""
        foodType = ""
        description = ""
        selectedImages = listOf()
        feastDate = null
        startTime = null
        endTime = null
        address = ""
        landmark = ""
        estimatedCapacity = ""
        errorMessage = null
        currentStep = 1
    }

    // Step validation
    val finalMenuItems = if (currentMenuItem.isNotBlank()) {
        menuItems + currentMenuItem.trim()
    } else {
        menuItems
    }

    val canProceed = when (currentStep) {
        1 -> selectedImages.isNotEmpty() && finalMenuItems.isNotEmpty()
        2 -> feastDate != null && startTime != null && endTime != null
        3 -> true
        else -> false
    }

    // Step labels for progress
    val stepLabels = listOf("Food & Photos", "When & Contact", "Location")

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear Entire Form?") },
            text = { Text("This will clear all fields — photos, menu items, date, times, organizer and location. This cannot be undone.") },
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Bhandara") },
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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Upload progress indicator
                    if (isLoading && uploadProgress > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "Uploading images... $uploadProgress%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Back button (steps 2-3)
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).height(48.dp),
                                enabled = !isLoading
                            ) {
                                Text("Back")
                            }
                        }

                        if (currentStep < totalSteps) {
                            // Next button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    currentStep++
                                },
                                modifier = Modifier.weight(if (currentStep > 1) 2f else 1f).height(48.dp),
                                enabled = canProceed
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            // Final step - Submit button
                            Button(
                                onClick = {
                                    scope.launch {
                                        // Final validation
                                        val actualMenuItems = if (currentMenuItem.isNotBlank()) {
                                            menuItems + currentMenuItem.trim()
                                        } else {
                                            menuItems
                                        }

                                        if (actualMenuItems.isEmpty()) {
                                            errorMessage = "Please enter menu items"
                                            currentStep = 1
                                            return@launch
                                        }
                                        if (feastDate == null) {
                                            errorMessage = "Please select a date"
                                            currentStep = 2
                                            return@launch
                                        }
                                        if (startTime == null) {
                                            errorMessage = "Please select start time"
                                            currentStep = 2
                                            return@launch
                                        }
                                        if (endTime == null) {
                                            errorMessage = "Please select end time"
                                            currentStep = 2
                                            return@launch
                                        }
                                        if (selectedImages.isEmpty()) {
                                            errorMessage = "Please add at least one photo"
                                            currentStep = 1
                                            return@launch
                                        }
                                        
                                        isLoading = true
                                        errorMessage = null
                                        
                                        try {
                                            // Upload images to Firebase Storage
                                            val imageUrls = imageUploadHelper.uploadImages(selectedImages) { progress ->
                                                uploadProgress = progress
                                            }
                                            
                                            if (imageUrls.isEmpty()) {
                                                errorMessage = "Failed to upload images. Please check your internet connection."
                                                isLoading = false
                                                return@launch
                                            }
                                            
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
                                                menuItems = actualMenuItems,
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
                                modifier = Modifier.weight(if (currentStep > 1) 2f else 1f).height(48.dp),
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
                label = "stepTransition",
                modifier = Modifier.weight(1f)
            ) { step ->
                when (step) {
                    1 -> Step1FoodDetailsStep(
                        selectedImages = selectedImages,
                        onAddPhotosClick = { showImageSourceSheet = true },
                        onRemoveImage = { uri -> selectedImages = selectedImages - uri },
                        menuItems = menuItems,
                        onMenuItemsChange = { menuItems = it },
                        currentMenuItem = currentMenuItem,
                        onCurrentMenuItemChange = { currentMenuItem = it },
                        foodType = foodType,
                        onFoodTypeChange = { foodType = it },
                        description = description,
                        onDescriptionChange = { description = it }
                    )

                    2 -> Step2WhenContactStep(
                        feastDate = feastDate,
                        onFeastDateClick = { showDatePicker = true },
                        startTime = startTime,
                        onStartTimeClick = { showStartTimePicker = true },
                        endTime = endTime,
                        onEndTimeClick = { showEndTimePicker = true },
                        organizerName = organizerName,
                        onOrganizerNameChange = { organizerName = it },
                        contactPhone = contactPhone,
                        onContactPhoneChange = { contactPhone = it }
                    )

                    3 -> Step3LocationCapacityStep(
                        address = address,
                        onAddressChange = { address = it },
                        landmark = landmark,
                        onLandmarkChange = { landmark = it },
                        estimatedCapacity = estimatedCapacity,
                        onEstimatedCapacityChange = { estimatedCapacity = it }
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
