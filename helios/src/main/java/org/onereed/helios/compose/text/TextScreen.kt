package org.onereed.helios.compose.text

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import org.onereed.helios.compose.shared.ScrollbarActions
import org.onereed.helios.compose.shared.SimpleVerticalScrollbar
import org.onereed.helios.compose.shared.confirm
import org.onereed.helios.compose.shared.sunColorFamilies
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.sun.SunEventType
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun TextScreen(textViewModel: TextViewModel = hiltViewModel()) {
  val textUi by textViewModel.textUiFlow.collectAsStateWithLifecycle()
  val coroutineScope = rememberCoroutineScope()

  val scrollState = rememberScrollState()
  val canScrollUp by remember { derivedStateOf { scrollState.canScrollBackward } }
  val canScrollDown by remember { derivedStateOf { scrollState.canScrollForward } }
  val scrollbarActions =
    remember(scrollState, coroutineScope) { ScrollbarActions(scrollState, coroutineScope) }

  val eventMenuExpandedState = remember { mutableStateOf(false) }
  val haptics = LocalHapticFeedback.current
  val eventMenuActions =
    remember(eventMenuExpandedState, textViewModel, haptics) {
      EventMenuActions(eventMenuExpandedState, textViewModel, haptics)
    }

  LaunchedEffect(textUi) { scrollState.scrollTo(0) }

  StatelessTextScreen(
    textUi,
    eventMenuExpandedState.value,
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
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
      Column(
        modifier = Modifier.widthIn(max = 640.dp).padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        TitleBar(
          textUi = textUi,
          eventMenuActions = eventMenuActions,
          eventMenuExpanded = eventMenuExpanded,
        )

        Body(
          textUi = textUi,
          scrollState = scrollState,
          canScrollUp = canScrollUp,
          canScrollDown = canScrollDown,
          scrollbarActions = scrollbarActions,
        )
      }
    }
  }
}

@Composable
private fun TitleBar(
  textUi: TextUi,
  eventMenuActions: EventMenuActions,
  eventMenuExpanded: Boolean,
) {
  val sunColorFamilies = sunColorFamilies()

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(sunColorFamilies[textUi.selected.index].colorContainer)
        .padding(all = 10.dp)
  ) {
    // Enclosing the select button with its dropdown menu in a column makes the menu pop up just
    // below the button.

    Column(modifier = Modifier.align(Alignment.CenterStart)) {
      OutlinedButton(
        onClick = eventMenuActions.onExpanded,
        colors =
          ButtonDefaults.outlinedButtonColors(
            containerColor = sunColorFamilies[textUi.selected.index].colorContainer,
            contentColor = sunColorFamilies[textUi.selected.index].onColorContainer,
          ),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
      ) {
        Icon(
          painter = painterResource(id = textUi.selected.iconRes),
          contentDescription = stringResource(textUi.selected.nameRes),
        )
      }

      DropdownMenu(
        expanded = eventMenuExpanded,
        onDismissRequest = eventMenuActions.onDismissed,
        offset = DpOffset(0.dp, 10.dp),
      ) {
        textUi.menu.forEach { eventUi ->
          DropdownMenuItem(
            enabled = eventUi.enabled,
            onClick = { eventMenuActions.onSelectIndex(eventUi.index) },
            colors =
              MenuDefaults.itemColors(
                leadingIconColor = sunColorFamilies[eventUi.index].onColorContainer,
                textColor = sunColorFamilies[eventUi.index].onColorContainer,
              ),
            leadingIcon = {
              Icon(
                painter = painterResource(eventUi.iconRes),
                contentDescription = stringResource(eventUi.nameRes),
              )
            },
            text = {
              Text(
                text = stringResource(eventUi.nameRes),
                style = MaterialTheme.typography.labelLarge,
              )
            },
          )
        }
      }
    }

    Text(
      text = stringResource(textUi.selected.nameRes),
      color = sunColorFamilies[textUi.selected.index].onColorContainer,
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.align(Alignment.Center),
    )
  }
}

@Composable
private fun Body(
  textUi: TextUi,
  scrollState: ScrollState,
  canScrollUp: Boolean,
  canScrollDown: Boolean,
  scrollbarActions: ScrollbarActions,
) {
  Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceContainer)) {
    RichText(
      modifier =
        Modifier.verticalScroll(scrollState)
          .padding(horizontal = 40.dp, vertical = 10.dp)
          .align(alignment = Alignment.TopCenter),
      style = RichTextStyle(paragraphSpacing = TextUnit(15.0f, TextUnitType.Sp)),
    ) {
      Markdown(content = textUi.rubric)
    }

    SimpleVerticalScrollbar(
      canScrollUp = canScrollUp,
      canScrollDown = canScrollDown,
      scrollbarActions = scrollbarActions,
      modifier = Modifier.align(Alignment.CenterEnd),
    )
  }
}

@Immutable
private data class EventMenuActions(
  val onExpanded: () -> Unit,
  val onDismissed: () -> Unit,
  val onSelectIndex: (Int) -> Unit,
) {
  constructor(
    eventMenuExpandedState: MutableState<Boolean>,
    textViewModel: TextViewModel,
    haptics: HapticFeedback,
  ) : this(
    onExpanded = {
      haptics.confirm()
      eventMenuExpandedState.value = true
    },
    onDismissed = { eventMenuExpandedState.value = false },
    onSelectIndex = { index ->
      haptics.confirm()
      eventMenuExpandedState.value = false
      textViewModel.selectTextIndex(index)
    },
  )
}

@Preview
@Composable
fun TextScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val textUi = TextUi.Factory(sunResources).create(SunEventType.SET.ordinal)
  val eventMenuActions = EventMenuActions(onExpanded = {}, onDismissed = {}, onSelectIndex = {})
  val scrollbarActions = ScrollbarActions(onScrollToTop = {}, onScrollToBottom = {})

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
