package com.example.bhandara

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.example.bhandara.navigation.Route
import com.example.bhandara.navigation.rememberAppNavigator
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.theme.BhandaraTheme

/** The iOS app's entry point: ContentView.swift shows this */
fun MainViewController() = ComposeUIViewController {
    BhandaraTheme {
        val navigator = rememberAppNavigator()
        var homeTabIndex by remember { mutableIntStateOf(0) }

        when (val route = navigator.current) {
            Route.Home -> HomeScreen(
                modifier = Modifier.safeDrawingPadding(),
                selectedTabIndex = homeTabIndex,
                onTabSelected = { homeTabIndex = it },
                onHungryClick = { navigator.navigate(Route.Hungry) },
                onReportFeastClick = { navigator.navigate(Route.ReportFeast) },
                onFindShopsClick = { navigator.navigate(Route.ShopsMap) },
                onAddShopClick = { navigator.navigate(Route.AddShop) },
            )
            // Screens move to the shared module one by one; until then iOS says so
            else -> NotOnIphoneYet(route, onBack = { navigator.back() })
        }
    }
}

@Composable
private fun NotOnIphoneYet(route: Route, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "${route::class.simpleName} is coming soon on iPhone",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onBack) { Text("Back") }
        }
    }
}
