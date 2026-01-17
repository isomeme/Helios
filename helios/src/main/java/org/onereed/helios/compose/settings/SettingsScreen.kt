package org.onereed.helios.compose.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.onereed.helios.R
import org.onereed.helios.common.dynamicThemeSupported
import org.onereed.helios.compose.shared.ScrollbarActions
import org.onereed.helios.compose.shared.SimpleVerticalScrollbar
import org.onereed.helios.compose.shared.confirm
import org.onereed.helios.ui.theme.DarkHeliosTheme
import org.onereed.helios.ui.theme.ThemeType

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = hiltViewModel()) {
  val themeType by settingsViewModel.themeTypeFlow.collectAsStateWithLifecycle()
  val isDynamicTheme by settingsViewModel.isDynamicThemeFlow.collectAsStateWithLifecycle()
  val isCompassSouthTop by settingsViewModel.isCompassSouthTopFlow.collectAsStateWithLifecycle()

  val uriHandler = LocalUriHandler.current
  val haptics = LocalHapticFeedback.current
  val settingsActions =
    remember(settingsViewModel, uriHandler, haptics) {
      SettingsActions(settingsViewModel, uriHandler, haptics)
    }
  val scrollState = rememberScrollState()
  val coroutineScope = rememberCoroutineScope()
  val canScrollUp by remember { derivedStateOf { scrollState.canScrollBackward } }
  val canScrollDown by remember { derivedStateOf { scrollState.canScrollForward } }
  val scrollbarActions =
    remember(scrollState, coroutineScope) { ScrollbarActions(scrollState, coroutineScope) }

  StatelessSettingsScreen(
    canScrollUp = canScrollUp,
    canScrollDown = canScrollDown,
    themeType = themeType,
    isDynamicTheme = isDynamicTheme,
    isCompassSouthTop = isCompassSouthTop,
    settingsActions = settingsActions,
    scrollbarActions = scrollbarActions,
    scrollState = scrollState,
  )
}

@Composable
private fun StatelessSettingsScreen(
  canScrollUp: Boolean,
  canScrollDown: Boolean,
  themeType: ThemeType,
  isDynamicTheme: Boolean,
  isCompassSouthTop: Boolean,
  settingsActions: SettingsActions,
  scrollbarActions: ScrollbarActions,
  scrollState: ScrollState,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    ProvideTextStyle(value = MaterialTheme.typography.labelMedium) {
      ConstraintLayout(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp)) {
        val (settings, scrollbar) = createRefs()

        Column(
          modifier =
            Modifier.width(IntrinsicSize.Max).verticalScroll(scrollState).constrainAs(settings) {
              centerTo(parent)
            },
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          ThemeSettings(themeType, isDynamicTheme, settingsActions)

          CompassSettings(isCompassSouthTop, settingsActions)

          OnlineDocLink(settingsActions)
        }

        SimpleVerticalScrollbar(
          canScrollUp = canScrollUp,
          canScrollDown = canScrollDown,
          scrollbarActions = scrollbarActions,
          modifier =
            Modifier.constrainAs(scrollbar) { start.linkTo(anchor = settings.end, margin = 10.dp) },
        )
      }
    }
  }
}

@Composable
private fun ThemeSettings(
  themeType: ThemeType,
  isDynamicTheme: Boolean,
  settingsActions: SettingsActions,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .padding(all = 15.dp),
    verticalArrangement = Arrangement.spacedBy(15.dp),
  ) {
    Text(
      text = stringResource(R.string.heading_theme),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
    )

    Column(
      modifier = Modifier.selectableGroup(),
      verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
      ThemeType.entries.forEach { type ->
        Row(
          modifier =
            Modifier.padding(start = 30.dp)
              .selectable(
                selected = type == themeType,
                enabled = type != themeType,
                onClick = { settingsActions.onThemeTypeSelected(type) },
                role = Role.RadioButton,
              ),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          RadioButton(selected = themeType == type, onClick = null)

          Spacer(modifier = Modifier.width(10.dp))

          Text(text = stringResource(type.labelRes))
        }
      }
    }

    if (dynamicThemeSupported) {
      Row(
        modifier =
          Modifier.toggleable(
            value = isDynamicTheme,
            onValueChange = settingsActions.onDynamicThemeSelected,
            role = Role.Checkbox,
          ),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Checkbox(checked = isDynamicTheme, onCheckedChange = null)

        Spacer(modifier = Modifier.width(10.dp))

        Text(text = stringResource(R.string.label_use_dynamic_theme_colors))
      }
    }
  }
}

@Composable
private fun CompassSettings(isCompassSouthTop: Boolean, settingsActions: SettingsActions) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .padding(all = 15.dp),
    verticalArrangement = Arrangement.spacedBy(15.dp),
  ) {
    Text(
      text = stringResource(R.string.heading_compass),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Bold,
    )

    Row(
      modifier =
        Modifier.toggleable(
          value = isCompassSouthTop,
          onValueChange = settingsActions.onCompassSouthTopSelected,
          role = Role.Checkbox,
        ),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked = isCompassSouthTop, onCheckedChange = null)

      Spacer(modifier = Modifier.width(10.dp))

      Text(text = stringResource(R.string.label_compass_south_top))
    }
  }
}

@Composable
private fun OnlineDocLink(settingsActions: SettingsActions) {
  TextButton(
    modifier = Modifier.fillMaxWidth(),
    onClick = settingsActions.onViewDoc,
    colors =
      ButtonDefaults.textButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
    shape = RectangleShape,
    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 15.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.help_24px),
      contentDescription = stringResource(R.string.view_online_documentation),
    )

    Spacer(modifier = Modifier.width(10.dp))

    Text(
      text = stringResource(R.string.view_online_documentation),
      style = MaterialTheme.typography.labelMedium,
    )
  }
}

@Immutable
private data class SettingsActions(
  val onThemeTypeSelected: (ThemeType) -> Unit,
  val onDynamicThemeSelected: (Boolean) -> Unit,
  val onCompassSouthTopSelected: (Boolean) -> Unit,
  val onViewDoc: () -> Unit,
) {
  constructor(
    settingsViewModel: SettingsViewModel,
    uriHandler: UriHandler,
    haptics: HapticFeedback,
  ) : this(
    onThemeTypeSelected = {
      haptics.confirm()
      settingsViewModel.setThemeType(it)
    },
    onDynamicThemeSelected = {
      haptics.confirm()
      settingsViewModel.setDynamicTheme(it)
    },
    onCompassSouthTopSelected = {
      haptics.confirm()
      settingsViewModel.setCompassSouthTop(it)
    },
    onViewDoc = { uriHandler.openUri("https://www.one-reed.org/helios") },
  )
}

@Preview
@Composable
fun SettingsScreenPreview() {
  val settingsActions =
    SettingsActions(
      onThemeTypeSelected = {},
      onDynamicThemeSelected = {},
      onCompassSouthTopSelected = {},
      onViewDoc = {},
    )
  val scrollbarActions = ScrollbarActions(onScrollToTop = {}, onScrollToBottom = {})

  DarkHeliosTheme {
    StatelessSettingsScreen(
      canScrollUp = false,
      canScrollDown = true,
      themeType = ThemeType.SYSTEM,
      isDynamicTheme = true,
      isCompassSouthTop = true,
      settingsActions = settingsActions,
      scrollbarActions = scrollbarActions,
      scrollState = ScrollState(0),
    )
  }
}
