package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.FssaiRead
import com.example.bhandara.data.models.api.FssaiStatus
import com.example.bhandara.photos.CompressedPhoto
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

/**
 * A food shop's FSSAI licence: AI reads the number and expiry off the certificate for the owner to check, then the
 * owner sends it with the certificate (private, for the moderators) to be checked before the badge shows.
 */
class FssaiApi(private val client: ApiClient) {

    /** Reads 1–2 pages (photos or a PDF). Paid per request, so limited to a few in a row (429 after that) */
    suspend fun read(pages: List<CompressedPhoto>): ApiResult<FssaiRead> =
        client.call("api/fssai/read") {
            method = HttpMethod.Post
            // The form sets its own content type (multipart, with a boundary) instead of the client's JSON default
            headers.remove(HttpHeaders.ContentType)
            setBody(MultiPartFormDataContent(formData { appendPages(pages) }))
        }

    /** Sends the owner's licence for checking; [validTill] is yyyy-MM-dd */
    suspend fun submit(shopId: String, licenceNumber: String, validTill: String, pages: List<CompressedPhoto>): ApiResult<FssaiStatus> =
        client.call("api/fssai/shops/$shopId") {
            method = HttpMethod.Post
            headers.remove(HttpHeaders.ContentType)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("licenceNumber", licenceNumber)
                        append("validTill", validTill)
                        appendPages(pages)
                    }
                )
            )
        }

    /** The latest licence the owner sent for their shop; null when none was ever sent */
    suspend fun mine(shopId: String): ApiResult<FssaiStatus?> =
        client.send("api/fssai/shops/$shopId/mine", { method = HttpMethod.Get }) { response ->
            if (response.status == HttpStatusCode.NoContent) null else client.json.decodeFromString<FssaiStatus>(response.bodyAsText())
        }

    private fun io.ktor.client.request.forms.FormBuilder.appendPages(pages: List<CompressedPhoto>) {
        pages.forEachIndexed { index, page ->
            append(
                "pages",
                page.bytes,
                Headers.build {
                    append(HttpHeaders.ContentType, page.contentType)
                    append(HttpHeaders.ContentDisposition, "filename=\"certificate_${index + 1}.${page.extension}\"")
                },
            )
        }
    }
}
