package org.onereed.helios.compose.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.compose.app.NavActions
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
internal fun ScheduleScreen(
    actions: NavActions,
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
  val scheduleUi by scheduleViewModel.scheduleUiFlow.collectAsStateWithLifecycle()

  StatelessScheduleScreen(scheduleUi = scheduleUi, onSelectEvent = actions::navigateToTextIndex)
}

@Composable
fun StatelessScheduleScreen(scheduleUi: ScheduleUi, onSelectEvent: (Int) -> Unit) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    if (scheduleUi.events.isEmpty()) {
      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      return@Box
    }

    LazyColumn(
      modifier = Modifier.width(COLUMN_WIDTH).wrapContentHeight().padding(horizontal = 40.dp),
      verticalArrangement = Arrangement.spacedBy(25.dp),
    ) {
      items(items = scheduleUi.events, key = { it.key }) { event ->
        OutlinedCard(
          onClick = { onSelectEvent(event.ordinal) },
          colors =
            CardDefaults.outlinedCardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer,
              contentColor = MaterialTheme.colorScheme.onSurface,
            ),
          border = BorderStroke(width = 1.dp, color = event.color),
          modifier = Modifier.fillMaxWidth().wrapContentHeight().animateItem(),
        ) {
          Row(
            modifier = Modifier.wrapContentSize().padding(horizontal = 15.dp, vertical = 10.dp),
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
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
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

  DarkHeliosTheme { StatelessScheduleScreen(scheduleUi = scheduleUi, onSelectEvent = {}) }
}

/**
 * This width has been determined empirically to be enough to render an event with room for
 * date-time text, but without excess wasted space between that and the end of the display.
 */
private val COLUMN_WIDTH = 400.dp
