package com.example.bhandara.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.bhandara.R
import com.example.bhandara.data.models.api.ShopClaimRequest
import com.example.bhandara.data.models.api.ShopClaimResponse
import com.example.bhandara.data.repository.BackendRepository
import com.example.bhandara.ui.screens.addshop.Step2GoogleSignInStep
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

private const val MAX_CLAIM_PHOTOS = 3

/**
 * Lets the real owner of a platform-listed shop take it over.
 *
 * The owner must be standing at the shop and take 1-3 fresh camera photos of the shopfront with the
 * signboard visible. The backend checks their location and the photos (with AI) and either approves
 * the claim straight away, rejects it, or sends it to an admin for review.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimShopScreen(
    shopId: String,
    shopName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val repository = remember { BackendRepository() }
    val locationHelper = remember { LocationHelper(context) }
    val imageUploadHelper = remember { ImageUploadHelper(context) }

    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var ownerPhone by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<ShopClaimResponse?>(null) }

    // ── Google Sign-In (claims are tied to a real Google account, not an anonymous one) ──
    var needsSignIn by remember { mutableStateOf(auth.currentUser?.isAnonymous != false) }
    var signInLoading by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        scope.launch {
            try {
                val idToken = task.getResult(ApiException::class.java)?.idToken
                if (idToken == null) {
                    signInError = "Google Sign-In failed. Please try again."
                    signInLoading = false
                    return@launch
                }
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                try {
                    auth.currentUser?.linkWithCredential(credential)?.await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    auth.signInWithCredential(credential).await()
                }
                signInLoading = false
                signInError = null
                needsSignIn = false
            } catch (e: ApiException) {
                Log.e("ClaimShopScreen", "Google Sign-In failed", e)
                signInError = "Sign-in failed (code ${e.statusCode}). Please try again."
                signInLoading = false
            } catch (e: Exception) {
                Log.e("ClaimShopScreen", "Sign-in or linking error", e)
                signInError = "Sign-in error: ${e.message}"
                signInLoading = false
            }
        }
    }

    fun launchGoogleSignIn() {
        signInLoading = true
        signInError = null
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    // ── Camera (no gallery: proof photos must be taken at the shop, now) ──
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile("CLAIM_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", image)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            photos = photos + uri
        }
        pendingPhotoUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri()
            pendingPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is needed to photograph your shop", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitClaim() {
        scope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                progressMessage = "Getting your location..."
                val location = locationHelper.getCurrentLocation()
                if (location == null) {
                    errorMessage = "Could not get your location. Turn on GPS and try again."
                    return@launch
                }

                progressMessage = "Uploading photos..."
                val imageUrls = imageUploadHelper.uploadImages(photos)
                if (imageUrls.isEmpty()) {
                    errorMessage = "Could not upload your photos. Please try again."
                    return@launch
                }

                progressMessage = "Checking your claim..."
                val claimResult = repository.submitShopClaim(
                    ShopClaimRequest(
                        shopId = shopId.toLong(),
                        imageUrls = imageUrls,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        ownerPhone = ownerPhone.trim().ifBlank { null }
                    )
                )
                if (claimResult.claim != null) {
                    result = claimResult.claim
                } else {
                    errorMessage = claimResult.errorMessage
                }
            } catch (e: Exception) {
                Log.e("ClaimShopScreen", "Error submitting claim", e)
                errorMessage = "Something went wrong: ${e.message}"
            } finally {
                isSubmitting = false
                progressMessage = null
            }
        }
    }

    if (needsSignIn) {
        Step2GoogleSignInStep(
            isLoading = signInLoading,
            errorMessage = signInError,
            onGoogleSignInClick = { launchGoogleSignIn() },
            onNavigateBack = onBackClick
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claim this shop") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isSubmitting) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val claim = result
            if (claim != null) {
                ClaimResultCard(
                    claim = claim,
                    onDone = onBackClick,
                    onTryAgain = {
                        result = null
                        photos = emptyList()
                    }
                )
                return@Column
            }

            Text(
                text = shopName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Do you own this shop? Claim it to manage its details, menu and timings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("How it works", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("1. Stand at your shop.")
                    Text("2. Take 1–$MAX_CLAIM_PHOTOS photos of the shop front with the signboard clearly visible.")
                    Text("3. We check your location and photos. Most claims are approved in seconds; some are reviewed by our team.")
                }
            }

            Text("Shop photos", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Shop photo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { photos = photos - uri },
                            enabled = !isSubmitting,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                            )
                        }
                    }
                }
                if (photos.size < MAX_CLAIM_PHOTOS) {
                    item {
                        OutlinedCard(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            enabled = !isSubmitting,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Text("Take photo", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = ownerPhone,
                onValueChange = { ownerPhone = it },
                label = { Text("Your phone number (optional)") },
                supportingText = { Text("Shown on the shop page so customers can call you") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = { submitClaim() },
                enabled = photos.isNotEmpty() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(progressMessage ?: "Submitting...")
                } else {
                    Text("Submit claim")
                }
            }
        }
    }
}

@Composable
private fun ClaimResultCard(
    claim: ShopClaimResponse,
    onDone: () -> Unit,
    onTryAgain: () -> Unit
) {
    val (icon: ImageVector, title: String, container) = when (claim.status) {
        "APPROVED" -> Triple(Icons.Default.CheckCircle, "You now own this shop", MaterialTheme.colorScheme.primaryContainer)
        "REJECTED" -> Triple(Icons.Default.Cancel, "Claim not approved", MaterialTheme.colorScheme.errorContainer)
        else -> Triple(Icons.Default.HourglassTop, "Claim under review", MaterialTheme.colorScheme.tertiaryContainer)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            claim.decisionReason?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    if (claim.status == "REJECTED") {
        OutlinedButton(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Try again with new photos")
        }
    }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(if (claim.status == "APPROVED") "Go to my shop" else "Done")
    }
}
