# Hebrew Spoken-Time Web Clock — Technical Specification v1.3

## 1. Overview

A single-file client-side web page (`index.html`) that displays the current time as a natural Hebrew spoken phrase. The clock uses the browser's local time, updates every second, and requires no server or build tools.

Example outputs:

- `שלוש` (3:00)
- `שלוש ורבע` (3:15)
- `קצת לפני רבע לארבע` (approaching 3:45)
- `אחת עשרה בלילה` (11 PM)

With niqqud enabled:

- `שָׁלוֹשׁ וְרֶבַע בַּבֹּקֶר`
- `קְצָת לִפְנֵי רֶבַע לְאַרְבַּע`

---

## 2. Technology & File Structure

- **Single HTML file** — all CSS and JS inline, no frameworks
- **Fonts**: Google Fonts (Heebo, Rubik, Frank Ruhl Libre, Secular One, Suez One, Bellefair) + one custom font
- **Custom font file**: `fonts/fridge_Regular.ttf` — loaded via `@font-face`
- **Browser APIs used**: `Date`, `setInterval`, `localStorage`, `navigator.geolocation`

```
heb-clock-web/
├── index.html
└── fonts/
    └── fridge_Regular.ttf
```

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

| Anchor | Pattern                              | Plain suffix/prefix | Niqqud suffix/prefix    |
|--------|--------------------------------------|---------------------|------------------------|
| :00    | hour only                            | *(none)*            | *(none)*               |
| :05    | hour + suffix                        | ` וחמישה`           | ` וַחֲמִישָׁה`           |
| :10    | hour + suffix                        | ` ועשרה`            | ` וַעֲשָׂרָה`            |
| :15    | hour + suffix                        | ` ורבע`             | ` וְרֶבַע`              |
| :20    | hour + suffix                        | ` ועשרים`           | ` וְעֶשְׂרִים`           |
| :25    | hour + suffix                        | ` עשרים וחמש`       | ` עֶשְׂרִים וְחָמֵשׁ`     |
| :30    | hour + suffix                        | ` וחצי`             | ` וְחֵצִי`              |
| :35    | hour + suffix                        | ` שלושים וחמש`      | ` שְׁלוֹשִׁים וְחָמֵשׁ`    |
| :40    | hour + suffix                        | ` ארבעים`           | ` אַרְבָּעִים`           |
| :45    | prefix + next hour with ל            | `רבע `              | `רֶבַע `               |
| :50    | prefix + next hour with ל            | `עשרה `             | `עֲשָׂרָה `              |
| :55    | prefix + next hour with ל            | `חמישה `            | `חֲמִישָׁה `             |

**Note:** suffixes for :05–:40 begin with a space. Prefixes for :45–:55 end with a space.

### 3.4 Second-Precision Anchor Selection

Rather than using only the minute, the clock uses second-precision rounding to the nearest 5-minute anchor.

**Definitions:**

```
t            = 60 * minute + second       (seconds since top of hour)
ANCHOR_STEP  = 300                        (5 minutes in seconds)
EXACT_WINDOW = 60                         (±60 seconds)
anchorSec    = round(t / ANCHOR_STEP) * ANCHOR_STEP
delta        = t - anchorSec
```

**Key behavior:** the anchor switches at the midpoint between two anchors. For example, 07:17:29 is anchored to 07:15, but 07:17:30 switches to 07:20.

**Hour wrap:** if `anchorSec / 60 >= 60`, set `anchorMinute = 0` and increment the hour (mod 24).

### 3.5 Modifier Prefix ("קצת לפני" / "קצת אחרי")

Based on `delta` from section 3.4:

| Condition               | Modifier (plain) | Modifier (niqqud)   |
|------------------------|-------------------|---------------------|
| `|delta| <= 60`        | *(none)*          | *(none)*            |
| `delta < -60`          | `קצת לפני`        | `קְצָת לִפְנֵי`       |
| `delta > +60`          | `קצת אחרי`        | `קְצָת אַחֲרֵי`       |

The modifier can be displayed in two layout modes (see §5.4):
- **Inline** (default): prepended to the phrase with a space
- **Split**: shown on a separate line above the main phrase, same font size

### 3.6 Day-Part Suffix

Appended after the phrase on a separate line.

The **effective hour** for the suffix depends on the anchor minute:
- Anchors :00–:40 → use the anchor's hour
- Anchors :45–:55 → use `(anchorHour + 1) % 24` (since the phrase references the next hour)

| Effective hour (24h) | Suffix (plain) | Suffix (niqqud)  |
|---------------------|---------------|-----------------|
| 05:00–09:59         | בבוקר          | בַּבֹּקֶר           |
| 10:00–17:59         | *(none)*       | *(none)*        |
| 18:00–21:59         | בערב           | בָּעֶרֶב           |
| 22:00–04:59         | בלילה          | בַּלַּיְלָה          |

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
          [digital time]           ← small, gray (hidden by default)
         [modifier line]           ← same size as main (optional, only in split mode)
        [hebrew phrase]            ← large, main display
         [day-part suffix]         ← smaller

      ─── settings handle ───      ← thin pill at bottom center
```

All elements are horizontally centered. The page is `dir="rtl"` with `lang="he"`.

### 4.2 Light Theme (default)

| Property            | Value                       |
|---------------------|-----------------------------|
| Background          | `#ffffff`                    |
| Text color          | `#1a1a1a` (all lines)        |
| Digital clock color | `#bbb`                       |
| Settings panel bg   | `#f5f5f5`                    |
| Settings buttons    | `#e8e8e8`, hover `#ddd`      |
| Handle pill         | `#d0d0d0`, hover `#aaa`      |
| Version text        | `#bbb`                       |

### 4.3 Dark Theme

Applied via `body.dark` CSS class.

| Property            | Value                       |
|---------------------|-----------------------------|
| Background          | `#121212`                    |
| Text color          | `#e8e8e8` (all lines)        |
| Digital clock color | `#555`                       |
| Settings panel bg   | `#252525`                    |
| Settings buttons    | `#3a3a3a`, hover `#484848`   |
| Handle pill         | `#444`, hover `#666`         |
| Version text        | `#444`                       |
| Separators          | `#444`                       |

### 4.4 Typography

| Element        | Font family    | Weight | Size                          |
|---------------|----------------|--------|-------------------------------|
| Body          | Heebo          | —      | —                             |
| Main phrase   | User-selected  | varies | `max(2.5rem, Xvw)` where X is adjustable (default 6.5) |
| Modifier line | User-selected  | varies | Same as main phrase           |
| Day-part      | User-selected  | varies | `max(1.3rem, 3vw)`           |
| Digital clock | Heebo          | 400    | `clamp(1rem, 2.5vw, 1.3rem)` |
| Settings UI   | Heebo          | 400    | `0.85rem`                     |

The `max()` function ensures a minimum floor of 2.5rem on mobile screens.

### 4.5 Font Size Stability

The font size must **not** change when the text content changes length. Use viewport-relative units (`vw`) with `max()` for responsive sizing. Use `white-space: nowrap` on the main phrase.

### 4.6 Transition Animation

When the displayed text changes, all text elements fade out (opacity → 0), swap content, then fade back in.

| Mode   | Fade duration |
|--------|--------------|
| Normal | 350ms        |
| Demo   | 60ms         |

If a new change occurs while a fade is in progress, cancel the pending timeout and start a new fade cycle.

---

## 5. Settings Panel

### 5.1 Discoverability

A **settings handle** (thin horizontal pill, 36×3px) is always visible at the bottom center of the screen.

- **First visit**: the handle pulses gently 3 times (opacity animation, 2s per cycle) to hint that it's interactive. After the animation or on first click, `localStorage` key `hc_seen` is set to `'1'` to suppress future hints.
- **Hover** (desktop): the pill widens from 36px to 48px and darkens.
- **Click/tap**: opens the settings panel. The handle hides while the panel is open.
- **Click outside** the panel: closes it and restores the handle.

### 5.2 Panel Behavior

- Fixed to the bottom center of the viewport.
- On **desktop**: also appears on hover over the bottom edge (via `.settings-zone:hover`).
- On **mobile** (≤600px): flex-wraps into multiple rows, separators are hidden, button sizes increase slightly.
- Shows/hides with a 300ms ease slide + opacity transition.

### 5.3 Font Family Selector

- **UI element**: `<select>` dropdown styled as a rounded button
- **Available fonts**:

| ID         | Name              | CSS Family                      | Weight | Niqqud support |
|-----------|-------------------|--------------------------------|--------|----------------|
| `heebo`    | Heebo             | `'Heebo', sans-serif`           | 300    | Yes            |
| `rubik`    | Rubik             | `'Rubik', sans-serif`           | 300    | Yes            |
| `frank`    | Frank Ruhl Libre  | `'Frank Ruhl Libre', serif`     | 400    | Yes            |
| `secular`  | Secular One       | `'Secular One', sans-serif`     | 400    | Yes            |
| `suez`     | Suez One          | `'Suez One', serif`             | 400    | Yes            |
| `bellefair`| Bellefair         | `'Bellefair', serif`            | 400    | Yes            |
| `fridge`   | Fridge            | `'Fridge', sans-serif`          | 400    | **No**         |

- Font is applied to the main phrase, modifier line, and day-part suffix. The settings panel always uses Heebo.
- **Fridge + niqqud interaction**: when Fridge is selected, niqqud is forced off and the niqqud toggle button is disabled (grayed out). When switching away from Fridge, the niqqud button is re-enabled.
- Persisted in `localStorage` key `hc_font` (default: `heebo`)

### 5.4 Font Size Control

- Label: `גודל`
- Two buttons: `−` and `+`
- Adjusts the main Hebrew text and modifier line font size in **0.5vw** increments
- Applied as `max(2.5rem, Xvw)` to maintain a mobile floor
- Range: **1.5vw** to **10vw**
- Persisted in `localStorage` key `hc_fontVW` (default: `6.5`)

### 5.5 Digital Clock Toggle

- Toggle button:
  - `הצג שעון` → click to **show** the digital time (HH:MM:SS)
  - `הסתר שעון` → click to **hide** it
- Digital clock displays in `tabular-nums` with letter-spacing for clean appearance
- Persisted in `localStorage` key `hc_digital` (`visible` / `hidden`, default: **hidden**)

### 5.6 Modifier Split Toggle

- Toggle button with two states:
  - `קצת — שורה נפרדת` → click to **enable** split mode
  - `קצת — שורה אחת` → click to return to inline mode
- **Inline mode**: modifier is prepended to the main phrase on one line
- **Split mode** (default): modifier appears on its own line above the main phrase, same font size and color
- Persisted in `localStorage` key `hc_split` (default: `true`)

### 5.7 Niqqud Toggle

- Toggle button:
  - `ניקוד` → click to **enable** niqqud (vowel diacritics)
  - `ללא ניקוד` → click to return to plain text
- Switches **all** displayed Hebrew text to niqqud variants from the data tables in §3
- **Disabled** (grayed out) when a font without niqqud support is selected (e.g., Fridge)
- Persisted in `localStorage` key `hc_niqqud` (`true` / `false`, default: **true**)

### 5.8 Theme Toggle (Night Mode)

- Cycling button with three states:
  - `תצוגה: אוטו` → auto mode (default)
  - `תצוגה: יום` → always light
  - `תצוגה: לילה` → always dark
- **Auto mode** behavior:
  - Requests browser geolocation on first use; coordinates are cached in `localStorage` keys `hc_lat` and `hc_lng`
  - Uses a solar declination formula to calculate sunrise/sunset for the current date and location
  - Dark mode is active when `currentHour < sunrise || currentHour >= sunset`
  - If geolocation is denied or unavailable, falls back to fixed times: sunrise 06:00, sunset 18:00
  - Rechecks every 60 seconds
- Persisted in `localStorage` key `hc_theme` (`auto` / `day` / `night`, default: `auto`)

**Sunset calculation (simplified solar declination):**

```
dayOfYear   = days since Jan 1
declination = -23.45 * cos(radians(360/365 * (dayOfYear + 10)))
cosH        = (sin(-0.833°) - sin(lat) * sin(dec)) / (cos(lat) * cos(dec))
hourAngle   = acos(cosH) in degrees / 15  (hours)
solarNoon   = 12 + (timezoneOffsetHours * 15 - longitude) / 15
sunrise     = solarNoon - hourAngle
sunset      = solarNoon + hourAngle
```

Accuracy: ±15 minutes, sufficient for a theme toggle.

### 5.9 Demo Mode

- Toggle button:
  - `דמו` → click to **start** demo
  - `עצור דמו` (red-tinted button) → click to **stop**
- When active:
  - Time advances by **20 seconds** of simulated time every **400ms** of real time (~50x speed)
  - The digital clock shows the simulated time
  - Fade transition duration is reduced to **60ms**
  - A full hour cycles through in ~6 seconds
- When stopped: immediately returns to real time with normal 1-second tick
- Red tint adapts to light/dark theme
- **Not persisted** — always starts in real-time mode on page load

### 5.10 Version Number

- Displayed as small gray text at the end of the settings panel
- Format: `v1.3`
- Should be bumped with each change

### 5.11 Settings Panel Visual Spec

| Property          | Light            | Dark              |
|-------------------|------------------|-------------------|
| Background        | `#f5f5f5`        | `#252525`         |
| Border radius     | 12px 12px 0 0    | same              |
| Button size       | 36×36px          | same              |
| Button background | `#e8e8e8`        | `#3a3a3a`         |
| Button hover      | `#ddd`           | `#484848`         |
| Button radius     | 8px              | same              |
| Gap between groups| 1.2rem           | same              |
| Divider           | 1px × 20px `#ddd`| 1px × 20px `#444` |
| Demo active bg    | `#fde8e8`        | `#3a1a1a`         |
| Demo active text  | `#c0392b`        | `#e74c3c`         |

---

## 6. localStorage Keys Summary

| Key          | Type    | Default     | Description                         |
|-------------|---------|-------------|-------------------------------------|
| `hc_fontVW` | float   | `6.5`       | Main text font size in vw units     |
| `hc_font`   | string  | `heebo`     | Selected font family ID             |
| `hc_digital`| string  | `hidden`    | Digital clock visibility            |
| `hc_split`  | string  | `true`      | Modifier on separate line           |
| `hc_niqqud` | string  | `true`      | Enable niqqud diacritics            |
| `hc_theme`  | string  | `auto`      | Theme mode (auto/day/night)         |
| `hc_lat`    | float   | *(none)*    | Cached geolocation latitude         |
| `hc_lng`    | float   | *(none)*    | Cached geolocation longitude        |
| `hc_seen`   | string  | *(none)*    | Set to `'1'` after first interaction|

---

## 7. Complete Text Data Reference

Store as two parallel data objects (plain and niqqud). Switch between them based on the niqqud setting.

### 7.1 Plain Text Set

```
hours:    { 1: 'אחת', 2: 'שתיים', 3: 'שלוש', 4: 'ארבע', 5: 'חמש', 6: 'שש',
            7: 'שבע', 8: 'שמונה', 9: 'תשע', 10: 'עשר',
            11: 'אחת עשרה', 12: 'שתים עשרה' }

hoursL:   { 1: 'לאחת', 2: 'לשתיים', 3: 'לשלוש', 4: 'לארבע', 5: 'לחמש', 6: 'לשש',
            7: 'לשבע', 8: 'לשמונה', 9: 'לתשע', 10: 'לעשר',
            11: 'לאחת עשרה', 12: 'לשתים עשרה' }

m5:  ' וחמישה'          m10: ' ועשרה'           m15: ' ורבע'
m20: ' ועשרים'          m25: ' עשרים וחמש'      m30: ' וחצי'
m35: ' שלושים וחמש'     m40: ' ארבעים'

p45: 'רבע '             p50: 'עשרה '            p55: 'חמישה '

before:  'קצת לפני'      after:  'קצת אחרי'
morning: 'בבוקר'         evening: 'בערב'          night: 'בלילה'
```

### 7.2 Niqqud Text Set

```
hours:    { 1: 'אַחַת', 2: 'שְׁתַּיִם', 3: 'שָׁלוֹשׁ', 4: 'אַרְבַּע', 5: 'חָמֵשׁ', 6: 'שֵׁשׁ',
            7: 'שֶׁבַע', 8: 'שְׁמוֹנֶה', 9: 'תֵּשַׁע', 10: 'עֶשֶׂר',
            11: 'אַחַת עֶשְׂרֵה', 12: 'שְׁתֵּים עֶשְׂרֵה' }

hoursL:   { 1: 'לְאַחַת', 2: 'לִשְׁתַּיִם', 3: 'לְשָׁלוֹשׁ', 4: 'לְאַרְבַּע', 5: 'לְחָמֵשׁ', 6: 'לְשֵׁשׁ',
            7: 'לְשֶׁבַע', 8: 'לִשְׁמוֹנֶה', 9: 'לְתֵּשַׁע', 10: 'לְעֶשֶׂר',
            11: 'לְאַחַת עֶשְׂרֵה', 12: 'לִשְׁתֵּים עֶשְׂרֵה' }

m5:  ' וַחֲמִישָׁה'       m10: ' וַעֲשָׂרָה'        m15: ' וְרֶבַע'
m20: ' וְעֶשְׂרִים'       m25: ' עֶשְׂרִים וְחָמֵשׁ'  m30: ' וְחֵצִי'
m35: ' שְׁלוֹשִׁים וְחָמֵשׁ' m40: ' אַרְבָּעִים'

p45: 'רֶבַע '            p50: 'עֲשָׂרָה '           p55: 'חֲמִישָׁה '

before:  'קְצָת לִפְנֵי'    after:  'קְצָת אַחֲרֵי'
morning: 'בַּבֹּקֶר'        evening: 'בָּעֶרֶב'         night: 'בַּלַּיְלָה'
```

---

## 8. Algorithm Pseudocode

```
function getHebrewTime(date):
    hour24  = date.hours
    minute  = date.minutes
    second  = date.seconds

    t = 60 * minute + second
    anchorSec = round(t / 300) * 300
    delta = t - anchorSec

    anchorHour = hour24
    anchorMinute = anchorSec / 60

    if anchorMinute >= 60:
        anchorMinute = 0
        anchorHour = (anchorHour + 1) % 24

    phrase = buildPhrase(anchorHour, anchorMinute)

    modifier = ""
    if delta < -60:  modifier = txt.before
    if delta > +60:  modifier = txt.after

    effectiveHour = anchorHour
    if anchorMinute >= 45:
        effectiveHour = (anchorHour + 1) % 24

    suffix = dayPartSuffix(effectiveHour)

    return { modifier, phrase, suffix }
```

---

## 9. Mobile Responsiveness (≤600px)

- Font sizes use `max(2.5rem, Xvw)` to guarantee a readable minimum
- Settings panel wraps into multiple rows via `flex-wrap: wrap`
- Vertical separator lines are hidden
- Button touch targets increase to 38px height
- Settings handle is always visible (not hover-dependent)

---

## 10. Edge Cases

1. **Midnight (00:00)** → hour 12 → `שתים עשרה בלילה`
2. **Noon (12:00)** → hour 12 → `שתים עשרה` (no suffix, 10–17 range)
3. **Hour wrap via anchor rounding** — e.g., at 23:57:30, anchor rounds to 00:00 of next day → hour becomes 0 → displays hour 12 with `בלילה`
4. **Anchor at :45/:50/:55** — phrase references next hour; effective hour for suffix is also next hour
5. **Day-part boundary at :45** — e.g., 21:45 → "רבע לעשר בלילה" (suffix based on hour 22, which is "night")
6. **Fridge font selected** — niqqud is forced off and toggle disabled; switching to another font re-enables the toggle
7. **Geolocation denied** — auto theme falls back to sunrise=06:00, sunset=18:00
8. **Polar latitudes** — if `|cosH| > 1`, sunset calculation returns null and fixed times are used
