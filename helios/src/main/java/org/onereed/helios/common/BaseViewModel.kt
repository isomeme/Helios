package org.onereed.helios.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow

/** See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab */
abstract class BaseViewModel : ViewModel() {

  fun <T, K> StateFlow<T>.mapState(transform: (data: T) -> K): StateFlow<K> {
    return mapState(scope = viewModelScope, transform = transform)
  }

  @Suppress("unused")
  fun <T, K> StateFlow<T>.mapState(
    initialValue: K,
    transform: suspend (data: T) -> K,
  ): StateFlow<K> {
    return mapState(scope = viewModelScope, initialValue = initialValue, transform = transform)
  }
}
