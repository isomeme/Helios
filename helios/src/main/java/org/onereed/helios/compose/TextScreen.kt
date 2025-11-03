package org.onereed.helios.compose

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
internal fun TextScreen(
  padding: PaddingValues = PaddingValues(),
  textViewModel: TextViewModel = viewModel(),
) {
  val textState by textViewModel.textStateFlow.collectAsState()

  TextScreenImpl(textState, padding)
}

// By isolating the ViewModel dependency above, we can preview this version.
@Composable
internal fun TextScreenImpl(textState: TextState, padding: PaddingValues = PaddingValues()) {
  var eventMenuExpanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val haptics = LocalHapticFeedback.current

  LaunchedEffect(textState) { scrollState.scrollTo(0) }

  Column(modifier = Modifier.fillMaxSize().padding(padding)) {
    // Top of screen: select button on the left, title centered.

    ConstraintLayout(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(all = 10.dp)) {
      val (button, title) = createRefs()

      // Enclosing the select button with its dropdown menu in a column makes the menu pop up just
      // below the button.

      Column(
        modifier =
          Modifier.constrainAs(button) {
            start.linkTo(parent.start)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
          }
      ) {
        OutlinedButton(onClick = { eventMenuExpanded = true }) {
          Icon(
            painter = painterResource(id = textState.selected.icon),
            tint = textState.selected.color,
            contentDescription = textState.selected.name,
          )
        }

        DropdownMenu(
          expanded = eventMenuExpanded,
          onDismissRequest = { eventMenuExpanded = false },
          offset = DpOffset(0.dp, 10.dp),
        ) {
          textState.menu.forEach { eventDisplay ->
            DropdownMenuItem(
              enabled = eventDisplay.enabled,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                eventDisplay.onSelect()
                eventMenuExpanded = false
              },
              colors =
                MenuDefaults.itemColors(
                  leadingIconColor = eventDisplay.color,
                  textColor = eventDisplay.color,
                ),
              leadingIcon = {
                Icon(
                  painter = painterResource(id = eventDisplay.icon),
                  contentDescription = eventDisplay.name,
                )
              },
              text = { Text(text = eventDisplay.name, style = MaterialTheme.typography.labelLarge) },
            )
          }
        }
      }

      Text(
        text = textState.selected.name,
        color = textState.selected.color,
        style = MaterialTheme.typography.headlineMedium,
        modifier =
          Modifier.constrainAs(title) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
          },
      )
    }

    // Rubric text

    MarkdownText(
      markdown = textState.rubric,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
      isTextSelectable = true,
      modifier = Modifier.fillMaxWidth().padding(all = 20.dp).verticalScroll(scrollState),
    )
  }
}

@Preview(showBackground = true)
@Composable
fun TextScreenPreview() {
  val sunResources = SunResources.load(LocalContext.current)
  val textState = TextState.create(sunResources, 2) {}

  HeliosTheme { TextScreenImpl(textState) }
}
