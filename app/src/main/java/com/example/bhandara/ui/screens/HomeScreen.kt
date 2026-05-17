package com.example.bhandara.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.R
import com.example.bhandara.ui.components.PillToggle
import com.example.bhandara.ui.theme.BhandaraTheme


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onHungryClick: () -> Unit = {},
    onReportFeastClick: () -> Unit = {},
    onFindShopsClick: () -> Unit = {},
    onAddShopClick: () -> Unit = {}
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Logo positioned independently at top center
        Image(
            painter = painterResource(id = R.drawable.localfeast),
            contentDescription = "Bhandara Logo",
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        )
        

        // Content Switching using State
        // Content Switching using State
        val options = listOf(
            stringResource(R.string.local_shops),
            stringResource(R.string.community_feast)
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedTabIndex == 0) {
                LocalShopsContent(
                    onFindShopsClick = onFindShopsClick,
                    onAddShopClick = onAddShopClick
                )
            } else {
                CommunityFeastContent(
                    onHungryClick = onHungryClick,
                    onReportFeastClick = onReportFeastClick
                )
            }
        }

        // Reusable PillToggle component
        PillToggle(
            selectedIndex = selectedTabIndex,
            options = options,
            onOptionSelected = onTabSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    BhandaraTheme {
        HomeScreen()
    }
}
