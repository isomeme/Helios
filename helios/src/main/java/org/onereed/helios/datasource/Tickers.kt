@file:OptIn(ExperimentalTime::class)
@file:Suppress("SameParameterValue")

package org.onereed.helios.datasource

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> tickerFlow(interval: Duration, iteratorSource: () -> Iterator<T>): Flow<T> = flow {
  val iterator = iteratorSource()

  // We emit the first item immediately, then loop with a delay before emitting each subsequent
  // item. This avoids a pointless delay after the last item.

  if (iterator.hasNext()) {
    emit(iterator.next())

    while (iterator.hasNext()) {
      delay(interval)
      emit(iterator.next())
    }
  }
}

fun countingTickerFlow(interval: Duration): Flow<Int> =
  tickerFlow(interval) {
    iterator {
      var count = 0
      while (true) yield(count++)
    }
  }

fun <T> repeatingTickerFlow(interval: Duration, value: T): Flow<T> =
  tickerFlow(interval) { iterator { while (true) yield(value) } }

fun unitTickerFlow(interval: Duration): Flow<Unit> = repeatingTickerFlow(interval, Unit)
