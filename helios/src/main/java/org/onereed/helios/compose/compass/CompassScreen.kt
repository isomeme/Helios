package org.onereed.helios.compose.compass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.onereed.helios.R
import org.onereed.helios.common.arc
import org.onereed.helios.ui.theme.DarkHeliosTheme
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun CompassScreen(compassViewModel: CompassViewModel = hiltViewModel()) {
  // We still need this to get the *initial* value, but we won't
  // use it in the LaunchedEffect anymore.
  val initialHeading by compassViewModel.headingFlow.collectAsStateWithLifecycle(0f)

  // Initialize the Animatable with the first heading we receive.
  val rotationAnimatable = remember { Animatable(initialHeading) }

  // This LaunchedEffect will launch ONCE and run for the lifetime of the composable.
  // The key is now the ViewModel's flow, ensuring the effect restarts if the
  // ViewModel (and thus the flow instance) were to ever change.
  LaunchedEffect(compassViewModel.headingFlow) {
    compassViewModel.headingFlow
      // You might want a small debounce here to filter out sensor noise if it's
      // extremely high frequency, e.g., .debounce(10)
      .debounce(10.milliseconds)
      .collectLatest { newHeading ->
        // The logic inside is identical to before.
        rotationAnimatable.stop()

        val currentRotation = rotationAnimatable.value
        val shortestRotationDelta = arc(from = currentRotation, to = -newHeading)

        rotationAnimatable.animateTo(
          targetValue = currentRotation + shortestRotationDelta,
          animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        )
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
