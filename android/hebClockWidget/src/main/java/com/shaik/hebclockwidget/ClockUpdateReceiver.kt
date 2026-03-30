package com.shaik.hebclockwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ClockUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Re-arm the alarm on boot so the widget stays alive after a restart
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleNextUpdate(context)
            return
        }
        MainScope().launch {
            HebClockWidget().updateAll(context)
        }
        scheduleNextUpdate(context)
    }
}

fun scheduleNextUpdate(context: Context) {
    val am = context.getSystemService(AlarmManager::class.java)
    val nextMinute = (System.currentTimeMillis() / 60_000L + 1L) * 60_000L
    val pi = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ClockUpdateReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // On Android 12+, check if we have permission for exact alarms.
    // USE_EXACT_ALARM (API 33+) is auto-granted; SCHEDULE_EXACT_ALARM (API 31-32)
    // is pre-granted by default but users can revoke it.
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            am.canScheduleExactAlarms()

    if (canExact) {
        // Fires at exactly the next minute, even during Doze and after screen wake
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinute, pi)
    } else {
        // Fallback: fires within ~10s of the next minute when the device is active
        am.setWindow(AlarmManager.RTC_WAKEUP, nextMinute, 10_000L, pi)
    }
}
