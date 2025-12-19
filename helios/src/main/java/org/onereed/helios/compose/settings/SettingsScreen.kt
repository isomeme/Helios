package org.onereed.helios.compose.settings

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.onereed.helios.R
import org.onereed.helios.common.dynamicThemeSupported
import org.onereed.helios.compose.theme.ThemeType
import org.onereed.helios.compose.theme.ThemeViewModel
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel = hiltViewModel()) {
  val themeType by themeViewModel.themeTypeFlow.collectAsStateWithLifecycle(ThemeType.SYSTEM)
  val isDynamicTheme by themeViewModel.isDynamicThemeFlow.collectAsStateWithLifecycle(false)

  val haptics = LocalHapticFeedback.current
  val themeActions = remember(themeViewModel, haptics) { ThemeActions(themeViewModel, haptics) }

  val uriHandler = LocalUriHandler.current
  val onViewDoc = remember(uriHandler) { { uriHandler.openUri("https://www.one-reed.org/helios") } }

  StatelessSettingsScreen(
    themeType = themeType,
    isDynamicTheme = isDynamicTheme,
    themeActions = themeActions,
    onViewDoc = onViewDoc,
  )
}

@Composable
private fun StatelessSettingsScreen(
  themeType: ThemeType,
  isDynamicTheme: Boolean,
  themeActions: ThemeActions,
  onViewDoc: () -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    ProvideTextStyle(value = MaterialTheme.typography.labelMedium) {
      ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (settings) = createRefs()

        Column(
          modifier =
            Modifier.width(IntrinsicSize.Max).padding(all = 20.dp).constrainAs(settings) {
              centerTo(parent)
            },
          verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
          ThemeSettings(themeType, isDynamicTheme, themeActions)

          OnlineDocLink(onViewDoc)
        }
      }
    }
  }
}

@Composable
private fun ThemeSettings(
  themeType: ThemeType,
  isDynamicTheme: Boolean,
  themeActions: ThemeActions,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        .padding(all = 15.dp),
    verticalArrangement = Arrangement.spacedBy(15.dp),
  ) {
    Text(
      text = stringResource(R.string.heading_theme_type),
      style = MaterialTheme.typography.labelLarge,
    )

    Column(
      modifier = Modifier.selectableGroup(),
      verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
      ThemeType.entries.forEach { type ->
        Row(
          modifier =
            Modifier.selectable(
              selected = type == themeType,
              enabled = type != themeType,
              onClick = { themeActions.onThemeTypeSelected(type) },
              role = Role.RadioButton,
            ),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(modifier = Modifier.width(30.dp))

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
            onValueChange = { themeActions.onDynamicThemeSelected(it) },
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
private fun OnlineDocLink(onViewDoc: () -> Unit) {
  TextButton(
    modifier = Modifier.fillMaxWidth(),
    onClick = onViewDoc,
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

private data class ThemeActions(
  val onThemeTypeSelected: (ThemeType) -> Unit,
  val onDynamicThemeSelected: (Boolean) -> Unit,
) {
  constructor(
    themeViewModel: ThemeViewModel,
    haptics: HapticFeedback,
  ) : this(
    onThemeTypeSelected = {
      haptics.performHapticFeedback(HapticFeedbackType.Confirm)
      themeViewModel.setThemeType(it)
    },
    onDynamicThemeSelected = {
      haptics.performHapticFeedback(HapticFeedbackType.Confirm)
      themeViewModel.setDynamicTheme(it)
    },
  )
}

@Preview
@Composable
fun SettingsScreenPreview() {
  val themeActions = ThemeActions(onThemeTypeSelected = {}, onDynamicThemeSelected = {})

  DarkHeliosTheme {
    StatelessSettingsScreen(
      themeType = ThemeType.SYSTEM,
      isDynamicTheme = true,
      themeActions = themeActions,
      onViewDoc = {},
    )
  }
}
