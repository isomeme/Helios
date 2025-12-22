package org.onereed.helios.compose.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
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
import kotlinx.coroutines.FlowPreview
import org.onereed.helios.R
import org.onereed.helios.common.arc
import org.onereed.helios.ui.theme.DarkHeliosTheme

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val initialHeading by compassViewModel.headingFlow.collectAsStateWithLifecycle(0f)
  val rotationAnimatable = remember { Animatable(initialHeading) }

  LaunchedEffect(compassViewModel.headingFlow) {
    compassViewModel.headingFlow.collect { newHeading ->
      val currentRotation = rotationAnimatable.value
      val shortestRotationDelta = arc(from = currentRotation, to = -newHeading)
      val nextRotation = currentRotation + shortestRotationDelta

      rotationAnimatable.animateTo(targetValue = nextRotation, animationSpec = snap())
    }
  }

  StatelessCompassScreen(rotationAnimatable.value)
}

@Composable
fun StatelessCompassScreen(animatedHeading: Float) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Image(
        painter = painterResource(id = R.drawable.ic_view_line),
        contentDescription = "View line",
        colorFilter =
          ColorFilter.tint(
            color = MaterialTheme.colorScheme.outlineVariant,
            blendMode = BlendMode.SrcIn,
          ),
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize().zIndex(0f),
      )
      Image(
        painter = painterResource(id = R.drawable.ic_compass_face),
        contentDescription = "Compass face",
        colorFilter =
          ColorFilter.tint(color = MaterialTheme.colorScheme.outline, blendMode = BlendMode.SrcIn),
        contentScale = ContentScale.Fit,
        modifier =
          Modifier.fillMaxSize().zIndex(1f).graphicsLayer {
            // The animatedHeading value smoothly updates the rotation
            rotationZ = animatedHeading
          },
      )
    }
  }
}

@Preview
@Composable
fun CompassScreenPreview() {
  DarkHeliosTheme { StatelessCompassScreen(animatedHeading = 30.0f) }
}
