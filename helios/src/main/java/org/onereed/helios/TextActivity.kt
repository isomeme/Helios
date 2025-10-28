package org.onereed.helios

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.HapticFeedbackConstants
import androidx.annotation.IdRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.noties.markwon.Markwon
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8
import org.onereed.helios.databinding.ActivityTextBinding
import org.onereed.helios.sun.SunEventType

/** Displays the text of Liber Resh. */
class TextActivity : BaseActivity() {

  private lateinit var binding: ActivityTextBinding

  private lateinit var markwon: Markwon

  private lateinit var invocationTemplate: String

  private lateinit var adoration: String

  @IdRes override val myActionsMenuId = R.id.action_text

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTextBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    adoration = readAssetText("adoration.md")
    invocationTemplate = readAssetText("invocation_template.md")

    markwon = Markwon.create(this)

    val typeOrdinal = intent.getIntExtra(SUN_EVENT_TYPE_ORDINAL, SunEventType.RISE.ordinal)

    binding.composeView.setContent {
      MaterialTheme {
        TextScreen(
          initialIndex = typeOrdinal,
          markwon = markwon,
          invocationTemplate = invocationTemplate,
          adoration = adoration,
        )
      }
    }
  }

  @Composable
  private fun TextScreen(
    initialIndex: Int,
    markwon: Markwon,
    invocationTemplate: String,
    adoration: String,
  ) {

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
    val menuBgColor = Color(resources.getColor(R.color.compass_dial))

    val subs = invocationMadLib.map { resources.getStringArray(it)[selectedIndex] }.toTypedArray()
    val invocation = String.format(invocationTemplate, *subs)

    val invocationRendered = markwon.toMarkdown(invocation).toAnnotatedString()
    val adorationRendered = remember { markwon.toMarkdown(adoration).toAnnotatedString() }

    LaunchedEffect(selectedIndex) { scrollState.scrollTo(0) }

    Column(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier =
          Modifier.padding(start = 15.dp, top = 10.dp, bottom = 10.dp).background(menuBgColor)
      ) {
        Text(
          text = sunEventNames[selectedIndex],
          modifier = Modifier.clickable { expanded = true },
          style = MaterialTheme.typography.titleMedium,
          color = bodyColor,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          sunEventNames.forEachIndexed { index, name ->
            DropdownMenuItem(
              text = { Text(name) },
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

        Text(
          text = invocationRendered,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.fillMaxWidth(),
          color = bodyColor,
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
          text = adorationRendered,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
          color = bodyColor,
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

    /**
     * I can't believe there's no standard method for this. My implementation is cobbled together
     * from things I found on the web.
     */
    private fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
      // Step 1: Copy over the raw text.
      append(this@toAnnotatedString.toString())

      // Step 2: Go through each span and apply corresponding styles.
      getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        when (span) {
          is StyleSpan -> {
            when (span.style) {
              Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
              Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
            // Handle BOLD_ITALIC if needed
            }
          }
          is UnderlineSpan ->
            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
          is StrikethroughSpan ->
            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
          is ForegroundColorSpan ->
            addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
          is BackgroundColorSpan ->
            addStyle(SpanStyle(background = Color(span.backgroundColor)), start, end)
          // Add more span types as needed, such as URLSpan for clickable links
          is URLSpan -> addStringAnnotation("URL", span.url, start, end)
        }
      }
    }
  }
}
