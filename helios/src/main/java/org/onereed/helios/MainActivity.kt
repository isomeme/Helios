package org.onereed.helios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.onereed.helios.ui.theme.HeliosTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { HeliosTheme { HeliosApp() } }
  }
}

@PreviewScreenSizes
@Composable
fun HeliosApp() {
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SCHEDULE) }

  val sunResources = SunResources.from(LocalContext.current)

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      AppDestinations.entries.forEach {
        item(
          icon = { Icon(it.icon, contentDescription = it.label) },
          label = { Text(it.label) },
          selected = it == currentDestination,
          onClick = { currentDestination = it },
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      TextDisplay(2, sunResources, innerPadding)
    }
  }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
  SCHEDULE("Schedule", Icons.Outlined.Schedule),
  TEXT("Text", Icons.AutoMirrored.Outlined.Article),
  COMPASS("Compass", Icons.Outlined.Navigation),
  HELP("Help", Icons.AutoMirrored.Outlined.Help),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  HeliosTheme { Greeting("Android") }
}
