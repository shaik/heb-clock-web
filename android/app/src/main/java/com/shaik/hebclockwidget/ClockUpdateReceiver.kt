package com.shaik.hebclockwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ClockUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MainScope().launch {
            HebClockWidget().updateAll(context)
        }
        scheduleNextUpdate(context)
    }
}

fun scheduleNextUpdate(context: Context) {
    val am = context.getSystemService(AlarmManager::class.java)
    // Fire within 10 seconds of the start of the next minute
    val nextMinute = (System.currentTimeMillis() / 60_000L + 1L) * 60_000L
    val pi = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ClockUpdateReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    am.setWindow(AlarmManager.RTC, nextMinute, 10_000L, pi)
}
