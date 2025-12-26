@file:OptIn(ExperimentalCoroutinesApi::class)

package org.onereed.helios.common

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

// See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab

fun <T, K> StateFlow<T>.mapState(
  scope: CoroutineScope,
  stopTimeout: Duration? = null,
  transform: (data: T) -> K,
): StateFlow<K> {
  return mapLatest { transform(it) }
    .stateIn(scope = scope, initialValue = transform(value), stopTimeout = stopTimeout)
}

fun <T> Flow<T>.stateIn(
  scope: CoroutineScope,
  initialValue: T,
  stopTimeout: Duration? = null,
): StateFlow<T> {
  val stopTimeoutMillis = stopTimeout?.inWholeMilliseconds ?: defaultStopTimeoutMillis

  return stateIn(
    scope = scope,
    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = stopTimeoutMillis),
    initialValue = initialValue,
  )
}

/**
 * We build in a short delay before unsubscribing so that established flows survive brief
 * interruptions for e.g. navigation events or portrait/landscape swaps.
 */
private val defaultStopTimeoutMillis = 2.seconds.inWholeMilliseconds
