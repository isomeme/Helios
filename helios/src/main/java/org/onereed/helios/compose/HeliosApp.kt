package org.onereed.helios.compose

import android.Manifest
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import timber.log.Timber

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HeliosApp(
  heliosAppState: HeliosAppState = rememberHeliosAppState(),
  heliosAppViewModel: HeliosAppViewModel = hiltViewModel(),
) {
  Timber.d("HeliosApp start")

  val currentDestination = heliosAppState.currentDestination

  val navActions =
    object : NavActions() {
      override fun navigateTo(screen: Screen) = heliosAppState.navigateTo(screen)

      override fun selectTextIndex(index: Int) = heliosAppViewModel.selectTextIndex(index)
    }

  StatelessHeliosApp(
    locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
    navHostController = heliosAppState.navHostController,
    isSelected = { currentDestination?.hasRoute(it::class) ?: false },
    navActions = navActions,
  )
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
@Composable
fun StatelessHeliosApp(
  locationPermissionState: PermissionState,
  navHostController: NavHostController,
  isSelected: (Screen) -> Boolean,
  navActions: NavActions,
) {
  if (!locationPermissionState.status.isGranted) {
    PermissionScreen(locationPermissionState)
    return
  }

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      Screen.TopLevelScreens.forEach { screen ->
        item(
          icon = { Icon(painterResource(screen.iconRes), stringResource(screen.titleRes)) },
          label = { Text(stringResource(screen.titleRes)) },
          selected = isSelected(screen),
          onClick = { navActions.navigateTo(screen) },
        )
      }
    }
  ) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
      NavHost(
        navController = navHostController,
        startDestination = Screen.Schedule,
        modifier = Modifier.padding(innerPadding),
      ) {
        composable<Screen.Schedule> { ScheduleScreen(navActions = navActions) }

        composable<Screen.Text> { TextScreen(navActions = navActions) }

        composable<Screen.Compass> { Greeting("compass") }

        composable<Screen.Settings> { Greeting("settings") }
      }
    }
  }
}

@Composable
fun Greeting(name: String, padding: PaddingValues = PaddingValues()) {
  Text(text = "Hello $name!", Modifier.padding(padding))
}
