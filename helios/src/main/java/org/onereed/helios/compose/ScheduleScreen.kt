package org.onereed.helios.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.common.blend
import org.onereed.helios.compose.NavActions.Companion.NavActionsStub
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
internal fun ScheduleScreen(
  navActions: NavActions,
  padding: PaddingValues = PaddingValues(),
  scheduleUiFlow: StateFlow<ScheduleUi> = hiltViewModel<ScheduleViewModel>().scheduleUiFlow,
) {
  val scheduleUi by scheduleUiFlow.collectAsStateWithLifecycle()

  Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
    if (scheduleUi.events.isEmpty()) {
      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      return@Box
    }

    LazyColumn(
      modifier = Modifier.fillMaxWidth().wrapContentHeight(),
      verticalArrangement = Arrangement.spacedBy(15.dp),
      contentPadding = PaddingValues(20.dp),
    ) {
      items(items = scheduleUi.events, key = { it.key }) { event ->
        ElevatedCard(
          elevation =
            CardDefaults.cardElevation(defaultElevation = if (event.isClosestEvent) 6.dp else 3.dp),
          onClick = { navActions.navigateToTextIndex(event.ordinal) },
          modifier = Modifier.fillMaxWidth().wrapContentHeight(),
          colors =
            CardDefaults.elevatedCardColors(
              containerColor = MaterialTheme.colorScheme.surface.blend(event.color, 0.15f),
              contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(all = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              painter = painterResource(event.iconRes),
              contentDescription = event.name,
              tint = event.color,
              modifier = Modifier.padding(start = 5.dp, end = 20.dp),
            )
            Text(
              text = event.timeText,
              fontWeight = if (event.isClosestEvent) FontWeight.Bold else FontWeight.Normal,
            )
          }
        }
      }
    }
  }
}

// 0xFF0F1416
@Preview(showBackground = true, backgroundColor = 0xFFDDDDDD)
@Composable
fun ScheduleScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val hereNow = PlaceTime(lat = 34.0, lon = -118.0, alt = 0.0, instant = Instant.now())
  val sunTimeSeries = SunTimeSeries(hereNow)
  val sunSchedule = SunSchedule(sunTimeSeries)
  val scheduleUi = ScheduleUi.Factory(LocalContext.current, sunResources).create(sunSchedule)

  HeliosTheme {
    ScheduleScreen(
      navActions = NavActionsStub,
      scheduleUiFlow = MutableStateFlow(scheduleUi),
    )
  }
}
