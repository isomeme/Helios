@file:Suppress("unused") // Used by Hilt

package org.onereed.helios.datasource

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocatorModule {

  @Singleton @Binds abstract fun bindLocator(locatorImpl: LocatorImpl): Locator
}
