import FirebaseStorage
import Foundation
import Shared

/// Firebase Storage on iOS, for the shared photo uploader (shared/src/iosMain/.../photos/IosStorage.kt).
final class IosFirebaseStorage: IosStorageBridge {

    func upload(
        path: String,
        data: Data,
        contentType: String,
        onProgress: @escaping (KotlinDouble) -> Void,
        onResult: @escaping (String?, String?) -> Void
    ) {
        let file = Storage.storage().reference(withPath: path)
        let metadata = StorageMetadata()
        metadata.contentType = contentType
        let task = file.putData(data, metadata: metadata) { _, error in
            if let error {
                onResult(nil, error.localizedDescription)
                return
            }
            file.downloadURL { url, error in
                onResult(url?.absoluteString, url == nil ? (error?.localizedDescription ?? "No download link") : nil)
            }
        }
        task.observe(.progress) { snapshot in
            if let progress = snapshot.progress, progress.totalUnitCount > 0 {
                onProgress(KotlinDouble(value: Double(progress.completedUnitCount) / Double(progress.totalUnitCount)))
            }
        }
    }

    func delete(path: String) {
        Storage.storage().reference(withPath: path).delete { _ in }
    }
}
