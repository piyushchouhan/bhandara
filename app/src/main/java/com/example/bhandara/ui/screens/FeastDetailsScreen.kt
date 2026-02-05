package com.example.bhandara.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.repository.BackendRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeastDetailsScreen(
    feastId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { BackendRepository() }
    
    var feast by remember { mutableStateOf<FeastResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isReporting by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    // Fetch feast details - TODO: Add getFeastById API endpoint
    // For now, we create a mock feast based on the ID
    LaunchedEffect(feastId) {
        // Mock data for testing - replace with actual API call
        feast = FeastResponse(
            id = feastId,
            firebaseUid = null,
            organizerName = "Sample Organizer",
            contactPhone = "+91 98765 43210",
            menuItems = listOf("Rajma Chawal", "Roti", "Sabzi", "Gulab Jamun"),
            foodType = "Vegetarian",
            description = "Join us for a community feast serving traditional home-style food",
            imageUrls = emptyList(),
            feastDate = "2026-02-06",
            startTime = "12:00",
            endTime = "15:00",
            latitude = 28.6139,
            longitude = 77.2090,
            address = "Connaught Place, New Delhi",
            landmark = "Near Central Park",
            distance = 500.0,
            estimatedCapacity = 100,
            isActive = true,
            isVerified = true
        )
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bhandara Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReportDialog = true },
                        enabled = feast?.isActive == true && !isReporting
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = "Report",
                            tint = MaterialTheme.colorScheme.error
                        )
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
        } else if (feast != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Carousel
                if (feast!!.imageUrls.isNotEmpty()) {
                    val carouselState = rememberCarouselState(
                        initialItem = 0,
                        itemCount = { feast!!.imageUrls.size }
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
                        ) {
                            AsyncImage(
                                model = feast!!.imageUrls[index],
                                contentDescription = "Feast image ${index + 1}",
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
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main content
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Organizer info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = feast!!.organizerName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (feast!!.isVerified) {
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
                                        text = "Verified Organizer",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        if (!feast!!.isActive) {
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
                        }
                    }
                    
                    // Date, Time, Location
                    InfoCard(feast!!)
                    
                    // Menu Items
                    MenuItemsSection(feast!!.menuItems)
                    
                    // Description
                    if (feast!!.description.isNotBlank()) {
                        DescriptionSection(feast!!.description)
                    }
                    
                    // Additional Info
                    AdditionalInfoSection(feast!!)
                    
                    // Action Buttons
                    ActionButtons(
                        feast = feast!!,
                        onGetDirections = {
                            val uri = Uri.parse("geo:${feast!!.latitude},${feast!!.longitude}?q=${feast!!.latitude},${feast!!.longitude}(${feast!!.organizerName})")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        onCall = {
                            if (feast!!.contactPhone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${feast!!.contactPhone}"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
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
                Text("Failed to load feast details")
            }
        }
    }
    
    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null)
            },
            title = {
                Text("Report this Bhandara?")
            },
            text = {
                Text("Are you sure you want to report this bhandara as fake or inappropriate? This helps keep the community safe.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isReporting = true
                            val result = repository.reportFeast(feastId)
                            if (result != null) {
                                // Update local feast state with the result
                                feast = result
                                
                                if (!result.isActive) {
                                    Toast.makeText(
                                        context,
                                        "Thank you! This bhandara has been deactivated due to multiple reports.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onBackClick()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Thank you for reporting. We'll review this.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to report. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            isReporting = false
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isReporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Report")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoCard(feast: FeastResponse) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    
    val date = try {
        LocalDate.parse(feast.feastDate).format(dateFormatter)
    } catch (e: Exception) {
        feast.feastDate
    }
    
    val startTime = try {
        LocalTime.parse(feast.startTime).format(timeFormatter)
    } catch (e: Exception) {
        feast.startTime
    }
    
    val endTime = try {
        LocalTime.parse(feast.endTime).format(timeFormatter)
    } catch (e: Exception) {
        feast.endTime
    }
    
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
            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Time
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
                        text = feast.address,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (feast.landmark != null && feast.landmark.isNotBlank()) {
                        Text(
                            text = "Near: ${feast.landmark}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    if (feast.distance != null) {
                        val distanceKm = (feast.distance / 1000.0).roundToInt()
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

@Composable
private fun AdditionalInfoSection(feast: FeastResponse) {
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
            
            if (feast.foodType.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocalDining,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Food Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = feast.foodType,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            if (feast.estimatedCapacity > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Estimated Capacity",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${feast.estimatedCapacity} people",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    feast: FeastResponse,
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
            enabled = feast.isActive
        ) {
            Icon(
                Icons.Default.Directions,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Directions")
        }
        
        if (feast.contactPhone.isNotBlank()) {
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.weight(1f),
                enabled = feast.isActive
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
