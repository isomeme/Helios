package org.onereed.helios.sun

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import java.time.Instant
import org.onereed.helios.common.Place
import org.onereed.helios.concurrent.BackgroundThreadPoolExecutor
import org.onereed.helios.sun.SunInfo.Companion.compute

/** Provides a static method for asynchronous [SunInfo] requests. */
object SunInfoSource {

  private val sunInfoExecutor = BackgroundThreadPoolExecutor("sunInfo")

  fun request(place: Place, instant: Instant): Task<SunInfo> {
    val taskCompletionSource = TaskCompletionSource<SunInfo>()
    sunInfoExecutor.execute { taskCompletionSource.setResult(compute(place, instant)) }
    return taskCompletionSource.getTask()
  }
}
