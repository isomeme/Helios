package org.onereed.helios.common

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

fun <T> Flow<T>.logAllEvents(label: String = "flow"): Flow<T> =
  logKeyEvents(label).onEach { Timber.d("$label each $it") }

fun <T> Flow<T>.logKeyEvents(label: String = "flow"): Flow<T> =
  onStart { Timber.d("$label start") }.onCompletion { Timber.d("$label completion") }

fun <T> Task<T>.logOutcomes(label: String = "task"): Task<T> =
  addOnSuccessListener { Timber.d("$label success") }
    .addOnFailureListener { e -> Timber.e(e, "$label failure") }
