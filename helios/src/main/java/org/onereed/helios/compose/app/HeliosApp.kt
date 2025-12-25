package org.onereed.helios.compose.app

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults.navigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.onereed.helios.compose.compass.CompassScreen
import org.onereed.helios.compose.permission.PermissionScreen
import org.onereed.helios.compose.schedule.ScheduleScreen
import org.onereed.helios.compose.settings.SettingsScreen
import org.onereed.helios.compose.text.TextScreen

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
fun HeliosApp(heliosAppState: HeliosAppState = rememberHeliosAppState()) {
  val navActions = remember(heliosAppState) { NavActions(heliosAppState) }
  val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

  if (locationPermissionState.status.isGranted) {
    val currentDestination = heliosAppState.currentDestination

    StatelessHeliosApp(
      navHostController = heliosAppState.navHostController,
      isSelected = { currentDestination?.hasRoute(it::class) ?: false },
      navActions = navActions,
    )
  } else {
    PermissionScreen(locationPermissionState = locationPermissionState)
  }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
fun StatelessHeliosApp(
  navHostController: NavHostController,
  isSelected: (Screen) -> Boolean,
  navActions: NavActions,
) {
  NavigationSuiteScaffold(
    navigationSuiteType = navSuiteType(),
    navigationItems = {
      Screen.TopLevelScreens.forEach { screen ->
        val isSelected = isSelected(screen)

        NavigationSuiteItem(
          icon = { Icon(painterResource(screen.iconRes), stringResource(screen.titleRes)) },
          label = { Text(stringResource(screen.titleRes)) },
          selected = isSelected,
          onClick = {
            if (!isSelected) {
              navActions.navigateTo(screen)
            }
          },
        )
      }
    },
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = navHostController,
        startDestination = Screen.Schedule,
        modifier = Modifier.padding(innerPadding),
      ) {
        composable<Screen.Schedule> { ScheduleScreen(navActions = navActions) }
        composable<Screen.Text> { TextScreen() }
        composable<Screen.Compass> { CompassScreen() }
        composable<Screen.Settings> { SettingsScreen() }
      }
    }
  }
}

// See https://issuetracker.google.com/issues/378726489#comment5
@Composable
private fun navSuiteType(): NavigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo())
