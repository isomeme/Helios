package org.onereed.helios.compose.settings

import androidx.compose.runtime.Immutable
import org.onereed.helios.ui.theme.ThemeType

@Immutable
data class SettingsUi(
  val isDynamicTheme: Boolean,
  val themeType: ThemeType,
  val isCompassSouthTop: Boolean,
) {
  companion object {
    val INITIAL =
      SettingsUi(isDynamicTheme = false, themeType = ThemeType.SYSTEM, isCompassSouthTop = false)
  }
}
