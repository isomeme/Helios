package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class HeliosAppViewModel @Inject constructor(private val textStateHolder: TextStateHolder) :
  ViewModel() {

  fun selectTextIndex(index: Int) {
    Timber.d("selectTextIndex: $index")
    textStateHolder.selectIndex(index)
  }
}
