package org.onereed.helios

import android.os.Bundle
import androidx.annotation.IdRes
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
    val sunResources = SunResources.from(this)

    binding.composeView.setContent {
      HeliosTheme { TextDisplay(initialIndex = typeOrdinal, sunResources = sunResources) }
    }
  }

  companion object {

    /** Intent extra name for the ordinal index of a [SunEventType] value. */
    const val SUN_EVENT_TYPE_ORDINAL = "org.onereed.helios.SunEventTypeOrdinal"
  }
}
