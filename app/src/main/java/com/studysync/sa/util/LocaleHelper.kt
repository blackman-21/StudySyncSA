package com.studysync.sa.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Utility for dynamically wrapping application context with selected locale (English, isiZulu, Afrikaans, isiXhosa).
 */
object LocaleHelper {

    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode.lowercase()) {
            "isizulu", "zu" -> Locale("zu")
            "afrikaans", "af" -> Locale("af")
            "isixhosa", "xh" -> Locale("xh")
            else -> Locale("en")
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
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
