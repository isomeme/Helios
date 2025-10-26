package org.onereed.helios

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.EnumSet
import java.util.concurrent.Executor
import kotlinx.coroutines.launch
import org.onereed.helios.common.DirectionUtil.ang
import org.onereed.helios.common.DirectionUtil.arc
import org.onereed.helios.databinding.ActivityCompassBinding
import org.onereed.helios.datasource.PlaceTimeDataSource
import org.onereed.helios.sun.SunCompass
import org.onereed.helios.sun.SunEventType
import timber.log.Timber

/** Displays a compass view of the directions of sun events. */
class CompassActivity : BaseActivity(), DeviceOrientationListener {

  private lateinit var binding: ActivityCompassBinding

  private lateinit var orientationProvider: FusedOrientationProviderClient
  private lateinit var mainExecutor: Executor

  private lateinit var sunEventViews: Map<SunEventType, ImageView>
  private lateinit var compassRadiusViews: List<ImageView>

  private lateinit var noonWrapper: NoonNadirWrapper
  private lateinit var nadirWrapper: NoonNadirWrapper

  private enum class CompassDisplayState {
    LOCKED,
    UNLOCK_PENDING,
    UNLOCKING,
    UNLOCKED,
  }

  private lateinit var compassDisplayState: CompassDisplayState

  private lateinit var placeTimeDataSource: PlaceTimeDataSource

  private val sunCompassViewModel: SunCompassViewModel by viewModels()

  private var lastRotation = 0.0
  private var compassRadiusPx = 0
  private var radiusPxRange = 0

  @IdRes override val myActionsMenuId = R.id.action_compass

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityCompassBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)

    sunEventViews =
      mapOf(
        SunEventType.RISE to binding.rise,
        SunEventType.NOON to binding.noon,
        SunEventType.SET to binding.set,
        SunEventType.NADIR to binding.nadir,
      )

    // Note that noon and nadir can be inset, and sunMovement is at a fraction of the compass
    // radius, so they're handled differently.

    compassRadiusViews = listOf(binding.sun, binding.rise, binding.set)

    noonWrapper = NoonNadirWrapper(binding.noon)
    nadirWrapper = NoonNadirWrapper(binding.nadir)

    placeTimeDataSource = PlaceTimeDataSource(this)

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        placeTimeDataSource.placeTimeFlow.collect { sunCompassViewModel.acceptPlaceTime(it) }
      }
    }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
        sunCompassViewModel.sunCompassFlow.collect { acceptSunCompass(it) }
      }
    }

    orientationProvider = LocationServices.getFusedOrientationProviderClient(this)
    mainExecutor = ContextCompat.getMainExecutor(this)
  }

  override fun onResume() {
    super.onResume()

    applyPreferences()
    applyCompassControls()

    // Delay compass radius calculations until layout is complete.
    binding.compassComposite.post { applyCompassRadius() }
  }

  override fun onPause() {
    super.onPause()

    ignoreOrientation()

    getPreferences(MODE_PRIVATE).edit {
      putBoolean(PREF_LOCK_COMPASS, binding.lockCompass.isChecked)
      putBoolean(PREF_SOUTH_AT_TOP, binding.southAtTop.isChecked)
    }
  }

  fun acceptSunCompass(sunCompass: SunCompass) {
    Timber.d("sunCompass=$sunCompass")

    with(sunCompass) {
      updateCircleAngle(binding.sun, sunAzimuth)
      updateCircleAngle(binding.sunMovement, sunAzimuth)

      val sunMovementRotation = if (isSunClockwise) sunAzimuth else sunAzimuth + 180.0f

      binding.sunMovement.rotation = sunMovementRotation.toFloat()

      events.forEach { type, event ->
        val view = sunEventViews.getValue(type)
        updateCircleAngle(view, event.azimuth)
        view.setVisibility(View.VISIBLE)
      }

      val shownTypes = EnumSet.copyOf(events.keys)
      val missingTypes = EnumSet.complementOf(shownTypes)

      missingTypes.forEach { type -> sunEventViews.getValue(type).setVisibility(View.INVISIBLE) }

      // In the tropics, noon and nadir can be at the same azimuth, so inset and dim the icon for
      // the one that comes later. We also explicitly reset the non-offset event(s) to correct
      // previous later-event status.

      val noonEvent = events.getValue(SunEventType.NOON)
      val nadirEvent = events.getValue(SunEventType.NADIR)

      val isOverlap = ang(noonEvent.azimuth, nadirEvent.azimuth) < 20.0
      val isNoonEarlier = noonEvent < nadirEvent
      val isNoonInset = isOverlap && !isNoonEarlier
      val isNadirInset = isOverlap && isNoonEarlier

      noonWrapper.selectInset(isNoonInset)
      nadirWrapper.selectInset(isNadirInset)
    }
  }

  override fun onDeviceOrientationChanged(deviceOrientation: DeviceOrientation) {
    val compassAzimuth = deviceOrientation.headingDegrees.toDouble()

    when (compassDisplayState) {
      CompassDisplayState.LOCKED,
      CompassDisplayState.UNLOCKING -> {}

      CompassDisplayState.UNLOCK_PENDING -> {
        // We use a single orientation update to animate rotating the compass to the reported
        // heading. We block further orientation updates until the animation is complete.

        compassDisplayState = CompassDisplayState.UNLOCKING

        rotateCompass(
          compassAzimuth,
          object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
              compassDisplayState = CompassDisplayState.UNLOCKED
            }

            override fun onAnimationEnd(animation: Animator) {
              compassDisplayState = CompassDisplayState.UNLOCKED
            }
          },
        )
      }

      CompassDisplayState.UNLOCKED -> rotateCompass(compassAzimuth)
    }
  }

  @Keep
  fun onLockCompassClicked(view: View) {
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    applyCompassControls()
  }

  @Keep
  fun onSouthAtTopClicked(view: View) {
    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    rotateCompassToLockedPosition()
  }

  private fun applyPreferences() {
    val prefs = getPreferences(MODE_PRIVATE)
    val isLocked = prefs.getBoolean(PREF_LOCK_COMPASS, false)
    val southAtTop = prefs.getBoolean(PREF_SOUTH_AT_TOP, false)

    compassDisplayState = if (isLocked) CompassDisplayState.LOCKED else CompassDisplayState.UNLOCKED

    binding.lockCompass.isChecked = isLocked
    binding.lockCompass.isEnabled = true

    binding.southAtTop.isChecked = southAtTop
    binding.southAtTop.isEnabled = isLocked
  }

  private fun applyCompassControls() {
    if (binding.lockCompass.isChecked) {
      compassDisplayState = CompassDisplayState.LOCKED
      ignoreOrientation()
      binding.southAtTop.isEnabled = true
      rotateCompassToLockedPosition()
    } else {
      trackOrientation()
    }
  }

  private fun applyCompassRadius() {
    val widthPx = binding.compassFace.width

    val pxPerDp = widthPx.toDouble() / COMPASS_SIDE_DP
    compassRadiusPx = (pxPerDp * COMPASS_RADIUS_DP).toInt()
    val insetRadiusPx = (compassRadiusPx * RADIUS_INSET_SCALE).toInt()
    radiusPxRange = compassRadiusPx - insetRadiusPx

    compassRadiusViews.forEach { updateCircleRadius(it, compassRadiusPx) }

    val sunMovementRadiusPx = (compassRadiusPx * RADIUS_SUN_MOVEMENT_SCALE).toInt()
    updateCircleRadius(binding.sunMovement, sunMovementRadiusPx)

    noonWrapper.redraw()
    nadirWrapper.redraw()
  }

  private fun trackOrientation() {
    orientationProvider
      .requestOrientationUpdates(DEVICE_ORIENTATION_REQUEST, mainExecutor, this)
      .addOnSuccessListener { _ ->
        compassDisplayState = CompassDisplayState.UNLOCK_PENDING
        binding.southAtTop.isEnabled = false
      }
      .addOnFailureListener { e ->
        Timber.e(e, "Failed to request orientation updates.")
        binding.lockCompass.isChecked = true
        binding.lockCompass.isEnabled = false
        applyCompassControls()
      }
  }

  private fun ignoreOrientation() {
    orientationProvider.removeOrientationUpdates(this).addOnFailureListener {
      Timber.e(it, "Failed to remove orientation updates.")
    }
  }

  private fun rotateCompassToLockedPosition() {
    rotateCompass(if (binding.southAtTop.isChecked) 180.0 else 0.0)
  }

  /**
   * Executes an animated sweep from the old compass rotation to the new one, and updates the old
   * one to the new value to prepare for the next update.
   */
  private fun rotateCompass(
    compassAzimuth: Double,
    animatorListener: Animator.AnimatorListener? = null,
  ) {
    // When animating from -179 to +179, we don't want to go the long way around the circle.

    val delta = arc(lastRotation, -compassAzimuth)
    val desiredRotation = lastRotation + delta

    with(
      ObjectAnimator.ofFloat(
        binding.compassRotating,
        "rotation",
        lastRotation.toFloat(),
        desiredRotation.toFloat(),
      )
    ) {
      setDuration(ROTATION_ANIMATION_DURATION_MILLIS)
      animatorListener?.let { addListener(it) }
      setAutoCancel(true)
      start()
    }

    lastRotation = desiredRotation
  }

  /**
   * Wrapper for noon and nadir [ImageView] objects which provides a single object and property for
   * ObjectAnimator to use in doing overlap inset swaps.
   */
  private inner class NoonNadirWrapper(private val view: ImageView) {

    private var insetPct = 0

    fun selectInset(isInset: Boolean) {
      val targetPct = if (isInset) 100 else 0

      if (targetPct != insetPct) {
        val insetAnimator =
          ObjectAnimator.ofInt(this, "insetPct", targetPct)
            .setDuration(INSET_ANIMATION_DURATION_MILLIS)
        insetAnimator.setAutoCancel(true)
        insetAnimator.start()
      }
    }

    @Keep
    fun setInsetPct(pct: Int) {
      insetPct = pct
      redraw()
    }

    fun redraw() {
      val insetFraction = insetPct / 100.0f
      val alpha = OPAQUE_ALPHA - insetFraction * ALPHA_RANGE
      val radiusPx = (compassRadiusPx - insetFraction * radiusPxRange).toInt()

      view.setAlpha(alpha)
      updateCircleRadius(view, radiusPx)
    }
  }

  companion object {

    /**
     * How long the compass rotation would last, in theory. In practice it will be interrupted by a
     * new azimuth reading before then, but this serves to damp the motion of the compass, making it
     * visually smoother.
     */
    private const val ROTATION_ANIMATION_DURATION_MILLIS = 200L

    /** How long it takes to swap the noon and nadir inset positions. */
    private const val INSET_ANIMATION_DURATION_MILLIS = 1000L

    private const val COMPASS_SIDE_DP = 100
    private const val COMPASS_RADIUS_DP = 44

    private const val RADIUS_INSET_SCALE = 0.82
    private const val RADIUS_SUN_MOVEMENT_SCALE = 0.6

    private const val OPAQUE_ALPHA = 1.0f
    private const val INSET_ALPHA = 0.5f
    private const val ALPHA_RANGE = OPAQUE_ALPHA - INSET_ALPHA

    /** Shared preference key for compass lock status. */
    private const val PREF_LOCK_COMPASS = "lockCompass"

    /** Shared preference key for (locked) compass south-at-top status. */
    private const val PREF_SOUTH_AT_TOP = "southAtTop"

    private val DEVICE_ORIENTATION_REQUEST =
      DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()

    /** Sets the radius of a view that is using circle-constrained layout. */
    fun updateCircleRadius(view: View, radius: Int) {
      val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
      layoutParams.circleRadius = radius
      view.layoutParams = layoutParams
    }

    /** Sets the angle of a view that is using circle-constrained layout. */
    fun updateCircleAngle(view: View, angle: Double) {
      val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
      layoutParams.circleAngle = angle.toFloat()
      view.layoutParams = layoutParams
    }
  }
}
