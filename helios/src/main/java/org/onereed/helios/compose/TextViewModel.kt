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
  private val stateHolder: TextSelectedIndexStateHolder,
  private val sunResources: SunResources,
) : ViewModel() {

  val textStateFlow =
    stateHolder.selectedIndexFlow
      .map { toTextState(it) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = toTextState(0),
      )

  private fun toTextState(index: Int): TextState {
    return TextState.create(sunResources, index) { stateHolder.updateSelectedIndex(it) }
  }

  companion object {

    /** How long to hold the flow open after there are no subscribers. */
    private val STOP_TIMEOUT_MILLIS = Duration.ofSeconds(5).toMillis()
  }
}
