package org.onereed.helios.compose.compass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.onereed.helios.R
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  val tickCount by compassViewModel.tickerFlow.collectAsStateWithLifecycle(0)
  val rawTargetHeading by remember { derivedStateOf { 60.0f * tickCount } }
  val animatedHeading: Float by
    animateFloatAsState(
      targetValue = rawTargetHeading,
      animationSpec =
        tween(
          durationMillis = 300, // Time it takes to reach the new heading
          easing = FastOutSlowInEasing, // Defines the acceleration/deceleration curve
        ),
      label = "Heading Animation",
    )

  StatelessCompassScreen(animatedHeading)
}

@Composable
fun StatelessCompassScreen(animatedHeading: Float) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Image(
        painter = painterResource(id = R.drawable.ic_compass_face),
        contentDescription = "Compass face",
        colorFilter =
          ColorFilter.tint(color = MaterialTheme.colorScheme.primary, blendMode = BlendMode.SrcIn),
        contentScale = ContentScale.Fit,
        modifier =
          Modifier.graphicsLayer {
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
