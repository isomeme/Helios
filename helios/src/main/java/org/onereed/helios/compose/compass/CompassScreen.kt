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
import kotlinx.coroutines.FlowPreview
import org.onereed.helios.R
import org.onereed.helios.ui.theme.DarkHeliosTheme
import org.onereed.helios.ui.theme.extendedColors
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val sunCompass by compassViewModel.sunCompassFlow.collectAsStateWithLifecycle()
  val sunAzimuth by remember { derivedStateOf { sunCompass.sunAzimuth.toFloat() } }
  val isSunClockwise by remember { derivedStateOf { sunCompass.isSunClockwise } }

  val heading by compassViewModel.headingFlow.collectAsStateWithLifecycle()
  val compassAngle by remember {
    derivedStateOf { 360f - heading } // Compass turns opposite heading
  }

  val isLocked by compassViewModel.isLockedFlow.collectAsStateWithLifecycle()
  val haptics = LocalHapticFeedback.current

  StatelessCompassScreen(
    sunAzimuth = sunAzimuth,
    isSunClockwise = isSunClockwise,
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
  compassAngle: Float,
  sunAzimuth: Float,
  isSunClockwise: Boolean,
  isLocked: Boolean,
  onLockChange: (Boolean) -> Unit,
) {
  val viewLineColor = MaterialTheme.colorScheme.outlineVariant
  val compassFaceColor = MaterialTheme.colorScheme.outline
  val sunColor = MaterialTheme.extendedColors.sun.color

  val viewLineColorFilter =
    remember(viewLineColor) { ColorFilter.tint(color = viewLineColor, blendMode = BlendMode.SrcIn) }
  val compassFaceColorFilter =
    remember(compassFaceColor) {
      ColorFilter.tint(color = compassFaceColor, blendMode = BlendMode.SrcIn)
    }
  val sunColorFilter =
    remember(sunColor) { ColorFilter.tint(color = sunColor, blendMode = BlendMode.SrcIn) }

  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Row(
        modifier =
          Modifier.align(Alignment.BottomEnd)
            .padding(all = 20.dp)
            .toggleable(value = isLocked, onValueChange = onLockChange, role = Role.Checkbox),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Checkbox(checked = isLocked, onCheckedChange = null)

        Spacer(modifier = Modifier.width(10.dp))

        Text(
          text = stringResource(R.string.control_lock_compass),
          style = MaterialTheme.typography.labelMedium,
        )
      }

      Image(
        painter = painterResource(id = R.drawable.ic_view_line),
        colorFilter = viewLineColorFilter,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().zIndex(0f),
        contentDescription = null,
      )

      Box(
        modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = compassAngle },
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_compass_face),
          colorFilter = compassFaceColorFilter,
          contentScale = ContentScale.Fit,
          modifier = Modifier.fillMaxSize().zIndex(1f),
          contentDescription = null,
        )

        val sunAzimuthRad = toRadians(sunAzimuth.toDouble()).toFloat()

        Image(
          painter = painterResource(id = R.drawable.ic_solid_dot),
          colorFilter = sunColorFilter,
          modifier =
            Modifier.fillMaxSize().zIndex(2f).graphicsLayer {
              scaleX = 0.1f
              scaleY = 0.1f
              translationX = sin(sunAzimuthRad) * 0.44f * size.minDimension
              translationY = -cos(sunAzimuthRad) * 0.44f * size.minDimension
            },
          contentDescription = null,
        )
      }
    }
  }
}

@Preview
@Composable
fun CompassScreenPreview() {
  DarkHeliosTheme {
    StatelessCompassScreen(
      compassAngle = 30f,
      sunAzimuth = 60f,
      isSunClockwise = true,
      isLocked = true,
      onLockChange = {},
    )
  }
}
