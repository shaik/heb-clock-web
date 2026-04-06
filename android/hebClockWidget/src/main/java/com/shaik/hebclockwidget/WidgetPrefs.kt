package com.shaik.hebclockwidget

import android.content.Context

object WidgetPrefs {
    private const val NAME = "widget_prefs"

    // ── Theme ─────────────────────────────────────────────────────────────────

    fun theme(ctx: Context): String =
        prefs(ctx).getString("theme", "dark") ?: "dark"

    fun setTheme(ctx: Context, v: String) =
        prefs(ctx).edit().putString("theme", v).apply()

    // ── Suffix master toggle ──────────────────────────────────────────────────

    /** Master on/off for all day-part suffixes. */
    fun showSuffix(ctx: Context): Boolean =
        prefs(ctx).getBoolean("show_suffix", true)

    fun setShowSuffix(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("show_suffix", v).apply()

    // ── Per-slot suffix configuration ─────────────────────────────────────────

    /**
     * Returns the 7 suffix slots, merging stored overrides on top of
     * [HebTime.SUFFIX_DEFAULTS]. Only [enabled], [from], and [until] are
     * persisted; the text strings always come from the defaults.
     */
    fun suffixSlots(ctx: Context): List<HebTime.SuffixSlot> {
        val p = prefs(ctx)
        return HebTime.SUFFIX_DEFAULTS.mapIndexed { i, def ->
            HebTime.SuffixSlot(
                enabled = p.getBoolean("suffix_${i}_enabled", def.enabled),
                from    = p.getInt("suffix_${i}_from",    def.from),
                until   = p.getInt("suffix_${i}_until",   def.until),
                plain   = def.plain,
                niqqud  = def.niqqud
            )
        }
    }

    fun setSuffixSlot(ctx: Context, index: Int, slot: HebTime.SuffixSlot) {
        prefs(ctx).edit()
            .putBoolean("suffix_${index}_enabled", slot.enabled)
            .putInt("suffix_${index}_from",  slot.from)
            .putInt("suffix_${index}_until", slot.until)
            .apply()
    }

    // ── Other settings ────────────────────────────────────────────────────────

    fun showModifier(ctx: Context): Boolean =
        prefs(ctx).getBoolean("show_modifier", true)

    fun setShowModifier(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("show_modifier", v).apply()

    fun fontSize(ctx: Context): Int =
        prefs(ctx).getInt("font_size", 26)

    fun setFontSize(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt("font_size", v).apply()

    fun useNiqqud(ctx: Context): Boolean =
        prefs(ctx).getBoolean("use_niqqud", true)

    fun setUseNiqqud(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("use_niqqud", v).apply()

    fun compactLabels(ctx: Context): Boolean =
        prefs(ctx).getBoolean("compact_labels", false)

    fun setCompactLabels(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean("compact_labels", v).apply()

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
