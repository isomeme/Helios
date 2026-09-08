package org.onereed.helios.compose.settings

import androidx.compose.runtime.Immutable
import org.onereed.helios.ui.theme.ThemeType

@Immutable
data class SettingsUi(
  val isDynamicTheme: Boolean = false,
  val themeType: ThemeType = ThemeType.SYSTEM,
  val isCompassSouthTop: Boolean = false,
  val accuracyImprovementAvailable: Boolean = false,
)
