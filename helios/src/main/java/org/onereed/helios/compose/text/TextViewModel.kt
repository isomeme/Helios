package org.onereed.helios.compose.text

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.common.mapState

@HiltViewModel
class TextViewModel
@Inject
constructor(
  uiFactory: TextUi.Factory,
  textStateHolder: TextStateHolder,
  val selectTextIndex: SelectTextIndexUseCase,
) : BaseViewModel() {

  val textUiFlow = textStateHolder.selectedIndexFlow.mapState(viewModelScope, uiFactory::create)
}
