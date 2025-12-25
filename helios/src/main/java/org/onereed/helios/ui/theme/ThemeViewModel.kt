package org.onereed.helios.ui.theme

import dagger.hilt.android.lifecycle.HiltViewModel
import org.onereed.helios.common.BaseViewModel
import org.onereed.helios.datasource.StoreRepository
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(storeRepository: StoreRepository) : BaseViewModel() {

  val isDynamicThemeFlow = storeRepository.isDynamicThemeFlow.stateIn(false)

  val themeTypeFlow = storeRepository.themeTypeFlow.stateIn(ThemeType.SYSTEM)
}
