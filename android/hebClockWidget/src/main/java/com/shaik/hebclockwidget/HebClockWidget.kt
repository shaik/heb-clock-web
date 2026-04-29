package com.shaik.hebclockwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class HebClockWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val niqqud      = WidgetPrefs.useNiqqud(context)
        val suffixOn    = WidgetPrefs.showSuffix(context)
        val suffixSlots = WidgetPrefs.suffixSlots(context)
        val t           = HebTime.getHebrewTime(
            niqqud             = niqqud,
            suffixMasterEnabled = suffixOn,
            suffixSlots        = suffixSlots
        )
        val isDark    = WidgetPrefs.theme(context) == "dark"
        val compact   = WidgetPrefs.compactLabels(context)
        val fontSize  = WidgetPrefs.fontSize(context).sp
        val labelSize = if (compact) 13.sp else fontSize
        scheduleNextUpdate(context)

        provideContent {
            HebClockContent(
                modifierText = if (WidgetPrefs.showModifier(context)) t.modifier else "",
                phrase       = t.phrase,
                suffix       = t.suffix,
                isDark       = isDark,
                fontSize     = fontSize,
                labelSize    = labelSize,
                labelBold    = !compact
            )
        }
    }
}

@Composable
fun HebClockContent(
    modifierText: String,
    phrase: String,
    suffix: String,
    isDark: Boolean,
    fontSize: TextUnit,
    labelSize: TextUnit,
    labelBold: Boolean
) {
    val bgColor   = ColorProvider(if (isDark) Color(0xFF1a1a2eL) else Color(0xFFF5F5F0L))
    val mainColor = ColorProvider(if (isDark) Color.White else Color(0xFF1a1a2eL))
    val dimColor  = ColorProvider(if (isDark) Color(0xFFaaaacc) else Color(0xFF666688L))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (modifierText.isNotEmpty()) {
            Text(
                text  = modifierText,
                style = TextStyle(
                    fontSize   = labelSize,
                    fontWeight = if (labelBold) FontWeight.Bold else null,
                    color      = if (labelBold) mainColor else dimColor,
                    textAlign  = TextAlign.Center
                )
            )
        }

        Text(
            text  = phrase,
            style = TextStyle(
                fontSize   = fontSize,
                fontWeight = FontWeight.Bold,
                color      = mainColor,
                textAlign  = TextAlign.Center
            )
        )

        if (suffix.isNotEmpty()) {
            Text(
                text  = suffix,
                style = TextStyle(
                    fontSize   = labelSize,
                    fontWeight = if (labelBold) FontWeight.Bold else null,
                    color      = if (labelBold) mainColor else dimColor,
                    textAlign  = TextAlign.Center
                )
            )
        }
    }
}
