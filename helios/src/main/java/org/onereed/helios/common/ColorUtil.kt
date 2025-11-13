package org.onereed.helios.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/** See https://stackoverflow.com/a/78143040/331864 */
fun Color.blend(topColor: Color, ratio: Float = 0.5f): Color {
  if (ratio == 0f) return this
  if (ratio == 1f) return topColor
  val intColor = ColorUtils.blendARGB(toArgb(), topColor.toArgb(), ratio)
  return Color(intColor)
}

