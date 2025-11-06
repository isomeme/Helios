package org.onereed.helios.compose

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.onereed.helios.ui.theme.HeliosTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent { HeliosTheme { HeliosApp() } }
  }
}

@Composable
fun HeliosApp(navigator: Navigator = hiltViewModel<NavigatorViewModel>().navigator) {
  val currentDestination = navigator.currentDestination

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      AppDestination.entries.forEach {
        item(
          icon = { Icon(painterResource(it.iconId), it.label) },
          label = { Text(it.label) },
          selected = it == currentDestination,
          onClick = { navigator.navigateTo(it) },
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      when (currentDestination) {
        AppDestination.SCHEDULE -> Greeting("Schedule", padding = innerPadding)
        AppDestination.TEXT -> TextScreen(padding = innerPadding)
        AppDestination.COMPASS -> Greeting("Compass", padding = innerPadding)
        AppDestination.HELP -> Greeting("Help", padding = innerPadding)
      }
    }
  }
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

@PreviewScreenSizes
@Composable
fun HeliosAppPreview() {
  HeliosTheme { HeliosApp(Navigator()) }
}
