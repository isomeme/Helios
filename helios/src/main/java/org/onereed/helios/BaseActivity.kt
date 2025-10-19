package org.onereed.helios

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.errorprone.annotations.ForOverride

/** Parent class for Helios activities. */
abstract class BaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.apply {
      enterTransition = Fade()
      exitTransition = Fade()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.actions_menu, menu)

    menu.findItem(myActionsMenuId())?.isEnabled = false

    return super.onCreateOptionsMenu(menu)
  }

  /**
   * Subclass implementations of this method provide their actions menu ID so we can disable that
   * control in the nav bar.
   */
  @ForOverride protected abstract fun myActionsMenuId(): Int

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    hapticClick()

    when (item.itemId) {
      R.id.action_schedule -> {
        go(Intent(this, MainActivity::class.java))
      }

      R.id.action_text -> {
        go(Intent(this, LiberActivity::class.java))
      }

      R.id.action_compass -> {
        go(Intent(this, CompassActivity::class.java))
      }

      R.id.action_help -> {
        openHelp()
      }

      else -> {
        return super.onOptionsItemSelected(item)
      }
    }

    return true
  }

  private fun openHelp() {
    try {
      go(HELP_INTENT)
    } catch (_: ActivityNotFoundException) {
      hapticReject()
      runOnUiThread {
        Toast.makeText(this, getString(R.string.toast_no_browser), Toast.LENGTH_LONG).show()
      }
    }
  }

  protected fun go(intent: Intent?) {
    val bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
    startActivity(intent, bundle)
  }

  private fun hapticClick() {
    hapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
  }

  private fun hapticReject() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      hapticFeedback(HapticFeedbackConstants.REJECT)
    }
  }

  private fun hapticFeedback(feedbackConstant: Int) {
    window.decorView.performHapticFeedback(feedbackConstant)
  }

  companion object {

    private val HELP_PAGE = "https://www.one-reed.org/helios".toUri()
    private val HELP_INTENT = Intent(Intent.ACTION_VIEW, HELP_PAGE)
  }
}
