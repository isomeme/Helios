@file:Suppress("unused") // Used by Hilt

package org.onereed.helios.datasource

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SunResourcesModule {

  @Provides
  @Singleton
  fun provideSunResources(@ApplicationContext context: Context): SunResources =
    SunResources.create(context)
}
