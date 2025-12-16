package org.onereed.helios.compose.shared

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.onereed.helios.R

@Composable
fun SimpleVerticalScrollbar(
  canScrollUp: Boolean,
  canScrollDown: Boolean,
  scrollbarActions: ScrollbarActions,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.wrapContentWidth().fillMaxHeight(),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    AnimatedContent(
      targetState = canScrollUp,
      transitionSpec = { fadeIn(animationSpec).togetherWith(fadeOut(animationSpec)) },
    ) { enabled ->
      ScrollButton(
        onScrollTo = scrollbarActions.onScrollToTop,
        enabled = enabled,
        icon = R.drawable.arrow_upward_24px,
        contentDescription = R.string.scroll_to_top,
      )
    }
    AnimatedContent(
      targetState = canScrollDown,
      transitionSpec = { fadeIn(animationSpec).togetherWith(fadeOut(animationSpec)) },
    ) { enabled ->
      ScrollButton(
        onScrollTo = scrollbarActions.onScrollToBottom,
        enabled = enabled,
        icon = R.drawable.arrow_downward_24px,
        contentDescription = R.string.scroll_to_bottom,
      )
    }
  }
}

@Composable
private fun ScrollButton(
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

@Immutable
data class ScrollbarActions(val onScrollToTop: () -> Unit, val onScrollToBottom: () -> Unit) {
  constructor(
    scrollState: ScrollState,
    coroutineScope: CoroutineScope,
  ) : this(
    onScrollToTop = { coroutineScope.launch { scrollState.animateScrollTo(0) } },
    onScrollToBottom = {
      coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) }
    },
  )

  constructor(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
  ) : this(
    onScrollToTop = { coroutineScope.launch { lazyListState.animateScrollToItem(0) } },
    onScrollToBottom = {
      coroutineScope.launch {
        lazyListState.animateScrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
      }
    },
  )
}

private const val SCROLL_BUTTON_ANIM_MILLIS = 500

private val animationSpec = TweenSpec<Float>(SCROLL_BUTTON_ANIM_MILLIS)
