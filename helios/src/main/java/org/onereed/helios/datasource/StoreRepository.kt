package org.onereed.helios.datasource

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.onereed.helios.ui.theme.ThemeType

@Singleton
class StoreRepository @Inject constructor(@ApplicationContext val context: Context) {

  private val Context.dataStore by preferencesDataStore("app_preferences")

  val isDynamicThemeFlow: Flow<Boolean> =
    context.dataStore.data
      .map { preferences -> preferences[isDynamicThemeKey] ?: false }
      .distinctUntilChanged()

  val themeTypeFlow: Flow<ThemeType> =
    context.dataStore.data
      .map { preferences -> ThemeType.entries[preferences[themeTypeKey] ?: 0] }
      .distinctUntilChanged()

  val isCompassLockedFlow: Flow<Boolean> =
    context.dataStore.data
      .map { preferences -> preferences[isCompassLockedKey] ?: false }
      .distinctUntilChanged()

  val isCompassSouthTopFlow: Flow<Boolean> =
    context.dataStore.data
      .map { preferences -> preferences[isCompassSouthTopKey] ?: false }
      .distinctUntilChanged()

  fun setDynamicTheme(value: Boolean, scope: CoroutineScope) {
    scope.launch {
      context.dataStore.edit { preferences -> preferences[isDynamicThemeKey] = value }
    }
  }

  fun setThemeType(value: ThemeType, scope: CoroutineScope) {
    scope.launch {
      context.dataStore.edit { preferences -> preferences[themeTypeKey] = value.ordinal }
    }
  }

  fun setCompassLocked(value: Boolean, scope: CoroutineScope) {
    scope.launch {
      context.dataStore.edit { preferences -> preferences[isCompassLockedKey] = value }
    }
  }

  fun setCompassSouthTop(value: Boolean, scope: CoroutineScope) {
    scope.launch {
      context.dataStore.edit { preferences -> preferences[isCompassSouthTopKey] = value }
    }
  }

  private companion object {

    val isDynamicThemeKey = booleanPreferencesKey("is_dynamic_theme")
    val themeTypeKey = intPreferencesKey("theme_type")

    val isCompassLockedKey = booleanPreferencesKey("is_compass_locked")
    val isCompassSouthTopKey = booleanPreferencesKey("is_compass_south_top")
  }
}
