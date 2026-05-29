package com.example.bhandara.ui.screens.addshop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step4OptionalDetailsStep(
    // Description
    description: String,
    onDescriptionChange: (String) -> Unit,
    // Contact
    ownerPhone: String,
    onOwnerPhoneChange: (String) -> Unit,
    // Address
    fullAddress: String,
    onFullAddressChange: (String) -> Unit,
    landmark: String,
    onLandmarkChange: (String) -> Unit,
    // Cuisine & Cost
    cuisineType: String,
    onCuisineTypeChange: (String) -> Unit,
    averageCostForTwo: String,
    onAverageCostForTwoChange: (String) -> Unit,
    // Facilities
    takeaway: Boolean,
    onTakeawayChange: (Boolean) -> Unit,
    homeDelivery: Boolean,
    onHomeDeliveryChange: (Boolean) -> Unit,
    hasSeating: Boolean,
    onHasSeatingChange: (Boolean) -> Unit,
    wifiAvailable: Boolean,
    onWifiAvailableChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Additional Details (Optional)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "These details help customers find your shop better. You can always update them later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Contact
        OutlinedTextField(
            value = ownerPhone,
            onValueChange = onOwnerPhoneChange,
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Address
        OutlinedTextField(
            value = fullAddress,
            onValueChange = onFullAddressChange,
            label = { Text("Full Address") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = landmark,
            onValueChange = onLandmarkChange,
            label = { Text("Landmark") },
            modifier = Modifier.fillMaxWidth()
        )

        // Cuisine & Cost
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cuisineType,
                onValueChange = onCuisineTypeChange,
                label = { Text("Cuisine Type") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = averageCostForTwo,
                onValueChange = { onAverageCostForTwoChange(it.filter { char -> char.isDigit() }) },
                label = { Text("Cost for 2 (₹)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Facilities Toggles
        Text("Facilities", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = takeaway, onCheckedChange = onTakeawayChange)
                Text("Takeaway")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = homeDelivery, onCheckedChange = onHomeDeliveryChange)
                Text("Delivery")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasSeating, onCheckedChange = onHasSeatingChange)
                Text("Seating")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = wifiAvailable, onCheckedChange = onWifiAvailableChange)
                Text("WiFi")
            }
        }

        // Bottom padding
        Spacer(modifier = Modifier.height(40.dp))
    }
}
