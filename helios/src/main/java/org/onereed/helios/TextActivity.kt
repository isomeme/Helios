package org.onereed.helios

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.onereed.helios.compose.SunResources
import org.onereed.helios.compose.TextScreen
import org.onereed.helios.databinding.ActivityTextBinding
import org.onereed.helios.sun.SunEventType
import org.onereed.helios.ui.theme.HeliosTheme
import javax.inject.Inject

/** Displays the text of Liber Resh. */
@AndroidEntryPoint
class TextActivity : BaseActivity() {

  private lateinit var binding: ActivityTextBinding

  @Inject lateinit var sunResources: SunResources

  @IdRes override val myActionsMenuId = R.id.action_text

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTextBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val typeOrdinal = intent.getIntExtra(SUN_EVENT_TYPE_ORDINAL, SunEventType.RISE.ordinal)

    binding.composeView.setContent {
      var selectedIndex by rememberSaveable { mutableIntStateOf(typeOrdinal) }

      HeliosTheme {
        TextScreen(
          selectedIndex = selectedIndex,
          onSelectedIndexChanged = { selectedIndex = it },
          sunResources = sunResources,
          padding = PaddingValues(top = 15.dp),
        )
      }
    }
  }

  companion object {

    /** Intent extra name for the ordinal index of a [SunEventType] value. */
    const val SUN_EVENT_TYPE_ORDINAL = "org.onereed.helios.SunEventTypeOrdinal"
  }
}
