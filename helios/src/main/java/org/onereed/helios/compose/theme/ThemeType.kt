package org.onereed.helios.compose.theme

import androidx.annotation.StringRes
import org.onereed.helios.R

enum class ThemeType {
  SYSTEM(R.string.label_system_theme),
  LIGHT(R.string.label_light_theme),
  DARK(R.string.label_dark_theme);

  val labelRes: Int

  constructor(@StringRes labelRes: Int) {
    this.labelRes = labelRes
  }
}
