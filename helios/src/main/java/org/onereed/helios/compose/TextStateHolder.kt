package org.onereed.helios.compose

import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@Singleton
class TextStateHolder @Inject constructor() {

  private val _selectedIndexFlow = MutableStateFlow(0)

  val selectedIndexFlow = _selectedIndexFlow.asStateFlow()

  fun selectIndex(index: Int) {
    _selectedIndexFlow.value = index
  }
}
