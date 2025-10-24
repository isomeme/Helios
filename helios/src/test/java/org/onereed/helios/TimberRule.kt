package org.onereed.helios

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date

class TimberRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                Timber.plant(object : Timber.DebugTree() {
                    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                        val time = timeFormatter.format(Date())
                        println("*** $time: $message")
                    }
                })
                try {
                    base.evaluate()
                } finally {
                    Timber.uprootAll()
                }
            }
        }
    }

    companion object {
      val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS")
    }
}
