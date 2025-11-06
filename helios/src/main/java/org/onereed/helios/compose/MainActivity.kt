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
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun HeliosApp() {
  val navController = rememberNavController()
  val currentBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentBackStackEntry?.destination?.route

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      AppDestination.entries.forEach {
        item(
          icon = { Icon(painterResource(it.iconId), it.label) },
          label = { Text(it.label) },
          selected = currentRoute == it.route,
          onClick = { navController.navigate(it.route) },
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = Screen.Schedule.route,
        modifier = Modifier.padding(innerPadding),
      ) {
        composable(Screen.Schedule.route) {
          ScheduleScreen(navToText = { navController.navigate(Screen.Text.route) })
        }
        composable(Screen.Text.route) { TextScreen() }
        composable(Screen.Compass.route) { Greeting("compass") }
        composable(Screen.Help.route) { Greeting("help") }
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
  HeliosTheme { HeliosApp() }
}
