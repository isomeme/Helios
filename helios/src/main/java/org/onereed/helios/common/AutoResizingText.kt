package org.onereed.helios.common

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/** A text element which scales down its contents until they can be fully displayed on one line. */
@Suppress("unused")
@Composable
fun AutoResizingText(
  modifier: Modifier = Modifier,
  text: String,
  style: TextStyle = LocalTextStyle.current,
  fontWeight: FontWeight? = null,
) {
  var scaledTextStyle by remember { mutableStateOf(style) }
  var readyToDraw by remember { mutableStateOf(false) }

  Text(
    text,
    modifier =
      modifier.drawWithContent {
        if (readyToDraw) {
          drawContent()
        }
      },
    style = scaledTextStyle,
    fontWeight = fontWeight,
    softWrap = false,
    onTextLayout = { textLayoutResult ->
      if (textLayoutResult.didOverflowWidth) {
        scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.95)
      } else {
        readyToDraw = true
      }
    },
  )
}
