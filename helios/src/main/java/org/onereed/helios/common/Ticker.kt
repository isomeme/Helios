package org.onereed.helios.common

import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class Ticker(
  val interval: Duration,
  val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {

  @OptIn(ExperimentalTime::class)
  val stateFlow =
    flow {
        while (true) {
          delay(interval)
          val t = now()
          Timber.d("Ticker.emit: $t")
          emit(t)
        }
      }
      .onCompletion { Timber.d("Ticker.onCompletion") }
      .stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        initialValue = now(),
      )
}
