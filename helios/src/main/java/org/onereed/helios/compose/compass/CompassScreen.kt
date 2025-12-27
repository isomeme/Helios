package org.onereed.helios.compose.compass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.FlowPreview
import org.onereed.helios.R
import org.onereed.helios.common.PlaceTime
import org.onereed.helios.compose.shared.sunColorFilters
import org.onereed.helios.datasource.SunResources
import org.onereed.helios.sun.SunCompass
import org.onereed.helios.sun.SunTimeSeries
import org.onereed.helios.ui.theme.DarkHeliosTheme

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val compassUi by compassViewModel.compassUiFlow.collectAsStateWithLifecycle()
  val heading by compassViewModel.headingFlow.collectAsStateWithLifecycle()
  val compassAngle by remember {
    derivedStateOf { 360f - heading } // Compass turns opposite heading
  }

  val isLocked by compassViewModel.isLockedFlow.collectAsStateWithLifecycle()
  val haptics = LocalHapticFeedback.current

  StatelessCompassScreen(
    compassUi = compassUi,
    compassAngle = compassAngle,
    isLocked = isLocked,
    onLockChange = {
      haptics.performHapticFeedback(HapticFeedbackType.Confirm)
      compassViewModel.setLocked(it)
    },
  )
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
fun StatelessCompassScreen(
  compassUi: CompassUi,
  compassAngle: Float,
  isLocked: Boolean,
  onLockChange: (Boolean) -> Unit,
) {
  val viewLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  val compassFaceColor = MaterialTheme.colorScheme.outlineVariant

  val viewLineColorFilter =
    remember(viewLineColor) { ColorFilter.tint(color = viewLineColor, blendMode = BlendMode.SrcIn) }
  val compassFaceColorFilter =
    remember(compassFaceColor) {
      ColorFilter.tint(color = compassFaceColor, blendMode = BlendMode.SrcIn)
    }

  val sunColorFilters = sunColorFilters()

  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Row(
        modifier =
          Modifier.align(Alignment.BottomEnd)
            .padding(all = 15.dp)
            .toggleable(value = isLocked, onValueChange = onLockChange, role = Role.Checkbox)
            .padding(all = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Checkbox(checked = isLocked, onCheckedChange = null)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
          text = stringResource(id = R.string.control_lock_compass),
          style = MaterialTheme.typography.labelMedium,
        )
      }

      Image(
        painter = painterResource(id = R.drawable.ic_view_line),
        contentDescription = stringResource(id = R.string.content_view_line),
        colorFilter = viewLineColorFilter,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().zIndex(0f),
      )

      Box(
        modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = compassAngle },
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_compass_face),
          contentDescription = stringResource(id = R.string.content_compass_display),
          colorFilter = compassFaceColorFilter,
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize().zIndex(1f),
        )

        compassUi.items.forEach { item ->
          Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = stringResource(id = item.nameRes),
            colorFilter = sunColorFilters[item.ordinal],
            modifier =
              Modifier.fillMaxSize().zIndex(2f).graphicsLayer {
                scaleX = item.scale
                scaleY = item.scale
                translationX = item.point.x * size.minDimension
                translationY = item.point.y * size.minDimension
                rotationZ = item.rotation
              },
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun CompassScreenPreview() {
  val sunResources = SunResources(LocalContext.current)
  val hereNow = PlaceTime(lat = 34.0, lon = -118.0, alt = 0.0, instant = now())
  val sunTimeSeries = SunTimeSeries(hereNow)
  val sunCompass = SunCompass.compute(sunTimeSeries)
  val compassUi = CompassUi.Factory(sunResources).create(sunCompass)

  DarkHeliosTheme {
    StatelessCompassScreen(
      compassUi = compassUi,
      compassAngle = 30f,
      isLocked = true,
      onLockChange = {},
    )
  }
}
