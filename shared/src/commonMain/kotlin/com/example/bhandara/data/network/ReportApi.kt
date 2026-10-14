package com.example.bhandara.data.network

import com.example.bhandara.data.models.api.MyReport
import com.example.bhandara.data.models.api.ReportDetails
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

/** The reports the signed-in user sent about shops, reviews and feasts */
class ReportApi(private val client: ApiClient) {

    /** Newest first, each with where it stands */
    suspend fun getMyReports(): ApiResult<List<MyReport>> =
        client.call("api/reports/mine") { method = HttpMethod.Get }

    /** "Add more info": a note, photos or both on a report still in review; returns it with everything said */
    suspend fun addInfo(report: MyReport, details: ReportDetails): ApiResult<MyReport> =
        client.call("api/reports/${report.kind}/${report.reportId}/info") {
            method = HttpMethod.Post
            setBody(details)
        }

    /** Takes back a report still in review; it stops counting and can be sent again later */
    suspend fun withdraw(report: MyReport): ApiResult<Unit> =
        client.callForNothing("api/reports/${report.kind}/${report.reportId}") { method = HttpMethod.Delete }
}
