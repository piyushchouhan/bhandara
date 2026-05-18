package com.example.bhandara.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bhandara.data.models.api.ReviewRequest
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.utils.ImageUploadHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun ReviewsSection(
    targetId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BackendRepository() }
    val currentUserUid = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var reviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<ReviewResponse?>(null) }
    val imageUploadHelper = remember { ImageUploadHelper(context) }

    fun loadReviews() {
        scope.launch {
            isLoading = true
            val allReviews = repository.getReviewsForShop(targetId)
            // Filter only active reviews
            reviews = allReviews.filter { it.isActive != false }
            isLoading = false
        }
    }

    LaunchedEffect(targetId) {
        loadReviews()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (currentUserUid != null) {
                Button(onClick = { showAddReviewDialog = true }) {
                    Text("Write a Review")
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (reviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "No reviews yet. Be the first to review!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            reviews.forEach { review ->
                ReviewItem(
                    review = review,
                    currentUserId = currentUserUid,
                    onEdit = {
                        editingReview = review
                        showAddReviewDialog = true
                    },
                    onDelete = {
                        scope.launch {
                            val success = repository.deleteReview(review.id)
                            if (success) loadReviews()
                        }
                    },
                    onReport = {
                        scope.launch {
                            val response = repository.reportReview(review.id)
                            if (response != null) {
                                loadReviews()
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddReviewDialog) {
        var rating by remember { mutableIntStateOf(editingReview?.rating ?: 5) }
        var comment by remember { mutableStateOf(editingReview?.comment ?: "") }
        var isSubmitting by remember { mutableStateOf(false) }
        var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
        var existingImages by remember { mutableStateOf(editingReview?.imageUrls ?: emptyList()) }
        var uploadProgress by remember { mutableStateOf(0) }
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
                if (existingImages.size + selectedImages.size < 5) {
                    selectedImages = selectedImages + tempPhotoUri!!
                }
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
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
        ) { uris ->
            // Max 5 images
            val totalImages = existingImages.size + selectedImages.size + uris.size
            if (totalImages <= 5) {
                selectedImages = selectedImages + uris
            }
            showImageSourceSheet = false
        }

        AlertDialog(
            onDismissRequest = { 
                if (!isSubmitting) {
                    showAddReviewDialog = false
                    editingReview = null
                }
            },
            title = { Text(if (editingReview != null) "Edit Review" else "Write a Review") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Star Rating Selector
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Rate $i stars",
                                tint = if (i <= rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { rating = i }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 1000) comment = it },
                        label = { Text("Comment (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                    
                    // Image Upload Section
                    Text("Images (optional, max 5)", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(existingImages) { url ->
                            Box {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { existingImages = existingImages - url },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(2.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        items(selectedImages) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedImages = selectedImages - uri },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp).padding(2.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (existingImages.size + selectedImages.size < 5) {
                            item {
                                OutlinedButton(
                                    onClick = { showImageSourceSheet = true },
                                    modifier = Modifier.size(64.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add image")
                                }
                            }
                        }
                    }
                    if (uploadProgress > 0) {
                        Text("Uploading... $uploadProgress%", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            // Upload new images
                            val newImageUrls = if (selectedImages.isNotEmpty()) {
                                imageUploadHelper.uploadImages(selectedImages) { progress ->
                                    uploadProgress = progress
                                }
                            } else {
                                emptyList()
                            }
                            
                            val allImageUrls = existingImages + newImageUrls
                            
                            val request = ReviewRequest(
                                shopId = targetId,
                                rating = rating,
                                comment = comment.takeIf { it.isNotBlank() },
                                imageUrls = allImageUrls.takeIf { it.isNotEmpty() }
                            )
                            
                            val response = if (editingReview != null) {
                                repository.updateReview(editingReview!!.id, request)
                            } else {
                                repository.createReview(request)
                            }
                            
                            if (response != null) {
                                showAddReviewDialog = false
                                editingReview = null
                                loadReviews()
                            }
                            isSubmitting = false
                            uploadProgress = 0
                        }
                    },
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Submit")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddReviewDialog = false
                        editingReview = null
                    },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel")
                }
            }
        )

        // Image Selection Sheet
        if (showImageSourceSheet) {
            @OptIn(ExperimentalMaterial3Api::class)
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

@Composable
fun ReviewItem(
    review: ReviewResponse,
    currentUserId: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ReviewActionsPrefs", android.content.Context.MODE_PRIVATE) }
    var hasReported by remember { mutableStateOf(prefs.getBoolean("reported_review_${review.id}", false)) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Date
                    val displayDate = try {
                        val parsed = LocalDateTime.parse(review.createdAt ?: "")
                        parsed.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    } catch (e: Exception) {
                        review.createdAt?.take(10) ?: ""
                    }
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Edit and Delete buttons if owner, Report if not owner
                    if (currentUserId != null) {
                        if (review.reviewerUid == currentUserId) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(24.dp).padding(start = 8.dp)) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Review",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp).padding(start = 8.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Review",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickable(enabled = !hasReported) {
                                        onReport()
                                        hasReported = true
                                        prefs.edit().putBoolean("reported_review_${review.id}", true).apply()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    if (hasReported) Icons.Default.Flag else Icons.Outlined.Flag,
                                    contentDescription = if (hasReported) "Reported" else "Report Review",
                                    modifier = Modifier.size(16.dp),
                                    tint = if (hasReported) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (hasReported) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reported",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!review.comment.isNullOrBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Display Images if available
            if (!review.imageUrls.isNullOrEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(review.imageUrls.size) { index ->
                        AsyncImage(
                            model = review.imageUrls[index],
                            contentDescription = "Review Image",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedImageIndex = index },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    if (selectedImageIndex != null && !review.imageUrls.isNullOrEmpty()) {
        FullScreenImageCarousel(
            imageUrls = review.imageUrls,
            initialIndex = selectedImageIndex!!,
            onDismissRequest = { selectedImageIndex = null }
        )
    }
}
