package org.onereed.helios.compose

import kotlinx.serialization.Serializable

sealed class Screen {

  @Serializable
  object Schedule : Screen()

  @Serializable
  object Text : Screen()

  @Serializable
  object Compass : Screen()

  @Serializable
  object Help : Screen()
}
