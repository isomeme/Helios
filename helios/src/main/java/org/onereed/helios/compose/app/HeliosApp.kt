package org.onereed.helios.compose.app

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults.navigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.onereed.helios.R
import org.onereed.helios.compose.compass.CompassScreen
import org.onereed.helios.compose.schedule.ScheduleScreen
import org.onereed.helios.compose.settings.SettingsScreen
import org.onereed.helios.compose.text.TextScreen
import org.onereed.shared.permission.hasPermission
import org.onereed.shared.screen.PermissionScreen
import timber.log.Timber

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
@SuppressLint("InlinedApi") // ACCESS_FINE_LOCATION protected by hasPerm processing
fun HeliosApp(heliosAppState: HeliosAppState = rememberHeliosAppState()) {
  val context = LocalContext.current
  val activity = LocalActivity.current!!

  val navActions = remember(heliosAppState) { NavActions(heliosAppState) }
  var hasPerm by remember { mutableStateOf(context.hasPermission(ACCESS_FINE_LOCATION)) }

  LaunchedEffect(key1 = hasPerm) { Timber.d("Δ hasPerm -> $hasPerm") }

  if (hasPerm) {
    val currentDestination = heliosAppState.currentDestination

    StatelessHeliosApp(
      navHostController = heliosAppState.navHostController,
      isSelectedFn = { currentDestination?.hasRoute(it::class) ?: false },
      navActions = navActions,
    )
  } else {
    PermissionScreen(
      permission = ACCESS_FINE_LOCATION,
      rationaleId = R.string.location_permission_rationale,
      settingsId = R.string.location_permission_use_settings,
      onPermissionGranted = { hasPerm = true },
      onDismiss = activity::finish,
    )
  }

  // When the user changes app permissions using system settings while the app is closed, this
  // onResume check syncs to the new state.

  LifecycleResumeEffect(Unit) {
    hasPerm = context.hasPermission(ACCESS_FINE_LOCATION)
    onPauseOrDispose {}
  }
}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
fun StatelessHeliosApp(
  navHostController: NavHostController,
  isSelectedFn: (Screen) -> Boolean,
  navActions: NavActions,
) {
  NavigationSuiteScaffold(
    navigationSuiteType = navSuiteType(),
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
private fun navSuiteType(): NavigationSuiteType = navigationSuiteType(currentWindowAdaptiveInfoV2())
