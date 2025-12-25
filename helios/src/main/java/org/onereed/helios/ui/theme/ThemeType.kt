package org.onereed.helios.ui.theme

import androidx.annotation.StringRes
import org.onereed.helios.R

enum class ThemeType(@param:StringRes val labelRes: Int) {
  SYSTEM(R.string.label_system_theme),
  LIGHT(R.string.label_light_theme),
  DARK(R.string.label_dark_theme),
}
