package com.shaik.hebclockwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
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
        val niqqud   = WidgetPrefs.useNiqqud(context)
        val t        = HebTime.getHebrewTime(niqqud = niqqud)
        val isDark   = WidgetPrefs.theme(context) == "dark"
        val fontSize = WidgetPrefs.fontSize(context).sp
        scheduleNextUpdate(context)

        provideContent {
            HebClockContent(
                modifierText = if (WidgetPrefs.showModifier(context)) t.modifier else "",
                phrase       = t.phrase,
                suffix       = if (WidgetPrefs.showSuffix(context)) t.suffix else "",
                isDark       = isDark,
                fontSize     = fontSize
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
    fontSize: TextUnit
) {
    val bgColor   = ColorProvider(if (isDark) Color(0xFF1a1a2eL) else Color(0xFFF5F5F0L))
    val mainColor = ColorProvider(if (isDark) Color.White else Color(0xFF1a1a2eL))
    val dimColor  = ColorProvider(if (isDark) Color(0xFFaaaacc) else Color(0xFF666688L))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<WidgetConfigActivity>()),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (modifierText.isNotEmpty()) {
            Text(
                text  = modifierText,
                style = TextStyle(
                    fontSize  = 13.sp,
                    color     = dimColor,
                    textAlign = TextAlign.Center
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
                    fontSize  = 13.sp,
                    color     = dimColor,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
