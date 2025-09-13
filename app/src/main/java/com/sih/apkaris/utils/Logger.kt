package com.sih.apkaris.utils

import android.util.Log

object Logger {
    private const val TAG = "APkARiS"
    fun d(msg: String) = Log.d(TAG, msg)
    fun e(msg: String, t: Throwable? = null) = Log.e(TAG, msg, t)
}
