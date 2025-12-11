package org.onereed.helios.compose.theme

import androidx.annotation.StringRes
import org.onereed.helios.R

enum class ThemeType {
  SYSTEM(R.string.system_theme),
  LIGHT(R.string.light_theme),
  DARK(R.string.dark_theme);

  val labelRes: Int

  constructor(@StringRes labelRes: Int) {
    this.labelRes = labelRes
  }
}
