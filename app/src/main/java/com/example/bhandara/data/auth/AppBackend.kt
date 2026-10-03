package com.example.bhandara.data.auth

import android.content.Context
import com.example.bhandara.BuildConfig
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.push.PushTokenSync

/** The shared backend client on Android: one per process, sending the signed-in user's token */
object AppBackend {

    val client: ApiClient by lazy { ApiClient(BuildConfig.API_BASE_URL, AppAuth.service::idToken) }

    val userApi: UserApi by lazy { UserApi(client) }

    fun pushTokenSync(context: Context) = PushTokenSync(userApi, AppSettings(context))
}
