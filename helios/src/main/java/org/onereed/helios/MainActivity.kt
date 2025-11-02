package org.onereed.helios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
  var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TEXT) }
  var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

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
      when (currentDestination) {
        AppDestinations.SCHEDULE -> Greeting("Schedule", padding = innerPadding)
        AppDestinations.TEXT -> TextScreen(
          selectedIndex = selectedIndex,
          onSelectedIndexChanged = { selectedIndex = it },
          padding = innerPadding)
        AppDestinations.COMPASS -> Greeting("Compass", padding = innerPadding)
        AppDestinations.HELP -> Greeting("Help", padding = innerPadding)
      }
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
fun Greeting(name: String, padding: PaddingValues = PaddingValues()) {
  Text(text = "Hello $name!", Modifier.padding(padding))
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  HeliosTheme { Greeting("Android") }
}
