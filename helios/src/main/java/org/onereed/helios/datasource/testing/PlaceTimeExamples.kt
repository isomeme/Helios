@file:OptIn(ExperimentalTime::class)
@file:Suppress("unused")

package org.onereed.helios.datasource.testing

import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import org.onereed.helios.datasource.PlaceTime
import org.onereed.helios.datasource.PlaceTime.Place

// Example PlaceTime values for use in previews and tests.

fun santaMonicaNow(): PlaceTime = PlaceTime(place = santaMonica, time = now())

val lahaina = Place(lat = 20.9, lon = -156.7, alt = 0.0)
val santaMonica = Place(lat = 34.0, lon = -118.5, alt = 0.0)
val svalbard = Place(lat = 77.9, lon = 21.0, alt = 0.0)

val juneSolstice = Instant.parse("2023-06-21T18:47:00.000Z")
