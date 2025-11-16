package org.onereed.helios.common

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/** See https://stackoverflow.com/a/78143040/331864 */
@Suppress("unused")
fun Color.blend(towardColor: Color, @FloatRange(from = 0.0, to = 1.0) ratio: Float = 0.5f): Color {
  return when (ratio) {
    0f -> this
    1f -> towardColor
    else -> {
      val intColor = ColorUtils.blendARGB(toArgb(), towardColor.toArgb(), ratio)
      Color(intColor)
    }
  }
}
