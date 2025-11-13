package org.onereed.helios.compose

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.onereed.helios.common.BaseViewModel

@HiltViewModel
class TextViewModel
@Inject
constructor(textStateHolder: TextStateHolder, uiFactory: TextUi.Factory) : BaseViewModel() {

  val textUiFlow = textStateHolder.selectedIndexFlow.mapState { uiFactory.create(it) }
}
