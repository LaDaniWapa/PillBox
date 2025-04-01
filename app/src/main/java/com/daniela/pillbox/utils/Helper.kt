package com.daniela.pillbox.utils

import android.content.Context
import com.daniela.pillbox.R
import io.appwrite.exceptions.AppwriteException
import java.io.IOException

class Helper(private val ctx: Context) {
    fun handleRegistrationError(e: Exception): String {
        return when (e) {
            is AppwriteException -> {
                when (e.code?.toInt()) {
                    400 -> ctx.getString(R.string.error_400)
                    401 -> ctx.getString(R.string.error_401)
                    409 -> ctx.getString(R.string.error_409)
                    500 -> ctx.getString(R.string.error_500)
                    else -> ctx.getString(R.string.error_unknown, e.message)
                }
            }

            is IOException -> ctx.getString(R.string.error_network)
            else -> ctx.getString(R.string.error_unknown)
        }
    }
}