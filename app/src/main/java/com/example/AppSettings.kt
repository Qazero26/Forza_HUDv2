package com.example

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("forzahud_prefs", Context.MODE_PRIVATE)

    var isMph: Boolean
        get() = prefs.getBoolean("is_mph", false)
        set(value) = prefs.edit().putBoolean("is_mph", value).apply()

    var themeOrdinal: Int
        get() = prefs.getInt("theme_ordinal", 0)
        set(value) = prefs.edit().putInt("theme_ordinal", value).apply()
        
    var layoutOrdinal: Int
        get() = prefs.getInt("layout_ordinal", 0)
        set(value) = prefs.edit().putInt("layout_ordinal", value).apply()
}
