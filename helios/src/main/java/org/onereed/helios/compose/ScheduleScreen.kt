package org.onereed.helios.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.compose.NavActions.Companion.NavActionsStub
import org.onereed.helios.compose.ScheduleUi.EventUi
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
internal fun ScheduleScreen(
  actions: NavActions,
  scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
  val scheduleUi by scheduleViewModel.scheduleUiFlow.collectAsStateWithLifecycle()

  StatelessScheduleScreen(actions = actions, scheduleUi = scheduleUi)
}

@Composable
fun StatelessScheduleScreen(actions: NavActions, scheduleUi: ScheduleUi) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    if (scheduleUi.events.isEmpty()) {
      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      return@Box
    }

    LazyColumn(
      modifier =
        Modifier.wrapContentSize(align = Alignment.CenterStart).padding(horizontal = 40.dp),
      verticalArrangement = Arrangement.spacedBy(25.dp),
    ) {
      items(items = scheduleUi.events, key = { it.key }) { event ->
        EventDisplay(
          event = event,
          onClick = { actions.navigateToTextIndex(event.ordinal) },
          Modifier.animateItem(),
        )
      }
    }
  }
}

@Composable
private fun EventDisplay(event: EventUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
  OutlinedCard(
    onClick = onClick,
    colors =
      CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
    border = BorderStroke(width = 1.dp, color = event.color),
    modifier = modifier.fillMaxWidth().wrapContentHeight(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(event.iconRes),
        contentDescription = event.name,
        tint = event.color,
        modifier = Modifier.padding(end = 20.dp),
      )
      Text(
        text = event.timeText,
        fontWeight = if (event.isClosestEvent) FontWeight.Bold else FontWeight.Normal,
      )
    }
  }
}

// 0xFF0F1416
@OptIn(ExperimentalTime::class)
@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun ScheduleScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val hereNow = PlaceTime(lat = 34.0, lon = -118.0, alt = 0.0, instant = now())
  val sunTimeSeries = SunTimeSeries(hereNow)
  val sunSchedule = SunSchedule(sunTimeSeries)
  val scheduleUi = ScheduleUi.Factory(LocalContext.current, sunResources).create(sunSchedule)

  HeliosTheme { StatelessScheduleScreen(actions = NavActionsStub, scheduleUi = scheduleUi) }
}
