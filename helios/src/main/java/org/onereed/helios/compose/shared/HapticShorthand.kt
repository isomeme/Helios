package org.onereed.helios.compose.shared

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

fun HapticFeedback.confirm() = performHapticFeedback(HapticFeedbackType.Confirm)
