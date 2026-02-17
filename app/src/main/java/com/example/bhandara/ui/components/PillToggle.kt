package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.bhandara.ui.theme.googleSansFamily

@Composable
fun PillToggle(
    selectedIndex: Int,
    options: List<String>,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Light Mode: Cream container (F9F6EE), Gray chip (E2E4E9)
    // Dark Mode: Dark Gray container (1F1F1F), Near Black chip (121212)
    val containerColor = if (isDark) Color(0xFF1F1F1F) else Color(0xFFF9F6EE)
    val selectedChipColor = if (isDark) Color(0xFF121212) else Color(0xFFE2E4E9)
    val selectedContentColor = if (isDark) Color.White else Color(0xFF1F1F1F)
    val unselectedContentColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF444746)

    Row(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = selectedIndex == index
            
            Surface(
                onClick = { onOptionSelected(index) },
                shape = RoundedCornerShape(100.dp),
                color = if (isSelected) selectedChipColor else Color.Transparent,
                contentColor = if (isSelected) selectedContentColor else unselectedContentColor
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = TextStyle(
                        fontFamily = googleSansFamily,
                        fontSize = 14.sp, 
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.1.sp
                    )
                )
            }
        }
    }
}
