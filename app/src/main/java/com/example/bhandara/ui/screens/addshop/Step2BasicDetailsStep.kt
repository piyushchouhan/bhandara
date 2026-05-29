package com.example.bhandara.ui.screens.addshop

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
import androidx.compose.material.icons.filled.*
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
fun Step2BasicDetailsStep(
    // Photos
    selectedImages: List<Uri>,
    onAddPhotosClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    // Shop Name
    shopName: String,
    onShopNameChange: (String) -> Unit,
    // Shop Type
    shopType: String,
    onShopTypeChange: (String) -> Unit,
    // Menu Items (quick chips)
    menuItems: List<String>,
    onMenuItemsChange: (List<String>) -> Unit,
    currentMenuItem: String,
    onCurrentMenuItemChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    val shopTypes = listOf("Restaurant", "Food Truck", "Street Food", "Cafe", "Fast Food", "Bakery", "Sweet Shop")
    var shopTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Photos Section ---
        Text(
            text = "Photos",
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

        // --- Basic Details ---
        Text(
            text = "Basic Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Shop Name
        OutlinedTextField(
            value = shopName,
            onValueChange = onShopNameChange,
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
                            onShopTypeChange(selectionOption)
                            shopTypeExpanded = false
                        }
                    )
                }
            }
        }

        // Menu Items (quick chips)
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
                            onClick = { onMenuItemsChange(menuItems - item) },
                            label = { Text(item) },
                            trailingIcon = { Icon(Icons.Default.Close, "Remove", Modifier.size(16.dp)) }
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
                label = { Text("Add Top Dishes") },
                placeholder = { Text("e.g. Samosa, Chai, Vada Pav") },
                supportingText = { Text("Type and press comma or done to add") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (currentMenuItem.isNotBlank() && !menuItems.contains(currentMenuItem.trim())) {
                            onMenuItemsChange(menuItems + currentMenuItem.trim())
                            onCurrentMenuItemChange("")
                        }
                    }
                )
            )
        }

        // Padding at bottom for scroll
        Spacer(modifier = Modifier.height(24.dp))
    }
}
