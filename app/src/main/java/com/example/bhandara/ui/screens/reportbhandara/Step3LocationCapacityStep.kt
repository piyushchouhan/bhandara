package com.example.bhandara.ui.screens.reportbhandara

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * This composable handles the location and capacity step of the add bhandara flow.
 * It uses the MaterialTheme to style the UI.
 * It uses the OutlinedTextField composable to display the text fields.
 */

@Composable
fun Step3LocationCapacityStep(
    // Address & Landmark
    address: String,
    onAddressChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    // Capacity
    estimatedCapacity: String,
    onEstimatedCapacityChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location Section
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        OutlinedTextField(
            value = address,
            onValueChange = { if (it.length <= 500) onAddressChange(it) },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            leadingIcon = { Icon(Icons.Default.LocationOn, null) }
        )
        
        OutlinedTextField(
            value = landmark,
            onValueChange = { if (it.length <= 255) onLandmarkChange(it) },
            label = { Text("Landmark") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Place, null) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Capacity Section
        Text(
            text = "Capacity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedTextField(
            value = estimatedCapacity,
            onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onEstimatedCapacityChange(it) },
            label = { Text("Estimated Capacity") },
            placeholder = { Text("Number of people") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.People, null) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
