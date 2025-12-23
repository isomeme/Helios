package org.onereed.helios.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Retention(AnnotationRetention.RUNTIME) @Qualifier annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopesModule {

  @Singleton
  @ApplicationScope
  @Provides
  fun providesCoroutineScope(): CoroutineScope {
    // SupervisorJob is crucial here. It means if one coroutine in this
    // scope fails, it won't cancel the entire scope.
    return CoroutineScope(SupervisorJob() + Dispatchers.Default)
  }
}
