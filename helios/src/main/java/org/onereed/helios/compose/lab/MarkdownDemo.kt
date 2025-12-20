package org.onereed.helios.compose.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import org.onereed.helios.ui.theme.DarkHeliosTheme

@Composable
fun MarkdownDemo() {
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Column(
        modifier =
          Modifier.padding(all = 20.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(all = 20.dp)
      ) {
        RichText(style = RichTextStyle(paragraphSpacing = TextUnit(15.0f, TextUnitType.Sp))) {
          Markdown(content = mdExample)
        }
      }
    }
  }
}

private val mdExample =
  """
  ## Flow and line breaks

  Lorem ipsum dolor sit amet,
  consectetur **adipiscing** elit.
  Vivamus id dui in eros
  venenatis viverra quis quis
  arcu. Pellentesque quis
  efficitur massa, id tristique
  arcu.

  Mauris dictum, justo
  non accumsan **congue**, nisl
  nibh gravida sapien, sed
  tincidunt mauris lorem vel
  libero.

  ---

  Lorem ipsum dolor sit amet, \
  consectetur **adipiscing** elit. \
  Vivamus id dui in eros \
  venenatis viverra quis quis \
  arcu. Pellentesque quis \
  efficitur massa, id tristique \
  arcu.

  Mauris dictum, justo \
  non accumsan **congue**, nisl \
  nibh gravida sapien, sed \
  tincidunt mauris lorem vel \
  libero.
  """
    .trimIndent()

@Preview
@Composable
fun MarkdownDemoPreview() {
  DarkHeliosTheme { MarkdownDemo() }
}
