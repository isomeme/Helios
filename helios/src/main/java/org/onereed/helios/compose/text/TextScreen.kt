package org.onereed.helios.compose.text

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.onereed.helios.compose.app.NavActions
import org.onereed.helios.compose.shared.ScrollbarActions
import org.onereed.helios.compose.shared.SimpleVerticalScrollbar
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun TextScreen(navActions: NavActions, textViewModel: TextViewModel = hiltViewModel()) {
  val textUi by textViewModel.textUiFlow.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()

  val scrollState = rememberScrollState()
  val canScrollUp by remember { derivedStateOf { scrollState.canScrollBackward } }
  val canScrollDown by remember { derivedStateOf { scrollState.canScrollForward } }

  var eventMenuExpanded by remember { mutableStateOf(false) }

  LaunchedEffect(textUi) { scrollState.scrollTo(0) }

  val eventMenuActions =
    object : EventMenuActions {
      override fun onExpanded() {
        eventMenuExpanded = true
      }

      override fun onDismissed() {
        eventMenuExpanded = false
      }

      override fun onSelectIndex(ix: Int) {
        navActions.selectTextIndex(ix)
        eventMenuExpanded = false
      }
    }

  val scrollbarActions = ScrollbarActions.forScrollState(scrollState, coroutineScope)

  StatelessTextScreen(
    textUi,
    eventMenuExpanded,
    canScrollUp,
    canScrollDown,
    eventMenuActions,
    scrollbarActions,
    scrollState,
  )
}

@Composable
private fun StatelessTextScreen(
  textUi: TextUi,
  eventMenuExpanded: Boolean,
  canScrollUp: Boolean,
  canScrollDown: Boolean,
  eventMenuActions: EventMenuActions,
  scrollbarActions: ScrollbarActions,
  scrollState: ScrollState,
) {
  val haptics = LocalHapticFeedback.current

  Column(modifier = Modifier.fillMaxSize()) {

    // Top of screen: select button on the left, title centered.
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .padding(all = 10.dp)
    ) {

      // Enclosing the select button with its dropdown menu in a column makes the menu pop up just
      // below the button.
      Column(modifier = Modifier.align(Alignment.CenterStart)) {
        OutlinedButton(onClick = eventMenuActions::onExpanded) {
          Icon(
            painter = painterResource(id = textUi.selected.iconRes),
            tint = textUi.selected.color,
            contentDescription = textUi.selected.name,
          )
        }

        DropdownMenu(
          expanded = eventMenuExpanded,
          onDismissRequest = eventMenuActions::onDismissed,
          offset = DpOffset(0.dp, 10.dp),
        ) {
          textUi.menu.forEach { eventUi ->
            DropdownMenuItem(
              enabled = eventUi.enabled,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                eventMenuActions.onSelectIndex(eventUi.index)
              },
              colors =
                MenuDefaults.itemColors(
                  leadingIconColor = eventUi.color,
                  textColor = eventUi.color,
                ),
              leadingIcon = {
                Icon(painter = painterResource(eventUi.iconRes), contentDescription = eventUi.name)
              },
              text = { Text(text = eventUi.name, style = MaterialTheme.typography.labelLarge) },
            )
          }
        }
      }

      Text(
        text = textUi.selected.name,
        color = textUi.selected.color,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.align(Alignment.Center),
      )
    }

    // Rubric text with scroll controls
    Box(modifier = Modifier.fillMaxSize().padding(vertical = 20.dp)) {
      MarkdownText(
        modifier = Modifier.verticalScroll(scrollState).fillMaxWidth().padding(horizontal = 40.dp),
        markdown = textUi.rubric,
        style =
          MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
      )

      SimpleVerticalScrollbar(
        canScrollUp = canScrollUp,
        canScrollDown = canScrollDown,
        scrollbarActions = scrollbarActions,
        modifier = Modifier.align(Alignment.CenterEnd),
      )
    }
  }
}

@Immutable
private interface EventMenuActions {
  fun onExpanded()

  fun onDismissed()

  fun onSelectIndex(ix: Int)
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun TextScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val textUi = TextUi.Factory(sunResources).create(2) // Sunset

  val eventMenuActions =
    object : EventMenuActions {
      override fun onExpanded() {}

      override fun onDismissed() {}

      override fun onSelectIndex(ix: Int) {}
    }

  val scrollbarActions =
    object : ScrollbarActions {
      override fun onScrollToTop() {}

      override fun onScrollToBottom() {}
    }

  DarkHeliosTheme {
    StatelessTextScreen(
      textUi = textUi,
      eventMenuExpanded = false,
      canScrollUp = false,
      canScrollDown = true,
      eventMenuActions = eventMenuActions,
      scrollbarActions = scrollbarActions,
      scrollState = ScrollState(0),
    )
  }
}
