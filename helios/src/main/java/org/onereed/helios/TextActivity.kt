package org.onereed.helios

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.onereed.helios.compose.TextScreen
import org.onereed.helios.compose.TextStateHolder
import org.onereed.helios.databinding.ActivityTextBinding
import org.onereed.helios.sun.SunEventType
import org.onereed.helios.ui.theme.HeliosTheme
import javax.inject.Inject

/** Displays the text of Liber Resh. */
@AndroidEntryPoint
class TextActivity : BaseActivity() {

  private lateinit var binding: ActivityTextBinding

  @Inject lateinit var textStateHolder: TextStateHolder

  @IdRes override val myActionsMenuId = R.id.action_text

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTextBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    val typeOrdinal = intent.getIntExtra(SUN_EVENT_TYPE_ORDINAL, SunEventType.RISE.ordinal)
    textStateHolder.selectIndex(typeOrdinal)

    binding.composeView.setContent {
      HeliosTheme { TextScreen(padding = PaddingValues(top = 15.dp)) }
    }
  }

  companion object {

    /** Intent extra name for the ordinal index of a [SunEventType] value. */
    const val SUN_EVENT_TYPE_ORDINAL = "org.onereed.helios.SunEventTypeOrdinal"
  }
}
