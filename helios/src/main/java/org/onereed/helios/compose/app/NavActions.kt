package org.onereed.helios.compose.app

import androidx.compose.runtime.Immutable

@Immutable
data class NavActions(val navigateTo: (Screen) -> Unit) {
  constructor(heliosAppState: HeliosAppState) : this(navigateTo = heliosAppState::navigateTo)
}
