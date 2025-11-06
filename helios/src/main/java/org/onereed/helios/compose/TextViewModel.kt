package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TextViewModel
@Inject
constructor(
  private val stateHolder: TextStateHolder,
  private val sunResources: SunResources,
) : ViewModel() {

  val textUiFlow =
    stateHolder.selectedIndexFlow
      .map { toTextUi(it) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = toTextUi(0),
      )

  private fun toTextUi(index: Int): TextUi {
    return TextUi.create(sunResources, index) { stateHolder.selectIndex(it) }
  }

  companion object {

    /** How long to hold the flow open after there are no subscribers. */
    private val STOP_TIMEOUT_MILLIS = Duration.ofSeconds(5).toMillis()
  }
}
