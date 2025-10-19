package org.onereed.helios.concurrent

import android.os.Process
import java.util.Locale
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong

/**
 * A [ThreadFactory] which returns [Thread] instances set to run at background priority.
 */
internal class BackgroundThreadFactory(private val nameBase: String) : ThreadFactory {

    private val threadNumberSource = AtomicLong()

    override fun newThread(runnable: Runnable): Thread {
        val wrapperRunnable =
            Runnable {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                runnable.run()
            }
        val name =
            String.format(Locale.ENGLISH, "%s-%d", nameBase, threadNumberSource.incrementAndGet())
        return Thread(wrapperRunnable, name)
    }
}
