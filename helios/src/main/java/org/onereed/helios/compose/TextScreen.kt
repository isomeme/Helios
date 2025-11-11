package org.onereed.helios.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.onereed.helios.ui.theme.HeliosTheme

interface TextScreenActions {
  fun selectIndex(index: Int) {
    // Default: Do nothing.
  }
}

@Composable
internal fun TextScreen(
  actions: TextScreenActions,
  padding: PaddingValues = PaddingValues(),
  textUiFlow: StateFlow<TextUi> = hiltViewModel<TextViewModel>().textUiFlow,
) {
  val textUi by textUiFlow.collectAsState()

  // These state values are internal to TextScreen.

  var eventMenuExpanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val haptics = LocalHapticFeedback.current

  LaunchedEffect(textUi) { scrollState.scrollTo(0) }

  Column(modifier = Modifier.fillMaxSize().padding(padding)) {
    // Top of screen: select button on the left, title centered.

    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(all = 10.dp)) {
      // Enclosing the select button with its dropdown menu in a column makes the menu pop up just
      // below the button.

      Column(modifier = Modifier.align(Alignment.CenterStart)) {
        OutlinedButton(onClick = { eventMenuExpanded = true }) {
          Icon(
            painter = painterResource(id = textUi.selected.iconRes),
            tint = textUi.selected.color,
            contentDescription = textUi.selected.name,
          )
        }

        DropdownMenu(
          expanded = eventMenuExpanded,
          onDismissRequest = { eventMenuExpanded = false },
          offset = DpOffset(0.dp, 10.dp),
        ) {
          textUi.menu.forEach { eventUi ->
            DropdownMenuItem(
              enabled = eventUi.enabled,
              onClick = {
                eventMenuExpanded = false
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                actions.selectIndex(eventUi.index)
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

    // Rubric text

    MarkdownText(
      markdown = textUi.rubric,
      isTextSelectable = true,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
      modifier = Modifier.fillMaxWidth().padding(all = 20.dp).verticalScroll(scrollState),
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun TextScreenPreview() {
  val sunResources = SunResources.load(LocalContext.current)
  val textUi = TextUi.create(sunResources, 2) // Sunset

  HeliosTheme {
    TextScreen(actions = object : TextScreenActions {}, textUiFlow = MutableStateFlow(textUi))
  }
}
