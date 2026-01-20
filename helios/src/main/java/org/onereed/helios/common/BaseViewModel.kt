package org.onereed.helios.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab */
abstract class BaseViewModel : ViewModel() {

  fun <T, K> StateFlow<T>.mapState(transform: (data: T) -> K): StateFlow<K> =
    mapState(scope = viewModelScope, transform = transform)

  fun <T> Flow<T>.stateIn(initialValue: T): StateFlow<T> =
    stateIn(scope = viewModelScope, initialValue = initialValue)
}
