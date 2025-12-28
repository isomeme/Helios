@file:OptIn(ExperimentalTime::class)
@file:Suppress("unused")

package org.onereed.helios.datasource

import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

// Example PlaceTime values for use in previews and tests.

fun santaMonicaNow(): PlaceTime = PlaceTime(lat = 34.0, lon = -118.0, alt = 0.0, instant = now())

private val juneSolstice = Instant.parse("2023-06-21T18:47:00.000Z")

val lahainaJuneSolstice = PlaceTime(lat = 20.9, lon = -156.7, alt = 0.0, instant = juneSolstice)

val svalbardJuneSolstice = PlaceTime(lat = 77.9, lon = 21.0, alt = 0.0, instant = juneSolstice)
