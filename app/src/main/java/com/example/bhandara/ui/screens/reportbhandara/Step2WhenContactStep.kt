package com.example.bhandara.ui.screens.reportbhandara

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2WhenContactStep(
    // Feast Date
    feastDate: LocalDate?,
    onFeastDateClick: () -> Unit,
    // Times
    startTime: LocalTime?,
    onStartTimeClick: () -> Unit,
    endTime: LocalTime?,
    onEndTimeClick: () -> Unit,
    // Organizer & Contact info
    organizerName: String,
    onOrganizerNameChange: (String) -> Unit,
    contactPhone: String,
    onContactPhoneChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date & Time Section
        Text(
            text = "When *",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        // Feast Date (Required)
        OutlinedCard(
            onClick = onFeastDateClick,
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
                onClick = onStartTimeClick,
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
                onClick = onEndTimeClick,
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Contact Info
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        OutlinedTextField(
            value = organizerName,
            onValueChange = onOrganizerNameChange,
            label = { Text("Organizer Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
        
        OutlinedTextField(
            value = contactPhone,
            onValueChange = onContactPhoneChange,
            label = { Text("Contact Phone") },
            placeholder = { Text("+919876543210") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
