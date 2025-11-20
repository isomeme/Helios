package org.onereed.helios.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import org.onereed.helios.R
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
internal fun TextScreen(actions: NavActions, textViewModel: TextViewModel = hiltViewModel()) {
  val coroutineScope = rememberCoroutineScope()
  val textUi by textViewModel.textUiFlow.collectAsStateWithLifecycle()
  var eventMenuExpanded by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()
  val scrollToTopEnabled by remember { derivedStateOf { scrollState.canScrollBackward } }
  val scrollToBottomEnabled by remember { derivedStateOf { scrollState.canScrollForward } }

  LaunchedEffect(textUi) { scrollState.scrollTo(0) }

  StatelessTextScreen(
    textUi = textUi,
    eventMenuExpanded = eventMenuExpanded,
    onEventMenuExpanded = { eventMenuExpanded = true },
    onEventMenuDismissed = { eventMenuExpanded = false },
    onSelectIndex = { index ->
      actions.selectTextIndex(index)
      eventMenuExpanded = false
    },
    scrollState = scrollState,
    scrollToTopEnabled = scrollToTopEnabled,
    scrollToBottomEnabled = scrollToBottomEnabled,
    onScrollToTop = { coroutineScope.launch { scrollState.animateScrollTo(0) } },
    onScrollToBottom = {
      coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
    },
  )
}

@Composable
fun StatelessTextScreen(
  textUi: TextUi,
  eventMenuExpanded: Boolean,
  onEventMenuExpanded: () -> Unit,
  onEventMenuDismissed: () -> Unit,
  onSelectIndex: (Int) -> Unit,
  scrollState: ScrollState,
  scrollToTopEnabled: Boolean,
  scrollToBottomEnabled: Boolean,
  onScrollToTop: () -> Unit,
  onScrollToBottom: () -> Unit,
) {
  val haptics = LocalHapticFeedback.current

  Column(modifier = Modifier.fillMaxSize()) {
    // Top of screen: select button on the left, title centered.

    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(all = 10.dp)) {
      // Enclosing the select button with its dropdown menu in a column makes the menu pop up just
      // below the button.

      Column(modifier = Modifier.align(Alignment.CenterStart)) {
        OutlinedButton(onClick = onEventMenuExpanded) {
          Icon(
            painter = painterResource(id = textUi.selected.iconRes),
            tint = textUi.selected.color,
            contentDescription = textUi.selected.name,
          )
        }

        DropdownMenu(
          expanded = eventMenuExpanded,
          onDismissRequest = onEventMenuDismissed,
          offset = DpOffset(0.dp, 10.dp),
        ) {
          textUi.menu.forEach { eventUi ->
            DropdownMenuItem(
              enabled = eventUi.enabled,
              onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onSelectIndex(eventUi.index)
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

    Row(modifier = Modifier.fillMaxSize().padding(start = 20.dp, top = 20.dp, bottom = 20.dp)) {
      MarkdownText(
        markdown = textUi.rubric,
        isTextSelectable = true,
        style =
          MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxHeight().weight(1f).verticalScroll(scrollState),
      )

      Column(
        modifier = Modifier.wrapContentWidth().fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        AnimatedContent(
          targetState = scrollToTopEnabled,
          transitionSpec = {
            (fadeIn(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))).togetherWith(
              fadeOut(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))
            )
          },
        ) { enabled ->
          ScrollButton(onScrollToTop, enabled, R.drawable.arrow_upward_24px, R.string.scroll_to_top)
        }
        AnimatedContent(
          targetState = scrollToBottomEnabled,
          transitionSpec = {
            (fadeIn(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))).togetherWith(
              fadeOut(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))
            )
          },
        ) { enabled ->
          ScrollButton(
            onScrollToBottom,
            enabled,
            R.drawable.arrow_downward_24px,
            R.string.scroll_to_bottom,
          )
        }
      }
    }
  }
}

@Composable
private fun ScrollButton(
  onScrollTo: () -> Unit,
  enabled: Boolean,
  @DrawableRes icon: Int,
  @StringRes contentDescription: Int,
) {
  IconButton(
    onClick = onScrollTo,
    enabled = enabled,
    colors = IconButtonDefaults.iconButtonColors(disabledContentColor = Color.Transparent),
  ) {
    Icon(
      painter = painterResource(id = icon),
      contentDescription = stringResource(id = contentDescription),
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun TextScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val textUi = TextUi.Factory(sunResources).create(2) // Sunset

  HeliosTheme {
    StatelessTextScreen(
      textUi = textUi,
      eventMenuExpanded = false,
      scrollState = ScrollState(0),
      onEventMenuExpanded = {},
      onEventMenuDismissed = {},
      onSelectIndex = {},
      scrollToTopEnabled = false,
      scrollToBottomEnabled = true,
      onScrollToTop = {},
      onScrollToBottom = {},
    )
  }
}

private const val SCROLL_BUTTON_ANIM_MILLIS = 500
