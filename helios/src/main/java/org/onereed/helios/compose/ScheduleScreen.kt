package org.onereed.helios.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
  val scheduleUi by scheduleUiFlow.collectAsState(ScheduleUi(emptyList()))

  Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      if (scheduleUi.events.isEmpty()) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        return@Column
      }

      scheduleUi.events.forEach { event ->
        OutlinedButton(
          modifier = Modifier.fillMaxWidth().padding(all = 5.dp),
          onClick = { actions.onTextIndexSelected(event.ordinal) },
          border = BorderStroke(2.dp, event.color),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              painter = painterResource(event.iconRes),
              contentDescription = event.name,
              tint = event.color,
              modifier = Modifier.padding(end = 20.dp),
            )
            Text(text = event.timeText, fontWeight = event.timeFontWeight)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
@SuppressLint("ViewModelConstructorInComposable")
internal fun ScheduleScreenPreview() {
  val scheduleUi = ScheduleUi(emptyList())
  val flow = flowOf(scheduleUi)
  HeliosTheme { ScheduleScreen(actions = object : ScheduleScreenActions {}, scheduleUiFlow = flow) }
}
