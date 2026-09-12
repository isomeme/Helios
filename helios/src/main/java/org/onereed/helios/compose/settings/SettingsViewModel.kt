package org.onereed.helios.compose.settings

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.datasource.StoreRepository
import org.onereed.helios.ui.theme.ThemeType
import org.onereed.shared.permission.isAccuracyImprovementAvailable
import org.onereed.shared.ui.UiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
  private val storeRepository: StoreRepository,
  @ApplicationContext private val context: Context,
) : BaseViewModel() {

  private val _accuracyImprovementAvailableFlow = MutableStateFlow(false)
  private val accuracyImprovementAvailableFlow = _accuracyImprovementAvailableFlow.asStateFlow()

  val uiStateFlow =
    combine(
        storeRepository.isDynamicThemeFlow,
        storeRepository.themeTypeFlow,
        storeRepository.isCompassSouthTopFlow,
        accuracyImprovementAvailableFlow,
        ::SettingsUi,
      )
      .map { UiState.Success(it) }
      .stateIn(initialValue = UiState.Loading)

  init {
    updateAccuracyImprovementAvailable()
  }

  fun setDynamicTheme(value: Boolean) = storeRepository.setDynamicTheme(value, viewModelScope)

  fun setThemeType(value: ThemeType) = storeRepository.setThemeType(value, viewModelScope)

  fun setCompassSouthTop(value: Boolean) = storeRepository.setCompassSouthTop(value, viewModelScope)

  fun updateAccuracyImprovementAvailable() {
    _accuracyImprovementAvailableFlow.value = context.isAccuracyImprovementAvailable()
  }
}
