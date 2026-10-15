package com.example.bhandara

import com.example.bhandara.vendor.IosVendorTracker
import com.example.bhandara.vendor.VendorMode
import com.example.bhandara.vendor.LocalVendorMode
import com.example.bhandara.push.KeepDeviceRegistered
import com.example.bhandara.data.crowd.KeepCountingCrowds
import com.example.bhandara.data.saved.KeepSavedPlacesSynced
import com.example.bhandara.ui.privacy.privacyStart
import com.example.bhandara.ui.settings.SettingsScreen
import com.example.bhandara.data.settings.StartTab
import com.example.bhandara.ui.theme.ProvideAppPreferences
import com.example.bhandara.ui.addshop.AddShopScreen
import com.example.bhandara.ui.claim.ClaimShopScreen
import com.example.bhandara.ui.drawer.AboutScreen
import com.example.bhandara.ui.drawer.SupportScreen
import com.example.bhandara.ui.drawer.ReportIssueScreen
import com.example.bhandara.ui.drawer.SuggestFeatureScreen
import com.example.bhandara.ui.drawer.HowToUseScreen
import com.example.bhandara.ui.drawer.TermsScreen
import com.example.bhandara.ui.profile.ProfileScreen
import com.example.bhandara.ui.feasts.FeastDetailsScreen
import com.example.bhandara.ui.reportfeast.ReportFeastScreen
import platform.Foundation.NSBundle
import com.example.bhandara.ui.feasts.FeastsMapScreen
import com.example.bhandara.ui.shopdetails.ShopDetailsScreen
import com.example.bhandara.ui.shopdetails.ManageShopScreen
import com.example.bhandara.ui.feasts.MyFeastsScreen
import com.example.bhandara.ui.privacy.PrivacyScreen
import com.example.bhandara.ui.reports.MyReportsScreen
import com.example.bhandara.ui.saved.SavedPlacesScreen
import com.example.bhandara.ui.reportfeast.FeastFormStart
import com.example.bhandara.ui.reviews.MyPhotosScreen
import com.example.bhandara.ui.reviews.MyReviewsScreen
import com.example.bhandara.ui.reviews.ShopReviewsScreen
import com.example.bhandara.ui.reviews.WriteReviewScreen
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.AppDependencies
import com.example.bhandara.data.settings.createAppSettings
import com.example.bhandara.data.network.ApiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.example.bhandara.auth.AuthService
import com.example.bhandara.auth.PlatformAuth
import com.example.bhandara.navigation.Route
import com.example.bhandara.shared.resources.back
import com.example.bhandara.auth.IosAccountSignIn
import com.example.bhandara.auth.IosGoogleSignInBridge
import com.example.bhandara.navigation.NavEntries
import com.example.bhandara.ui.profile.LocalAccountSignIn
import com.example.bhandara.photos.IosPlatformStorage
import com.example.bhandara.photos.IosStorageBridge
import com.example.bhandara.photos.PhotoUploader
import com.example.bhandara.navigation.rememberAppNavigator
import com.example.bhandara.navigation.swipeFromEdgeToGoBack
import com.example.bhandara.platform.ProvideAppEnvironment
import com.example.bhandara.ui.map.IosMapBridge
import com.example.bhandara.ui.map.LocalIosMapBridge
import com.example.bhandara.ui.home.HomeScreen
import com.example.bhandara.ui.theme.BhandaraTheme

/**
 * The iOS app's entry point: ContentView.swift shows this.
 *
 * @param auth Firebase sign-in, implemented in Swift (IosPlatformAuth.swift)
 * @param mapBridge Google Maps, implemented in Swift (IosGoogleMapBridge.swift)
 * @param apiBaseUrl the backend's address (ApiBaseUrl in Info.plist, from Config/Secrets.xcconfig)
 * @param storage Firebase Storage for photos, implemented in Swift (IosFirebaseStorage.swift)
 * @param googleSignIn Google sign-in, implemented in Swift (IosGoogleSignIn.swift)
 */
fun MainViewController(
    auth: PlatformAuth,
    mapBridge: IosMapBridge,
    apiBaseUrl: String,
    storage: IosStorageBridge,
    googleSignIn: IosGoogleSignInBridge,
) = ComposeUIViewController {
    val authService = remember { AuthService(auth) }
    val dependencies = remember { AppDependencies(
            ApiClient(apiBaseUrl, authService::idToken),
            createAppSettings(),
            authService,
            appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "",
            photoUploader = PhotoUploader(IosPlatformStorage(storage)),
            adminEmails = (NSBundle.mainBundle.objectForInfoDictionaryKey("AdminEmails") as? String).orEmpty().split(","),
        ) }

    // Like on Android, everyone is signed in (anonymously) from the start, so the backend knows who did what
    LaunchedEffect(Unit) {
        authService.ensureSignedIn()
            .onSuccess { uid ->
                println("Signed in: ${uid.take(6)}…, ID token for the backend: ${if (authService.idToken() != null) "yes" else "no"}")
            }
            .onFailure { println("Sign-in failed: ${it.message}") }
    }

    ProvideAppEnvironment {
        CompositionLocalProvider(
            LocalIosMapBridge provides mapBridge,
            LocalAppDependencies provides dependencies,
            LocalAccountSignIn provides remember { IosAccountSignIn(googleSignIn) },
            LocalVendorMode provides remember { VendorMode(dependencies.settings, IosVendorTracker(apiBaseUrl, authService::idToken)) },
        ) {
          ProvideAppPreferences(dependencies.settings.preferences) {
            KeepSavedPlacesSynced()
            KeepCountingCrowds()
            // Registers the phone and shares its location while open (Android does this in the background)
            KeepDeviceRegistered()
            BhandaraTheme {
                val navigator = rememberAppNavigator(startOnFeasts = dependencies.settings.preferences.startTab == StartTab.FEASTS)

                // Each screen keeps its view models while open; swiping from the left edge goes back, as on iPhone
                Box(Modifier.fillMaxSize().swipeFromEdgeToGoBack(navigator.canGoBack) { navigator.back() }) {
                NavEntries(navigator) { route ->
                when (route) {
                    Route.Home -> HomeScreen(
                        onShopClick = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onAddShopClick = { navigator.navigate(Route.AddShop) },
                        onFeastsClick = { navigator.navigate(Route.Hungry) },
                        onProfileClick = { navigator.navigate(Route.Profile) },
                        onSettingsClick = { navigator.navigate(Route.Settings) },
                        onAboutClick = { navigator.navigate(Route.About) },
                        onSupportClick = { navigator.navigate(Route.Support) },
                        onTermsClick = { navigator.navigate(Route.Terms) },
                        onReportIssueClick = { navigator.navigate(Route.ReportIssue) },
                        onSuggestFeatureClick = { navigator.navigate(Route.SuggestFeature) },
                        onHowToUseClick = { navigator.navigate(Route.HowToUse) },
                    )
                    Route.Hungry -> FeastsMapScreen(
                        onBack = { navigator.back() },
                        onFeastClick = { feastId -> navigator.navigate(Route.FeastDetails(feastId)) },
                        onPostFeast = { navigator.navigate(Route.ReportFeast) },
                    )
                    is Route.FeastDetails -> FeastDetailsScreen(
                        feastId = route.feastId,
                        onBack = { navigator.back() },
                        // Back to the feasts map if it's under this page, else open it in this page's place
                        onFindOtherFeasts = {
                            if (navigator.backStack.dropLast(1).lastOrNull() == Route.Hungry) navigator.back() else navigator.replace(Route.Hungry)
                        },
                    )
                    Route.ReportFeast -> ReportFeastScreen(
                        onBack = { navigator.back() },
                        onPosted = { feastId -> navigator.replace(Route.FeastDetails(feastId)) },
                    )
                    Route.AddShop -> {
                        val vendorMode = LocalVendorMode.current
                        AddShopScreen(
                            onBack = { navigator.back() },
                            onAdded = { shopId -> navigator.replace(Route.ShopDetails(shopId)) },
                            onGoLive = { shopId ->
                                vendorMode?.set(true)
                                navigator.replace(Route.ShopDetails(shopId))
                            },
                        )
                    }
                    is Route.ClaimShop -> ClaimShopScreen(shopId = route.shopId, shopName = route.shopName, onDone = { navigator.back() })
                    Route.Profile -> ProfileScreen(
                        onBack = { navigator.back() },
                        onManageShop = { shopId -> navigator.navigate(Route.ManageShop(shopId)) },
                        onOpenShop = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onAddShop = { navigator.navigate(Route.AddShop) },
                        onMyReviews = { navigator.navigate(Route.MyReviews) },
                        onMyPhotos = { navigator.navigate(Route.MyPhotos) },
                        onMyFeasts = { navigator.navigate(Route.MyFeasts) },
                        onSavedPlaces = { navigator.navigate(Route.SavedPlaces) },
                        onMyReports = { navigator.navigate(Route.MyReports) },
                        onPrivacy = { startAtDelete -> navigator.navigate(Route.Privacy(startAtDelete)) },
                    )
                    is Route.Privacy -> PrivacyScreen(
                        onBack = { navigator.back() },
                        onOpenTerms = { navigator.navigate(Route.Terms) },
                        onAccountDeleted = { navigator.backToHome() },
                        startAt = privacyStart(route),
                    )
                    Route.Settings -> SettingsScreen(
                        onBack = { navigator.back() },
                        onOpenLocation = { navigator.navigate(Route.Privacy(startAtLocation = true)) },
                        onOpenPrivacy = { navigator.navigate(Route.Privacy()) },
                        onDeleteAccount = { navigator.navigate(Route.Privacy(startAtDelete = true)) },
                        appleMaps = true,
                    )
                    Route.MyReports -> MyReportsScreen(
                        onBack = { navigator.back() },
                        onOpenShop = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onOpenFeast = { feastId -> navigator.navigate(Route.FeastDetails(feastId)) },
                    )
                    Route.SavedPlaces -> SavedPlacesScreen(
                        onBack = { navigator.back() },
                        onOpenShop = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onOpenFeast = { feastId -> navigator.navigate(Route.FeastDetails(feastId)) },
                        onExploreMap = { navigator.backToHome() },
                    )
                    Route.MyFeasts -> MyFeastsScreen(
                        onBack = { navigator.back() },
                        onOpenFeast = { feastId -> navigator.navigate(Route.FeastDetails(feastId)) },
                        onEdit = { feastId -> navigator.navigate(Route.EditFeast(feastId)) },
                        onRepost = { feastId -> navigator.navigate(Route.RepostFeast(feastId)) },
                        onResubmit = { feastId -> navigator.navigate(Route.ResubmitFeast(feastId)) },
                        onPostFeast = { navigator.navigate(Route.ReportFeast) },
                        onContactSupport = { navigator.navigate(Route.Support) },
                    )
                    is Route.EditFeast -> ReportFeastScreen(
                        onBack = { navigator.back() },
                        // Back to the list, which shows the change
                        onPosted = { navigator.back() },
                        start = FeastFormStart.Edit(route.feastId),
                    )
                    is Route.ResubmitFeast -> ReportFeastScreen(
                        onBack = { navigator.back() },
                        // Back to the list, which shows it waiting for a check
                        onPosted = { navigator.back() },
                        start = FeastFormStart.Resubmit(route.feastId),
                    )
                    is Route.RepostFeast -> ReportFeastScreen(
                        onBack = { navigator.back() },
                        onPosted = { feastId -> navigator.replace(Route.FeastDetails(feastId)) },
                        start = FeastFormStart.Repost(route.feastId),
                    )
                    Route.MyPhotos -> MyPhotosScreen(
                        onBack = { navigator.back() },
                        onOpenShop = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onEditReview = { review -> navigator.navigate(Route.WriteReview(review.shopId, review.shopName.orEmpty(), review.shopImageUrl)) },
                        onFindShop = { navigator.backToHome() },
                    )
                    Route.MyReviews -> MyReviewsScreen(
                        onBack = { navigator.back() },
                        onOpenShop = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                        onEdit = { review -> navigator.navigate(Route.WriteReview(review.shopId, review.shopName.orEmpty(), review.shopImageUrl)) },
                        onFindShop = { navigator.backToHome() },
                    )
                    is Route.ManageShop -> ManageShopScreen(
                        shopId = route.shopId,
                        onBack = { navigator.back() },
                        onViewShop = { navigator.navigate(Route.ShopDetails(route.shopId)) },
                        onOpenReviews = { shopName, photoUrl -> navigator.navigate(Route.ShopReviews(route.shopId, shopName, photoUrl)) },
                        onDeactivated = { navigator.back() },
                    )
                    Route.About -> AboutScreen(onBack = { navigator.back() })
                    Route.Support -> SupportScreen(
                        onBack = { navigator.back() },
                        onReportIssue = { navigator.navigate(Route.ReportIssue) },
                        onSuggestFeature = { navigator.navigate(Route.SuggestFeature) },
                        onHowToUse = { navigator.navigate(Route.HowToUse) },
                    )
                    Route.ReportIssue -> ReportIssueScreen(onBack = { navigator.back() })
                    Route.SuggestFeature -> SuggestFeatureScreen(onBack = { navigator.back() })
                    Route.HowToUse -> HowToUseScreen(onBack = { navigator.back() })
                    Route.Terms -> TermsScreen(onBack = { navigator.back() })
                    is Route.ShopDetails -> ShopDetailsScreen(
                        shopId = route.shopId,
                        onBack = { navigator.back() },
                        onOpenReviews = { shopName, photoUrl -> navigator.navigate(Route.ShopReviews(route.shopId, shopName, photoUrl)) },
                        onWriteReview = { shopName, photoUrl -> navigator.navigate(Route.WriteReview(route.shopId, shopName, photoUrl)) },
                        onClaim = { shopName -> navigator.navigate(Route.ClaimShop(route.shopId, shopName)) },
                    )
                    is Route.ShopReviews -> ShopReviewsScreen(
                        shopId = route.shopId,
                        shopName = route.shopName,
                        photoUrl = route.photoUrl,
                        onBack = { navigator.back() },
                        onWriteReview = { rating -> navigator.navigate(Route.WriteReview(route.shopId, route.shopName, route.photoUrl, rating)) },
                    )
                    is Route.WriteReview -> WriteReviewScreen(
                        shopId = route.shopId,
                        shopName = route.shopName,
                        photoUrl = route.photoUrl,
                        rating = route.rating,
                        onClose = { navigator.back() },
                    )
                }
                }
                }
            }
          }
        }
    }
}
