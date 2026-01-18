package org.onereed.helios.compose.compass

import androidx.compose.runtime.Immutable

@Immutable
data class CompassUi(
  val compassItems: CompassItems,
  val compassAngle: Float,
  val isLocked: Boolean,
) {

  companion object {
    val INITIAL = CompassUi(CompassItems.INVALID, 0f, false)
  }
}
