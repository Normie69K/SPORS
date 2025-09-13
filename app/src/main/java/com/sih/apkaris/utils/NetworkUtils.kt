package com.sih.apkaris.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

object NetworkUtils {

    private val client = OkHttpClient()

    fun sendDataToServer(json: String, url: String, callback: Callback) {
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }
}
