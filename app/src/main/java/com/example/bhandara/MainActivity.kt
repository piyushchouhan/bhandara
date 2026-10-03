package com.example.bhandara

import com.example.bhandara.navigation.Route
import com.example.bhandara.navigation.rememberAppNavigator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bhandara.services.CrowdPingManager
import kotlinx.coroutines.launch
import com.example.bhandara.managers.UserManager
import com.example.bhandara.ui.components.AppDrawerMenu
import com.example.bhandara.ui.components.OwnerConfirmationDialog
import com.example.bhandara.ui.screens.FeastDetailsScreen
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.screens.HungryScreen
import com.example.bhandara.ui.screens.ReportBhandaraScreen
import com.example.bhandara.ui.screens.AddLocalShopScreen
import com.example.bhandara.ui.screens.LocalShopsMapScreen
import com.example.bhandara.ui.screens.ClaimShopScreen
import com.example.bhandara.ui.screens.LocalShopDetailsScreen
import com.example.bhandara.ui.screens.ProfileScreen
import com.example.bhandara.ui.components.appDrawer.AboutScreen
import com.example.bhandara.ui.components.appDrawer.SupportScreen
import com.example.bhandara.ui.components.appDrawer.TermsScreen
import com.example.bhandara.BuildConfig
import com.example.bhandara.ui.theme.BhandaraTheme
import com.example.bhandara.utils.LocationHelper

// Simple navigation states

// Navigation arguments

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

        // "How busy is it" badges: while the app is visible (any screen), report when the user is at a shop.
        // Stops automatically when the app goes to the background.
        val crowdPingManager = CrowdPingManager(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                crowdPingManager.runWhileForeground()
            }
        }
        
        setContent {
            BhandaraTheme {
                // Which screens are open (shared with iOS); kept across rotation
                val navigator = rememberAppNavigator()
                
                // Lifted home tab state
                var homeTabIndex by rememberSaveable { mutableIntStateOf(0) }
                
                var showOwnerConfirmDialog by remember { mutableStateOf(false) }
                
                // Request permissions on first composition
                LaunchedEffect(Unit) {
                    requestPermissions()
                }
                
                fun navigateTo(route: Route) = navigator.navigate(route)

                fun navigateBack() {
                    navigator.back()
                }

                // Back from the first screen is left to the system (closes the app)
                BackHandler(enabled = navigator.canGoBack) {
                    navigator.back()
                }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (val route = navigator.current) {
                        Route.Home -> {
                            AppDrawerMenu(
                                onProfileClick = { navigateTo(Route.Profile) },
                                onAboutClick = { navigateTo(Route.About) },
                                onSupportClick = { navigateTo(Route.Support) },
                                onTermsClick = { navigateTo(Route.Terms) }
                            ) {
                                HomeScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    selectedTabIndex = homeTabIndex,
                                    onTabSelected = { homeTabIndex = it },
                                    onHungryClick = { navigateTo(Route.Hungry) },
                                    onReportFeastClick = { navigateTo(Route.ReportFeast) },
                                    onFindShopsClick = { navigateTo(Route.ShopsMap) },
                                    onAddShopClick = {
                                        // Admins list shops on behalf of their owners, so the "only add your own shop" warning doesn't apply
                                        if (isAdminUser()) navigateTo(Route.AddShop) else showOwnerConfirmDialog = true
                                    }
                                )
                            }
                        }
                        Route.Hungry -> {
                            HungryScreen(
                                onBackClick = { navigateBack() },
                                onFeastClick = { feastId ->
                                    navigateTo(Route.FeastDetails(feastId))
                                }
                            )
                        }
                        Route.ReportFeast -> {
                            ReportBhandaraScreen(
                                onNavigateBack = { navigateBack() }
                            )
                        }
                        is Route.FeastDetails -> {
                            FeastDetailsScreen(
                                feastId = route.feastId,
                                onBackClick = { navigateBack() }
                            )
                        }
                        Route.AddShop -> {
                            AddLocalShopScreen(
                                onNavigateBack = { navigateBack() }
                            )
                        }
                        Route.ShopsMap -> {
                            LocalShopsMapScreen(
                                onBackClick = { navigateBack() },
                                onShopClick = { shopId ->
                                    navigateTo(Route.ShopDetails(shopId))
                                }
                            )
                        }
                        is Route.ShopDetails -> {
                            LocalShopDetailsScreen(
                                shopId = route.shopId,
                                onBackClick = { navigateBack() },
                                onClaimClick = { shopName ->
                                    navigateTo(Route.ClaimShop(route.shopId, shopName))
                                }
                            )
                        }
                        is Route.ClaimShop -> {
                            ClaimShopScreen(
                                shopId = route.shopId,
                                shopName = route.shopName,
                                onBackClick = { navigateBack() }
                            )
                        }
                        Route.Profile -> {
                            ProfileScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                        Route.About -> {
                            AboutScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                        Route.Support -> {
                            SupportScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                        Route.Terms -> {
                            TermsScreen(
                                onBackClick = { navigateBack() }
                            )
                        }
                    }
                }

                if (showOwnerConfirmDialog) {
                    OwnerConfirmationDialog(
                        onConfirm = {
                            showOwnerConfirmDialog = false
                            navigateTo(Route.AddShop)
                        },
                        onDismissRequest = {
                            showOwnerConfirmDialog = false
                        }
                    )
                }
            }
        }
    }
    
    private fun isAdminUser(): Boolean {
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: return false
        return BuildConfig.ADMIN_EMAILS.split(",").any { it.trim().equals(email, ignoreCase = true) }
    }

    /**
     * Request required permissions
     */
    private fun requestPermissions() {
        permissionLauncher.launch(LocationHelper.REQUIRED_PERMISSIONS)
    }
}
