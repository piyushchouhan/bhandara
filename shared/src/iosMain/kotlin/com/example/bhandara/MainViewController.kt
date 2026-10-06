package com.example.bhandara

import com.example.bhandara.ui.addshop.AddShopScreen
import com.example.bhandara.ui.drawer.AboutScreen
import com.example.bhandara.ui.drawer.AppDrawer
import com.example.bhandara.ui.drawer.SupportScreen
import com.example.bhandara.ui.drawer.TermsScreen
import com.example.bhandara.ui.profile.ProfileScreen
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.bhandara.ui.feasts.FeastDetailsScreen
import com.example.bhandara.ui.reportfeast.ReportFeastScreen
import platform.Foundation.NSBundle
import com.example.bhandara.ui.feasts.FeastsMapScreen
import com.example.bhandara.ui.shopdetails.ShopDetailsScreen
import com.example.bhandara.ui.shopsmap.ShopsMapScreen
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.AppDependencies
import com.example.bhandara.data.settings.createAppSettings
import com.example.bhandara.data.network.ApiClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.example.bhandara.auth.AuthService
import com.example.bhandara.auth.PlatformAuth
import com.example.bhandara.navigation.Route
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.coming_soon_text
import com.example.bhandara.shared.resources.coming_soon_title
import com.example.bhandara.shared.resources.feature_claim_shop
import com.example.bhandara.shared.resources.feature_this
import org.jetbrains.compose.resources.stringResource
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
import com.example.bhandara.ui.screens.HomeScreen
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
        ) {
            BhandaraTheme {
                val navigator = rememberAppNavigator()
                var homeTabIndex by remember { mutableIntStateOf(0) }

                // Each screen keeps its view models while open; swiping from the left edge goes back, as on iPhone
                Box(Modifier.fillMaxSize().swipeFromEdgeToGoBack(navigator.canGoBack) { navigator.back() }) {
                NavEntries(navigator) { route ->
                when (route) {
                    Route.Home -> AppDrawer(
                        onProfileClick = { navigator.navigate(Route.Profile) },
                        onAboutClick = { navigator.navigate(Route.About) },
                        onSupportClick = { navigator.navigate(Route.Support) },
                        onTermsClick = { navigator.navigate(Route.Terms) },
                    ) {
                        HomeScreen(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                            selectedTabIndex = homeTabIndex,
                            onTabSelected = { homeTabIndex = it },
                            onHungryClick = { navigator.navigate(Route.Hungry) },
                            onReportFeastClick = { navigator.navigate(Route.ReportFeast) },
                            onFindShopsClick = { navigator.navigate(Route.ShopsMap) },
                            onAddShopClick = { navigator.navigate(Route.AddShop) },
                        )
                    }
                    Route.ShopsMap -> ShopsMapScreen(
                        onBack = { navigator.back() },
                        onShopClick = { shopId -> navigator.navigate(Route.ShopDetails(shopId)) },
                    )
                    Route.Hungry -> FeastsMapScreen(
                        onBack = { navigator.back() },
                        onFeastClick = { feastId -> navigator.navigate(Route.FeastDetails(feastId)) },
                    )
                    is Route.FeastDetails -> FeastDetailsScreen(feastId = route.feastId, onBack = { navigator.back() })
                    Route.ReportFeast -> ReportFeastScreen(
                        onBack = { navigator.back() },
                        onPosted = { feastId -> navigator.replace(Route.FeastDetails(feastId)) },
                    )
                    Route.AddShop -> AddShopScreen(
                        onBack = { navigator.back() },
                        onAdded = { shopId -> navigator.replace(Route.ShopDetails(shopId)) },
                    )
                    // Claiming comes to iPhone with the other write flows (phase 2)
                    Route.Profile -> ProfileScreen(onBack = { navigator.back() })
                    Route.About -> AboutScreen(onBack = { navigator.back() })
                    Route.Support -> SupportScreen(onBack = { navigator.back() })
                    Route.Terms -> TermsScreen(onBack = { navigator.back() })
                    is Route.ShopDetails -> ShopDetailsScreen(shopId = route.shopId, onBack = { navigator.back() })
                    // Screens move to the shared module one by one; until then iOS says so
                    else -> NotOnIphoneYet(route, onBack = { navigator.back() })
                }
                }
                }
            }
        }
    }
}

/** Screens that come to iPhone with the other write flows (phase 2); says so instead of showing nothing */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotOnIphoneYet(route: Route, onBack: () -> Unit) {
    val feature = stringResource(
        when (route) {
            is Route.ClaimShop -> Res.string.feature_claim_shop
            else -> Res.string.feature_this
        }
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.coming_soon_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.coming_soon_text, feature),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
