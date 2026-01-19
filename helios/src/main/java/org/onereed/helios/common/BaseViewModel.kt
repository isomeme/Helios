package org.onereed.helios.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** See https://proandroiddev.com/clean-stateflow-transformations-in-kotlin-608f4c7de5ab */
abstract class BaseViewModel : ViewModel() {

  fun <T> Flow<T>.stateIn(initialValue: T): StateFlow<T> {
    return stateIn(scope = viewModelScope, initialValue = initialValue)
  }
}
