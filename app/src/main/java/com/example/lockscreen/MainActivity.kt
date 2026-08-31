package com.example.lockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.lockscreen.ui.lockscreen.LockScreenRoute
import com.example.lockscreen.ui.theme.LockScreenTheme

/**
 * The single Activity of the app.
 *
 * IMPORTANT – what this app is and is not:
 *  - it is an ordinary, sandboxed app that *draws* something that looks like a
 *    lock screen inside its own window;
 *  - it does NOT replace, modify, bypass or interfere with the real Android
 *    system lock screen, and it requests no permissions whatsoever.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the system bars so the wallpaper covers the whole screen.
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemStatusBar()

        setContent {
            LockScreenTheme {
                LockScreenHost(onExitToHome = ::exitToHomeScreen)
            }
        }
    }

    /**
     * Hides the real status bar so the app can show its own decorative one.
     * A swipe from the edge brings the system bars back temporarily, exactly
     * like any full-screen app (a video player, a game...).
     */
    private fun hideSystemStatusBar() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * Closes the app after the fourth attempt and returns the user to the
     * device's normal launcher / home screen.
     *
     * `finishAndRemoveTask()` finishes this Activity *and* removes its task from
     * the recents list. Because this Activity is the root of its task, Android
     * then shows whatever was behind it – the home screen. Nothing else is
     * launched and no special permission is involved.
     */
    private fun exitToHomeScreen() {
        finishAndRemoveTask()

        // A plain cross-fade instead of the default "close" animation, so the
        // transition matches the unlock effect that just played.
        @Suppress("DEPRECATION")
        overridePendingTransition(0, android.R.anim.fade_out)
    }
}

/**
 * Small wrapper so the back-button behaviour lives next to the screen it
 * guards.
 *
 * A lock screen that vanished on Back would break the illusion, so Back is
 * swallowed here. The Home button and gesture keep working normally, so the
 * user is never trapped. Delete this composable (and call [LockScreenRoute]
 * directly) if you would rather let Back close the app.
 */
@Composable
private fun LockScreenHost(onExitToHome: () -> Unit) {
    BackHandler(enabled = true) {
        // Intentionally empty: swallow the back press.
    }
    LockScreenRoute(onExitToHome = onExitToHome)
}
