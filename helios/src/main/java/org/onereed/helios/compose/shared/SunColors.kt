package org.onereed.helios.compose.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.onereed.helios.ui.theme.ColorFamily
import org.onereed.helios.ui.theme.extendedColors

@Composable
fun sunColors(): List<ColorFamily> {
  val riseColors = MaterialTheme.extendedColors.rise
  val noonColors = MaterialTheme.extendedColors.noon
  val setColors = MaterialTheme.extendedColors.set
  val nadirColors = MaterialTheme.extendedColors.nadir

  return remember(riseColors, noonColors, setColors, nadirColors) {
    listOf(riseColors, noonColors, setColors, nadirColors)
  }
}
