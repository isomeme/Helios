package org.onereed.helios.util

import java.text.SimpleDateFormat
import java.util.Date
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

/**
 * A [TestRule] which configures the [Timber] logging system to write log entries to the console.
 * Each line includes a timestamp and the logged message.
 */
class TimberRule : TestRule {
  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        Timber.Forest.plant(
          object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
              val time = timeFormatter.format(Date())
              println("*** $time: $message")
              t?.printStackTrace()
            }
          }
        )

        try {
          base.evaluate()
        } finally {
          Timber.Forest.uprootAll()
        }
      }
    }
  }

  companion object {
    val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS")
  }
}
