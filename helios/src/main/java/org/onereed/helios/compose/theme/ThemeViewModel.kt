package org.onereed.helios.compose.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import org.onereed.helios.datasource.StoreRepository

@HiltViewModel
class ThemeViewModel @Inject constructor(val storeRepository: StoreRepository) : ViewModel() {

  val isDynamicThemeFlow: Flow<Boolean> = storeRepository.isDynamicThemeFlow

  val themeTypeFlow: Flow<ThemeType> = storeRepository.themeTypeFlow

  fun setDynamicTheme(value: Boolean) {
    storeRepository.setDynamicTheme(value, viewModelScope)
  }

  fun setThemeType(value: ThemeType) {
    storeRepository.setThemeType(value, viewModelScope)
  }
}
