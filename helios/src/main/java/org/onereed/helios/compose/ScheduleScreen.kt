package org.onereed.helios.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
internal fun ScheduleScreen(
  padding: PaddingValues = PaddingValues(),
  scheduleUi: ScheduleUi = hiltViewModel<ScheduleViewModel>().ui,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.width(IntrinsicSize.Min).padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      scheduleUi.buttons.forEachIndexed { index, ui ->
        FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = { ui.onSelect() }) {
          Text(ui.name)
        }
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
@SuppressLint("ViewModelConstructorInComposable")
internal fun ScheduleScreenPreview() {
  val sunResources = SunResources.load(LocalContext.current)
  val scheduleUi = ScheduleUi.create(sunResources) {}
  HeliosTheme { ScheduleScreen(scheduleUi = scheduleUi) }
}
