@file:Suppress("unused")

package org.onereed.helios.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab */
abstract class BaseViewModel : ViewModel() {

  fun <T, K> StateFlow<T>.mapState(
    transform: (data: T) -> K,
  ): StateFlow<K> {
    return mapState(scope = viewModelScope, transform = transform)
  }

  fun <T, K> StateFlow<T>.mapState(
    stopTimeout: Duration,
    transform: (data: T) -> K,
  ): StateFlow<K> {
    return mapState(scope = viewModelScope, stopTimeout = stopTimeout, transform = transform)
  }

  fun <T> Flow<T>.stateIn(initialValue: T): StateFlow<T> {
    return stateIn(scope = viewModelScope, initialValue = initialValue)
  }

  fun <T> Flow<T>.stateIn(initialValue: T, stopTimeout: Duration): StateFlow<T> {
    return stateIn(scope = viewModelScope, initialValue = initialValue, stopTimeout = stopTimeout)
  }
}
