package org.onereed.helios.common

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

/** See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab */
object FlowUtils {

  private val FLOW_TIMEOUT_MILLIS = 5.seconds.inWholeMilliseconds

  @OptIn(ExperimentalCoroutinesApi::class)
  fun <T, K> StateFlow<T>.mapState(scope: CoroutineScope, transform: (data: T) -> K): StateFlow<K> {
    return mapLatest { transform(it) }
      .stateIn(scope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MILLIS), transform(value))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  fun <T, K> StateFlow<T>.mapState(
    scope: CoroutineScope,
    initialValue: K,
    transform: suspend (data: T) -> K,
  ): StateFlow<K> {
    return mapLatest { transform(it) }
      .stateIn(scope, SharingStarted.WhileSubscribed(FLOW_TIMEOUT_MILLIS), initialValue)
  }
}
