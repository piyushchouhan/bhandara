import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import Shared
import UIKit

/// Google sign-in on iOS, for the shared profile and sign-in gate (shared/src/iosMain/.../auth/IosGoogleSignIn.kt).
/// The guest (anonymous) account is upgraded to the Google one, so the user keeps what they did as a guest; if that
/// Google account is already in use, the user switches to it, as on Android.
final class IosGoogleSignIn: IosGoogleSignInBridge {

    func signIn(onResult: @escaping (GoogleSignInResult, String?) -> Void) {
        // The iOS OAuth client from GoogleService-Info.plist, which Firebase has read
        guard let clientID = FirebaseApp.app()?.options.clientID, let presenter = Self.topViewController() else {
            onResult(.failed, "Google sign-in isn't set up")
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as NSError? {
                let cancelled = error.domain == kGIDSignInErrorDomain && error.code == GIDSignInError.canceled.rawValue
                onResult(cancelled ? .cancelled : .failed, error.localizedDescription)
                return
            }
            guard let user = result?.user, let idToken = user.idToken?.tokenString else {
                onResult(.failed, "Google didn't return an ID token")
                return
            }
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: user.accessToken.tokenString)
            Self.signInToFirebase(with: credential, onResult: onResult)
        }
    }

    func signOut(onResult: @escaping (String?) -> Void) {
        GIDSignIn.sharedInstance.signOut()
        do {
            try Auth.auth().signOut()
            onResult(nil)
        } catch {
            onResult(error.localizedDescription)
        }
    }

    private static func signInToFirebase(with credential: AuthCredential, onResult: @escaping (GoogleSignInResult, String?) -> Void) {
        let finish: (Error?) -> Void = { error in
            if let error {
                onResult(.failed, error.localizedDescription)
                return
            }
            // Loads the account's name, email and photo for the profile
            Auth.auth().currentUser?.reload { _ in onResult(.signedIn, nil) }
        }
        guard let guest = Auth.auth().currentUser, guest.isAnonymous else {
            Auth.auth().signIn(with: credential) { _, error in finish(error) }
            return
        }
        guest.link(with: credential) { _, error in
            if let error = error as NSError?, error.code == AuthErrorCode.credentialAlreadyInUse.rawValue {
                // That Google account already exists: switch to it
                let existing = error.userInfo[AuthErrorUserInfoUpdatedCredentialKey] as? AuthCredential ?? credential
                Auth.auth().signIn(with: existing) { _, error in finish(error) }
            } else {
                finish(error)
            }
        }
    }

    private static func topViewController() -> UIViewController? {
        let window = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow }
            .first
        var top = window?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
