package org.onereed.helios.util

import timber.log.Timber

/** See https://stackoverflow.com/a/75651678/331864 */
class TestTree : Timber.Tree() {

  val logs = mutableListOf<Log>()

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    logs.add(Log(priority, tag, message, t))
  }

  data class Log(val priority: Int, val tag: String?, val message: String, val t: Throwable?)
}
