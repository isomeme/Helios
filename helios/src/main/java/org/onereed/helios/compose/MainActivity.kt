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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.onereed.helios.R
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
  val currentDestination = currentBackStackEntry?.destination

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      item(
        icon = {
          Icon(
            painterResource(R.drawable.schedule_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            stringResource(R.string.screen_schedule),
          )
        },
        label = { Text(stringResource(R.string.screen_schedule)) },
        selected = currentDestination?.hasRoute<Screen.Schedule>() ?: false,
        onClick = { navController.navigate(Screen.Schedule) },
      )
      item(
        icon = {
          Icon(
            painterResource(R.drawable.article_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            stringResource(R.string.screen_text),
          )
        },
        label = { Text(stringResource(R.string.screen_text)) },
        selected = currentDestination?.hasRoute<Screen.Text>() ?: false,
        onClick = { navController.navigate(Screen.Text) },
      )
      item(
        icon = {
          Icon(
            painterResource(R.drawable.navigation_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            stringResource(R.string.screen_compass),
          )
        },
        label = { Text(stringResource(R.string.screen_compass)) },
        selected = currentDestination?.hasRoute<Screen.Compass>() ?: false,
        onClick = { navController.navigate(Screen.Compass) },
      )
      item(
        icon = {
          Icon(
            painterResource(R.drawable.help_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
            stringResource(R.string.screen_help),
          )
        },
        label = { Text(stringResource(R.string.screen_help)) },
        selected = currentDestination?.hasRoute<Screen.Help>() ?: false,
        onClick = { navController.navigate(Screen.Help) },
      )
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = Screen.Schedule,
        modifier = Modifier.padding(innerPadding),
      ) {
        composable<Screen.Schedule> {
          ScheduleScreen(navToText = { navController.navigate(Screen.Text) })
        }
        composable<Screen.Text> { TextScreen() }
        composable<Screen.Compass> { Greeting("compass") }
        composable<Screen.Help> { Greeting("help") }
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
