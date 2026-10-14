package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.BugReportRequest
import com.example.bhandara.data.models.api.FeatureSuggestionRequest
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** Users telling the team about problems and ideas */
class SupportApi(private val client: ApiClient) {

    suspend fun reportBug(description: String): ApiResult<Unit> =
        client.callForNothing("api/support/bugs") {
            method = HttpMethod.Post
            setBody(BugReportRequest(bug = description))
        }

    suspend fun suggestFeature(description: String): ApiResult<Unit> =
        client.callForNothing("api/support/features") {
            method = HttpMethod.Post
            setBody(FeatureSuggestionRequest(feature = description))
        }
}
