package com.studysync.sa.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

/**
 * Utility for dynamically wrapping application context with selected locale (English, isiZulu, Afrikaans, isiXhosa).
 */
object LocaleHelper {

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode.lowercase()) {
            "isizulu", "zu" -> Locale.Builder().setLanguage("zu").build()
            "afrikaans", "af" -> Locale.Builder().setLanguage("af").build()
            "isixhosa", "xh" -> Locale.Builder().setLanguage("xh").build()
            else -> Locale.Builder().setLanguage("en").build()
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration).apply {
            setLocales(LocaleList(locale))
        }

        return context.createConfigurationContext(config)
    }

    fun getLanguageCode(languageDisplayName: String): String {
        return when (languageDisplayName.lowercase()) {
            "isizulu" -> "zu"
            "afrikaans" -> "af"
            "isixhosa" -> "xh"
            else -> "en"
        }
    }
}
