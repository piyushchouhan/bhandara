package com.example.bhandara

import com.example.bhandara.services.AndroidVendorTracker
import com.example.bhandara.vendor.VendorMode
import com.example.bhandara.vendor.LocalVendorMode
import com.example.bhandara.data.crowd.KeepCountingCrowds
import com.example.bhandara.navigation.NOTIFICATION_DATA_KEYS
import com.example.bhandara.navigation.routeForNotification
import com.example.bhandara.data.saved.KeepSavedPlacesSynced
import com.example.bhandara.ui.privacy.privacyStart
import com.example.bhandara.ui.settings.SettingsScreen
import com.example.bhandara.data.settings.StartTab
import com.example.bhandara.ui.theme.ProvideAppPreferences
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.AppDependencies
import com.example.bhandara.data.settings.AppSettings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.bhandara.managers.UserManager
import com.example.bhandara.ui.shopdetails.ShopDetailsScreen
import com.example.bhandara.ui.feasts.MyFeastsScreen
import com.example.bhandara.ui.privacy.PrivacyScreen
import com.example.bhandara.ui.reports.MyReportsScreen
import com.example.bhandara.ui.saved.SavedPlacesScreen
import com.example.bhandara.ui.reportfeast.FeastFormStart
import com.example.bhandara.ui.reviews.MyPhotosScreen
import com.example.bhandara.ui.reviews.MyReviewsScreen
import com.example.bhandara.ui.reviews.ShopReviewsScreen
import com.example.bhandara.ui.reviews.WriteReviewScreen
import com.example.bhandara.navigation.NavEntries
import com.example.bhandara.ui.drawer.AboutScreen
import com.example.bhandara.ui.drawer.SupportScreen
import com.example.bhandara.ui.drawer.ReportIssueScreen
import com.example.bhandara.ui.drawer.SuggestFeatureScreen
import com.example.bhandara.ui.drawer.HowToUseScreen
import com.example.bhandara.ui.drawer.TermsScreen
import com.example.bhandara.ui.profile.LocalAccountSignIn
import com.example.bhandara.ui.profile.ProfileScreen
import com.example.bhandara.ui.shopdetails.ManageShopScreen
import com.example.bhandara.ui.components.GoogleAccountSignIn
import com.example.bhandara.ui.feasts.FeastDetailsScreen
import com.example.bhandara.ui.reportfeast.ReportFeastScreen
import com.example.bhandara.ui.feasts.FeastsMapScreen
import com.example.bhandara.ui.home.HomeScreen
import com.example.bhandara.ui.addshop.AddShopScreen
import com.example.bhandara.ui.claim.ClaimShopScreen
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

        
        setContent {
            ProvideAppEnvironment {
              CompositionLocalProvider(
                  LocalAppDependencies provides remember { AppDependencies(AppBackend.client, AppSettings(this), AppAuth.service, BuildConfig.VERSION_NAME, PhotoUploader(AndroidFirebaseStorage()), BuildConfig.ADMIN_EMAILS.split(",")) },
                  LocalAccountSignIn provides remember { GoogleAccountSignIn(this) },
                  LocalVendorMode provides remember { VendorMode(AppSettings(this), AndroidVendorTracker(this)) },
              ) {
               val preferences = LocalAppDependencies.current.settings.preferences
               ProvideAppPreferences(preferences) {
                KeepSavedPlacesSynced()
                // "How busy is it" badges: while the app is visible, report when the user is at a shop
                KeepCountingCrowds()
                BhandaraTheme {
                    // Which screens are open (shared with iOS); kept across rotation
                    val navigator = rememberAppNavigator(startOnFeasts = preferences.startTab == StartTab.FEASTS)

                    // Opened from a notification: go to what it's about (only on a fresh start, not after rotation)
                    LaunchedEffect(Unit) {
                        if (savedInstanceState == null) {
                            routeForNotification(NOTIFICATION_DATA_KEYS.associateWith { intent.getStringExtra(it) })?.let(navigator::navigate)
                        }
                    }

                
                    // Request permissions on first composition
                    LaunchedEffect(Unit) {
                        requestPermissions()
                    }
                
                    val vendorMode = LocalVendorMode.current

                    fun navigateTo(route: Route) = navigator.navigate(route)

                    fun navigateBack() {
                        navigator.back()
                    }

                    // Back from the first screen is left to the system (closes the app)
                    BackHandler(enabled = navigator.canGoBack) {
                        navigator.back()
                    }
                
                    // Each screen draws its own bars and keeps clear of the system bars itself
                    NavEntries(navigator) { route ->
                        when (route) {
                            Route.Home -> {
                                // Shared with iOS, live cart tracking included
                                HomeScreen(
                                    onShopClick = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onAddShopClick = { navigateTo(Route.AddShop) },
                                    onFeastsClick = { navigateTo(Route.Hungry) },
                                    onProfileClick = { navigateTo(Route.Profile) },
                                    onSettingsClick = { navigateTo(Route.Settings) },
                                    onAboutClick = { navigateTo(Route.About) },
                                    onSupportClick = { navigateTo(Route.Support) },
                                    onTermsClick = { navigateTo(Route.Terms) },
                                    onReportIssueClick = { navigateTo(Route.ReportIssue) },
                                    onSuggestFeatureClick = { navigateTo(Route.SuggestFeature) },
                                    onHowToUseClick = { navigateTo(Route.HowToUse) },
                                )
                            }
                            Route.Hungry -> {
                                FeastsMapScreen(
                                    onBack = { navigateBack() },
                                    onFeastClick = { feastId -> navigateTo(Route.FeastDetails(feastId)) },
                                    onPostFeast = { navigateTo(Route.ReportFeast) },
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
                                FeastDetailsScreen(
                                    feastId = route.feastId,
                                    onBack = { navigateBack() },
                                    // Back to the feasts map if it's under this page, else open it in this page's place
                                    onFindOtherFeasts = {
                                        if (navigator.backStack.dropLast(1).lastOrNull() == Route.Hungry) navigator.back() else navigator.replace(Route.Hungry)
                                    },
                                )
                            }
                            Route.AddShop -> {
                                AddShopScreen(
                                    onBack = { navigateBack() },
                                    // The new shop's page replaces the form, so back goes home
                                    onAdded = { shopId -> navigator.replace(Route.ShopDetails(shopId)) },
                                    onGoLive = { shopId ->
                                        vendorMode?.set(true)
                                        navigator.replace(Route.ShopDetails(shopId))
                                    },
                                )
                            }
                            is Route.ShopDetails -> {
                                ShopDetailsScreen(
                                    shopId = route.shopId,
                                    onBack = { navigateBack() },
                                    onOpenReviews = { shopName, photoUrl -> navigateTo(Route.ShopReviews(route.shopId, shopName, photoUrl)) },
                                    onWriteReview = { shopName, photoUrl -> navigateTo(Route.WriteReview(route.shopId, shopName, photoUrl)) },
                                    onClaim = { shopName -> navigateTo(Route.ClaimShop(route.shopId, shopName)) },
                                )
                            }
                            is Route.ShopReviews -> {
                                ShopReviewsScreen(
                                    shopId = route.shopId,
                                    shopName = route.shopName,
                                    photoUrl = route.photoUrl,
                                    onBack = { navigateBack() },
                                    onWriteReview = { rating -> navigateTo(Route.WriteReview(route.shopId, route.shopName, route.photoUrl, rating)) },
                                )
                            }
                            is Route.WriteReview -> {
                                WriteReviewScreen(
                                    shopId = route.shopId,
                                    shopName = route.shopName,
                                    photoUrl = route.photoUrl,
                                    rating = route.rating,
                                    onClose = { navigateBack() },
                                )
                            }
                            is Route.ClaimShop -> {
                                ClaimShopScreen(shopId = route.shopId, shopName = route.shopName, onDone = { navigateBack() })
                            }
                            Route.Profile -> {
                                ProfileScreen(
                                    onBack = { navigateBack() },
                                    onManageShop = { shopId -> navigateTo(Route.ManageShop(shopId)) },
                                    onOpenShop = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onAddShop = { navigateTo(Route.AddShop) },
                                    onMyReviews = { navigateTo(Route.MyReviews) },
                                    onMyPhotos = { navigateTo(Route.MyPhotos) },
                                    onMyFeasts = { navigateTo(Route.MyFeasts) },
                                    onSavedPlaces = { navigateTo(Route.SavedPlaces) },
                                    onMyReports = { navigateTo(Route.MyReports) },
                                    onPrivacy = { startAtDelete -> navigateTo(Route.Privacy(startAtDelete)) },
                                )
                            }
                            is Route.Privacy -> {
                                PrivacyScreen(
                                    onBack = { navigateBack() },
                                    onOpenTerms = { navigateTo(Route.Terms) },
                                    onAccountDeleted = { navigator.backToHome() },
                                    startAt = privacyStart(route),
                                )
                            }
                            Route.Settings -> {
                                SettingsScreen(
                                    onBack = { navigateBack() },
                                    onOpenLocation = { navigateTo(Route.Privacy(startAtLocation = true)) },
                                    onOpenPrivacy = { navigateTo(Route.Privacy()) },
                                    onDeleteAccount = { navigateTo(Route.Privacy(startAtDelete = true)) },
                                    appleMaps = false,
                                )
                            }
                            Route.MyReports -> {
                                MyReportsScreen(
                                    onBack = { navigateBack() },
                                    onOpenShop = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onOpenFeast = { feastId -> navigateTo(Route.FeastDetails(feastId)) },
                                )
                            }
                            Route.SavedPlaces -> {
                                SavedPlacesScreen(
                                    onBack = { navigateBack() },
                                    onOpenShop = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onOpenFeast = { feastId -> navigateTo(Route.FeastDetails(feastId)) },
                                    onExploreMap = { navigator.backToHome() },
                                )
                            }
                            Route.MyFeasts -> {
                                MyFeastsScreen(
                                    onBack = { navigateBack() },
                                    onOpenFeast = { feastId -> navigateTo(Route.FeastDetails(feastId)) },
                                    onEdit = { feastId -> navigateTo(Route.EditFeast(feastId)) },
                                    onRepost = { feastId -> navigateTo(Route.RepostFeast(feastId)) },
                                    onResubmit = { feastId -> navigateTo(Route.ResubmitFeast(feastId)) },
                                    onPostFeast = { navigateTo(Route.ReportFeast) },
                                    onContactSupport = { navigateTo(Route.Support) },
                                )
                            }
                            is Route.EditFeast -> {
                                ReportFeastScreen(
                                    onBack = { navigateBack() },
                                    // Back to the list, which shows the change
                                    onPosted = { navigateBack() },
                                    start = FeastFormStart.Edit(route.feastId),
                                )
                            }
                            is Route.ResubmitFeast -> {
                                ReportFeastScreen(
                                    onBack = { navigateBack() },
                                    // Back to the list, which shows it waiting for a check
                                    onPosted = { navigateBack() },
                                    start = FeastFormStart.Resubmit(route.feastId),
                                )
                            }
                            is Route.RepostFeast -> {
                                ReportFeastScreen(
                                    onBack = { navigateBack() },
                                    onPosted = { feastId -> navigator.replace(Route.FeastDetails(feastId)) },
                                    start = FeastFormStart.Repost(route.feastId),
                                )
                            }
                            Route.MyPhotos -> {
                                MyPhotosScreen(
                                    onBack = { navigateBack() },
                                    onOpenShop = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onEditReview = { review -> navigateTo(Route.WriteReview(review.shopId, review.shopName.orEmpty(), review.shopImageUrl)) },
                                    onFindShop = { navigator.backToHome() },
                                )
                            }
                            Route.MyReviews -> {
                                MyReviewsScreen(
                                    onBack = { navigateBack() },
                                    onOpenShop = { shopId -> navigateTo(Route.ShopDetails(shopId)) },
                                    onEdit = { review -> navigateTo(Route.WriteReview(review.shopId, review.shopName.orEmpty(), review.shopImageUrl)) },
                                    onFindShop = { navigator.backToHome() },
                                )
                            }
                            is Route.ManageShop -> {
                                ManageShopScreen(
                                    shopId = route.shopId,
                                    onBack = { navigateBack() },
                                    onViewShop = { navigateTo(Route.ShopDetails(route.shopId)) },
                                    onOpenReviews = { shopName, photoUrl -> navigateTo(Route.ShopReviews(route.shopId, shopName, photoUrl)) },
                                    onDeactivated = { navigateBack() },
                                )
                            }
                            Route.About -> {
                                AboutScreen(onBack = { navigateBack() })
                            }
                            Route.Support -> {
                                SupportScreen(
                                    onBack = { navigateBack() },
                                    onReportIssue = { navigateTo(Route.ReportIssue) },
                                    onSuggestFeature = { navigateTo(Route.SuggestFeature) },
                                    onHowToUse = { navigateTo(Route.HowToUse) },
                                )
                            }
                            Route.ReportIssue -> {
                                ReportIssueScreen(onBack = { navigateBack() })
                            }
                            Route.SuggestFeature -> {
                                SuggestFeatureScreen(onBack = { navigateBack() })
                            }
                            Route.HowToUse -> {
                                HowToUseScreen(onBack = { navigateBack() })
                            }
                            Route.Terms -> {
                                TermsScreen(onBack = { navigateBack() })
                            }
                        }
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
