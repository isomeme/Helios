package org.onereed.helios

import android.app.ActivityOptions.makeSceneTransitionAnimation
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import org.onereed.helios.datasource.LocationPermissionManager
import org.onereed.helios.util.LifecycleLogger

/** Parent class for Helios activities. */
abstract class BaseActivity : AppCompatActivity() {

  private lateinit var navMap: Map<Int, Intent>

  private lateinit var locationPermissionManager: LocationPermissionManager

  @get:IdRes protected abstract val myActionsMenuId: Int

  init {
    lifecycle.addObserver(LifecycleLogger())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    navMap = mapOf(
      R.id.action_schedule to Intent(this, ScheduleActivity::class.java),
      R.id.action_text to Intent(this, TextActivity::class.java),
      R.id.action_compass to Intent(this, CompassActivity::class.java),
      R.id.action_help to HELP_INTENT,
    )

    locationPermissionManager = LocationPermissionManager(this)

    window.apply {
      enterTransition = Fade()
      exitTransition = Fade()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.actions_menu, menu)
    menu.findItem(myActionsMenuId)?.isEnabled = false
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    window.decorView.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)

    val intent = navMap.getValue(item.itemId)
    val bundle = makeSceneTransitionAnimation(this).toBundle()

    try {
      startActivity(intent, bundle)
    } catch (_: ActivityNotFoundException) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.REJECT)
      }
      runOnUiThread {
        Toast.makeText(this, getString(R.string.toast_activity_not_found), Toast.LENGTH_LONG).show()
      }
    }

    return true
  }

  companion object {

    private const val HELP_PAGE_URL = "https://www.one-reed.org/helios"
    private val HELP_INTENT = Intent(Intent.ACTION_VIEW, HELP_PAGE_URL.toUri())
  }
}
