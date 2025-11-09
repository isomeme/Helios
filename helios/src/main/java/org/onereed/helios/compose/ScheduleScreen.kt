package org.onereed.helios.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.ui.theme.HeliosTheme

interface ScheduleScreenActions {
  fun onTextIndexSelected(index: Int) {
    // Default: Do nothing.
  }
}

@Composable
internal fun ScheduleScreen(
  actions: ScheduleScreenActions,
  padding: PaddingValues = PaddingValues(),
  scheduleUiFlow: Flow<ScheduleUi> = hiltViewModel<ScheduleViewModel>().scheduleUiFlow,
) {
  val scheduleUi by scheduleUiFlow.collectAsState(ScheduleUi("Waiting...", emptyList()))

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.width(IntrinsicSize.Min).padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = scheduleUi.placeTimeStr, modifier = Modifier.fillMaxWidth().padding(all = 5.dp))
      scheduleUi.buttons.forEachIndexed { index, ui ->
        FilledTonalButton(
          modifier = Modifier.fillMaxWidth().padding(all = 5.dp),
          onClick = { actions.onTextIndexSelected(index) },
        ) {
          Text(ui.name)
        }
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
@SuppressLint("ViewModelConstructorInComposable")
internal fun ScheduleScreenPreview() {
  val sunResources = SunResources.load(LocalContext.current)
  val scheduleUi = ScheduleUi.create(PlaceTime.NONE, sunResources)
  val flow = flowOf(scheduleUi)
  HeliosTheme { ScheduleScreen(actions = object : ScheduleScreenActions {}, scheduleUiFlow = flow) }
}
