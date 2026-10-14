package com.example.bhandara.photos

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Uploading a post's photos, the same on Android and iOS: all of them or none. Storage is a fake. */
class PhotoUploaderTest {

    private class FakePhoto(val name: String, val readable: Boolean = true) : PickedPhoto {
        override val preview: Any = name
        override suspend fun compressed() =
            if (readable) CompressedPhoto(name.encodeToByteArray(), "webp", "image/webp") else null
    }

    /** Uploads instantly with progress halfway, unless the photo's name is in [failing] */
    private class FakeStorage(val failing: Set<String> = emptySet()) : PlatformStorage {
        val uploads = mutableListOf<Triple<String, String, String>>() // path, content, type
        val deleted = mutableListOf<String>()

        override fun upload(
            path: String, bytes: ByteArray, contentType: String,
            onProgress: (Float) -> Unit, onResult: (url: String?, error: String?) -> Unit,
        ) {
            val name = bytes.decodeToString()
            uploads += Triple(path, name, contentType)
            onProgress(0.5f)
            if (name in failing) onResult(null, "Permission denied") else onResult("https://img.test/$path", null)
        }

        override fun delete(path: String) {
            deleted += path
        }
    }

    private var ids = 0
    private fun uploader(storage: PlatformStorage) = PhotoUploader(storage, folder = "photos", newId = { "id${++ids}" })

    @Test
    fun photosAreUploadedInOrderAndTheirLinksReturned() = runTest {
        val storage = FakeStorage()

        val urls = uploader(storage).upload(listOf(FakePhoto("a"), FakePhoto("b")))

        assertEquals(listOf("https://img.test/photos/id1.webp", "https://img.test/photos/id2.webp"), urls)
        assertEquals(listOf("a", "b"), storage.uploads.map { it.second })
        assertTrue(storage.uploads.all { it.third == "image/webp" })
    }

    @Test
    fun eachPhotoGetsItsOwnFileSoNothingIsOverwritten() = runTest {
        val storage = FakeStorage()

        uploader(storage).upload(listOf(FakePhoto("a"), FakePhoto("b"), FakePhoto("c")))

        val paths = storage.uploads.map { it.first }
        assertEquals(paths.size, paths.toSet().size)
        assertTrue(paths.all { it.startsWith("photos/") })
    }

    @Test
    fun progressGoesFrom0To100WithoutGoingBack() = runTest {
        val progress = mutableListOf<Int>()

        uploader(FakeStorage()).upload(listOf(FakePhoto("a"), FakePhoto("b"))) { progress += it }

        assertEquals(0, progress.first())
        assertEquals(100, progress.last())
        assertEquals(progress.sorted(), progress)
        assertTrue(25 in progress && 50 in progress && 75 in progress, "was $progress")
    }

    @Test
    fun ifOnePhotoFailsNoneAreKept() = runTest {
        // A post with a missing photo would look complete to the user; better to say it failed
        val storage = FakeStorage(failing = setOf("b"))

        val error = assertFailsWith<PhotoUploadException> {
            uploader(storage).upload(listOf(FakePhoto("a"), FakePhoto("b"), FakePhoto("c")))
        }

        assertEquals("Permission denied", error.message)
        assertEquals(listOf("photos/id1.webp"), storage.deleted)
        assertEquals(listOf("a", "b"), storage.uploads.map { it.second }) // "c" is never sent
    }

    @Test
    fun anUnreadablePhotoStopsTheUpload() = runTest {
        val storage = FakeStorage()

        assertFailsWith<PhotoUploadException> {
            uploader(storage).upload(listOf(FakePhoto("a"), FakePhoto("broken", readable = false)))
        }

        assertEquals(listOf("photos/id1.webp"), storage.deleted)
    }

    @Test
    fun noPhotosMeansNothingToDo() = runTest {
        val storage = FakeStorage()

        assertEquals(emptyList(), uploader(storage).upload(emptyList()))
        assertTrue(storage.uploads.isEmpty())
    }

    @Test
    fun photosGoWhereTheAppHasAlwaysPutThem() {
        // Firebase Storage rules allow uploads to this folder
        assertEquals("feast_images", PhotoUploader.PHOTO_FOLDER)
    }
}
