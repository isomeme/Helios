package org.onereed.helios.datasource

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@OptIn(ExperimentalTime::class)
class Ticker(private val interval: Duration, val name: String = "Ticker") {

  val flow =
    flow {
        var count = 0
        while (true) {
          emit(count++)
          delay(interval)
        }
      }
      .onStart { Timber.d("$name start") }
      .onCompletion { Timber.d("$name stop") }
}
