package com.daniela.pillbox.utils

import android.content.Context
import com.daniela.pillbox.R
import io.appwrite.exceptions.AppwriteException
import java.io.IOException
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

/**
 * Helper class with utility functions.
 */
class Helper(private val ctx: Context) {
    /**
     * Handles registration errors.
     */
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

fun getLocalizedWeekDayName(
    dayIndex: Int,
    locale: Locale = Locale.getDefault(),
    short: Boolean = false,
): String {
    val dayOfWeek = DayOfWeek.of(((dayIndex + 1) % 7).let { if (it == 0) 7 else it })
    return dayOfWeek.getDisplayName(if (short) TextStyle.SHORT else TextStyle.FULL, locale)
}

fun formatDayList(
    dayIndices: List<Int>,
    locale: Locale = Locale.getDefault(),
    finalSeparator: String = "and",
): String {
    if (dayIndices.isEmpty()) return ""
    if (dayIndices.size == 7) return "Everyday"

    val dayNames =
        dayIndices.sorted().map { getLocalizedWeekDayName(it, locale = locale, short = true) }

    return when (dayNames.size) {
        1 -> dayNames.first()
        else -> {
            val allButLast = dayNames.dropLast(1).joinToString(", ")
            "$allButLast $finalSeparator ${dayNames.last()}"
        }
    }
}

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 * https://stackoverflow.com/questions/67843986/is-there-a-shorter-replacement-for-kotlins-deprecated-string-capitalize-funct
 */
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}
