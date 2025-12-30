package com.example.bhandara

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.bhandara.managers.UserManager
import com.example.bhandara.ui.components.LanguageSwitcher
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.screens.HungryScreen
import com.example.bhandara.ui.screens.ReportFeastScreen
import com.example.bhandara.ui.theme.BhandaraTheme
import com.example.bhandara.utils.LocationHelper

// Simple navigation states
enum class Screen {
    HOME, HUNGRY, REPORT_FEAST
}

class MainActivity : AppCompatActivity() {
    
    private lateinit var userManager: UserManager
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            userManager.updateUserLocation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize user manager
        userManager = UserManager(this, lifecycleScope)
        
        // Initialize anonymous user on app start
        userManager.initializeUser()
        
        setContent {
            BhandaraTheme {
                // Navigation back stack
                var backStack by remember { mutableStateOf(listOf(Screen.HOME)) }
                val currentScreen = backStack.last()
                
                // Request permissions on first composition
                LaunchedEffect(Unit) {
                    requestPermissions()
                }
                
                // Navigate to a new screen
                fun navigateTo(screen: Screen) {
                    backStack = backStack + screen
                }
                
                // Navigate back
                fun navigateBack() {
                    if (backStack.size > 1) {
                        backStack = backStack.dropLast(1)
                    }
                }
                
                // Handle back press
                BackHandler(enabled = backStack.size > 1) {
                    navigateBack()
                }
                
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
                                onHungryClick = { navigateTo(Screen.HUNGRY) },
                                onReportFeastClick = { navigateTo(Screen.REPORT_FEAST) }
                            )
                        }
                        Screen.HUNGRY -> {
                            HungryScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                        Screen.REPORT_FEAST -> {
                            ReportFeastScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Request required permissions
     */
    private fun requestPermissions() {
        permissionLauncher.launch(LocationHelper.REQUIRED_PERMISSIONS)
    }
}
