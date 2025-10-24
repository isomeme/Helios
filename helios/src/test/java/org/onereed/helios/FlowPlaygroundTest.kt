package org.onereed.helios

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import timber.log.Timber

class FlowPlaygroundTest {

  @get:Rule val timberRule = TimberRule()

  @Test
  fun playground() = runBlocking {
    createDataSource()
    delay(3.seconds)

    val jobA = startCollector("A")
    delay(3.seconds)
    val jobB = startCollector("B")
    delay(3.seconds)
    jobA.cancel()
    delay(3.seconds)
    jobB.cancel()

    delay(3.seconds)
    Timber.d("Program finished")
  }

  companion object {

    private val _dataFlow = MutableSharedFlow<Int>(replay = 1)

    val dataFlow =
      _dataFlow
        .asSharedFlow()
        .onSubscription { Timber.d("dataFlow onSubscription") }
        .onStart {
          Timber.d("dataFlow onStart")
          isSourceActive = true
        }
        .onCompletion { e ->
          Timber.d("dataFlow onCompletion: ${e?.javaClass?.simpleName}")
          isSourceActive = false
        }

    var isSourceActive = false

    fun createDataSource() {
      Timber.d("startDataSource")

      _dataFlow.subscriptionCount
        .onEach { count -> Timber.d("Subscription count: $count") }
        .launchIn(CoroutineScope(Dispatchers.Default))

      CoroutineScope(Dispatchers.Default).launch {
        repeat(Int.MAX_VALUE) { i ->
          if (isSourceActive) {
            Timber.d("Emit $i >>>")
            _dataFlow.emit(i)
          } else {
            Timber.d("Skip $i")
          }

          delay(1.seconds)
        }
      }
    }

    private fun startCollector(label: String): Job {
      Timber.d("startCollector $label")

      val job =
        dataFlow
          .onEach { value -> Timber.d("$label: $value") }
          .launchIn(CoroutineScope(Dispatchers.Default))

      job.invokeOnCompletion { e ->
        Timber.d("Collector $label finished: ${e?.javaClass?.simpleName}")
      }

      return job
    }
  }
}
