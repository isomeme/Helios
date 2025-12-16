package org.onereed.helios.compose.app

import androidx.compose.runtime.Immutable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Immutable
data class NavActions(val navigateTo: (Screen) -> Unit) {
  constructor(
    heliosAppState: HeliosAppState,
    haptics: HapticFeedback,
  ) : this(
    navigateTo = { screen ->
      haptics.performHapticFeedback(HapticFeedbackType.Confirm)
      heliosAppState.navigateTo(screen)
    },
  )
}
