package org.onereed.helios.compose.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import org.onereed.helios.ui.theme.ColorFamily
import org.onereed.helios.ui.theme.extendedColors

/**
 * The sun color lists place the values for the 4 sun events at the usual ordinal indices 0-3, and
 * for the sun (and sun direction arrow) at ordinal index 4.
 */
const val SUN_ORDINAL = 4

@Composable
fun sunColorFamilies(): List<ColorFamily> {
  val riseColors = MaterialTheme.extendedColors.rise
  val noonColors = MaterialTheme.extendedColors.noon
  val setColors = MaterialTheme.extendedColors.set
  val nadirColors = MaterialTheme.extendedColors.nadir
  val sunColors = MaterialTheme.extendedColors.sun

  return remember(riseColors, noonColors, setColors, nadirColors, sunColors) {
    listOf(riseColors, noonColors, setColors, nadirColors, sunColors)
  }
}

@Composable
fun sunColorFilters(): List<ColorFilter> {
  val sunColorFamilies = sunColorFamilies()

  return sunColorFamilies.map(ColorFamily::color).map { color ->
    ColorFilter.tint(color = color, blendMode = BlendMode.SrcIn)
  }
}
