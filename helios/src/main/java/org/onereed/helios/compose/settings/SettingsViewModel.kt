package org.onereed.helios.compose.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.datasource.StoreRepository
import org.onereed.helios.ui.theme.ThemeType
import org.onereed.shared.permission.LocationPermissionState
import org.onereed.shared.permission.getLocationPermissionState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
  private val storeRepository: StoreRepository,
  @ApplicationContext private val context: Context,
) : BaseViewModel() {

  private val _locationUpgradeAvailableFlow = MutableStateFlow(false)
  private val locationUpgradeAvailableFlow = _locationUpgradeAvailableFlow.asStateFlow()

  val settingsUiFlow =
    combine(
        storeRepository.isDynamicThemeFlow,
        storeRepository.themeTypeFlow,
        storeRepository.isCompassSouthTopFlow,
        locationUpgradeAvailableFlow,
        ::SettingsUi,
      )
      .stateIn(initialValue = SettingsUi())

  fun setDynamicTheme(value: Boolean) = storeRepository.setDynamicTheme(value, viewModelScope)

  fun setThemeType(value: ThemeType) = storeRepository.setThemeType(value, viewModelScope)

  fun setCompassSouthTop(value: Boolean) = storeRepository.setCompassSouthTop(value, viewModelScope)

  fun updateLocationUpgradeAvailable() {
    _locationUpgradeAvailableFlow.value =
      (context.getLocationPermissionState() == LocationPermissionState.COARSE_ONLY)
  }
}
