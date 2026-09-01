package org.onereed.helios.compose.app

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.onereed.helios.R
import org.onereed.helios.compose.compass.CompassScreen
import org.onereed.helios.compose.schedule.ScheduleScreen
import org.onereed.helios.compose.settings.SettingsScreen
import org.onereed.helios.compose.text.TextScreen
import org.onereed.shared.permission.PermissionGate

@Composable
fun HeliosApp(heliosAppState: HeliosAppState = rememberHeliosAppState()) {
  val navActions = remember(heliosAppState) { NavActions(heliosAppState) }
  val currentDestination = heliosAppState.currentDestination

  StatelessHeliosApp(
    navHostController = heliosAppState.navHostController,
    isSelectedFn = { currentDestination?.hasRoute(it::class) ?: false },
    navActions = navActions,
  )
}

@Composable
fun StatelessHeliosApp(
  navHostController: NavHostController,
  isSelectedFn: (Screen) -> Boolean,
  navActions: NavActions,
) {
  NavigationSuiteScaffold(
    navigationItems = {
      Screen.TopLevelScreens.forEach { screen ->
        val isSelected = isSelectedFn(screen)

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
        modifier = Modifier.padding(innerPadding),
        navController = navHostController,
        startDestination = Screen.Schedule,
      ) {
        composable<Screen.Schedule> { WithPermissions { ScheduleScreen(navActions = navActions) } }
        composable<Screen.Text> { TextScreen() }
        composable<Screen.Compass> { WithPermissions { CompassScreen() } }
        composable<Screen.Settings> { SettingsScreen() }
      }
    }
  }
}

@Composable
private fun WithPermissions(content: @Composable () -> Unit) {
  PermissionGate(
    permissions = listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
    allowCoarseLocation = true,
    grantButtonLabel = stringResource(R.string.permission_button),
    rationaleTitle = stringResource(R.string.permission_title),
    rationaleDescription = stringResource(R.string.permission_rationale),
    useSettingsTitle = stringResource(R.string.permission_title),
    useSettingsDescription = stringResource(R.string.permission_use_settings),
  ) {
    content()
  }
}
