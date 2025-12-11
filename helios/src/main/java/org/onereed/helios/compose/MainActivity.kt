package org.onereed.helios.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.onereed.helios.compose.app.HeliosApp
import org.onereed.helios.ui.theme.HeliosTheme
import org.onereed.helios.util.LifecycleLogger

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  init {
    lifecycle.addObserver(LifecycleLogger())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent { HeliosTheme { HeliosApp() } }
  }
}
