# Hebrew Spoken-Time Clock

A web clock that displays the current time as a natural Hebrew phrase, the way you'd say it in conversation.

**Live demo:** [shaik.github.io/heb-clock-web](https://shaik.github.io/heb-clock-web/)

## Examples

| Time    | Display                    |
|---------|----------------------------|
| 3:00 AM | שלוש בבוקר                  |
| 7:15 AM | שבע ורבע בבוקר              |
| 7:17 AM | קצת אחרי שבע ורבע בבוקר     |
| 9:45 PM | רבע לעשר בערב               |
| 11:00 PM| אחת עשרה בלילה              |

## Features

- **Spoken Hebrew time** — 5-minute anchors with natural phrasing (:00 through :55)
- **Second-precision** — smoothly transitions between "קצת לפני" (a bit before), exact, and "קצת אחרי" (a bit after)
- **Day-part suffixes** — בבוקר (morning), בערב (evening), בלילה (night)
- **Niqqud mode** — optional vowel diacritics (e.g., שֶׁבַע וְרֶבַע בַּבֹּקֶר)
- **Adjustable font size** — +/− controls
- **Demo mode** — fast-forward through time to see all phrase transitions
- **Settings panel** — hidden by default, appears on hover at the bottom edge
- **No dependencies** — single HTML file, runs entirely client-side

## Usage

Open `index.html` in a browser. That's it.

## Settings

Hover the bottom edge of the screen to reveal the settings panel:

| Control       | Description                                         |
|---------------|-----------------------------------------------------|
| גודל +/−      | Adjust font size                                    |
| הסתר/הצג שעון | Show or hide the digital clock                      |
| קצת — שורה    | Show "קצת לפני/אחרי" on a separate line or inline   |
| ניקוד          | Toggle Hebrew vowel diacritics                      |
| דמו            | Fast-forward demo mode (50x speed)                  |

All settings (except demo) are saved in localStorage.

## Tech

Single `index.html` with inline CSS and JavaScript. Uses [Heebo](https://fonts.google.com/specimen/Heebo) from Google Fonts.
