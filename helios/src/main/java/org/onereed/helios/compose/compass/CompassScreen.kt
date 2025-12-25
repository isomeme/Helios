package org.onereed.helios.compose.compass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.FlowPreview
import org.onereed.helios.R
import org.onereed.helios.ui.theme.DarkHeliosTheme

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val isLocked by compassViewModel.isLockedFlow.collectAsStateWithLifecycle()
  val heading by compassViewModel.headingFlow.collectAsStateWithLifecycle()
  val compassAngle by remember {
    derivedStateOf { 360f - heading } // Compass turns opposite heading
  }

  StatelessCompassScreen(
    compassAngle = compassAngle,
    isLocked = isLocked,
    onLockChange = compassViewModel::setLocked,
  )
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
fun StatelessCompassScreen(
  compassAngle: Float,
  isLocked: Boolean,
  onLockChange: (Boolean) -> Unit,
) {
  val viewLinePainterResource = painterResource(id = R.drawable.ic_view_line)
  val compassFacePainterResource = painterResource(id = R.drawable.ic_compass_face)

  val viewLineColor = MaterialTheme.colorScheme.outlineVariant
  val compassFaceColor = MaterialTheme.colorScheme.outline

  val viewLineColorFilter =
    remember(viewLineColor) { ColorFilter.tint(color = viewLineColor, blendMode = BlendMode.SrcIn) }
  val compassFaceColorFilter =
    remember(compassFaceColor) {
      ColorFilter.tint(color = compassFaceColor, blendMode = BlendMode.SrcIn)
    }

  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Row(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Checkbox(checked = isLocked, onCheckedChange = onLockChange)

        Text(
          text = stringResource(R.string.control_lock_compass),
          style = MaterialTheme.typography.labelMedium,
        )
      }

      Image(
        painter = viewLinePainterResource,
        colorFilter = viewLineColorFilter,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().zIndex(0f),
        contentDescription = null,
      )

      Image(
        painter = compassFacePainterResource,
        colorFilter = compassFaceColorFilter,
        contentScale = ContentScale.Fit,
        modifier =
          Modifier.fillMaxSize().zIndex(1f).graphicsLayer {
            // The animatedHeading value smoothly updates the rotation
            rotationZ = compassAngle
          },
        contentDescription = null,
      )
    }
  }
}

@Preview
@Composable
fun CompassScreenPreview() {
  DarkHeliosTheme {
    StatelessCompassScreen(compassAngle = 30.0f, isLocked = true, onLockChange = {})
  }
}
