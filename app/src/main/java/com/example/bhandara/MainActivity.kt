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
import com.example.bhandara.ui.screens.FeastDetailsScreen
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.screens.HungryScreen
import com.example.bhandara.ui.screens.ReportBhandaraScreen
import com.example.bhandara.ui.theme.BhandaraTheme
import com.example.bhandara.utils.LocationHelper

// Simple navigation states
enum class Screen {
    HOME, HUNGRY, REPORT_BHANDARA, FEAST_DETAILS
}

// Navigation arguments
data class NavArgs(
    val feastId: String? = null
)

class MainActivity : AppCompatActivity() {
    
    private lateinit var userManager: UserManager
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            // Update location immediately
            userManager.updateUserLocation()
            
            // Start periodic background updates (every 15 minutes)
            userManager.startPeriodicLocationUpdates()
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
                // Navigation back stack with arguments
                var backStack by remember { mutableStateOf(listOf(Pair(Screen.HOME, NavArgs()))) }
                val (currentScreen, currentArgs) = backStack.last()
                
                // Request permissions on first composition
                LaunchedEffect(Unit) {
                    requestPermissions()
                }
                
                // Navigate to a new screen with optional arguments
                fun navigateTo(screen: Screen, args: NavArgs = NavArgs()) {
                    backStack = backStack + Pair(screen, args)
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
                                onReportFeastClick = { navigateTo(Screen.REPORT_BHANDARA) }
                            )
                        }
                        Screen.HUNGRY -> {
                            HungryScreen(
                                onBackClick = { navigateBack() },
                                onFeastClick = { feastId ->
                                    navigateTo(Screen.FEAST_DETAILS, NavArgs(feastId = feastId))
                                }
                            )
                        }
                        Screen.REPORT_BHANDARA -> {
                            ReportBhandaraScreen(
                                onNavigateBack = { navigateBack() }
                            )
                        }
                        Screen.FEAST_DETAILS -> {
                            currentArgs.feastId?.let { feastId ->
                                FeastDetailsScreen(
                                    feastId = feastId,
                                    onBackClick = { navigateBack() }
                                )
                            }
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
