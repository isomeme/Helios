package org.onereed.helios

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.onereed.helios.common.DirectionUtil
import org.onereed.helios.common.LayoutParamsUtil
import org.onereed.helios.databinding.ActivityCompassBinding
import org.onereed.helios.sun.SunEvent
import org.onereed.helios.sun.SunInfo
import timber.log.Timber
import java.util.EnumSet
import java.util.concurrent.Executor

/** Displays directions to sun events.  */
class CompassActivity : BaseSunInfoActivity(), DeviceOrientationListener, Observer<SunInfo?> {

    private lateinit var binding: ActivityCompassBinding

    private lateinit var fusedOrientationProviderClient: FusedOrientationProviderClient
    private lateinit var mainExecutor: Executor

    private lateinit var sunEventViews: ImmutableMap<SunEvent.Type, ImageView>
    private lateinit var compassRadiusViews: ImmutableList<ImageView>
    private lateinit var noonWrapper: NoonNadirWrapper
    private lateinit var nadirWrapper: NoonNadirWrapper

    private enum class CompassDisplayState {
        LOCKED, UNLOCK_PENDING, UNLOCKING, UNLOCKED
    }

    private lateinit var compassDisplayState: CompassDisplayState

    private var lastRotationDeg = 0.0
    private var compassRadiusPx = 0
    private var radiusPxRange = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityCompassBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setSupportActionBar(binding.toolbar)

        sunEventViews = ImmutableMap.of(
            SunEvent.Type.RISE,
            binding.rise,
            SunEvent.Type.NOON,
            binding.noon,
            SunEvent.Type.SET,
            binding.set,
            SunEvent.Type.NADIR,
            binding.nadir
        )

        // Note that noon and nadir can be inset, and sunMovement is at a fraction of the compass
        // radius, so they're handled differently.
        compassRadiusViews = ImmutableList.of(binding.sun, binding.rise, binding.set)

        noonWrapper = NoonNadirWrapper(binding.noon)
        nadirWrapper = NoonNadirWrapper(binding.nadir)

        fusedOrientationProviderClient = LocationServices.getFusedOrientationProviderClient(this)
        mainExecutor = ContextCompat.getMainExecutor(this)

        observeSunInfo(this)
    }

    override fun myActionsMenuId(): Int {
        return R.id.action_compass
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()

        applyPreferences()
        applyCompassControls()

        // Delay compass radius calculations until layout is complete.
        binding.compassComposite.post { applyCompassRadius() }
    }

    override fun onPause() {
        Timber.d("onPause")
        super.onPause()

        ignoreOrientation()

        getPreferences(MODE_PRIVATE).edit {
            putBoolean(PREF_LOCK_COMPASS, binding.lockCompass.isChecked)
            putBoolean(PREF_SOUTH_AT_TOP, binding.southAtTop.isChecked)
        }
    }

    override fun onChanged(value: SunInfo?) {
        if (value == null) {
            Timber.e("SunInfo is null")
            return
        }

        val sunAzimuthInfo = value.sunAzimuthInfo
        val sunAzimuthDeg = sunAzimuthInfo.azimuthDeg

        LayoutParamsUtil.changeConstraintLayoutCircleAngle(binding.sun, sunAzimuthDeg)
        LayoutParamsUtil.changeConstraintLayoutCircleAngle(binding.sunMovement, sunAzimuthDeg)

        val sunMovementRotation =
            if (sunAzimuthInfo.isClockwise) sunAzimuthDeg else sunAzimuthDeg + 180.0f
        binding.sunMovement.rotation = sunMovementRotation.toFloat()

        val shownEvents = HashMap<SunEvent.Type, SunEvent>()

        for (sunEvent in value.getSunEvents()) {
            val type = sunEvent.type

            if (!shownEvents.containsKey(type)) {
                shownEvents.put(type, sunEvent)
                val view = sunEventViews.get(type)!!
                LayoutParamsUtil.changeConstraintLayoutCircleAngle(view, sunEvent.getAzimuthDeg())
                view.setVisibility(View.VISIBLE)
            }
        }

        val shownTypes = EnumSet.copyOf(shownEvents.keys)
        val missingTypes = EnumSet.complementOf(shownTypes)

        missingTypes.forEach { type ->
            sunEventViews.get(type)?.setVisibility(View.INVISIBLE)
        }

        // In the tropics, noon and nadir can be at the same azimuth, so inset and dim the icon for
        // the one that comes later. We also explicitly reset the non-offset event(s) to correct
        // previous later-event status.

        val noonEvent = shownEvents.get(SunEvent.Type.NOON)
        val nadirEvent = shownEvents.get(SunEvent.Type.NADIR)

        if (noonEvent == null || nadirEvent == null) {
            Timber.e("Noon or nadir missing; shownEvents=%s", shownEvents)
            return
        }

        val isOverlap = noonEvent.isNear(nadirEvent)
        val isNoonEarlier = noonEvent.compareTo(nadirEvent) < 1
        val isNoonInset = isOverlap && !isNoonEarlier
        val isNadirInset = isOverlap && isNoonEarlier

        noonWrapper.selectInset(isNoonInset)
        nadirWrapper.selectInset(isNadirInset)
    }

    override fun onDeviceOrientationChanged(deviceOrientation: DeviceOrientation) {
        val compassAzimuthDeg = deviceOrientation.headingDegrees.toDouble()

        when (compassDisplayState) {
            CompassDisplayState.LOCKED, CompassDisplayState.UNLOCKING -> {}

            CompassDisplayState.UNLOCK_PENDING -> {
                // We use a single orientation update to animate rotating the compass to the reported
                // heading. We block further orientation updates until the animation is complete.

                compassDisplayState = CompassDisplayState.UNLOCKING

                rotateCompass(
                    compassAzimuthDeg, object : AnimatorListenerAdapter() {
                        override fun onAnimationCancel(animation: Animator) {
                            compassDisplayState = CompassDisplayState.UNLOCKED
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            compassDisplayState = CompassDisplayState.UNLOCKED
                        }
                    })
            }

            CompassDisplayState.UNLOCKED -> rotateCompass(compassAzimuthDeg,  /* listener= */ null)
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

        compassDisplayState =
            if (isLocked) CompassDisplayState.LOCKED else CompassDisplayState.UNLOCKED

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
        Timber.d("widthPx=%d", widthPx)

        val pxPerDp = widthPx.toDouble() / COMPASS_SIDE_DP
        compassRadiusPx = (pxPerDp * COMPASS_RADIUS_DP).toInt()
        val insetRadiusPx = (compassRadiusPx * RADIUS_INSET_SCALE).toInt()
        radiusPxRange = compassRadiusPx - insetRadiusPx

        compassRadiusViews.forEach { view ->
            LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, compassRadiusPx)
        }

        val sunMovementRadiusPx = (compassRadiusPx * RADIUS_SUN_MOVEMENT_SCALE).toInt()
        LayoutParamsUtil.changeConstraintLayoutCircleRadius(
            binding.sunMovement, sunMovementRadiusPx
        )

        noonWrapper.redraw()
        nadirWrapper.redraw()
    }

    private fun trackOrientation() {
        fusedOrientationProviderClient.requestOrientationUpdates(
            DEVICE_ORIENTATION_REQUEST, mainExecutor, this
        ).addOnSuccessListener { _ ->
            Timber.d("Request for orientation updates succeeded.")
            compassDisplayState = CompassDisplayState.UNLOCK_PENDING
            binding.southAtTop.isEnabled = false
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to request orientation updates.")
            binding.lockCompass.isChecked = true
            binding.lockCompass.isEnabled = false
            applyCompassControls()
        }
    }

    private fun ignoreOrientation() {
        fusedOrientationProviderClient.removeOrientationUpdates(this)
            .addOnSuccessListener { _ -> Timber.d("Orientation updates removed.") }
            .addOnFailureListener { e -> Timber.e(e, "Failed to remove orientation updates.") }
    }

    private fun rotateCompassToLockedPosition() {
        rotateCompass(if (binding.southAtTop.isChecked) 180.0 else 0.0,  /* listener= */ null)
    }

    /**
     * Executes an animated sweep from the old compass rotation to the new one, and updates the old
     * one to the new value to prepare for the next update.
     */
    private fun rotateCompass(compassAzimuthDeg: Double, listener: Animator.AnimatorListener?) {
        // When animating from -179 to +179, we don't want to go the long way around the circle.

        val deltaDeg = DirectionUtil.arc(lastRotationDeg, -compassAzimuthDeg)
        val desiredRotationDeg = lastRotationDeg + deltaDeg

        val compassAnimator = ObjectAnimator.ofFloat(
            binding.compassRotating,
            "rotation",
            lastRotationDeg.toFloat(),
            desiredRotationDeg.toFloat()
        ).setDuration(ROTATION_ANIMATION_DURATION_MILLIS)

        if (listener != null) {
            compassAnimator.addListener(listener)
        }

        compassAnimator.setAutoCancel(true)
        compassAnimator.start()

        lastRotationDeg = desiredRotationDeg
    }

    /**
     * Wrapper for noon and nadir [ImageView] objects which provides a single object and
     * property for ObjectAnimator to use in doing overlap inset swaps.
     */
    private inner class NoonNadirWrapper(private val view: ImageView) {

        private var insetPct = 0

        fun selectInset(isInset: Boolean) {
            val targetPct = if (isInset) 100 else 0

            if (targetPct != insetPct) {
                val insetAnimator = ObjectAnimator.ofInt(this, "insetPct", targetPct)
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
            LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, radiusPx)
        }
    }

    companion object {

        /**
         * How long the compass rotation would last, in theory. In practice it will be interrupted by a
         * new azimuth reading before then, but this serves to damp the motion of the compass, making it
         * visually smoother.
         */
        private const val ROTATION_ANIMATION_DURATION_MILLIS = 200L

        /** How long it takes to swap the noon and nadir inset positions.  */
        private const val INSET_ANIMATION_DURATION_MILLIS = 1000L

        private const val COMPASS_SIDE_DP = 100
        private const val COMPASS_RADIUS_DP = 44

        private const val RADIUS_INSET_SCALE = 0.82
        private const val RADIUS_SUN_MOVEMENT_SCALE = 0.6

        private const val OPAQUE_ALPHA = 1.0f
        private const val INSET_ALPHA = 0.5f
        private const val ALPHA_RANGE = OPAQUE_ALPHA - INSET_ALPHA

        /** Shared preference key for compass lock status.  */
        private const val PREF_LOCK_COMPASS = "lockCompass"

        /** Shared preference key for (locked) compass south-at-top status.  */
        private const val PREF_SOUTH_AT_TOP = "southAtTop"

        private val DEVICE_ORIENTATION_REQUEST =
            DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()
    }
}
