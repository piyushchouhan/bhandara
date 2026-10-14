package com.example.bhandara.data.storage

import com.example.bhandara.photos.PlatformStorage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata

/** Firebase Storage on Android, for the shared photo uploader */
class AndroidFirebaseStorage(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) : PlatformStorage {

    override fun upload(
        path: String,
        bytes: ByteArray,
        contentType: String,
        onProgress: (Float) -> Unit,
        onResult: (url: String?, error: String?) -> Unit,
    ) {
        val file = storage.reference.child(path)
        file.putBytes(bytes, StorageMetadata.Builder().setContentType(contentType).build())
            .addOnProgressListener { task ->
                if (task.totalByteCount > 0) onProgress(task.bytesTransferred.toFloat() / task.totalByteCount)
            }
            .continueWithTask { task ->
                task.exception?.let { throw it }
                file.downloadUrl
            }
            .addOnSuccessListener { url -> onResult(url.toString(), null) }
            .addOnFailureListener { error -> onResult(null, error.message) }
    }

    override fun delete(path: String) {
        storage.reference.child(path).delete()
    }
}
