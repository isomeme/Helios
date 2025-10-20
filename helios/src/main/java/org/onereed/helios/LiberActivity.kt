package org.onereed.helios

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import io.noties.markwon.Markwon
import org.onereed.helios.databinding.ActivityLiberBinding
import org.onereed.helios.sun.SunEvent
import timber.log.Timber
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets.UTF_8

/** Activity for displaying the text of Liber Resh. */
class LiberActivity : BaseActivity(), AdapterView.OnItemSelectedListener {

  private lateinit var binding: ActivityLiberBinding

  private lateinit var markwon: Markwon

  private lateinit var invocationTemplate: String

  override fun onCreate(savedInstanceState: Bundle?) {
    Timber.d("onCreate start")
    super.onCreate(savedInstanceState)

    binding = ActivityLiberBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val adoration = readAssetText("adoration.md")
    invocationTemplate = readAssetText("invocation_template.md")

    markwon = Markwon.create(this)
    markwon.setMarkdown(binding.adorationDisplay, adoration)

    val spinnerAdapter =
      ArrayAdapter.createFromResource(
        this,
        R.array.sun_event_names,
        android.R.layout.simple_spinner_item,
      )

    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    binding.sunEventSelector.setAdapter(spinnerAdapter)

    val typeOrdinal = intent.getIntExtra(IntentExtraTags.SUN_EVENT_TYPE, SunEvent.Type.RISE.ordinal)

    // The animate=false argument tells the item selected listener not to run based on this
    // initial selection. This avoids a double haptic click, one from the activity transition and
    // one from onItemSelected. Instead, we call displayInvocation directly for the initial load.

    binding.sunEventSelector.setSelection(typeOrdinal, /* animate= */ false)
    binding.sunEventSelector.onItemSelectedListener = this
    displayInvocation(typeOrdinal)

    Timber.d("onCreate end")
  }

  override fun myActionsMenuId(): Int {
    return R.id.action_text
  }

  override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
    Timber.d("onItemSelected: position=%d", position)
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    displayInvocation(position)
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    Timber.d("onNothingSelected")
  }

  private fun displayInvocation(ix: Int) {
    Timber.d("displayInvocation: ix=%d", ix)

    resources.obtainTypedArray(R.array.sun_event_icons).use { icons ->
      val icon = icons.getDrawable(ix)
      binding.iconDisplay.setImageDrawable(icon)
    }

    val title = resources.getStringArray(R.array.sun_event_names)[ix]
    binding.titleDisplay.text = title

    val color = resources.getIntArray(R.array.sun_event_fg_colors)[ix]
    binding.iconDisplay.setColorFilter(color)
    binding.titleDisplay.setTextColor(color)

    val god = resources.getStringArray(R.array.invocation_gods)[ix]
    val gerund = resources.getStringArray(R.array.invocation_gerunds)[ix]
    val noun = resources.getStringArray(R.array.invocation_nouns)[ix]
    val event = resources.getStringArray(R.array.invocation_events)[ix]
    val abode = resources.getStringArray(R.array.invocation_abodes)[ix]

    val invocation = String.format(invocationTemplate, god, gerund, noun, event, abode)
    markwon.setMarkdown(binding.invocationDisplay, invocation)

    binding.scrollView.fullScroll(View.FOCUS_UP)
  }

  @SuppressLint("NewApi") // Desugared nio readAllBytes()
  private fun readAssetText(assetName: String): String {
    try {
      return assets.open(assetName).use { String(it.readAllBytes(), UTF_8) }
    } catch (e: IOException) {
      throw UncheckedIOException(e)
    }
  }
}
