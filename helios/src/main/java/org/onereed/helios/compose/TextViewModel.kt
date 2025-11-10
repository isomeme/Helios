package org.onereed.helios.compose

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel
import timber.log.Timber

@HiltViewModel
class TextViewModel
@Inject
constructor(private val textStateHolder: TextStateHolder, private val sunResources: SunResources) :
  BaseViewModel() {

  val textUiFlow = textStateHolder.selectedIndexFlow.mapState { toTextUi(it) }

  fun selectIndex(index: Int) {
    Timber.d("selectIndex: $index")
    textStateHolder.selectIndex(index)
  }

  private fun toTextUi(selectedIndex: Int): TextUi = TextUi.create(sunResources, selectedIndex)
}
