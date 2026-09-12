package org.onereed.helios.compose.compass

import androidx.compose.runtime.Immutable

@Immutable
data class CompassUi(
  val compassItems: CompassItems,
  val compassAngle: Float,
  val isLocked: Boolean,
)
