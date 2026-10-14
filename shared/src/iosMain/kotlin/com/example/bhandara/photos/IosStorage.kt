package com.example.bhandara.photos

import platform.Foundation.NSData

/**
 * Firebase Storage on iOS, implemented in Swift (IosFirebaseStorage.swift) with the Firebase SDK. Takes NSData so
 * Swift gets the photo without copying it byte by byte.
 */
interface IosStorageBridge {
    fun upload(path: String, data: NSData, contentType: String, onProgress: (Double) -> Unit, onResult: (String?, String?) -> Unit)
    fun delete(path: String)
}

/** The shared [PlatformStorage] on top of the Swift bridge */
class IosPlatformStorage(private val bridge: IosStorageBridge) : PlatformStorage {

    override fun upload(
        path: String,
        bytes: ByteArray,
        contentType: String,
        onProgress: (Float) -> Unit,
        onResult: (url: String?, error: String?) -> Unit,
    ) = bridge.upload(path, bytes.toNSData(), contentType, { onProgress(it.toFloat()) }, onResult)

    override fun delete(path: String) = bridge.delete(path)
}

