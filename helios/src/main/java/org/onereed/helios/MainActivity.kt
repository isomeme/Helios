package org.onereed.helios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
          icon = { Icon(painterResource(it.iconId), it.label) },
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

enum class AppDestinations(val label: String, val iconId: Int) {
  SCHEDULE("Schedule", R.drawable.schedule_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  TEXT("Text", R.drawable.article_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  COMPASS("Compass", R.drawable.navigation_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
  HELP("Help", R.drawable.help_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
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
