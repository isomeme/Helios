package org.onereed.helios.compose.text

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel

@HiltViewModel
class TextViewModel
@Inject
constructor(uiFactory: TextUi.Factory, private val textStateHolder: TextStateHolder) :
  BaseViewModel() {

  val textUiFlow = textStateHolder.selectedIndexFlow.mapState { uiFactory.create(it) }

  fun selectTextIndex(index: Int) {
    textStateHolder.selectIndex(index)
  }
}
