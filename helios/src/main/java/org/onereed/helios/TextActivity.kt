package org.onereed.helios

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8
import org.onereed.helios.databinding.ActivityTextBinding
import org.onereed.helios.sun.SunEventType
import org.onereed.helios.ui.theme.HeliosTheme

/** Displays the text of Liber Resh. */
class TextActivity : BaseActivity() {

  private lateinit var binding: ActivityTextBinding

  @IdRes override val myActionsMenuId = R.id.action_text

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTextBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val typeOrdinal = intent.getIntExtra(SUN_EVENT_TYPE_ORDINAL, SunEventType.RISE.ordinal)
    val rubricTemplate = readRubricTemplate()

    binding.composeView.setContent {
      MaterialTheme { TextScreen(initialIndex = typeOrdinal, rubricTemplate = rubricTemplate) }
    }
  }

  companion object {

    /** Intent extra name for the ordinal index of a [SunEventType] value. */
    const val SUN_EVENT_TYPE_ORDINAL = "org.onereed.helios.SunEventTypeOrdinal"

    private const val RUBRIC_ASSET = "rubric_template.md"

    private fun Context.readRubricTemplate(): String {
      try {
        return assets.open(RUBRIC_ASSET).bufferedReader(UTF_8).use { it.readText() }
      } catch (e: IOException) {
        throw UncheckedIOException(e)
      }
    }
  }
}

@Composable
fun TextScreen(
  initialIndex: Int,
  rubricTemplate: String,
  padding: PaddingValues = PaddingValues(),
) {
  var selectedIndex by remember { mutableIntStateOf(initialIndex) }
  var expanded by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val sunRes = SunResources.from(LocalContext.current)
  val title = sunRes.eventNames[selectedIndex]
  val headingColor = sunRes.fgColors[selectedIndex]
  val headingIcon = sunRes.icons[selectedIndex]

  val rubricMadLib =
    listOf(
      R.array.rubric_gods,
      R.array.rubric_gerunds,
      R.array.rubric_nouns,
      R.array.rubric_events,
      R.array.rubric_abodes,
    )

  val subs = rubricMadLib.map { stringArrayResource(it)[selectedIndex] }.toTypedArray()
  val rubric = rubricTemplate.format(*subs)

  val haptics = LocalHapticFeedback.current

  LaunchedEffect(selectedIndex) { scrollState.scrollTo(0) }

  Column(modifier = Modifier.fillMaxSize().padding(padding)) {
    Box(modifier = Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp)) {
      OutlinedButton(
        onClick = { expanded = true },
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
        border = BorderStroke(width = 1.dp, color = Color.DarkGray),
      ) {
        Text(stringResource(R.string.button_select))
      }

      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier =
          Modifier.wrapContentWidth()
            .background(colorResource(R.color.screen_bg))
            .border(width = 1.dp, color = Color.DarkGray),
        offset = DpOffset(0.dp, 15.dp),
      ) {
        sunRes.eventNames.forEachIndexed { index, name ->
          val isCurrent = selectedIndex == index

          DropdownMenuItem(
            leadingIcon = {
              Image(
                painter = painterResource(id = sunRes.icons[index]),
                contentDescription = stringResource(R.string.sun_event_icon_description),
                colorFilter =
                  ColorFilter.tint(if (isCurrent) Color.DarkGray else sunRes.fgColors[index]),
              )
            },
            text = { Text(text = name, style = MaterialTheme.typography.labelLarge) },
            enabled = !isCurrent,
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.Confirm)
              selectedIndex = index
              expanded = false
            },
            colors =
              MenuDefaults.itemColors(
                textColor = sunRes.fgColors[index],
                disabledTextColor = Color.DarkGray,
              ),
          )
        }
      }
    }

    Column(
      modifier = Modifier.verticalScroll(scrollState).padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = headingIcon),
        contentDescription = stringResource(R.string.sun_event_icon_description),
        colorFilter = ColorFilter.tint(headingColor),
      )

      Spacer(modifier = Modifier.height(10.dp))

      Text(text = title, style = MaterialTheme.typography.headlineMedium, color = headingColor)

      Spacer(modifier = Modifier.height(25.dp))

      MarkdownText(
        markdown = rubric,
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
      )
    }
  }
}

@Preview
@Composable
fun TextScreenPreview() {
  HeliosTheme { TextScreen(initialIndex = 0, rubricTemplate = "I'm a *rubric*.") }
}
