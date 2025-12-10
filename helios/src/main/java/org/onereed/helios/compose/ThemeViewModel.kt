package org.onereed.helios.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class ThemeViewModel @Inject constructor(storeRepository: StoreRepository) : ViewModel() {

  val isDynamicThemeFlow: Flow<Boolean> = storeRepository.isDynamicThemeFlow

  val themeTypeFlow: Flow<ThemeType> = storeRepository.themeTypeFlow
}
