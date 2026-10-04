import FirebaseAuth
import Shared

/// Firebase Authentication on iOS, for the shared AuthService (shared/src/commonMain/.../auth).
final class IosPlatformAuth: PlatformAuth {

    func currentUid() -> String? {
        Auth.auth().currentUser?.uid
    }

    func currentAccount() -> UserAccount? {
        guard let user = Auth.auth().currentUser else { return nil }
        return UserAccount(
            uid: user.uid,
            isAnonymous: user.isAnonymous,
            name: user.displayName,
            email: user.email,
            photoUrl: user.photoURL?.absoluteString
        )
    }

    func signInAnonymously(onResult: @escaping (String?, String?) -> Void) {
        Auth.auth().signInAnonymously { result, error in
            onResult(result?.user.uid, error?.localizedDescription)
        }
    }

    func idToken(forceRefresh: Bool, onResult: @escaping (String?, String?) -> Void) {
        guard let user = Auth.auth().currentUser else {
            onResult(nil, "Not signed in")
            return
        }
        user.getIDTokenForcingRefresh(forceRefresh) { token, error in
            onResult(token, error?.localizedDescription)
        }
    }
}
