package org.onereed.helios.compose.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.onereed.helios.R
import org.onereed.helios.common.dynamicThemeSupported
import org.onereed.helios.compose.theme.ThemeType
import org.onereed.helios.compose.theme.ThemeViewModel
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel = hiltViewModel()) {
  val isDynamicTheme by themeViewModel.isDynamicThemeFlow.collectAsStateWithLifecycle(false)
  val themeType by themeViewModel.themeTypeFlow.collectAsStateWithLifecycle(ThemeType.SYSTEM)
  val uriHandler = LocalUriHandler.current

  StatelessSettingsScreen(
    isDynamicTheme,
    themeType,
    onDynamicThemeSelected = themeViewModel::setDynamicTheme,
    onThemeTypeSelected = themeViewModel::setThemeType,
    onViewDoc = { uriHandler.openUri("https://www.one-reed.org/helios") },
  )
}

@Composable
fun StatelessSettingsScreen(
  isDynamicTheme: Boolean,
  themeType: ThemeType,
  onDynamicThemeSelected: (Boolean) -> Unit,
  onThemeTypeSelected: (ThemeType) -> Unit,
  onViewDoc: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(all = 20.dp),
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .wrapContentHeight()
          .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
      Column(
        modifier = Modifier.padding(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.Start,
      ) {
        Text(
          text = stringResource(R.string.heading_theme_type),
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.labelLarge,
        )
        Column(
          modifier = Modifier.selectableGroup(),
          horizontalAlignment = Alignment.Start,
          verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
          ThemeType.entries.forEach { type ->
            Row(
              modifier =
                Modifier.selectable(
                  selected = (type == themeType),
                  onClick = { onThemeTypeSelected(type) },
                  role = Role.RadioButton,
                ),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Spacer(modifier = Modifier.width(30.dp))
              RadioButton(selected = themeType == type, onClick = null)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stringResource(type.labelRes),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
              )
            }
          }
        }
        if (dynamicThemeSupported) {
          Row(
            modifier =
              Modifier.toggleable(
                value = isDynamicTheme,
                onValueChange = { onDynamicThemeSelected(it) },
                role = Role.Checkbox,
              ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(checked = isDynamicTheme, onCheckedChange = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = stringResource(R.string.label_use_dynamic_theme_colors),
              color = MaterialTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }
      }
    }
    
    Spacer(modifier = Modifier.height(20.dp))
    
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .wrapContentHeight()
          .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
      Column(modifier = Modifier.padding(start = 10.dp)) {
        TextButton(onClick = onViewDoc, contentPadding = PaddingValues(all = 0.dp)) {
          Icon(
            painter = painterResource(R.drawable.help_24px),
            contentDescription = stringResource(R.string.view_online_documentation),
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = stringResource(R.string.view_online_documentation),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun SettingsScreenPreview() {
  DarkHeliosTheme {
    StatelessSettingsScreen(
      isDynamicTheme = true,
      themeType = ThemeType.SYSTEM,
      onDynamicThemeSelected = {},
      onThemeTypeSelected = {},
      onViewDoc = {},
    )
  }
}
