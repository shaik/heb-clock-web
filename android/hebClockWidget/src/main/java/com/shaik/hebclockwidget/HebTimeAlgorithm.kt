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

    // ── Exact-minute phrase data ──────────────────────────────────────────────

    private data class ExactText(
        val m:    Map<Int, String>,
        val tens: Map<Int, String>,
        val u:    Map<Int, String>,
        val m30:  String,
        val m40:  String
    )

    private val EXACT_PLAIN = ExactText(
        m = mapOf(
            1  to " ודקה",
            2  to " ושתי דקות",
            3  to " ושלוש דקות",
            4  to " וארבע דקות",
            5  to " וחמישה",
            6  to " ושש דקות",
            7  to " ושבע דקות",
            8  to " ושמונה דקות",
            9  to " ותשע דקות",
            10 to " ועשרה",
            11 to " ואחת עשרה דקות",
            12 to " ושתים עשרה דקות",
            13 to " ושלוש עשרה דקות",
            14 to " וארבע עשרה דקות",
            15 to " ורבע",
            16 to " ושש עשרה דקות",
            17 to " ושבע עשרה דקות",
            18 to " ושמונה עשרה דקות",
            19 to " ותשע עשרה דקות",
            20 to " ועשרים"
        ),
        tens = mapOf(20 to "עשרים", 30 to "שלושים", 40 to "ארבעים", 50 to "חמישים"),
        u = mapOf(
            1 to " ואחת", 2 to " ושתיים", 3 to " ושלוש", 4 to " וארבע",
            5 to " וחמש", 6 to " ושש",    7 to " ושבע",  8 to " ושמונה", 9 to " ותשע"
        ),
        m30 = " וחצי",
        m40 = " ארבעים"
    )

    private val EXACT_NIQQUD = ExactText(
        m = mapOf(
            1  to " וְדַקָּה",
            2  to " וּשְׁתֵּי דַּקּוֹת",
            3  to " וְשָׁלוֹשׁ דַּקּוֹת",
            4  to " וְאַרְבַּע דַּקּוֹת",
            5  to " וַחֲמִשָּׁה",
            6  to " וָשֵׁשׁ דַּקּוֹת",
            7  to " וְשֶׁבַע דַּקּוֹת",
            8  to " וּשְׁמוֹנֶה דַּקּוֹת",
            9  to " וְתֵשַׁע דַּקּוֹת",
            10 to " וַעֲשָׂרָה",
            11 to " וְאַחַת עֶשְׂרֵה דַּקּוֹת",
            12 to " וּשְׁתֵּים עֶשְׂרֵה דַּקּוֹת",
            13 to " וּשְׁלוֹשׁ עֶשְׂרֵה דַּקּוֹת",
            14 to " וְאַרְבַּע עֶשְׂרֵה דַּקּוֹת",
            15 to " וָרֶבַע",
            16 to " וְשֵׁשׁ עֶשְׂרֵה דַּקּוֹת",
            17 to " וּשְׁבַע עֶשְׂרֵה דַּקּוֹת",
            18 to " וּשְׁמוֹנֶה עֶשְׂרֵה דַּקּוֹת",
            19 to " וּתְשַׁע עֶשְׂרֵה דַּקּוֹת",
            20 to " וְעֶשְׂרִים"
        ),
        tens = mapOf(20 to "עֶשְׂרִים", 30 to "שְׁלוֹשִׁים", 40 to "אַרְבָּעִים", 50 to "חֲמִשִּׁים"),
        u = mapOf(
            1 to " וְאַחַת", 2 to " וּשְׁתַּיִם", 3 to " וְשָׁלוֹשׁ", 4 to " וְאַרְבַּע",
            5 to " וְחָמֵשׁ", 6 to " וָשֵׁשׁ",     7 to " וְשֶׁבַע",  8 to " וּשְׁמוֹנֶה", 9 to " וְתֵשַׁע"
        ),
        m30 = " וָחֵצִי",
        m40 = " אַרְבָּעִים"
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

    // ── Exact-minute phrase builder (matches web buildExactPhrase) ────────────

    fun buildExactPhrase(hour24: Int, minute: Int, niqqud: Boolean = false): String {
        val et = if (niqqud) EXACT_NIQQUD else EXACT_PLAIN
        val h = to12(hour24)
        val hourName = if (niqqud) hoursN[h]!! else hours[h]!!
        if (minute == 0) return hourName
        if (minute <= 20) return hourName + et.m[minute]
        if (minute == 30) return hourName + et.m30
        if (minute == 40) return hourName + et.m40
        if (minute == 45 || minute == 50 || minute == 55) {
            val hl = if (niqqud) hoursLN[to12(hour24 + 1)]!! else hoursL[to12(hour24 + 1)]!!
            val prefix = if (niqqud) when (minute) {
                45 -> "רֶבַע "
                50 -> "עֲשָׂרָה "
                else -> "חֲמִשָּׁה "
            } else when (minute) {
                45 -> "רבע "
                50 -> "עשרה "
                else -> "חמישה "
            }
            return prefix + hl
        }
        val tens = (minute / 10) * 10
        val unit = minute % 10
        return hourName + " " + et.tens[tens]!! + et.u[unit]!!
    }

    /** Minutes 1-4, 6-9, 11-14, 16-19 — produce long "N דקות" phrases that need a line break. */
    fun isLongExactPhrase(minute: Int): Boolean =
        minute in 1..19 && minute % 5 != 0

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

    data class HebrewTime(
        val modifier: String,
        val phrase: String,
        val suffix: String,
        val forceSplit: Boolean = false
    )

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

    /**
     * Exact-minute mode (web: getExactTime). Produces the spoken phrase for any
     * minute 0–59. For "long" minutes (1–4, 6–9, 11–14, 16–19) the hour name
     * is returned in [HebrewTime.modifier] and the minute fragment in
     * [HebrewTime.phrase], with [HebrewTime.forceSplit] = true, so the renderer
     * can break them onto two lines.
     */
    fun getExactTime(
        cal:                 Calendar         = Calendar.getInstance(),
        niqqud:              Boolean          = false,
        suffixMasterEnabled: Boolean          = true,
        suffixSlots:         List<SuffixSlot> = SUFFIX_DEFAULTS
    ): HebrewTime {
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val effectiveHour = if (minute >= 45) (hour24 + 1) % 24 else hour24
        val suffix = dayPartSuffix(effectiveHour, niqqud, suffixMasterEnabled, suffixSlots)

        if (isLongExactPhrase(minute)) {
            val h = to12(hour24)
            val hourName = if (niqqud) hoursN[h]!! else hours[h]!!
            val et = if (niqqud) EXACT_NIQQUD else EXACT_PLAIN
            val minutePart = et.m[minute]!!.trim()
            return HebrewTime(modifier = hourName, phrase = minutePart, suffix = suffix, forceSplit = true)
        }
        return HebrewTime(modifier = "", phrase = buildExactPhrase(hour24, minute, niqqud), suffix = suffix, forceSplit = false)
    }
}
