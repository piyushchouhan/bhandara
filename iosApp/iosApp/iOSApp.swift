import SwiftUI
import FirebaseCore
import FirebaseCrashlytics
import Shared
import GoogleMaps
import GoogleSignIn

@main
struct iOSApp: App {
    init() {
        // Reads GoogleService-Info.plist (Firebase project bhandara-e3435, the same one as Android)
        FirebaseApp.configure()
        // Crash reports come from testers' release builds only, not from development in Xcode
        #if DEBUG
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(false)
        #endif
        CrashReportingKt.startCrashReporting()
        // From Config/Secrets.xcconfig via Info.plist (restricted to this app and the Maps SDK for iOS)
        if let key = Bundle.main.object(forInfoDictionaryKey: "GoogleMapsApiKey") as? String, !key.isEmpty {
            GMSServices.provideAPIKey(key)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Google's sign-in page returns to the app through its URL scheme (REVERSED_CLIENT_ID)
                .onOpenURL { url in _ = GIDSignIn.sharedInstance.handle(url) }
        }
    }
}
