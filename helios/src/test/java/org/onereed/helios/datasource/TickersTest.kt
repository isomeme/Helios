package org.onereed.helios.datasource

import com.google.common.truth.Truth.assertThat
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.onereed.helios.util.TimberConsoleRule
import timber.log.Timber

class TickersTest {

  @get:Rule val timberConsoleRule = TimberConsoleRule()

  @Test
  fun countingTickerFlow() = runBlocking {
    val ticker = countingTickerFlow(interval = 1.seconds)

    val values1 = ticker.onEach { Timber.d("Received $it") }.take(3).toList()
    assertThat(values1).containsExactly(0, 1, 2).inOrder()

    // Flows are independent.

    val values2 = ticker.onEach { Timber.d("Received $it") }.take(3).toList()
    assertThat(values2).containsExactly(0, 1, 2).inOrder()
  }

  @Test
  fun repeatingTickerFlow(): Unit = runBlocking {
    val ticker = repeatingTickerFlow(interval = 1.seconds, value = "foo")
    val values = ticker.onEach { Timber.d("Received $it") }.take(3).toList()
    assertThat(values).containsExactly("foo", "foo", "foo")
  }
}
