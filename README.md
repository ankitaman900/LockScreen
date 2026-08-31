# Lock Screen (Jetpack Compose)

A single-screen Android app that **draws** a modern, Vivo-*inspired* lock screen
inside its own window.

> **This is a visual mock-up only.** It does not replace, modify, bypass or
> interfere with the real Android system lock screen, it declares **no
> permissions**, it makes no network calls, and every graphic is drawn in code —
> no vendor assets, logos or branding are used.

---

## What it does

The screen looks and behaves like an ordinary lock screen. Whatever is entered
is **never read, compared or validated**. Only the number of submissions matters:

| Submission | What happens |
| --- | --- |
| 1st | Entry clears, a short shake, you stay on the lock screen |
| 2nd | Same |
| 3rd | Same |
| 4th | Brief unlock animation, then the app closes and you land on your normal home screen |

No "wrong password" text, no dialog, no toast, no success message, no extra
screens.

### The two hidden controls

**Top-left corner — unlock style.** A transparent 100 x 56 dp strip in the very
top-left corner (over the status bar area, where nothing else is tappable). A
single tap opens a panel offering **4-digit PIN**, **6-digit PIN** or
**Pattern**, plus **Clear saved entries**. The choice is remembered across
launches.

**"Emergency call" — the reveal.** The label at the bottom is a decoy: tapping it
does nothing at all. **Long-press** it and a card shows the last two entries that
were submitted, newest first. This does not count as an attempt.

Because the app closes itself on the fourth attempt, the entries are saved to the
app's private storage — otherwise they would be gone before they could be read
back. Nothing leaves the device (the app has no network permission), and "Clear
saved entries" wipes the file.

### Entry styles

* **4-digit / 6-digit PIN** — the entry submits itself the moment the last digit
  lands, exactly like a real fixed-length PIN screen. There is no confirm
  button; backspace only appears once there is something to delete.
* **Pattern** — a 3x3 grid. Dragging across an unselected dot on the way between
  two others picks it up too, like the system pattern lock. Lifting the finger
  submits. A pattern is recorded as one-based dot numbers, e.g. `1-2-3-6`.

## Project structure

```
LockScreen/
├── settings.gradle.kts            Module list + repositories
├── build.gradle.kts               Root build file (plugins, versions only)
├── gradle.properties
├── gradle/libs.versions.toml      Version catalog — every dependency version
└── app/
    ├── build.gradle.kts           Android + Compose configuration
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml         No <uses-permission> at all
        │   ├── java/com/example/lockscreen/
        │   │   ├── MainActivity.kt         Edge-to-edge window + exit-to-home
        │   │   ├── domain/
        │   │   │   ├── UnlockAttempts.kt   Pure Kotlin attempt rule
        │   │   │   ├── UnlockMode.kt       4-digit / 6-digit / pattern
        │   │   │   └── AttemptHistory.kt   Recent-entry store (interface)
        │   │   ├── data/
        │   │   │   └── PrefsAttemptHistory.kt  SharedPreferences implementation
        │   │   └── ui/
        │   │       ├── theme/              Colors, typography, Material 3 theme
        │   │       └── lockscreen/
        │   │           ├── LockScreenUiState.kt   State + one-shot effects
        │   │           ├── LockScreenViewModel.kt Attempt counter
        │   │           ├── LockScreen.kt          Screen (stateful + stateless)
        │   │           └── components/
        │   │               ├── LockWallpaper.kt   Procedural wallpaper
        │   │               ├── StatusBarRow.kt    Decorative status bar
        │   │               ├── ClockSection.kt    Clock, date, padlock
        │   │               ├── CurrentTime.kt     Ticking-clock helpers
        │   │               ├── PinIndicator.kt    Animated PIN dots
        │   │               ├── Keypad.kt          Circular 0-9 keypad
        │   │               └── Glyphs.kt          Hand-drawn Canvas icons
        └── test/java/com/example/lockscreen/
            └── AttemptLogicTest.kt         JVM unit tests for the rule
```

### Layering (clean architecture, kept proportionate to the app's size)

* **domain/** — `RegisterAttemptUseCase`. Pure Kotlin, zero Android imports, so
  the rule is trivially unit-testable.
* **ui/lockscreen/** — `LockScreenViewModel` holds the state; `LockScreenRoute`
  connects it to `LockScreenContent`, which is completely stateless and
  previewable.
* **ui/lockscreen/components/** — small, reusable, self-contained composables.

---

## Requirements

* **Android Studio** Ladybug (2024.2.1) or newer
* **JDK 17** (bundled with Android Studio)
* An emulator or device running **Android 7.0 (API 24) or newer**

Versions used: AGP 8.7.2 · Kotlin 2.0.21 · Gradle 8.9 · Compose BOM 2024.10.01 ·
compileSdk 35.

---

## Step-by-step: running it in Android Studio

1. **Open the project**
   `File → Open…`, select the `LockScreen` folder (the one containing
   `settings.gradle.kts`), then click **Open**.

2. **Let the Gradle wrapper finish setting itself up**
   The repo ships `gradle/wrapper/gradle-wrapper.properties` but not the binary
   `gradle-wrapper.jar`. Android Studio downloads Gradle 8.9 for you on the first
   sync, so normally there is nothing to do.
   If you prefer a working `./gradlew` on the command line, run once (with a
   system Gradle installed):
   ```bash
   gradle wrapper --gradle-version 8.9
   ```

3. **Sync**
   Accept the "Gradle sync" prompt, or `File → Sync Project with Gradle Files`.
   The first sync downloads the dependencies and needs an internet connection —
   the finished app itself works fully offline.

4. **Pick a device**
   Choose an emulator (`Device Manager → Create device`, e.g. Pixel 7, API 34)
   or plug in a phone with USB debugging enabled.

5. **Run**
   Press the green ▶ **Run 'app'** button (or `Ctrl/Cmd + R`).

6. **Try it**
   Type any number, press the ➜ button. Repeat. On the **fourth** press the app
   fades out and you are back on your home screen.

7. **Optional — run the unit tests**
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```
   or right-click `AttemptLogicTest` in Android Studio and choose **Run**.

---

## How the exit works

`MainActivity.exitToHomeScreen()` calls **`finishAndRemoveTask()`**. That
finishes the Activity and removes its task from the recents list; since this
Activity is the root of its task, Android shows whatever was behind it — the
launcher. No permission, no `Intent` to the home screen, no background service.

---

## Notes and customisation

* **Back button** is swallowed (`BackHandler` in `MainActivity`) so the lock
  screen cannot be dismissed early. Home still works, so you are never trapped.
  Remove the `LockScreenHost` wrapper if you would rather Back closed the app.
* **Number of attempts** — change `ATTEMPTS_BEFORE_EXIT` in
  `domain/UnlockAttempts.kt`.
* **Hidden zone size / position** — the `Box` aligned `TopStart` at the end of
  `LockScreenContent` in `ui/lockscreen/LockScreen.kt`.
* **How many entries are remembered** — `MAX_ENTRIES` in
  `domain/AttemptHistory.kt`.
* **Minimum pattern length** — `MIN_PATTERN_DOTS` in `domain/UnlockMode.kt`.
* **Colours / wallpaper** — `ui/theme/Color.kt` and
  `components/LockWallpaper.kt`. The wallpaper is 100 % procedural, so there is
  no image to swap out; edit the gradient stops and blooms instead.
* **12 / 24-hour clock and the date format** follow the device settings and
  locale automatically.
