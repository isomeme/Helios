package org.onereed.helios.compose

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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

interface ScheduleScreenActions {
  fun navigateToText() {
    // Default: Do nothing.
  }
}

@Composable
internal fun ScheduleScreen(
  actions: ScheduleScreenActions,
  padding: PaddingValues = PaddingValues(),
  scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
  val scheduleUi by scheduleViewModel.scheduleUiFlow.collectAsState(ScheduleUi(emptyList()))

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
        OutlinedCard(
          onClick = {
            scheduleViewModel.selectTextIndex(event.ordinal)
            actions.navigateToText()
          },
          modifier =
            Modifier.fillMaxWidth()
              .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
          border = BorderStroke(2.dp, event.color),
          shape = MaterialTheme.shapes.medium,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
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
