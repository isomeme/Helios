package org.onereed.helios.sun

import java.time.Instant

data class SunInfo(
    val instant: Instant,
    val sunAzimuthInfo: SunAzimuthInfo,
    val closestEventIndex: Int,
    val sunEvents: List<SunEvent>
)
