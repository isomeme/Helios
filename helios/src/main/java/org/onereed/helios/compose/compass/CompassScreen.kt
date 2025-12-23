package org.onereed.helios.compose.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.abs
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import org.onereed.helios.R
import org.onereed.helios.common.arc
import org.onereed.helios.ui.theme.DarkHeliosTheme

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val initialHeading by compassViewModel.headingFlow.collectAsStateWithLifecycle()
  val rotationAnimatable = remember { Animatable(initialHeading) }

  LaunchedEffect(compassViewModel.headingFlow) {
    compassViewModel.headingFlow.collectLatest { newHeading ->
      val currentRotation = rotationAnimatable.value
      val shortestRotationDelta = arc(from = currentRotation, to = -newHeading)
      val nextRotation = currentRotation + shortestRotationDelta

      val animationSpec: AnimationSpec<Float> =
        if (abs(shortestRotationDelta) <= 3.0f) snap()
        else tween(durationMillis = 50, easing = LinearEasing)

      rotationAnimatable.animateTo(targetValue = nextRotation, animationSpec = animationSpec)
    }
  }

  StatelessCompassScreen(rotationAnimatable.value)
}

@OptIn(ExperimentalAtomicApi::class)
@Composable
fun StatelessCompassScreen(animatedHeading: Float) {
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
            rotationZ = animatedHeading
          },
        contentDescription = null,
      )
    }
  }
}

@Preview
@Composable
fun CompassScreenPreview() {
  DarkHeliosTheme { StatelessCompassScreen(animatedHeading = 30.0f) }
}
