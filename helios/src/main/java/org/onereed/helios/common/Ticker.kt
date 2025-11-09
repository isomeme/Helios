package org.onereed.helios.common

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.shareIn
import timber.log.Timber

class Ticker(
  val interval: Duration,
  val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {

  @OptIn(ExperimentalTime::class)
  val flow =
    flow {
        var count = 0
        while (true) {
          delay(interval)
          ++count
          Timber.d("Ticker count: $count")
          emit(Unit)
        }
      }
      .onStart { Timber.d("Ticker.onStart") }
      .onCompletion { Timber.d("Ticker.onCompletion") }
      .shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
      )
      .onSubscription { Timber.d("Ticker.onSubscription") }
}
