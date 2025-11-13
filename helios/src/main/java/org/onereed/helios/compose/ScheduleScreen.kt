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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.HeliosTheme

@Stable
interface ScheduleScreenActions {
  fun navigateToTextIndex(index: Int) {
    // Default: Do nothing.
  }
}

@Composable
internal fun ScheduleScreen(
  actions: ScheduleScreenActions,
  padding: PaddingValues = PaddingValues(),
  scheduleUiFlow: StateFlow<ScheduleUi> = hiltViewModel<ScheduleViewModel>().scheduleUiFlow,
) {
  val scheduleUi by scheduleUiFlow.collectAsStateWithLifecycle()

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
          onClick = { actions.navigateToTextIndex(event.ordinal) },
          modifier =
            Modifier.fillMaxWidth()
              .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
          border = BorderStroke(1.dp, event.color),
          colors =
            CardDefaults.outlinedCardColors(
              containerColor = event.color.copy(alpha = 0.1f),
              contentColor = MaterialTheme.colorScheme.onSurface,
            ),
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
              modifier = Modifier.padding(start = 5.dp, end = 20.dp),
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
fun ScheduleScreenPreview() {
  val sunResources = SunResources.load(LocalContext.current)
  val hereNow = PlaceTime(lat = 34.0, lon = -118.0, alt = 0.0, instant = Instant.now())
  val sunTimeSeries = SunTimeSeries.compute(hereNow)
  val sunSchedule = SunSchedule.compute(sunTimeSeries)
  val scheduleUi = ScheduleUi.Factory(LocalContext.current, sunResources).create(sunSchedule)

  HeliosTheme {
    ScheduleScreen(
      actions = object : ScheduleScreenActions {},
      scheduleUiFlow = MutableStateFlow(scheduleUi),
    )
  }
}
