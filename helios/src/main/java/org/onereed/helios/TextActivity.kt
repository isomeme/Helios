package org.onereed.helios

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.annotation.IdRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8
import org.onereed.helios.databinding.ActivityTextBinding
import org.onereed.helios.sun.SunEventType

/** Displays the text of Liber Resh. */
class TextActivity : BaseActivity() {

  private lateinit var binding: ActivityTextBinding

  private lateinit var invocationTemplate: String

  private lateinit var adoration: String

  @IdRes override val myActionsMenuId = R.id.action_text

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTextBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    invocationTemplate = readAssetText("invocation_template.md")
    adoration = readAssetText("adoration.md")

    val typeOrdinal = intent.getIntExtra(SUN_EVENT_TYPE_ORDINAL, SunEventType.RISE.ordinal)

    binding.composeView.setContent {
      MaterialTheme {
        TextScreen(
          initialIndex = typeOrdinal,
          invocationTemplate = invocationTemplate,
          adoration = adoration,
        )
      }
    }
  }

  @Composable
  private fun TextScreen(initialIndex: Int, invocationTemplate: String, adoration: String) {

    var selectedIndex by remember { mutableIntStateOf(initialIndex) }
    val scrollState = rememberScrollState()
    val sunEventNames = stringArrayResource(R.array.sun_event_names)
    var expanded by remember { mutableStateOf(false) }
    val view = LocalView.current

    val resources = view.context.resources
    val iconId =
      resources.obtainTypedArray(R.array.sun_event_icons).use { it.getResourceId(selectedIndex, 0) }
    val title = sunEventNames[selectedIndex]
    val headingColor = Color(resources.getIntArray(R.array.sun_event_fg_colors)[selectedIndex])
    val bodyColor = Color(resources.getColor(R.color.activities_menu_icon_default))

    val subs = invocationMadLib.map { resources.getStringArray(it)[selectedIndex] }.toTypedArray()
    val invocation = String.format(invocationTemplate, *subs)

    LaunchedEffect(selectedIndex) { scrollState.scrollTo(0) }

    Column(modifier = Modifier.fillMaxSize()) {
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
          modifier = Modifier.background(Color.DarkGray).wrapContentWidth(),
          offset = DpOffset(0.dp, 15.dp),
        ) {
          sunEventNames.forEachIndexed { index, name ->
            DropdownMenuItem(
              text = { Text(name) },
              colors = MenuDefaults.itemColors(textColor = Color.White),
              onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                selectedIndex = index
                expanded = false
              },
            )
          }
        }
      }

      Column(
        modifier = Modifier.verticalScroll(scrollState).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Image(
          painter = painterResource(id = iconId),
          contentDescription = stringResource(R.string.sun_event_icon_description),
          colorFilter = ColorFilter.tint(headingColor),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = headingColor)

        Spacer(modifier = Modifier.height(25.dp))

        MarkdownText(
          markdown = invocation,
          style = MaterialTheme.typography.bodyMedium.copy(color = bodyColor),
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(15.dp))

        MarkdownText(
          markdown = adoration,
          style = MaterialTheme.typography.bodyMedium.copy(color = bodyColor),
          modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        )
      }
    }
  }

  private fun readAssetText(assetName: String): String {
    try {
      return assets.open(assetName).use { it.readBytes().toString(UTF_8) }
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }
  }

  companion object {

    /** Intent extra name for the ordinal index of a [SunEventType] value. */
    const val SUN_EVENT_TYPE_ORDINAL = "org.onereed.helios.SunEventTypeOrdinal"

    private val invocationMadLib =
      listOf(
        R.array.invocation_gods,
        R.array.invocation_gerunds,
        R.array.invocation_nouns,
        R.array.invocation_events,
        R.array.invocation_abodes,
      )
  }
}
