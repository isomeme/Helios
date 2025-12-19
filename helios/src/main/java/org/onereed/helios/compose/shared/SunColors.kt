package org.onereed.helios.compose.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.onereed.helios.ui.theme.ColorFamily
import org.onereed.helios.ui.theme.extendedColors

@Composable
fun sunColors(): List<ColorFamily> {
  return listOf(
    MaterialTheme.extendedColors.rise,
    MaterialTheme.extendedColors.noon,
    MaterialTheme.extendedColors.set,
    MaterialTheme.extendedColors.nadir,
  )
}
