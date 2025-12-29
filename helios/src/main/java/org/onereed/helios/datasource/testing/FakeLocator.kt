@file:OptIn(ExperimentalTime::class)

package org.onereed.helios.datasource.testing

import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.onereed.helios.common.ApplicationScope
import org.onereed.helios.common.stateIn
import org.onereed.helios.datasource.Locator
import org.onereed.helios.datasource.PlaceTime
import org.onereed.helios.datasource.Ticker
import timber.log.Timber

class FakeLocator
@Inject
constructor(@param:ApplicationScope private val externalScope: CoroutineScope) : Locator {

  private val ticker = Ticker(tickerInterval)
  private val _placeTimeFlow =
    ticker.flow
      .map { tick -> t0 + dt * tick }
      .onEach { time -> Timber.d("time: ${timeFormat(time)}") }
      .map { time -> PlaceTime(place, time) }
      .stateIn(scope = externalScope, initialValue = PlaceTime.EMPTY, stopTimeout = 0.seconds)

  override fun placeTimeFlow(): StateFlow<PlaceTime> {
    return _placeTimeFlow
  }

  companion object {

    private val place = honolulu
    private val timeZone = honoluluTimeZone
    private val t0 = lahainaNoon2
    private val dt = 30.minutes
    private val tickerInterval = 500.milliseconds

    private fun timeFormat(time: Instant): String {
      val localTime = time.toLocalDateTime(timeZone)
      return formatter.format(localTime)
    }

    private val formatter =
      LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
        char(' ')
        hour()
        char(':')
        minute()
      }
  }
}
