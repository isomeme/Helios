package org.onereed.helios.compose.app

import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.onereed.helios.compose.schedule.ScheduleScreen
import org.onereed.helios.compose.text.TextScreen
import org.onereed.helios.compose.permission.PermissionActions
import org.onereed.helios.compose.permission.PermissionScreen
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
fun HeliosApp(
  heliosAppState: HeliosAppState = rememberHeliosAppState(),
  heliosAppViewModel: HeliosAppViewModel = hiltViewModel(),
) {
  Timber.d("HeliosApp start")

  // Marked as nullable, but expected to be non-null in runtime app.
  val activity = LocalActivity.current
  val currentDestination = heliosAppState.currentDestination
  val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

  val navActions =
    remember(heliosAppState, heliosAppViewModel) {
      NavActions.create(heliosAppState, heliosAppViewModel)
    }

  val permissionActions =
    remember(locationPermissionState, activity) {
      PermissionActions.create(locationPermissionState, activity)
    }

  if (locationPermissionState.status.isGranted) {
    StatelessHeliosApp(
      navHostController = heliosAppState.navHostController,
      isSelected = { currentDestination?.hasRoute(it::class) ?: false },
      actions = navActions,
    )
  } else {
    PermissionScreen(locationPermissionState = locationPermissionState, actions = permissionActions)
  }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
fun StatelessHeliosApp(
  navHostController: NavHostController,
  isSelected: (Screen) -> Boolean,
  actions: NavActions,
) {
  NavigationSuiteScaffold(
    navigationSuiteType = navSuiteType(),
    navigationItems = {
      Screen.TopLevelScreens.forEach { screen ->
        NavigationSuiteItem(
          icon = { Icon(painterResource(screen.iconRes), stringResource(screen.titleRes)) },
          label = { Text(stringResource(screen.titleRes)) },
          selected = isSelected(screen),
          onClick = { actions.navigateTo(screen) },
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
        composable<Screen.Schedule> { ScheduleScreen(actions = actions) }

        composable<Screen.Text> { TextScreen(actions = actions) }

        composable<Screen.Compass> { Greeting("compass") }

        composable<Screen.Settings> { Greeting("settings") }
      }
    }
  }
}

// See https://issuetracker.google.com/issues/378726489#comment5
@Composable
private fun navSuiteType(): NavigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfo())

@Composable
fun Greeting(name: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Hello $name!")
  }
}
