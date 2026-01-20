package org.onereed.helios.common

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onereed.helios.util.TestTree
import timber.log.Timber

class LoggingExtensionsTest {

  @Test
  fun flow_logAllEvents() = runBlocking {
    val testTree = TestTree()
    Timber.plant(testTree)

    val items = flowOf(1, 2).logAllEvents("foo").toList()
    assertThat(items).containsExactly(1, 2).inOrder()

    val messages = testTree.logs.map { it.message }
    assertThat(messages)
      .containsExactly("foo start", "foo each 1", "foo each 2", "foo completion")
      .inOrder()
  }
}
