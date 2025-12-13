package org.onereed.helios.compose.shared

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import org.onereed.helios.R

@Stable
data class ScrollbarParams(
  val scrollToTopEnabled: Boolean,
  val scrollToBottomEnabled: Boolean,
  val onScrollToTop: () -> Unit,
  val onScrollToBottom: () -> Unit,
)

@Composable
fun SimpleVerticalScrollbar(scrollbarParams: ScrollbarParams, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.wrapContentWidth().fillMaxHeight(),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    AnimatedContent(
      targetState = scrollbarParams.scrollToTopEnabled,
      transitionSpec = { fadeIn(animationSpec).togetherWith(fadeOut(animationSpec)) },
    ) { enabled ->
      ScrollButton(
        scrollbarParams.onScrollToTop,
        enabled,
        R.drawable.arrow_upward_24px,
        R.string.scroll_to_top,
      )
    }
    AnimatedContent(
      targetState = scrollbarParams.scrollToBottomEnabled,
      transitionSpec = { fadeIn(animationSpec).togetherWith(fadeOut(animationSpec)) },
    ) { enabled ->
      ScrollButton(
        scrollbarParams.onScrollToBottom,
        enabled,
        R.drawable.arrow_downward_24px,
        R.string.scroll_to_bottom,
      )
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

private val animationSpec = TweenSpec<Float>(SCROLL_BUTTON_ANIM_MILLIS)
