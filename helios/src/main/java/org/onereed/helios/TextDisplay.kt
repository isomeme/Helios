package org.onereed.helios

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.onereed.helios.ui.theme.HeliosTheme
import timber.log.Timber

@Composable
internal fun TextDisplay(
  initialIndex: Int,
  sunResources: SunResources,
  padding: PaddingValues = PaddingValues(),
) {
  var selectedIndex by remember { mutableIntStateOf(initialIndex) }
  var expanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val eventSets = sunResources.eventSets

  val haptics = LocalHapticFeedback.current

  LaunchedEffect(selectedIndex) { scrollState.scrollTo(0) }

  Column(modifier = Modifier.fillMaxSize().padding(padding)) {
    Box(modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp)) {
      OutlinedButton(onClick = { expanded = true }) { Text(stringResource(R.string.button_select)) }

      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        eventSets.forEachIndexed { index, eventSet ->
          with(eventSet) {
            DropdownMenuItem(
              enabled = selectedIndex != index,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                selectedIndex = index
                expanded = false
              },
              colors = MenuDefaults.itemColors(leadingIconColor = fgColor, textColor = fgColor),
              leadingIcon = {
                Icon(
                  painter = painterResource(id = icon),
                  contentDescription = stringResource(R.string.sun_event_icon_description),
                )
              },
              text = { Text(text = name, style = MaterialTheme.typography.labelLarge) },
            )
          }
        }
      }
    }

    Column(
      modifier = Modifier.verticalScroll(scrollState).padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      with(eventSets[selectedIndex]) {
        Icon(
          painter = painterResource(id = icon),
          contentDescription = stringResource(R.string.sun_event_icon_description),
          tint = fgColor,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = name,
          style = MaterialTheme.typography.headlineMedium,
          color = fgColor,
        )

        Spacer(modifier = Modifier.height(25.dp))

        Timber.d("Text style: ${LocalTextStyle.current}")
        Timber.d("Color: ${LocalContentColor.current}")

        MarkdownText(
          markdown = rubric,
          style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
          modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        )
      }
    }
  }
}

@Preview
@Composable
fun TextDisplayPreview() {
  HeliosTheme {
    TextDisplay(initialIndex = 0, sunResources = SunResources.from(LocalContext.current))
  }
}
