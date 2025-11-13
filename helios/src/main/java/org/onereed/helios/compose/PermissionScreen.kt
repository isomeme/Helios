package org.onereed.helios.compose

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import org.onereed.helios.R
import org.onereed.helios.ui.theme.HeliosTheme

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionScreen(
  locationPermissionState: PermissionState,
  permissionRequestCompletedOverride: Boolean? = null, // For use in preview
) {

  val context = LocalContext.current

  // Track if the permission request has been processed after user interaction
  var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
  var permissionRequestCompleted by rememberSaveable {
    mutableStateOf(permissionRequestCompletedOverride ?: false)
  }

  LaunchedEffect(locationPermissionState.status) {
    // Check if the permission state has changed after the request
    if (hasRequestedPermission) {
      permissionRequestCompleted = true
    }
  }

  if (permissionRequestCompleted) {
    Box(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
      contentAlignment = Alignment.Center,
    ) {
      if (locationPermissionState.status.shouldShowRationale) {
        ChoiceDisplay(explanationRes = R.string.location_permission_rationale) {
          locationPermissionState.launchPermissionRequest()
          hasRequestedPermission = true
        }
      } else {
        ChoiceDisplay(explanationRes = R.string.location_permission_use_settings) {
          val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.fromParts("package", context.packageName, /* fragment= */ null)
            }
          context.startActivity(intent)
        }
      }
    }
  } else {
    SideEffect { locationPermissionState.launchPermissionRequest() }
    @Suppress("AssignedValueIsNeverRead") // False positive
    hasRequestedPermission = true
  }
}

@Composable
fun ChoiceDisplay(@StringRes explanationRes: Int, positiveButtonAction: () -> Unit) {
  val activity = LocalActivity.current

  Column(modifier = Modifier.wrapContentSize().padding(30.dp)) {
    Text(text = stringResource(explanationRes), color = MaterialTheme.colorScheme.onSurface)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      FilledTonalButton(
        onClick = { activity?.finishAndRemoveTask() },
        modifier = Modifier.padding(20.dp),
      ) {
        Text(text = stringResource(R.string.button_exit))
      }
      FilledTonalButton(onClick = positiveButtonAction, modifier = Modifier.padding(20.dp)) {
        Text(text = stringResource(R.string.button_ok))
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun PermissionScreenPreview() {
  val permissionState =
    rememberPermissionState(
      permission = Manifest.permission.ACCESS_FINE_LOCATION,
      previewPermissionStatus = PermissionStatus.Denied(shouldShowRationale = true),
    )

  HeliosTheme {
    PermissionScreen(
      locationPermissionState = permissionState,
      permissionRequestCompletedOverride = true,
    )
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F1416)
@Composable
fun ChoiceDisplayPreview() {
  HeliosTheme { ChoiceDisplay(explanationRes = R.string.location_permission_use_settings) {} }
}
