package com.shaik.hebclockwidget

import android.content.Context

object WidgetPrefs {
    private const val NAME = "widget_prefs"

    fun theme(ctx: Context): String =
        prefs(ctx).getString("theme", "dark") ?: "dark"

    fun setTheme(ctx: Context, v: String) =
        prefs(ctx).edit().putString("theme", v).apply()

    fun showSuffix(ctx: Context): Boolean =
        prefs(ctx).getBoolean("show_suffix", true)

    fun setShowSuffix(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("show_suffix", v).apply()

    fun showModifier(ctx: Context): Boolean =
        prefs(ctx).getBoolean("show_modifier", true)

    fun setShowModifier(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("show_modifier", v).apply()

    fun fontSize(ctx: Context): Int =
        prefs(ctx).getInt("font_size", 26)

    fun setFontSize(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt("font_size", v).apply()

    fun useNiqqud(ctx: Context): Boolean =
        prefs(ctx).getBoolean("use_niqqud", false)

    fun setUseNiqqud(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("use_niqqud", v).apply()

    fun compactLabels(ctx: Context): Boolean =
        prefs(ctx).getBoolean("compact_labels", false)

    fun setCompactLabels(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("compact_labels", v).apply()

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
