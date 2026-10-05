package com.example.bhandara

import com.example.bhandara.ui.shopsmap.ShopsMapScreen
import com.example.bhandara.ui.components.MovingCartTracker
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.AppDependencies
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.storage.AndroidFirebaseStorage
import com.example.bhandara.data.auth.AppAuth
import com.example.bhandara.photos.PhotoUploader
import com.example.bhandara.data.auth.AppBackend
import androidx.compose.runtime.CompositionLocalProvider
import com.example.bhandara.data.location.AndroidDeviceLocation
import com.example.bhandara.platform.ProvideAppEnvironment
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bhandara.services.CrowdPingManager
import kotlinx.coroutines.launch
import com.example.bhandara.managers.UserManager
import com.example.bhandara.ui.components.OwnerConfirmationDialog
import com.example.bhandara.ui.shopdetails.ShopDetailsScreen
import com.example.bhandara.navigation.NavEntries
import com.example.bhandara.ui.drawer.AppDrawer
import com.example.bhandara.ui.drawer.AboutScreen
import com.example.bhandara.ui.drawer.SupportScreen
import com.example.bhandara.ui.drawer.TermsScreen
import com.example.bhandara.ui.profile.LocalAccountSignIn
import com.example.bhandara.ui.profile.ProfileScreen
import com.example.bhandara.ui.components.GoogleAccountSignIn
import com.example.bhandara.ui.components.VendorMode
import com.example.bhandara.ui.components.VendorModeBanner
import com.example.bhandara.ui.components.VendorModeItem
import com.example.bhandara.ui.feasts.FeastDetailsScreen
import com.example.bhandara.ui.reportfeast.ReportFeastScreen
import com.example.bhandara.ui.feasts.FeastsMapScreen
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.screens.AddLocalShopScreen
import com.example.bhandara.ui.screens.ClaimShopScreen
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
        // Location works with precise or approximate access; the notification permission doesn't matter here
        val locationAllowed = AndroidDeviceLocation.PERMISSIONS.any { permissions[it] == true }
        if (locationAllowed) {
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
            ProvideAppEnvironment {
              CompositionLocalProvider(
                  LocalAppDependencies provides remember { AppDependencies(AppBackend.client, AppSettings(this), AppAuth.service, BuildConfig.VERSION_NAME, PhotoUploader(AndroidFirebaseStorage())) },
                  LocalAccountSignIn provides remember { GoogleAccountSignIn(this) },
              ) {
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
                
                    val vendorMode = remember { VendorMode(this@MainActivity) }

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
                        NavEntries(navigator) { route ->
                        when (route) {
                            Route.Home -> {
                                AppDrawer(
                                    onProfileClick = { navigateTo(Route.Profile) },
                                    onAboutClick = { navigateTo(Route.About) },
                                    onSupportClick = { navigateTo(Route.Support) },
                                    onTermsClick = { navigateTo(Route.Terms) },
                                    extraItems = { VendorModeItem(vendorMode) },
                                    overlay = { VendorModeBanner(vendorMode, Modifier.align(Alignment.TopCenter)) },
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
                                FeastsMapScreen(
                                    onBack = { navigateBack() },
                                    onFeastClick = { feastId -> navigateTo(Route.FeastDetails(feastId)) },
                                )
                            }
                            Route.ReportFeast -> {
                                ReportFeastScreen(
                                    onBack = { navigateBack() },
                                    // The new feast's page replaces the form, so back goes home
                                    onPosted = { feastId -> navigator.replace(Route.FeastDetails(feastId)) },
                                )
                            }
                            is Route.FeastDetails -> {
                                FeastDetailsScreen(feastId = route.feastId, onBack = { navigateBack() })
                            }
                            Route.AddShop -> {
                                AddLocalShopScreen(
                                    onNavigateBack = { navigateBack() }
                                )
                            }
                            Route.ShopsMap -> {
                                // Shared with iOS; a tapped live cart opens Android's live tracking
                                var trackedCart by remember { mutableStateOf<LocalShopResponse?>(null) }
                                ShopsMapScreen(
                                    onBack = { navigateBack() },
                                    onShopClick = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onCartClick = { cart -> trackedCart = cart },
                                )
                                trackedCart?.let { cart ->
                                    MovingCartTracker(
                                        shopId = cart.id.toLongOrNull() ?: -1L,
                                        shopName = cart.shopName,
                                        initialLat = cart.latitude,
                                        initialLng = cart.longitude,
                                        onDismiss = { trackedCart = null },
                                        onViewDetails = {
                                            trackedCart = null
                                            navigateTo(Route.ShopDetails(cart.id))
                                        },
                                    )
                                }
                            }
                            is Route.ShopDetails -> {
                                ShopDetailsScreen(
                                    shopId = route.shopId,
                                    onBack = { navigateBack() },
                                    onClaim = { shopName -> navigateTo(Route.ClaimShop(route.shopId, shopName)) },
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
                                ProfileScreen(onBack = { navigateBack() })
                            }
                            Route.About -> {
                                AboutScreen(onBack = { navigateBack() })
                            }
                            Route.Support -> {
                                SupportScreen(onBack = { navigateBack() })
                            }
                            Route.Terms -> {
                                TermsScreen(onBack = { navigateBack() })
                            }
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
