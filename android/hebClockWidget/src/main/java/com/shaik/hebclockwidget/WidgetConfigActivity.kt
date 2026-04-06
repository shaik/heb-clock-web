package com.shaik.hebclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class WidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val hourLabels = (0..23).map { "%02d:00".format(it) }

    private val slotCheckboxes   = mutableListOf<CheckBox>()
    private val slotFromSpinners = mutableListOf<Spinner>()
    private val slotUntilSpinners = mutableListOf<Spinner>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_config)

        val rgTheme        = findViewById<RadioGroup>(R.id.rg_theme)
        val swSuffix       = findViewById<Switch>(R.id.sw_suffix)
        val swModifier     = findViewById<Switch>(R.id.sw_modifier)
        val swNiqqud       = findViewById<Switch>(R.id.sw_niqqud)
        val swCompact      = findViewById<Switch>(R.id.sw_compact)
        val rgFontSize     = findViewById<RadioGroup>(R.id.rg_font_size)
        val btnSave        = findViewById<Button>(R.id.btn_save)
        val tvVersion      = findViewById<TextView>(R.id.tv_version)
        val slotContainer  = findViewById<LinearLayout>(R.id.suffix_slots_container)
        val slotLabel      = findViewById<TextView>(R.id.tv_suffix_slots_label)

        val version = packageManager.getPackageInfo(packageName, 0).versionName
        tvVersion.text = "v$version"

        // ── Populate existing controls ────────────────────────────────────────
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

        // ── Build 7 suffix slot rows ──────────────────────────────────────────
        val savedSlots = WidgetPrefs.suffixSlots(this)
        savedSlots.forEachIndexed { i, slot ->
            val row = buildSlotRow(i, slot)
            slotContainer.addView(row)
        }

        // ── Master toggle wires to slot rows ──────────────────────────────────
        fun applyMasterState(on: Boolean) {
            slotLabel.alpha = if (on) 1f else 0.4f
            slotCheckboxes.forEachIndexed { i, chk ->
                chk.isEnabled = on
                slotFromSpinners[i].isEnabled  = on && chk.isChecked
                slotUntilSpinners[i].isEnabled = on && chk.isChecked
            }
        }

        swSuffix.setOnCheckedChangeListener { _, isChecked -> applyMasterState(isChecked) }
        applyMasterState(swSuffix.isChecked)

        // ── Save ──────────────────────────────────────────────────────────────
        btnSave.setOnClickListener {
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

            // Save each suffix slot
            slotCheckboxes.forEachIndexed { i, chk ->
                val def = HebTime.SUFFIX_DEFAULTS[i]
                WidgetPrefs.setSuffixSlot(this, i, HebTime.SuffixSlot(
                    enabled = chk.isChecked,
                    from    = slotFromSpinners[i].selectedItemPosition,
                    until   = slotUntilSpinners[i].selectedItemPosition,
                    plain   = def.plain,
                    niqqud  = def.niqqud
                ))
            }

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            }

            MainScope().launch {
                HebClockWidget().updateAll(applicationContext)
                finish()
            }

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

    // ── Row builder ───────────────────────────────────────────────────────────

    private fun buildSlotRow(@Suppress("UNUSED_PARAMETER") index: Int, slot: HebTime.SuffixSlot): LinearLayout {
        val dp = resources.displayMetrics.density

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (6 * dp).toInt(), 0, (6 * dp).toInt())
        }

        // Checkbox
        val chk = CheckBox(this).apply {
            isChecked = slot.enabled
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#AAAACC"))
            setPadding(0, 0, (8 * dp).toInt(), 0)
        }

        // Label (Hebrew text)
        val label = TextView(this).apply {
            text = slot.plain
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Hour spinner adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hourLabels).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // From spinner
        val fromSpinner = Spinner(this).apply {
            this.adapter = adapter
            setSelection(slot.from)
            isEnabled = slot.enabled
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = (4 * dp).toInt() }
        }

        // Separator
        val sep = TextView(this).apply {
            text = "–"
            setTextColor(Color.parseColor("#AAAACC"))
            textSize = 14f
            setPadding((4 * dp).toInt(), 0, (4 * dp).toInt(), 0)
        }

        // Until spinner
        val untilSpinner = Spinner(this).apply {
            this.adapter = ArrayAdapter(
                this@WidgetConfigActivity,
                android.R.layout.simple_spinner_item,
                hourLabels
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            setSelection(slot.until)
            isEnabled = slot.enabled
        }

        // Checkbox toggles its own spinners (and master state must also be on)
        chk.setOnCheckedChangeListener { _, isChecked ->
            fromSpinner.isEnabled  = isChecked
            untilSpinner.isEnabled = isChecked
        }

        row.addView(chk)
        row.addView(label)
        row.addView(fromSpinner)
        row.addView(sep)
        row.addView(untilSpinner)

        slotCheckboxes.add(chk)
        slotFromSpinners.add(fromSpinner)
        slotUntilSpinners.add(untilSpinner)

        return row
    }
}
