package org.onereed.helios.compose.text

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel

@HiltViewModel
class TextViewModel
@Inject
constructor(
  uiFactory: TextUi.Factory,
  textStateHolder: TextStateHolder,
  val selectTextIndex: SelectTextIndexUseCase,
) : BaseViewModel() {

  val textUiFlow = textStateHolder.selectedIndexFlow.mapState(uiFactory::create)
}
