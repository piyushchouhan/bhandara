package com.example.bhandara

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.bhandara.ui.components.LanguageSwitcher
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.screens.HungryScreen
import com.example.bhandara.ui.screens.ReportFeastScreen
import com.example.bhandara.ui.theme.BhandaraTheme

// Simple navigation states
enum class Screen {
    HOME, HUNGRY, REPORT_FEAST
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BhandaraTheme {
                // Track current screen
                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { 
                        if (currentScreen == Screen.HOME) {
                            LanguageSwitcher() 
                        }
                    }
                ) { innerPadding ->
                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onHungryClick = { currentScreen = Screen.HUNGRY },
                                onReportFeastClick = { currentScreen = Screen.REPORT_FEAST }
                            )
                        }
                        Screen.HUNGRY -> {
                            HungryScreen(
                                onBackClick = { currentScreen = Screen.HOME }
                            )
                        }
                        Screen.REPORT_FEAST -> {
                            ReportFeastScreen(
                                onBackClick = { currentScreen = Screen.HOME }
                            )
                        }
                    }
                }
            }
        }
    }
}
