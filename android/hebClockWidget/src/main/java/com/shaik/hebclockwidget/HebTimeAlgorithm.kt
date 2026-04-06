package com.shaik.hebclockwidget

import java.util.Calendar
import kotlin.math.roundToInt

object HebTime {

    // ── Suffix configuration ──────────────────────────────────────────────────

    data class SuffixSlot(
        val enabled: Boolean,
        val from:    Int,   // 0-23 inclusive
        val until:   Int,   // 0-23 exclusive (wraps if until <= from)
        val plain:   String,
        val niqqud:  String
    )

    /** Defaults mirror the web app defaults in index.html SUFFIX_DEFS. */
    val SUFFIX_DEFAULTS = listOf(
        SuffixSlot(false,  4,  5, "לפנות בוקר",    "לִפְנוֹת בֹּקֶר"),
        SuffixSlot(true,   5, 10, "בבוקר",           "בַּבֹּקֶר"),
        SuffixSlot(false, 12, 14, "בצהריים",         "בַּצָּהֳרַיִם"),
        SuffixSlot(false, 16, 18, "אחרי הצהריים",    "אַחֲרֵי הַצָּהֳרַיִם"),
        SuffixSlot(false, 17, 18, "לפנות ערב",       "לִפְנוֹת עֶרֶב"),
        SuffixSlot(true,  19, 20, "בערב",            "בָּעֶרֶב"),
        SuffixSlot(true,  22,  5, "בלילה",           "בַּלַּיְלָה")
    )

    // ── Plain (no niqqud) ─────────────────────────────────────────────────────

    private val hours = mapOf(
        1 to "אחת",        2 to "שתיים",       3 to "שלוש",
        4 to "ארבע",       5 to "חמש",          6 to "שש",
        7 to "שבע",        8 to "שמונה",        9 to "תשע",
        10 to "עשר",       11 to "אחת עשרה",   12 to "שתים עשרה"
    )
    private val hoursL = mapOf(
        1 to "לאחת",       2 to "לשתיים",      3 to "לשלוש",
        4 to "לארבע",      5 to "לחמש",         6 to "לשש",
        7 to "לשבע",       8 to "לשמונה",       9 to "לתשע",
        10 to "לעשר",      11 to "לאחת עשרה",  12 to "לשתים עשרה"
    )

    // ── Niqqud ────────────────────────────────────────────────────────────────

    private val hoursN = mapOf(
        1 to "אַחַת",       2 to "שְׁתַּיִם",     3 to "שָׁלוֹשׁ",
        4 to "אַרְבַּע",    5 to "חָמֵשׁ",         6 to "שֵׁשׁ",
        7 to "שֶׁבַע",      8 to "שְׁמוֹנֶה",     9 to "תֵּשַׁע",
        10 to "עֶשֶׂר",    11 to "אַחַת עֶשְׂרֵה", 12 to "שְׁתֵּים עֶשְׂרֵה"
    )
    private val hoursLN = mapOf(
        1 to "לְאַחַת",    2 to "לִשְׁתַּיִם",   3 to "לְשָׁלוֹשׁ",
        4 to "לְאַרְבַּע", 5 to "לְחָמֵשׁ",       6 to "לְשֵׁשׁ",
        7 to "לְשֶׁבַע",   8 to "לִשְׁמוֹנֶה",   9 to "לְתֵשַׁע",
        10 to "לְעֶשֶׂר",  11 to "לְאַחַת עֶשְׂרֵה", 12 to "לִשְׁתֵּים עֶשְׂרֵה"
    )

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun to12(h24: Int): Int {
        val h = ((h24 % 24) + 24) % 24 % 12
        return if (h == 0) 12 else h
    }

    // ── Phrase builder ────────────────────────────────────────────────────────

    fun buildPhrase(anchorHour24: Int, anchorMinute: Int, niqqud: Boolean = false): String {
        val h  = to12(anchorHour24)
        val hh = if (niqqud) hoursN[h]!! else hours[h]!!
        val hl = if (niqqud) hoursLN[to12(anchorHour24 + 1)]!! else hoursL[to12(anchorHour24 + 1)]!!

        return if (niqqud) when (anchorMinute) {
            0  -> hh
            5  -> "$hh וַחֲמִשָּׁה"
            10 -> "$hh וַעֲשָׂרָה"
            15 -> "$hh וָרֶבַע"
            20 -> "$hh וְעֶשְׂרִים"
            25 -> "$hh עֶשְׂרִים וְחָמֵשׁ"
            30 -> "$hh וָחֵצִי"
            35 -> "$hh שְׁלוֹשִׁים וְחָמֵשׁ"
            40 -> "$hh אַרְבָּעִים"
            45 -> "רֶבַע $hl"
            50 -> "עֲשָׂרָה $hl"
            55 -> "חֲמִשָּׁה $hl"
            else -> hh
        } else when (anchorMinute) {
            0  -> hh
            5  -> "$hh וחמישה"
            10 -> "$hh ועשרה"
            15 -> "$hh ורבע"
            20 -> "$hh ועשרים"
            25 -> "$hh עשרים וחמש"
            30 -> "$hh וחצי"
            35 -> "$hh שלושים וחמש"
            40 -> "$hh ארבעים"
            45 -> "רבע $hl"
            50 -> "עשרה $hl"
            55 -> "חמישה $hl"
            else -> hh
        }
    }

    // ── Day-part suffix (range-based, matches web app) ────────────────────────

    /**
     * Returns the day-part suffix for [hour24] based on configured [slots].
     * Iterates slots in definition order; returns the text of the first enabled
     * slot whose [from, until) range covers [hour24].
     * Returns "" if [masterEnabled] is false or no slot matches.
     */
    fun dayPartSuffix(
        hour24:        Int,
        niqqud:        Boolean      = false,
        masterEnabled: Boolean      = true,
        slots:         List<SuffixSlot> = SUFFIX_DEFAULTS
    ): String {
        if (!masterEnabled) return ""
        val h = ((hour24 % 24) + 24) % 24
        for (slot in slots) {
            if (!slot.enabled) continue
            val inRange = if (slot.from < slot.until) {
                h >= slot.from && h < slot.until
            } else {                          // wraps midnight
                h >= slot.from || h < slot.until
            }
            if (inRange) return if (niqqud) slot.niqqud else slot.plain
        }
        return ""
    }

    // ── Main entry point ──────────────────────────────────────────────────────

    data class HebrewTime(val modifier: String, val phrase: String, val suffix: String)

    fun getHebrewTime(
        cal:           Calendar         = Calendar.getInstance(),
        niqqud:        Boolean          = false,
        suffixMasterEnabled: Boolean    = true,
        suffixSlots:   List<SuffixSlot> = SUFFIX_DEFAULTS
    ): HebrewTime {
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        val t = 60 * minute + second
        val anchorSec = ((t.toDouble() / 300.0).roundToInt()) * 300
        val delta = t - anchorSec

        var anchorHour   = hour24
        var anchorMinute = anchorSec / 60
        if (anchorMinute >= 60) {
            anchorMinute = 0
            anchorHour = (anchorHour + 1) % 24
        }

        val phrase = buildPhrase(anchorHour, anchorMinute, niqqud)
        val modifier = when {
            delta < -60 -> if (niqqud) "קְצָת לִפְנֵי" else "קצת לפני"
            delta > 60  -> if (niqqud) "קְצָת אַחֲרֵי" else "קצת אחרי"
            else        -> ""
        }
        val effectiveHour = if (anchorMinute >= 45) (anchorHour + 1) % 24 else anchorHour
        return HebrewTime(modifier, phrase, dayPartSuffix(effectiveHour, niqqud, suffixMasterEnabled, suffixSlots))
    }
}
