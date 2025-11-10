package org.onereed.helios.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions

@Stable // Important for Compose performance
class HeliosAppState(val navController: NavHostController) {

  val currentDestination: NavDestination?
    @Composable get() = navController.currentBackStackEntryAsState().value?.destination

  /**
   * Navigation logic for top-level screens in the navigation suite. This pops up to the start
   * destination to avoid building up a large back stack.
   */
  fun navigateTo(screen: Screen) {
    val topLevelNavOptions = navOptions {

      // Pop up to the start destination of the graph to avoid building up a large stack of
      // destinations on the back stack as users select items.
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }

      // Avoid multiple copies of the same destination when re-selecting the same item.
      launchSingleTop = true

      // Restore state when re-selecting a previously selected item.
      restoreState = true
    }

    navController.navigate(screen, topLevelNavOptions)
  }
}

@Composable
fun rememberHeliosAppState(
  navController: NavHostController = rememberNavController()
): HeliosAppState {
  return remember(navController) { HeliosAppState(navController) }
}
