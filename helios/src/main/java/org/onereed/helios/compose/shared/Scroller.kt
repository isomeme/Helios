package org.onereed.helios.compose.shared

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.onereed.helios.R

@Stable
data class ScrollControl(
  val scrollToTopEnabled: Boolean,
  val scrollToBottomEnabled: Boolean,
  val onScrollToTop: () -> Unit,
  val onScrollToBottom: () -> Unit,
)

@Composable
fun Scroller(scrollControl: ScrollControl, content: @Composable () -> Unit) {
  Row(modifier = Modifier.fillMaxSize().padding(start = 30.dp, top = 20.dp, bottom = 20.dp)) {
    content()

    Column(
      modifier = Modifier.wrapContentWidth().fillMaxHeight(),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      AnimatedContent(
        targetState = scrollControl.scrollToTopEnabled,
        transitionSpec = {
          fadeIn(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))
            .togetherWith(fadeOut(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS)))
        },
      ) { enabled ->
        ScrollButton(
          scrollControl.onScrollToTop,
          enabled,
          R.drawable.arrow_upward_24px,
          R.string.scroll_to_top,
        )
      }
      AnimatedContent(
        targetState = scrollControl.scrollToBottomEnabled,
        transitionSpec = {
          fadeIn(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS))
            .togetherWith(fadeOut(animationSpec = tween(SCROLL_BUTTON_ANIM_MILLIS)))
        },
      ) { enabled ->
        ScrollButton(
          scrollControl.onScrollToBottom,
          enabled,
          R.drawable.arrow_downward_24px,
          R.string.scroll_to_bottom,
        )
      }
    }
  }
}

@Composable
fun ScrollButton(
  onScrollTo: () -> Unit,
  enabled: Boolean,
  @DrawableRes icon: Int,
  @StringRes contentDescription: Int,
) {
  IconButton(
    onClick = onScrollTo,
    enabled = enabled,
    colors =
      IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = Color.Transparent,
      ),
  ) {
    Icon(
      painter = painterResource(id = icon),
      contentDescription = stringResource(id = contentDescription),
    )
  }
}

private const val SCROLL_BUTTON_ANIM_MILLIS = 500
