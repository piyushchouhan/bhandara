import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // Reads GoogleService-Info.plist (Firebase project bhandara-e3435, the same one as Android)
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
