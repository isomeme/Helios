package org.onereed.helios.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.onereed.helios.ui.theme.HeliosTheme

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun HeliosApp(appState: HeliosAppState = rememberHeliosAppState()) {

  // Read the @Composable property here, in a @Composable context.
  val currentDestination = appState.currentDestination

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      Screen.TopLevelScreens.forEach { screen ->
        item(
          icon = { Icon(painterResource(screen.iconRes), stringResource(screen.titleRes)) },
          label = { Text(stringResource(screen.titleRes)) },
          selected = currentDestination?.hasRoute(screen::class) ?: false,
          onClick = { appState.navigateToTopLevelScreen(screen) },
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = appState.navController,
        startDestination = Screen.Schedule,
        modifier = Modifier.padding(innerPadding),
      ) {
        composable<Screen.Schedule> {
          val actions =
            object : ScheduleScreenActions {
              override fun onTextIndexSelected(index: Int) = appState.navigateToTextIndex(index)
            }
          ScheduleScreen(actions = actions)
        }

        composable<Screen.Text> { backStackEntry ->
          val screenText: Screen.Text = backStackEntry.toRoute()
          val actions =
            object : TextScreenActions {
              override fun onTextIndexSelected(index: Int) = appState.navigateToTextIndex(index)
            }
          TextScreen(selectedIndexFromNav = screenText.selectedIndex, actions = actions)
        }

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

@Preview
@Composable
fun HeliosAppPreview() {
  HeliosTheme { HeliosApp() }
}
