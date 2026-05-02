package com.example.accountbook.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

object WidgetPrefsManager {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_WIDGET_COLOR = "widget_color"
    private const val KEY_WIDGET_TEXT = "widget_text"
    val DEFAULT_COLOR = Color.parseColor("#6200EE")
    const val DEFAULT_TEXT = "记一笔"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getWidgetColor(context: Context): Int =
        prefs(context).getInt(KEY_WIDGET_COLOR, DEFAULT_COLOR)

    fun setWidgetColor(context: Context, color: Int) =
        prefs(context).edit().putInt(KEY_WIDGET_COLOR, color).apply()

    fun getWidgetText(context: Context): String =
        prefs(context).getString(KEY_WIDGET_TEXT, DEFAULT_TEXT) ?: DEFAULT_TEXT

    fun setWidgetText(context: Context, text: String) =
        prefs(context).edit().putString(KEY_WIDGET_TEXT, text).apply()
}
