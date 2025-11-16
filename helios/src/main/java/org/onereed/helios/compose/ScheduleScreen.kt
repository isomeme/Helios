package org.onereed.helios.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.min
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.HeliosTheme
import timber.log.Timber

@Composable
internal fun ScheduleScreen(
  actions: NavActions,
  scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
  val scheduleUi by scheduleViewModel.scheduleUiFlow.collectAsStateWithLifecycle()
  var eventWidthDpSize by remember { mutableIntStateOf(300) }

  StatelessScheduleScreen(
    scheduleUi = scheduleUi,
    eventWidth = eventWidthDpSize.dp,
    onSelectEvent = actions::navigateToTextIndex,
    onTextLayout = { textLayoutResult ->
      if (textLayoutResult.hasVisualOverflow) {
        eventWidthDpSize = min(eventWidthDpSize + EVENT_WIDTH_DP_DELTA, MAX_EVENT_WIDTH_DP_SIZE)
      }
    },
  )
}

@Composable
fun StatelessScheduleScreen(
  scheduleUi: ScheduleUi,
  eventWidth: Dp,
  onSelectEvent: (Int) -> Unit,
  onTextLayout: (TextLayoutResult) -> Unit,
) {
  Timber.d("Compose start, eventWidth=$eventWidth")

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    if (scheduleUi.events.isEmpty()) {
      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
      return@Box
    }

    LazyColumn(
      modifier = Modifier
        .width(eventWidth)
        .wrapContentHeight()
        .padding(horizontal = 40.dp),
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
          modifier = Modifier
            .animateItem()
            .fillMaxWidth()
            .wrapContentHeight(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 15.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
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
              onTextLayout = onTextLayout,
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

  HeliosTheme {
    StatelessScheduleScreen(
      scheduleUi = scheduleUi,
      eventWidth = 340.dp,
      onSelectEvent = {},
      onTextLayout = {},
    )
  }
}

/**
 * The amount by which we increase the width of the events `LazyColumn` each time an event reports
 * text layout overflow. We get reports from all events for each composition attempt, which means
 * that if all of them overflow, we get a cumulative delta of +5N for the next composition attempt.
 */
private const val EVENT_WIDTH_DP_DELTA = 4

/**
 * The maximum width to which we will expand the events `LazyColumn` in an attempt to fit events
 * without text overflow. This number is rather arbitrary, based on observations that 340 is big
 * enough for the cases I've tried.
 */
private const val MAX_EVENT_WIDTH_DP_SIZE = 800
