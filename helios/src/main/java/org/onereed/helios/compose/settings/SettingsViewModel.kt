package org.onereed.helios.compose.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.datasource.StoreRepository
import org.onereed.helios.ui.theme.ThemeType

@HiltViewModel
class SettingsViewModel @Inject constructor(val storeRepository: StoreRepository) :
  BaseViewModel() {

  val settingsUiFlow =
    combine(
        storeRepository.isDynamicThemeFlow,
        storeRepository.themeTypeFlow,
        storeRepository.isCompassSouthTopFlow,
        ::SettingsUi,
      )
      .stateIn(initialValue = SettingsUi.INITIAL)

  fun setDynamicTheme(value: Boolean) = storeRepository.setDynamicTheme(value, viewModelScope)

  fun setThemeType(value: ThemeType) = storeRepository.setThemeType(value, viewModelScope)

  fun setCompassSouthTop(value: Boolean) = storeRepository.setCompassSouthTop(value, viewModelScope)
}
