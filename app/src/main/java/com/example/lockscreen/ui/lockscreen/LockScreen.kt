package com.example.lockscreen.ui.lockscreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lockscreen.R
import com.example.lockscreen.data.PrefsAttemptHistory
import com.example.lockscreen.domain.UnlockMode
import com.example.lockscreen.ui.lockscreen.components.ClockSection
import com.example.lockscreen.ui.lockscreen.components.EmergencyCall
import com.example.lockscreen.ui.lockscreen.components.Keypad
import com.example.lockscreen.ui.lockscreen.components.LockWallpaper
import com.example.lockscreen.ui.lockscreen.components.ModePickerPanel
import com.example.lockscreen.ui.lockscreen.components.PatternGrid
import com.example.lockscreen.ui.lockscreen.components.PinIndicator
import com.example.lockscreen.ui.lockscreen.components.RevealCard
import com.example.lockscreen.ui.lockscreen.components.StatusBarRow
import com.example.lockscreen.ui.theme.InkFaint
import com.example.lockscreen.ui.theme.LockScreenTheme

/**
 * Builds a ViewModel backed by the on-disk history. Keeping the factory here
 * rather than inside the ViewModel means the ViewModel never has to know that a
 * `data` layer exists.
 */
@Composable
private fun rememberViewModelFactory(): ViewModelProvider.Factory {
    val context = LocalContext.current
    return remember(context) {
        viewModelFactory {
            initializer { LockScreenViewModel(PrefsAttemptHistory(context)) }
        }
    }
}

/**
 * Stateful entry point.
 *
 * It wires the [LockScreenViewModel] to the stateless [LockScreenContent] and
 * owns the two animations the ViewModel can request through its effect flow:
 * the shake (attempts 1-3) and the unlock (attempt 4).
 *
 * @param onExitToHome called once the unlock animation has finished. The
 *        Activity uses it to close the app and hand control back to the
 *        device's launcher.
 */
@Composable
fun LockScreenRoute(
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LockScreenViewModel = viewModel(factory = rememberViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Horizontal displacement used by the shake animation.
    val shakeOffset = remember { Animatable(0f) }
    // 0f = locked, 1f = fully unlocked. Drives the exit animation.
    val unlockProgress = remember { Animatable(0f) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                // Attempts 1-3. No dialog, no toast, no "wrong password" text -
                // the screen just twitches and stays locked.
                LockScreenEffect.Shake -> {
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 420
                            0f at 0
                            -16f at 60
                            14f at 120
                            -10f at 190
                            7f at 260
                            -3f at 330
                            0f at 420
                        }
                    )
                }

                // Attempt 4: a short unlock flourish, then leave the app.
                LockScreenEffect.Unlock -> {
                    unlockProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
                    )
                    onExitToHome()
                }
            }
        }
    }

    LockScreenContent(
        uiState = uiState,
        shakeOffset = shakeOffset.value,
        unlockProgress = unlockProgress.value,
        onDigit = viewModel::onDigitEntered,
        onBackspace = viewModel::onBackspace,
        onPatternChanged = viewModel::onPatternChanged,
        onPatternCompleted = viewModel::onPatternCompleted,
        onHiddenZoneTapped = viewModel::onHiddenZoneTapped,
        onModeSelected = viewModel::onModeSelected,
        onClearHistory = viewModel::onClearHistory,
        onModePickerDismissed = viewModel::onModePickerDismissed,
        onEmergencyLongPressed = viewModel::onEmergencyLongPressed,
        onRevealDismissed = viewModel::onRevealDismissed,
        modifier = modifier
    )
}

/**
 * Stateless UI. Everything it needs arrives as parameters, which keeps it easy
 * to preview in Android Studio and easy to reason about.
 */
@Composable
fun LockScreenContent(
    uiState: LockScreenUiState,
    shakeOffset: Float,
    unlockProgress: Float,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onPatternChanged: (List<Int>) -> Unit,
    onPatternCompleted: () -> Unit,
    onHiddenZoneTapped: () -> Unit,
    onModeSelected: (UnlockMode) -> Unit,
    onClearHistory: () -> Unit,
    onModePickerDismissed: () -> Unit,
    onEmergencyLongPressed: () -> Unit,
    onRevealDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // The foreground fades out and pushes forward slightly as the app unlocks.
    val contentAlpha = 1f - unlockProgress
    val contentScale = 1f + 0.10f * unlockProgress
    // A soft flash that peaks half way through the unlock animation.
    val flashAlpha = 4f * unlockProgress * (1f - unlockProgress) * 0.22f

    Box(modifier = modifier.fillMaxSize()) {

        // Layer 1 - the wallpaper, which zooms in a touch during the unlock.
        LockWallpaper(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1f + 0.06f * unlockProgress
                    scaleY = 1f + 0.06f * unlockProgress
                }
        )

        // Layer 2 - all of the lock-screen furniture.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer {
                    translationX = shakeOffset
                    alpha = contentAlpha
                    scaleX = contentScale
                    scaleY = contentScale
                }
        ) {
            // heightIn(min = maxHeight) makes the column fill the screen when
            // there is room (so SpaceBetween spreads the sections out) while
            // still allowing it to scroll on very short screens, e.g. landscape.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBarRow()

                ClockSection(modifier = Modifier.padding(top = 20.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 18.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (uiState.mode.isPin) R.string.enter_pin else R.string.draw_pattern
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkFaint
                    )

                    Spacer(Modifier.height(16.dp))

                    if (uiState.mode.isPin) {
                        PinIndicator(
                            filled = uiState.pin.length,
                            capacity = uiState.mode.pinLength
                        )

                        Spacer(Modifier.height(26.dp))

                        Keypad(
                            onDigit = onDigit,
                            onBackspace = onBackspace,
                            enabled = !uiState.isInputBlocked,
                            showBackspace = uiState.pin.isNotEmpty(),
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .padding(horizontal = 8.dp)
                        )
                    } else {
                        Spacer(Modifier.height(10.dp))

                        PatternGrid(
                            selected = uiState.pattern,
                            enabled = !uiState.isInputBlocked,
                            onChanged = onPatternChanged,
                            onCompleted = onPatternCompleted,
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // The decoy. Long-pressing it opens the reveal.
                    EmergencyCall(onLongPress = onEmergencyLongPressed)
                }
            }

            // The invisible control: a transparent strip in the top-left corner
            // that opens the hidden settings panel. It sits above the status bar
            // row, where nothing else is tappable.
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(width = 100.dp, height = 56.dp)
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        onClickLabel = stringResource(R.string.cd_hidden_settings),
                        onClick = onHiddenZoneTapped
                    )
            )
        }

        // Layer 3 - the unlock flash.
        if (flashAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flashAlpha))
            )
        }

        // Layer 4 - overlays.
        if (uiState.isModePickerVisible) {
            ModePickerPanel(
                selected = uiState.mode,
                onModeSelected = onModeSelected,
                onClearHistory = onClearHistory,
                onDismiss = onModePickerDismissed
            )
        }

        uiState.revealedEntries?.let { entries ->
            RevealCard(entries = entries, onDismiss = onRevealDismissed)
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun LockScreenPreview() {
    LockScreenTheme {
        LockScreenContent(
            uiState = LockScreenUiState(mode = UnlockMode.PIN_4, pin = "12"),
            shakeOffset = 0f,
            unlockProgress = 0f,
            onDigit = {},
            onBackspace = {},
            onPatternChanged = {},
            onPatternCompleted = {},
            onHiddenZoneTapped = {},
            onModeSelected = {},
            onClearHistory = {},
            onModePickerDismissed = {},
            onEmergencyLongPressed = {},
            onRevealDismissed = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun PatternPreview() {
    LockScreenTheme {
        LockScreenContent(
            uiState = LockScreenUiState(
                mode = UnlockMode.PATTERN,
                pattern = listOf(0, 1, 2, 4, 6)
            ),
            shakeOffset = 0f,
            unlockProgress = 0f,
            onDigit = {},
            onBackspace = {},
            onPatternChanged = {},
            onPatternCompleted = {},
            onHiddenZoneTapped = {},
            onModeSelected = {},
            onClearHistory = {},
            onModePickerDismissed = {},
            onEmergencyLongPressed = {},
            onRevealDismissed = {}
        )
    }
}
