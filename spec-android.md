# Hebrew Clock — Android Widget Specification

## 1. Overview

A native Android home-screen widget that displays the current Hebrew spoken-time phrase, updating every minute. The widget is companion to the web app described in `spec.md`; the time algorithm is identical.

The widget is a widget-only APK (no launcher activity). Users add it via the long-press widget picker and configure it via a settings screen that appears automatically on first placement and is re-opened by tapping the widget.

---

## 2. Technology

| Component | Choice | Reason |
|---|---|---|
| Widget API | Jetpack Glance 1.1.0 | Modern Compose-based widget API; replaces raw RemoteViews |
| Settings UI | `android.app.Activity` + XML layout | No extra dependencies |
| Persistence | `SharedPreferences` | Lightweight; no Room/DataStore needed |
| Update trigger | `AlarmManager.setWindow()` | Fires within ~10 s of each minute boundary; no special permissions required on Android 12+ |
| Language | Kotlin |  |
| Min SDK | 26 (Android 8.0) |  |

---

## 3. File Structure

```
android/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties                        ← android.useAndroidX=true
  gradle/wrapper/gradle-wrapper.properties
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      java/com/shaik/hebclockwidget/
        HebTimeAlgorithm.kt                ← time algorithm (Kotlin port)
        HebClockWidget.kt                  ← GlanceAppWidget + Composable view
        HebClockWidgetReceiver.kt          ← GlanceAppWidgetReceiver
        ClockUpdateReceiver.kt             ← BroadcastReceiver + scheduleNextUpdate()
        WidgetPrefs.kt                     ← SharedPreferences helpers
        WidgetConfigActivity.kt            ← settings screen Activity
      res/
        layout/activity_config.xml         ← settings screen layout
        xml/heb_clock_widget_info.xml      ← widget metadata (size, configure)
        values/strings.xml                 ← app_name: "שעון עברי"
```

---

## 4. Time Algorithm

`HebTimeAlgorithm.kt` is a direct Kotlin port of the JavaScript algorithm in `index.html`. The logic is identical to that described in `spec.md §3`. All Hebrew text data (plain and niqqud) is stored in the same `object HebTime`.

### 4.1 Entry point

```kotlin
HebTime.getHebrewTime(
    cal:                Calendar         = Calendar.getInstance(),
    niqqud:             Boolean          = false,
    suffixMasterEnabled: Boolean         = true,
    suffixSlots:        List<SuffixSlot> = SUFFIX_DEFAULTS
) → HebrewTime(modifier: String, phrase: String, suffix: String)
```

### 4.2 Text data

See `spec.md §7` for the full plain / niqqud text tables. Both sets are present in `HebTimeAlgorithm.kt`:

- `hours` / `hoursN` — 12-hour names
- `hoursL` / `hoursLN` — ל-prefixed forms for :45/:50/:55
- Inline string literals for minute suffixes
- `SUFFIX_DEFAULTS` — 7 `SuffixSlot` entries (plain + niqqud text + default times)

### 4.3 Day-part suffix — configurable range-based system

Identical to the web app (see `spec.md §3.6`). Each of 7 suffix slots has `enabled`, `from`, and `until` hours. `dayPartSuffix()` iterates slots in order and returns the text of the first enabled slot whose range covers the effective hour; wrapping is supported (`from > until` = wraps midnight).

```kotlin
data class SuffixSlot(enabled: Boolean, from: Int, until: Int, plain: String, niqqud: String)
```

**Default slots** (stored in `HebTime.SUFFIX_DEFAULTS`):

| Suffix (plain)    | Default | From  | Until |
|------------------|---------|-------|-------|
| לפנות בוקר        | off     | 04:00 | 05:00 |
| בבוקר             | **on**  | 05:00 | 10:00 |
| בצהריים           | off     | 12:00 | 14:00 |
| אחרי הצהריים      | off     | 16:00 | 18:00 |
| לפנות ערב         | off     | 17:00 | 18:00 |
| בערב              | **on**  | 19:00 | 20:00 |
| בלילה             | **on**  | 22:00 | 05:00 |

---

## 5. Update Mechanism

```
Widget placed / alarm fires
        │
        ▼
ClockUpdateReceiver.onReceive()
        │
        ├─→ HebClockWidget().updateAll(context)   ← triggers provideGlance
        │
        └─→ scheduleNextUpdate(context)            ← re-arms alarm
                │
                └─ AlarmManager.setWindow(
                       RTC,
                       nextMinuteMs,
                       windowMs = 10_000,
                       pendingIntent → ClockUpdateReceiver
                   )
```

`provideGlance` reads all prefs fresh on every call, so settings changes are applied on the next alarm tick at the latest. When the user saves settings, an `ACTION_APPWIDGET_UPDATE` broadcast is sent immediately to force a same-second refresh.

**Fallback:** `heb_clock_widget_info.xml` also sets `android:updatePeriodMillis="1800000"` (30 min) as a system-level backup in case all alarm state is lost.

---

## 6. Widget View

`HebClockWidget.kt` renders a Glance `Column` with up to three text rows:

```
[modifier]    ← קצת לפני / קצת אחרי  (dim color; hidden if pref off or empty)
[phrase]      ← main Hebrew phrase    (bold, large, centered)
[suffix]      ← בבוקר / בערב / בלילה (dim color; hidden if pref off or empty)
```

Tapping the widget launches `WidgetConfigActivity`.

### 6.1 Themes

| Theme | Background  | Main text | Dim text  |
|-------|-------------|-----------|-----------|
| Dark  | `#1a1a2e`   | `#ffffff` | `#aaaacc` |
| Light | `#f5f5f0`   | `#1a1a2e` | `#666688` |

### 6.2 Font sizes

| Setting | Size |
|---------|------|
| Small   | 20sp |
| Medium  | 26sp (default) |
| Large   | 32sp |

Modifier and suffix are always 13sp regardless of the main font size setting.

---

## 7. Settings

Stored in `SharedPreferences` file `"widget_prefs"`. Accessed via `WidgetPrefs.kt`.

| Pref key                | Type    | Default        | Description |
|------------------------|---------|----------------|-------------|
| `theme`                 | String  | `"dark"`       | `"dark"` or `"light"` |
| `show_suffix`           | Boolean | `true`         | Master toggle: show/hide all day-part suffixes |
| `suffix_N_enabled`      | Boolean | per slot       | Whether slot N (0–6) is enabled |
| `suffix_N_from`         | Int     | per slot       | From-hour for slot N (0–23) |
| `suffix_N_until`        | Int     | per slot       | Until-hour for slot N (0–23); wraps if ≤ from |
| `show_modifier`         | Boolean | `true`         | Show קצת לפני/אחרי modifier |
| `use_niqqud`            | Boolean | `true`         | Use niqqud vowel marks |
| `font_size`             | Int     | `26`           | Main phrase size in sp (20/26/32) |
| `compact_labels`        | Boolean | `false`        | Compact modifier/suffix labels |

---

## 8. Settings Screen (`WidgetConfigActivity`)

### 8.1 Launch triggers

| Trigger | How |
|---------|-----|
| Widget first added | `android:configure` in `heb_clock_widget_info.xml` |
| User taps widget | `GlanceModifier.clickable(actionStartActivity<WidgetConfigActivity>())` |

### 8.2 Behavior

- `android.app.Activity` — no AppCompat dependency
- On open: reads current prefs and populates all controls
- On Save:
  1. Writes all prefs
  2. Sends `ACTION_APPWIDGET_UPDATE` broadcast to `HebClockWidgetReceiver` → triggers immediate re-render
  3. If `appWidgetId != INVALID_APPWIDGET_ID` (initial configure flow): calls `setResult(RESULT_OK, intent)` so Android places the widget
  4. `finish()`
- On back/cancel (initial configure flow): `setResult(RESULT_CANCELED)` → widget is not placed

### 8.3 Controls

| Setting | Control |
|---------|---------|
| Theme | `RadioGroup` — Dark / Light |
| Show suffix | `Switch` (master toggle) |
| Suffix schedule | 7 programmatic rows — each: `CheckBox` + Hebrew label + from `Spinner` + `–` + until `Spinner` |
| Show modifier | `Switch` |
| Niqqud | `Switch` |
| Font size | `RadioGroup` — Small / Medium / Large |

The suffix schedule rows are built programmatically in `onCreate` from `WidgetPrefs.suffixSlots()`. Rows dim/disable when the master suffix toggle is off. Each checkbox also disables its own time spinners when unchecked. On Save, all 7 slots are written via `WidgetPrefs.setSuffixSlot()`.

---

## 9. Manifest

```xml
<receiver name=".HebClockWidgetReceiver" exported="true">
    ACTION_APPWIDGET_UPDATE → @xml/heb_clock_widget_info
</receiver>

<receiver name=".ClockUpdateReceiver" exported="false" />

<activity name=".WidgetConfigActivity" exported="true">
    ACTION_APPWIDGET_CONFIGURE
</activity>
```

---

## 10. Widget Info (`heb_clock_widget_info.xml`)

| Attribute | Value |
|-----------|-------|
| `minWidth` / `minHeight` | 110dp |
| `targetCellWidth` / `targetCellHeight` | 2×2 |
| `resizeMode` | horizontal\|vertical |
| `updatePeriodMillis` | 1800000 (30 min fallback) |
| `configure` | `com.shaik.hebclockwidget.WidgetConfigActivity` |

---

## 11. Build & Installation

### Requirements
- Android Studio (any recent version)
- Android SDK 34

### Build APK
*Build → Build APK(s)* in Android Studio. No signing configuration needed for sideloading.

### Install without Google Play (sideload)
1. Build APK
2. Transfer to phone (USB, Drive, email, etc.)
3. On phone: *Settings → Apps → Install unknown apps* → allow your file manager
4. Tap the APK file → Install
5. Long-press home screen → Widgets → "שעון עברי" → drag to home

No developer account, no Play Store, no expiry date.

---

## 12. Edge Cases

| Case | Behavior |
|------|----------|
| Widget tapped (not initial configure flow) | `appWidgetId = INVALID_APPWIDGET_ID`; `setResult` skipped; `updateAll` still fires via broadcast |
| Multiple widget instances | `updateAll` + broadcast update all instances simultaneously; all share the same prefs |
| Alarm lost (device restart, battery save kill) | System `updatePeriodMillis` fallback fires within 30 min; alarm is re-armed after first render |
| Niqqud + any font | Glance renders niqqud characters correctly via Android's bidi/Unicode text engine |
| RTL text | Android detects Hebrew script direction automatically; no explicit layout direction needed |
