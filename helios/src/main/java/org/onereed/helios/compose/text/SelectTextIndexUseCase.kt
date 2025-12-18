package org.onereed.helios.compose.text

import javax.inject.Inject

class SelectTextIndexUseCase @Inject constructor(private val textStateHolder: TextStateHolder) {

  operator fun invoke(index: Int) {
    textStateHolder.selectIndex(index)
  }
}
