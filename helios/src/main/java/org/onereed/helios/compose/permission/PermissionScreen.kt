package org.onereed.helios.compose.permission

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.onereed.helios.R
import org.onereed.helios.common.openSettings
import org.onereed.helios.ui.theme.DarkHeliosTheme

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(locationPermissionState: PermissionState) {
  // Marked as nullable, but expected to be non-null in runtime app.
  val activity = LocalActivity.current
  val permissionActions =
    remember(locationPermissionState, activity) {
      PermissionActions(locationPermissionState, activity)
    }

  // Track if the permission request has been processed after user interaction
  var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
  var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(locationPermissionState.status) {
    // Check if the permission state has changed after the request
    if (hasRequestedPermission) {
      permissionRequestCompleted = true
    }
  }

  if (permissionRequestCompleted) {
    if (locationPermissionState.status.shouldShowRationale) {
      StatelessPermissionScreen(
        explanationRes = R.string.location_permission_rationale,
        okButtonAction = permissionActions.requestPermission,
        exitButtonAction = permissionActions.exitApp,
      )
    } else {
      StatelessPermissionScreen(
        explanationRes = R.string.location_permission_use_settings,
        okButtonAction = permissionActions.openSettings,
        exitButtonAction = permissionActions.exitApp,
      )
    }
  } else {
    SideEffect {
      permissionActions.requestPermission()
      hasRequestedPermission = true
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun StatelessPermissionScreen(
  @StringRes explanationRes: Int,
  okButtonAction: () -> Unit,
  exitButtonAction: () -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
        modifier =
          Modifier.padding(all = 15.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(all = 15.dp)
      ) {
        Text(text = stringResource(explanationRes))

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          FilledTonalButton(onClick = exitButtonAction) {
            Text(text = stringResource(R.string.button_exit))
          }

          Spacer(modifier = Modifier.padding(horizontal = 20.dp))

          FilledTonalButton(onClick = okButtonAction) {
            Text(text = stringResource(R.string.button_ok))
          }
        }
      }
    }
  }
}

@Immutable
@OptIn(ExperimentalPermissionsApi::class)
private data class PermissionActions(
  val requestPermission: () -> Unit,
  val openSettings: () -> Unit,
  val exitApp: () -> Unit,
) {
  constructor(
    permissionState: PermissionState,
    activity: Activity?,
  ) : this(
    requestPermission = permissionState::launchPermissionRequest,
    openSettings = { activity?.openSettings() },
    exitApp = { activity?.finishAndRemoveTask() },
  )
}

@Preview
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionScreenPreview() {
  DarkHeliosTheme {
    StatelessPermissionScreen(
      explanationRes = R.string.location_permission_rationale,
      okButtonAction = {},
      exitButtonAction = {},
    )
  }
}
