package com.shaik.hebclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Default: cancelled, so Android won't place widget if user backs out
        setResult(RESULT_CANCELED)

        setContentView(R.layout.activity_config)

        val rgTheme    = findViewById<RadioGroup>(R.id.rg_theme)
        val swSuffix   = findViewById<Switch>(R.id.sw_suffix)
        val swModifier = findViewById<Switch>(R.id.sw_modifier)
        val swNiqqud   = findViewById<Switch>(R.id.sw_niqqud)
        val swCompact  = findViewById<Switch>(R.id.sw_compact)
        val rgFontSize = findViewById<RadioGroup>(R.id.rg_font_size)
        val btnSave    = findViewById<Button>(R.id.btn_save)
        val tvVersion  = findViewById<TextView>(R.id.tv_version)

        val version = packageManager.getPackageInfo(packageName, 0).versionName
        tvVersion.text = "v$version"

        // Populate from current prefs
        rgTheme.check(
            if (WidgetPrefs.theme(this) == "dark") R.id.rb_dark else R.id.rb_light
        )
        swSuffix.isChecked   = WidgetPrefs.showSuffix(this)
        swModifier.isChecked = WidgetPrefs.showModifier(this)
        swNiqqud.isChecked   = WidgetPrefs.useNiqqud(this)
        swCompact.isChecked  = WidgetPrefs.compactLabels(this)
        rgFontSize.check(
            when (WidgetPrefs.fontSize(this)) {
                20   -> R.id.rb_small
                32   -> R.id.rb_large
                else -> R.id.rb_medium
            }
        )

        btnSave.setOnClickListener {
            // Write prefs
            WidgetPrefs.setTheme(
                this,
                if (rgTheme.checkedRadioButtonId == R.id.rb_light) "light" else "dark"
            )
            WidgetPrefs.setShowSuffix(this, swSuffix.isChecked)
            WidgetPrefs.setShowModifier(this, swModifier.isChecked)
            WidgetPrefs.setUseNiqqud(this, swNiqqud.isChecked)
            WidgetPrefs.setCompactLabels(this, swCompact.isChecked)
            WidgetPrefs.setFontSize(
                this,
                when (rgFontSize.checkedRadioButtonId) {
                    R.id.rb_small -> 20
                    R.id.rb_large -> 32
                    else          -> 26
                }
            )

            // Tell Android to place the widget (initial configure flow only)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            }

            // Fast in-process path: updateAll() suspends until Glance has pushed new
            // RemoteViews to the launcher, so finish() only runs after the widget is
            // already visually updated — the user sees the new state instantly.
            MainScope().launch {
                HebClockWidget().updateAll(applicationContext)
                finish()
            }

            // Belt-and-suspenders: also send ACTION_APPWIDGET_UPDATE through the
            // GlanceAppWidgetReceiver. This backup fires asynchronously and is harmless.
            val manager = AppWidgetManager.getInstance(this)
            val ids = manager.getAppWidgetIds(
                ComponentName(this, HebClockWidgetReceiver::class.java)
            )
            sendBroadcast(
                Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = ComponentName(this@WidgetConfigActivity, HebClockWidgetReceiver::class.java)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }
}
