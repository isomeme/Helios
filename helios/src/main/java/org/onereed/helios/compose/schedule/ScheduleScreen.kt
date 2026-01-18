package org.onereed.helios.compose.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.ExperimentalTime
import org.onereed.helios.compose.app.NavActions
import org.onereed.helios.compose.app.Screen
import org.onereed.helios.compose.schedule.ScheduleUi.EventUi
import org.onereed.helios.compose.shared.ScrollbarActions
import org.onereed.helios.compose.shared.SimpleVerticalScrollbar
import org.onereed.helios.compose.shared.confirm
import org.onereed.helios.compose.shared.sunColorFamilies
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.datasource.testing.santaMonicaNow
import org.onereed.helios.sun.SunSchedule
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun ScheduleScreen(navActions: NavActions, scheduleViewModel: ScheduleViewModel = hiltViewModel()) {
  val scheduleUi by scheduleViewModel.scheduleUiFlow.collectAsStateWithLifecycle(ScheduleUi.INVALID)
  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()
  val canScrollUp by remember { derivedStateOf { lazyListState.canScrollBackward } }
  val canScrollDown by remember { derivedStateOf { lazyListState.canScrollForward } }
  val scrollbarActions =
    remember(lazyListState, coroutineScope) { ScrollbarActions(lazyListState, coroutineScope) }

  val haptics = LocalHapticFeedback.current
  val onSelectEvent =
    remember(scheduleViewModel, navActions, haptics) {
      { index: Int ->
        haptics.confirm()
        scheduleViewModel.selectTextIndex(index)
        navActions.navigateTo(Screen.Text)
      }
    }

  StatelessScheduleScreen(
    scheduleUi = scheduleUi,
    lazyListState = lazyListState,
    canScrollUp = canScrollUp,
    canScrollDown = canScrollDown,
    scrollbarActions = scrollbarActions,
    onSelectEvent = onSelectEvent,
  )
}

@Composable
fun StatelessScheduleScreen(
  scheduleUi: ScheduleUi,
  lazyListState: LazyListState,
  canScrollUp: Boolean,
  canScrollDown: Boolean,
  scrollbarActions: ScrollbarActions,
  onSelectEvent: (Int) -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    ConstraintLayout(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp)) {
      val (progress, events, scrollbar) = createRefs()

      if (!scheduleUi.isValid) {
        CircularProgressIndicator(modifier = Modifier.constrainAs(progress) { centerTo(parent) })
        return@ConstraintLayout
      }

      LazyColumn(
        modifier = Modifier.wrapContentSize().constrainAs(events) { centerTo(parent) },
        verticalArrangement = Arrangement.spacedBy(25.dp),
        state = lazyListState,
      ) {
        items(items = scheduleUi.events, key = { it.key }) { event ->
          EventCard(event, onSelectEvent)
        }
      }

      SimpleVerticalScrollbar(
        canScrollUp = canScrollUp,
        canScrollDown = canScrollDown,
        scrollbarActions = scrollbarActions,
        modifier =
          Modifier.constrainAs(scrollbar) { start.linkTo(anchor = events.end, margin = 10.dp) },
      )
    }
  }
}

@Composable
private fun LazyItemScope.EventCard(event: EventUi, onSelectEvent: (Int) -> Unit) {
  val eventColorFamily = sunColorFamilies()[event.ordinal]

  Card(
    modifier = Modifier.requiredWidth(CARD_WIDTH).animateItem(),
    onClick = { onSelectEvent(event.ordinal) },
    colors =
      CardDefaults.cardColors(
        containerColor = eventColorFamily.colorContainer,
        contentColor = eventColorFamily.onColorContainer,
      ),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        painter = painterResource(event.iconRes),
        contentDescription = stringResource(event.nameRes),
        tint = eventColorFamily.onColorContainer,
      )
      Spacer(modifier = Modifier.width(20.dp))
      Text(
        text = event.timeText,
        fontWeight = if (event.isClosestEvent) FontWeight.Bold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

// 0xFF0F1416
@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun ScheduleScreenPreview() {
  val sunResources = SunResources.create(LocalContext.current)
  val placeTime = santaMonicaNow()
  val sunTimeSeries = SunTimeSeries.create(placeTime)
  val sunSchedule = SunSchedule.create(sunTimeSeries)
  val scheduleUi = ScheduleUi.Factory(LocalContext.current, sunResources).create(sunSchedule)
  val scrollbarActions = ScrollbarActions(onScrollToTop = {}, onScrollToBottom = {})

  DarkHeliosTheme {
    StatelessScheduleScreen(
      scheduleUi = scheduleUi,
      lazyListState = LazyListState(),
      canScrollUp = false,
      canScrollDown = true,
      scrollbarActions = scrollbarActions,
      onSelectEvent = {},
    )
  }
}

/**
 * This width has been determined empirically to be enough to render an event with room for
 * date-time text, but without excess wasted space between that and the end of the display.
 */
private val CARD_WIDTH = 320.dp
