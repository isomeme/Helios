@file:OptIn(ExperimentalTime::class)

package org.onereed.helios.datasource

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> tickerFlow(interval: Duration, iteratorSource: () -> Iterator<T>): Flow<T> {
  return flow {
    val iterator = iteratorSource()
    while (iterator.hasNext()) {
      emit(iterator.next())
      if (iterator.hasNext()) delay(interval)
    }
  }
}

fun countingTickerFlow(interval: Duration): Flow<Int> {
  return tickerFlow(interval) {
    iterator {
      var count = 0
      while (true) yield(count++)
    }
  }
}

fun <T> repeatingTickerFlow(interval: Duration, value: T): Flow<T> {
  return tickerFlow(interval) { iterator { while (true) yield(value) } }
}
