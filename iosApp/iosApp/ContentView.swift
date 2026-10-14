import SwiftUI
import Shared

/// Shows the shared Compose UI (from shared/src/iosMain/.../MainViewController.kt) full screen.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            auth: IosPlatformAuth(),
            mapBridge: IosGoogleMapBridge(),
            apiBaseUrl: Bundle.main.object(forInfoDictionaryKey: "ApiBaseUrl") as? String ?? "",
            storage: IosFirebaseStorage(),
            googleSignIn: IosGoogleSignIn()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Edge to edge, as on Android: the map and the side menu's header run under the status bar, and each
        // screen keeps its own content clear of the notch and home indicator
        ComposeView()
            .ignoresSafeArea()
    }
}
