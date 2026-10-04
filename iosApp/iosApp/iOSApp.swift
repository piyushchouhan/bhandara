import SwiftUI
import FirebaseCore
import GoogleMaps

@main
struct iOSApp: App {
    init() {
        // Reads GoogleService-Info.plist (Firebase project bhandara-e3435, the same one as Android)
        FirebaseApp.configure()
        // From Config/Secrets.xcconfig via Info.plist (restricted to this app and the Maps SDK for iOS)
        if let key = Bundle.main.object(forInfoDictionaryKey: "GoogleMapsApiKey") as? String, !key.isEmpty {
            GMSServices.provideAPIKey(key)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
