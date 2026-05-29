package com.example.bhandara.ui.screens.reportbhandara

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Step1FoodDetailsStep(
    // Photos
    selectedImages: List<Uri>,
    onAddPhotosClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    // Menu items
    menuItems: List<String>,
    onMenuItemsChange: (List<String>) -> Unit,
    currentMenuItem: String,
    onCurrentMenuItemChange: (String) -> Unit,
    // Food type & Description
    foodType: String,
    onFoodTypeChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photos Section (Required)
        Text(
            text = "Photos *",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (selectedImages.isEmpty()) {
            OutlinedCard(
                onClick = onAddPhotosClick,
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
                            onClick = { onRemoveImage(uri) },
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
                
                if (selectedImages.size < 10) {
                    item {
                        OutlinedCard(
                            onClick = onAddPhotosClick,
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
        
        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items (Required)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Menu Items *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (menuItems.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuItems.forEach { item ->
                        InputChip(
                            selected = true,
                            onClick = { onMenuItemsChange(menuItems - item) },
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
                onValueChange = { input ->
                    if (input.endsWith(",") || input.endsWith("\n")) {
                        val newItem = input.trim().dropLast(1)
                        if (newItem.isNotBlank() && !menuItems.contains(newItem)) {
                            onMenuItemsChange(menuItems + newItem)
                            onCurrentMenuItemChange("")
                        }
                    } else {
                        onCurrentMenuItemChange(input)
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
                            val newItem = currentMenuItem.trim()
                            if (!menuItems.contains(newItem)) {
                                onMenuItemsChange(menuItems + newItem)
                            }
                            onCurrentMenuItemChange("")
                        }
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Food Type
        Text(
            text = "Food Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = foodType,
            onValueChange = onFoodTypeChange,
            label = { Text("Food Type") },
            placeholder = { Text("Vegetarian, Vegan, etc.") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 1000) onDescriptionChange(it) },
            label = { Text("Description") },
            placeholder = { Text("Tell us about this bhandara...") },
            supportingText = { Text("${description.length}/1000") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
