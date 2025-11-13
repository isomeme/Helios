package org.onereed.helios.compose

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.onereed.helios.ui.theme.HeliosTheme
import timber.log.Timber

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HeliosApp(
  appState: HeliosAppState = rememberHeliosAppState(),
  heliosAppViewModel: HeliosAppViewModel = hiltViewModel(),
) {
  Timber.d("HeliosApp start")

  val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

  if (!locationPermissionState.status.isGranted) {
    PermissionScreen(locationPermissionState)
    return
  }

  // Read the @Composable property here, in a @Composable context.
  val currentDestination = appState.currentDestination

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      Screen.TopLevelScreens.forEach { screen ->
        item(
          icon = { Icon(painterResource(screen.iconRes), stringResource(screen.titleRes)) },
          label = { Text(stringResource(screen.titleRes)) },
          selected = currentDestination?.hasRoute(screen::class) ?: false,
          onClick = { appState.navigateTo(screen) },
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
              override fun navigateToTextIndex(index: Int) {
                heliosAppViewModel.selectTextIndex(index)
                appState.navigateTo(Screen.Text)
              }
            }
          ScheduleScreen(actions = actions)
        }

        composable<Screen.Text> {
          val actions =
            object : TextScreenActions {
              override fun selectIndex(index: Int) {
                heliosAppViewModel.selectTextIndex(index)
              }
            }
          TextScreen(actions = actions)
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
@SuppressLint("ViewModelConstructorInComposable")
fun HeliosAppPreview() {
  val heliosAppViewModel = HeliosAppViewModel(TextStateHolder())
  HeliosTheme { HeliosApp(heliosAppViewModel = heliosAppViewModel) }
}
