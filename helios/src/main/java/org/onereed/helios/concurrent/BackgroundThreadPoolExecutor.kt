package org.onereed.helios.concurrent

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** A [ThreadPoolExecutor] which runs tasks at Android `BACKGROUND` priority.  */
class BackgroundThreadPoolExecutor(name: String) :
    ThreadPoolExecutor(
        POOL_SIZE,
        POOL_SIZE,
        KEEP_ALIVE_SEC,
        TimeUnit.SECONDS,
        LinkedBlockingQueue(),
        BackgroundThreadFactory(name)
    ) {

    companion object {
        private const val POOL_SIZE = 4
        private const val KEEP_ALIVE_SEC = 60L
    }
}
