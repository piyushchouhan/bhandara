package com.example.bhandara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.R
import com.example.bhandara.ui.theme.BhandaraTheme
import com.example.bhandara.ui.theme.FoodShopPrimary
import com.example.bhandara.ui.theme.FoodShopPrimaryContainer
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onHungryClick: () -> Unit = {},
    onReportFeastClick: () -> Unit = {}
) {
    val isDarkMode = isSystemInDarkTheme()
    val logoResource = if (isDarkMode) R.drawable.logo_1 else R.drawable.logo
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Logo positioned independently at top center
        Image(
            painter = painterResource(id = logoResource),
            contentDescription = "Bhandara Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
        

        // Content Switching using State
        var selectedIndex by remember { mutableStateOf(0) }
        val options = listOf(
            stringResource(R.string.community_feast),
            stringResource(R.string.local_shops)
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedIndex == 0) {
                CommunityFeastContent(
                    onHungryClick = onHungryClick,
                    onReportFeastClick = onReportFeastClick
                )
            } else {
                LocalShopsContent()
            }
        }

        // Toggle positioned at the bottom
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            options.forEachIndexed { index, label ->
                val buttonColors = if (index == 1) {
                    SegmentedButtonDefaults.colors(
                        activeContainerColor = FoodShopPrimaryContainer,
                        activeContentColor = FoodShopPrimary
                    )
                } else {
                    SegmentedButtonDefaults.colors()
                }
                
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex,
                    colors = buttonColors
                ) {
                    Text(
                        text = label,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BhandaraTheme {
        HomeScreen()
    }
}
