# Hebrew Spoken-Time Web Clock — Specification v2.0

## 1. Overview

A client-side web application (`index.html`) that displays the current time as a natural Hebrew spoken phrase. It updates every second, requires no server or build tools, and works offline once loaded. It is installable as a PWA on Android and desktop.

A native Android home-screen widget companion is located in the `android/` directory and documented separately in `spec-android.md`.

Example outputs:

- `שלוש` (3:00)
- `שלוש ורבע בבוקר` (3:15 AM)
- `קצת לפני רבע לארבע` (approaching 3:45)
- `אחת עשרה בלילה` (11 PM)

With niqqud:

- `שָׁלוֹשׁ וָרֶבַע בַּבֹּקֶר`
- `קְצָת לִפְנֵי רֶבַע לְאַרְבַּע`

---

## 2. Technology & File Structure

- **Single HTML file** — all CSS and JS inline, no frameworks, no build step
- **Fonts**: Google Fonts (Heebo, Rubik, Frank Ruhl Libre, Secular One, Suez One, Bellefair) + one local custom font
- **Custom font**: `fonts/fridge_Regular.ttf` — loaded via `@font-face`
- **Browser APIs**: `Date`, `setInterval`, `localStorage`, `navigator.geolocation`, `navigator.wakeLock`, Fullscreen API, Service Worker

```
heb-clock-web/
├── index.html
├── manifest.json
├── sw.js
├── fonts/
│   └── fridge_Regular.ttf
└── icons/
    ├── icon-196.png
    ├── icon-512.png
    └── icon-2048.png
```

### PWA Requirements

- `manifest.json` declares app name (`שעון עברי`), short name (`שעון`), `display: standalone`, `lang: he`, `dir: rtl`, `start_url`, and the icon set.
- `sw.js` is a service worker that caches `index.html` and the font file for offline use. HTML requests use a **network-first** strategy (always try the network, fall back to cache); font/icon requests use **cache-first**.
- The `<head>` includes `<link rel="manifest">` and `<meta name="theme-color">`.
- On supported browsers (Chrome/Android/Edge desktop), a browser-level install prompt becomes available; the app captures and defers it via the `beforeinstallprompt` event and exposes it through a settings panel button.

---

## 3. Hebrew Time Logic

### 3.1 Hour Names (12-hour clock)

Convert 24h to 12h: `h = hour24 % 12`, if `h === 0` then `h = 12`.

| 12h | Plain      | With niqqud       |
|-----|-----------|-------------------|
| 1   | אחת       | אַחַת              |
| 2   | שתיים     | שְׁתַּיִם           |
| 3   | שלוש      | שָׁלוֹשׁ             |
| 4   | ארבע      | אַרְבַּע             |
| 5   | חמש       | חָמֵשׁ              |
| 6   | שש        | שֵׁשׁ               |
| 7   | שבע       | שֶׁבַע              |
| 8   | שמונה     | שְׁמוֹנֶה            |
| 9   | תשע       | תֵּשַׁע              |
| 10  | עשר       | עֶשֶׂר              |
| 11  | אחת עשרה  | אַחַת עֶשְׂרֵה       |
| 12  | שתים עשרה | שְׁתֵּים עֶשְׂרֵה     |

### 3.2 Hour Names with ל Prefix

Used for :45, :50, :55 phrases that reference the **next** hour.

| 12h | Plain         | With niqqud          |
|-----|--------------|---------------------|
| 1   | לאחת         | לְאַחַת              |
| 2   | לשתיים       | לִשְׁתַּיִם           |
| 3   | לשלוש        | לְשָׁלוֹשׁ             |
| 4   | לארבע        | לְאַרְבַּע             |
| 5   | לחמש         | לְחָמֵשׁ              |
| 6   | לשש          | לְשֵׁשׁ               |
| 7   | לשבע         | לְשֶׁבַע              |
| 8   | לשמונה       | לִשְׁמוֹנֶה            |
| 9   | לתשע         | לְתֵּשַׁע              |
| 10  | לעשר         | לְעֶשֶׂר              |
| 11  | לאחת עשרה    | לְאַחַת עֶשְׂרֵה       |
| 12  | לשתים עשרה   | לִשְׁתֵּים עֶשְׂרֵה     |

### 3.3 Minute Phrase Patterns (5-minute anchors)

For anchors :00–:40, the phrase is `[hour_name] + [suffix]`.
For anchors :45–:55, the phrase is `[prefix] + [next_hour_with_ל]`.

| Anchor | Plain suffix/prefix | Niqqud suffix/prefix    |
|--------|---------------------|------------------------|
| :00    | *(none)*            | *(none)*               |
| :05    | ` וחמישה`           | ` וַחֲמִשָּׁה`           |
| :10    | ` ועשרה`            | ` וַעֲשָׂרָה`            |
| :15    | ` ורבע`             | ` וָרֶבַע`              |
| :20    | ` ועשרים`           | ` וְעֶשְׂרִים`           |
| :25    | ` עשרים וחמש`       | ` עֶשְׂרִים וְחָמֵשׁ`     |
| :30    | ` וחצי`             | ` וָחֵצִי`              |
| :35    | ` שלושים וחמש`      | ` שְׁלוֹשִׁים וְחָמֵשׁ`    |
| :40    | ` ארבעים`           | ` אַרְבָּעִים`           |
| :45    | `רבע ` (prefix)     | `רֶבַע ` (prefix)       |
| :50    | `עשרה ` (prefix)    | `עֲשָׂרָה ` (prefix)     |
| :55    | `חמישה ` (prefix)   | `חֲמִשָּׁה ` (prefix)    |

Suffixes for :05–:40 begin with a space. Prefixes for :45–:55 end with a space.

### 3.4 Second-Precision Anchor Selection

```
t            = 60 * minute + second       (seconds since top of hour)
ANCHOR_STEP  = 300                        (5 minutes in seconds)
EXACT_WINDOW = 60                         (±60 seconds)
anchorSec    = round(t / ANCHOR_STEP) * ANCHOR_STEP
delta        = t - anchorSec
```

The anchor switches at the midpoint between two anchors (e.g., 07:17:29 → 07:15; 07:17:30 → 07:20).

**Hour wrap:** if `anchorSec / 60 >= 60`, set `anchorMinute = 0` and increment the hour (mod 24).

### 3.5 Modifier Prefix ("קצת לפני" / "קצת אחרי")

| Condition        | Modifier (plain) | Modifier (niqqud)   |
|-----------------|-------------------|---------------------|
| `|delta| <= 60` | *(none)*          | *(none)*            |
| `delta < -60`   | `קצת לפני`        | `קְצָת לִפְנֵי`       |
| `delta > +60`   | `קצת אחרי`        | `קְצָת אַחֲרֵי`       |

Display modes (user-controlled):
- **Split** (default): modifier on its own line above the main phrase
- **Inline**: prepended to the main phrase with a space

### 3.6 Day-Part Suffix

The **effective hour** for the suffix:
- Anchors :00–:40 → use the anchor's hour
- Anchors :45–:55 → use `(anchorHour + 1) % 24`

The suffix system is fully user-configurable (see §5.9). There are 7 possible suffix slots; each has an enabled flag, a `from` hour, and an `until` hour. Evaluation iterates the slots in definition order and returns the **first** enabled slot whose range covers the effective hour. If no slot matches, no suffix is shown.

**Range check:**
- If `from < until`: matches when `h >= from && h < until`
- If `from >= until` (wraps midnight): matches when `h >= from || h < until`

**Default configuration:**

| Suffix (plain)       | Suffix (niqqud)           | Default | From  | Until |
|---------------------|--------------------------|---------|-------|-------|
| לפנות בוקר           | לִפְנוֹת בֹּקֶר             | off     | 04:00 | 05:00 |
| בבוקר                | בַּבֹּקֶר                   | **on**  | 05:00 | 10:00 |
| בצהריים              | בַּצָּהֳרַיִם                | off     | 12:00 | 14:00 |
| אחרי הצהריים         | אַחֲרֵי הַצָּהֳרַיִם          | off     | 16:00 | 18:00 |
| לפנות ערב            | לִפְנוֹת עֶרֶב              | off     | 17:00 | 18:00 |
| בערב                 | בָּעֶרֶב                    | **on**  | 19:00 | 20:00 |
| בלילה                | בַּלַּיְלָה                  | **on**  | 22:00 | 05:00 |

With the defaults (בבוקר 05–10, בערב 19–20, בלילה 22–05), hours 10–18 and 20–22 have no suffix.

### 3.7 Full Example Table (around 07:15)

| Time       | Anchor | delta | Output                    |
|-----------|--------|-------|---------------------------|
| 07:12:29  | 07:10  | —     | *(belongs to 07:10 set)*  |
| 07:12:30  | 07:15  | -150  | קצת לפני שבע ורבע בבוקר    |
| 07:14:00  | 07:15  | -60   | שבע ורבע בבוקר             |
| 07:16:00  | 07:15  | +60   | שבע ורבע בבוקר             |
| 07:16:01  | 07:15  | +61   | קצת אחרי שבע ורבע בבוקר    |
| 07:17:30  | 07:20  | -150  | קצת לפני שבע ועשרים בבוקר   |
| 07:19:00  | 07:20  | -60   | שבע ועשרים בבוקר            |

---

## 4. Display Layout

### 4.1 Structure (top to bottom, vertically centered)

```
         [digital time]          ← small, gray (hidden by default)
        [modifier line]          ← same size as main (split mode only)
       [hebrew phrase]           ← large, main display
        [day-part suffix]        ← same font as main

     ─── settings handle ───     ← thin pill at bottom center
```

All elements are horizontally centered. The page is `dir="rtl"` and `lang="he"`.

The entire time display area (modifier, phrase, suffix) is **clickable/tappable** — tapping anywhere on it opens the settings panel. Cursor is `pointer` on hover; `user-select: none` to prevent text selection.

### 4.2 Light Theme

| Element                 | Color / value          |
|------------------------|------------------------|
| Page background        | `#ffffff`              |
| All time text          | `#1a1a1a`              |
| Digital clock          | `#bbb`                 |
| Settings panel bg      | `rgba(245,245,245,0.97)` with backdrop blur |
| Settings button bg     | `#e6e6e6`, hover `#dadada` |
| Settings handle pill   | `#ccc`, hover `#aaa`   |
| Row label text         | `#444`                 |
| Version / credit       | `#777` / `#888`        |

### 4.3 Dark Theme

Applied via `body.dark` CSS class.

| Element                 | Color / value          |
|------------------------|------------------------|
| Page background        | `#121212`              |
| All time text          | `#e8e8e8`              |
| Digital clock          | `#555`                 |
| Settings panel bg      | `rgba(28,28,28,0.97)` with backdrop blur |
| Settings button bg     | `#383838`, hover `#444` |
| Settings handle pill   | `#555`, hover `#777`   |
| Row label text         | `#aaa`                 |
| Version / credit       | `#888` / `#777`        |

### 4.4 Typography

| Element        | Font family    | Weight | Size (default)                |
|---------------|----------------|--------|-------------------------------|
| Main phrase   | User-selected  | varies | `max(2.5rem, 6.5vw)`         |
| Modifier line | User-selected  | varies | Same as main phrase           |
| Day-part      | User-selected  | varies | Same as main phrase           |
| Digital clock | Heebo          | 400    | `clamp(1rem, 2.5vw, 1.3rem)` |
| Settings UI   | Heebo          | 400    | `1rem` (labels), `0.92rem` (buttons) |

The `max()` ensures a minimum floor on mobile. Font size must **not** change when text content changes — use viewport-relative units only. Use `white-space: nowrap` on the main phrase.

### 4.5 Transition Animation

When displayed text changes, all text elements fade out then in.

| Mode   | Fade duration |
|--------|--------------|
| Normal | 350ms        |
| Demo   | 60ms         |

If a change occurs mid-fade, cancel the pending timeout and restart.

---

## 5. Settings Panel

### 5.1 Opening and Closing

**Open:**
- Tap/click anywhere on the time display (modifier, phrase, or suffix lines)
- Tap/click the handle pill at the bottom of the screen

**Close:**
- Tap/click a transparent full-screen backdrop that covers everything behind the open panel

The handle pill hides while the panel is open and reappears when it closes.

**First-visit hint:** on first load, the handle pill pulses gently 3 times to indicate it is interactive. Once the user opens the panel (or the animation completes), store `hc_seen = '1'` in `localStorage` to suppress the hint permanently.

### 5.2 Panel Design

The panel is a **bottom sheet** — it slides up from the bottom of the screen with a smooth spring-like transition (~380ms). It is anchored to the bottom, centered horizontally, with a maximum width of 440px (full-width on mobile).

**Structure (top to bottom):**
```
  ─────  ← drag handle (decorative pill, 36×4px, centered)
  [live time preview]   ← sticky header, shows current phrase
  ─────────────────────
  [settings rows]       ← scrollable
  ...
  [footer: credit · version]
```

The **sticky header** (drag handle + live preview) remains fixed while the settings rows scroll beneath it. This ensures the preview is always visible during interaction.

**Max height:** `85dvh` on desktop, `80dvh` on mobile portrait, `90dvh` on landscape. The rows scroll vertically if they exceed the panel height (`overflow-y: auto`).

**Panel width must not be affected by content.** Long preview text must wrap rather than expand the panel. All flex children in the panel require `min-width: 0`.

### 5.3 Live Time Preview

A read-only, centered display of the current time phrase at the top of the settings panel (inside the sticky header). It mirrors the active font family, text content, niqqud setting, and modifier split mode in real time — updating every second and immediately on any setting change. Font size: `clamp(1.2rem, 5vw, 1.9rem)`.

### 5.4 Settings Rows

Each setting is a horizontal row: **label on the right** (Hebrew), **control(s) on the left**. Row height ~54px (58px on mobile). Rows are separated by thin hairlines. The last row has no border.

### 5.5 Font Family

Label: `גופן` | Control: `<select>` dropdown

| ID          | Name              | Weight | Niqqud |
|------------|-------------------|--------|--------|
| `heebo`     | Heebo             | 300    | Yes    |
| `rubik`     | Rubik             | 300    | Yes    |
| `frank`     | Frank Ruhl Libre  | 400    | Yes    |
| `secular`   | Secular One       | 400    | Yes    |
| `suez`      | Suez One          | 400    | Yes    |
| `bellefair` | Bellefair         | 400    | Yes    |
| `fridge`    | Fridge (custom)   | 400    | **No** |

Default: `secular`. Applied to main phrase, modifier, suffix, and live preview. Settings UI always uses Heebo.

When Fridge is selected: force niqqud off and disable the niqqud toggle. Switching away re-enables it.

Persisted: `hc_font`

### 5.6 Font Size

Label: `גודל` | Controls: `−` button · size dropdown · `+` button

- Step: 0.5vw. Range: **1.5vw – 20vw**. Default: 6.5vw.
- The dropdown lists all valid values; it updates when `+`/`−` are pressed, and pressing `+`/`−` updates the dropdown selection. Both controls are always in sync.
- Applied as `max(2.5rem, Xvw)`.

Persisted: `hc_fontVW`

### 5.7 Digital Clock

Label: `שעון דיגיטלי` | Control: toggle button (`הצג` / `הסתר`)

Shows or hides a `HH:MM:SS` digital clock above the main phrase. Uses `tabular-nums`. Hidden by default.

Persisted: `hc_digital` (`visible` / `hidden`, default: `hidden`)

### 5.8 Modifier Layout

Label: `קצת` | Control: toggle button (`שורה נפרדת` / `שורה אחת`)

Controls whether "קצת לפני/אחרי" appears on its own line (split, default) or inline with the main phrase.

Persisted: `hc_split` (default: `true`)

### 5.9 Day-Part Suffix Settings

Label: `השתמש בסיומת שעה` (with master checkbox) | Reset button: `ברירת מחדל`

The suffix section is a visually grouped block inside the settings panel. It contains:

**Master toggle** — a checkbox next to the label "השתמש בסיומת שעה". When unchecked, all suffix rows are disabled and no suffix is displayed. Checked by default.

**7 suffix rows** — one per suffix slot (see §3.6 defaults). Each row contains:
- A checkbox (enable/disable this suffix)
- The suffix Hebrew label
- A `from` hour selector (`00:00`–`23:00`)
- A `–` separator
- An `until` hour selector (`00:00`–`23:00`)

When the master toggle is unchecked, all per-suffix checkboxes and time selectors are disabled. When a per-suffix checkbox is unchecked, its time selectors are disabled.

**"ברירת מחדל" button** — resets all suffix slots and the master toggle to their defaults.

Persisted: `hc_suffix_on` (boolean string, default: `true`), `hc_suffixes` (JSON array of `{enabled, from, until}` per slot)

### 5.10 Niqqud

Label: `ניקוד` | Control: toggle button (`ללא ניקוד` / `ניקוד`)

Switches all Hebrew text between plain and niqqud (vowel diacritics) variants. Disabled when a non-niqqud font is active.

Persisted: `hc_niqqud` (default: `true`)

### 5.11 Theme

Label: `תצוגה` | Control: cycling button (`אוטו` → `יום` → `לילה` → …)

- **אוטו**: requests geolocation, calculates sunrise/sunset (see formula below), sets dark mode when `currentHour < sunrise || currentHour >= sunset`. Falls back to 06:00/18:00 if geolocation denied. Rechecks every 60 seconds.
- **יום**: always light theme
- **לילה**: always dark theme

**Solar declination formula** (sufficient accuracy for a theme toggle):
```
dayOfYear   = days since Jan 1
declination = -23.45 × cos(360/365 × (dayOfYear + 10))  [degrees]
cosH        = (sin(-0.833°) − sin(lat°) × sin(dec°)) / (cos(lat°) × cos(dec°))
hourAngle   = acos(cosH) / 15  [hours]
solarNoon   = 12 + (tzOffsetHours × 15 − longitude) / 15
sunrise     = solarNoon − hourAngle
sunset      = solarNoon + hourAngle
```
If `|cosH| > 1` (polar day/night), return null and use fixed fallback.

Geolocation cached: `hc_lat`, `hc_lng`. Persisted: `hc_theme` (default: `night`)

### 5.12 Demo Mode (הדגמה)

Label: `הדגמה` | Control: toggle button (`הדגמה` / `עצור`)

Fast-forward mode: simulated time advances 20 seconds every 400ms (~50× speed). The button turns red (light: `#fde8e8`/`#c0392b`, dark: `#3a1a1a`/`#e74c3c`) while active. Fade duration drops to 60ms.

Not persisted — always starts in real-time mode on page load.

### 5.13 Keep Screen On (מסך דלוק — always on)

Label: `מסך דלוק (always on)` | Control: toggle button (`הפעל` / `כבה`)

Uses the **Wake Lock API** (`navigator.wakeLock.request('screen')`) to prevent the screen from sleeping. Only shown when the browser supports the API. If the browser automatically releases the lock (e.g., on tab switch), it is re-acquired when the page becomes visible again (`visibilitychange` event).

Not persisted.

### 5.14 Fullscreen

Label: `מסך מלא` | Control: toggle button (`הפעל` / `צא`)

Enters/exits browser fullscreen via the Fullscreen API (`requestFullscreen` / `exitFullscreen`, with webkit prefix fallback). Only shown when `document.fullscreenEnabled` (or webkit equivalent) is true. Button label updates on `fullscreenchange` event.

Not persisted.

### 5.15 PWA Install

Label: `התקנה` | Control: button (`Install`)

Hidden by default. Shown automatically when the browser fires `beforeinstallprompt` (Chrome/Edge on Android and desktop). Clicking it calls `.prompt()` on the deferred event. The button hides after the user accepts, or on `appinstalled`.

Note: the button label is always in English (`Install`) regardless of UI language.

### 5.16 Footer

At the bottom of the scrollable panel:
- **Left/LTR side**: version string (e.g., `v2.0`), `0.88rem`, good contrast
- **Right/RTL side**: idea credit: "idea: Matty Mariansky" with a link, `0.85rem`

Version should be bumped with every change, using the scheme: `v1.9` → `v1.91` → … → `v1.99` → `v2.0`.

---

## 6. localStorage Keys

| Key            | Type    | Default     | Description                                        |
|---------------|---------|-------------|----------------------------------------------------|
| `hc_fontVW`   | float   | `6.5`       | Font size in vw units                              |
| `hc_font`     | string  | `secular`   | Selected font family ID                            |
| `hc_digital`  | string  | `hidden`    | Digital clock visibility                           |
| `hc_split`    | string  | `true`      | Modifier on separate line                          |
| `hc_suffix_on`| string  | `true`      | Master suffix enable/disable                       |
| `hc_suffixes` | JSON    | *(see §3.6)*| Array of `{enabled, from, until}` per suffix slot  |
| `hc_niqqud`   | string  | `true`      | Niqqud diacritics enabled                          |
| `hc_theme`    | string  | `night`     | Theme mode (`auto`/`day`/`night`)                  |
| `hc_lat`      | float   | *(none)*    | Cached geolocation latitude                        |
| `hc_lng`      | float   | *(none)*    | Cached geolocation longitude                       |
| `hc_seen`     | string  | *(none)*    | `'1'` after first settings interaction             |

---

## 7. Complete Text Data Reference

### 7.1 Plain Text

```
hours:    { 1:'אחת', 2:'שתיים', 3:'שלוש', 4:'ארבע', 5:'חמש', 6:'שש',
            7:'שבע', 8:'שמונה', 9:'תשע', 10:'עשר',
            11:'אחת עשרה', 12:'שתים עשרה' }

hoursL:   { 1:'לאחת', 2:'לשתיים', 3:'לשלוש', 4:'לארבע', 5:'לחמש', 6:'לשש',
            7:'לשבע', 8:'לשמונה', 9:'לתשע', 10:'לעשר',
            11:'לאחת עשרה', 12:'לשתים עשרה' }

m5:' וחמישה'  m10:' ועשרה'  m15:' ורבע'  m20:' ועשרים'
m25:' עשרים וחמש'  m30:' וחצי'  m35:' שלושים וחמש'  m40:' ארבעים'

p45:'רבע '  p50:'עשרה '  p55:'חמישה '

before:'קצת לפני'  after:'קצת אחרי'
morning:'בבוקר'  evening:'בערב'  night:'בלילה'
```

### 7.2 Niqqud Text

```
hours:    { 1:'אַחַת', 2:'שְׁתַּיִם', 3:'שָׁלוֹשׁ', 4:'אַרְבַּע', 5:'חָמֵשׁ', 6:'שֵׁשׁ',
            7:'שֶׁבַע', 8:'שְׁמוֹנֶה', 9:'תֵּשַׁע', 10:'עֶשֶׂר',
            11:'אַחַת עֶשְׂרֵה', 12:'שְׁתֵּים עֶשְׂרֵה' }

hoursL:   { 1:'לְאַחַת', 2:'לִשְׁתַּיִם', 3:'לְשָׁלוֹשׁ', 4:'לְאַרְבַּע', 5:'לְחָמֵשׁ', 6:'לְשֵׁשׁ',
            7:'לְשֶׁבַע', 8:'לִשְׁמוֹנֶה', 9:'לְתֵשַׁע', 10:'לְעֶשֶׂר',
            11:'לְאַחַת עֶשְׂרֵה', 12:'לִשְׁתֵּים עֶשְׂרֵה' }

m5:' וַחֲמִשָּׁה'  m10:' וַעֲשָׂרָה'  m15:' וָרֶבַע'  m20:' וְעֶשְׂרִים'
m25:' עֶשְׂרִים וְחָמֵשׁ'  m30:' וָחֵצִי'  m35:' שְׁלוֹשִׁים וְחָמֵשׁ'  m40:' אַרְבָּעִים'

p45:'רֶבַע '  p50:'עֲשָׂרָה '  p55:'חֲמִשָּׁה '

before:'קְצָת לִפְנֵי'  after:'קְצָת אַחֲרֵי'
morning:'בַּבֹּקֶר'  evening:'בָּעֶרֶב'  night:'בַּלַּיְלָה'
```

---

## 8. Algorithm Pseudocode

```
function getHebrewTime(date):
    t = 60 * date.minutes + date.seconds
    anchorSec = round(t / 300) * 300
    delta = t - anchorSec

    anchorHour = date.hours
    anchorMinute = anchorSec / 60

    if anchorMinute >= 60:
        anchorMinute = 0
        anchorHour = (anchorHour + 1) % 24

    phrase = buildPhrase(anchorHour, anchorMinute)

    modifier = ""
    if delta < -60: modifier = txt.before
    if delta > +60: modifier = txt.after

    effectiveHour = anchorMinute >= 45 ? (anchorHour + 1) % 24 : anchorHour
    suffix = dayPartSuffix(effectiveHour)

    return { modifier, phrase, suffix }


function dayPartSuffix(hour24):
    if not suffixMasterEnabled: return ""
    h = ((hour24 % 24) + 24) % 24
    for each slot in SUFFIX_DEFS (in order):
        if not slot.enabled: continue
        if slot.from < slot.until:
            inRange = h >= slot.from and h < slot.until
        else:                          // wraps midnight
            inRange = h >= slot.from or h < slot.until
        if inRange: return slot.text   // plain or niqqud
    return ""


function buildPhrase(anchorHour24, anchorMinute):
    h = to12(anchorHour24)
    switch anchorMinute:
        0:  return hours[h]
        5:  return hours[h] + m5
        ...
        45: return p45 + hoursL[to12(anchorHour24 + 1)]
        50: return p50 + hoursL[to12(anchorHour24 + 1)]
        55: return p55 + hoursL[to12(anchorHour24 + 1)]
```

---

## 9. Mobile & Responsive Behavior

- Font size uses `max(2.5rem, Xvw)` ensuring a readable minimum on all screens.
- The settings panel is full-width on mobile (≤600px), capped at 440px on wider screens.
- Panel max-height: `80dvh` portrait mobile, `90dvh` landscape, `85dvh` desktop.
- Panel body scrolls independently under the sticky header.
- Row height: 54px desktop, 58px mobile.
- iOS safe area inset (`env(safe-area-inset-bottom)`) is added to the bottom padding of the panel body.
- The handle pill is always visible at the bottom (no hover dependency on mobile).

---

## 10. Edge Cases

1. **Midnight (00:00)** → 12-hour conversion gives 12 → `שתים עשרה בלילה`
2. **Noon (12:00)** → hour 12 → `שתים עשרה` (no suffix by default — falls outside all enabled ranges)
3. **Hour wrap via rounding** — at 23:57:30 the anchor rounds to 00:00 next day → hour becomes 0 → displays 12 with `בלילה`
4. **Anchor at :45/:50/:55** — phrase references next hour; effective hour for suffix is also next hour
5. **Day-part boundary at :45** — e.g., 21:45 → "רבע לעשר בלילה" (effective hour 22)
6. **Fridge font** — niqqud forced off and toggle disabled; switching font re-enables toggle
7. **Geolocation denied** — auto theme falls back to sunrise 06:00 / sunset 18:00
8. **Polar latitudes** — `|cosH| > 1` → solar calc returns null → use fixed fallback
9. **Long phrases (e.g., "קצת לפני ארבע ארבעים")** — the live preview must wrap within the panel; panel width must not grow to accommodate preview text
10. **Wake lock released by browser** — re-acquire on `visibilitychange` if user has it enabled
11. **PWA already installed** — `appinstalled` event fires; hide the install button
