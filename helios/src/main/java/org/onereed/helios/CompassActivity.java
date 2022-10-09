package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.toDegrees;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.LayoutParamsUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.location.LocationManager;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

/** Displays directions to sun events. */
public class CompassActivity extends AbstractMenuActivity implements SensorEventListener {

  private static final String TAG = LogUtil.makeTag(CompassActivity.class);

  /**
   * How long the compass rotation would last, in theory. In practice it will be interrupted by a
   * new azimuth reading before then, but this serves to damp the motion of the compass, making it
   * visually smoother.
   */
  private static final long ROTATION_ANIMATION_DURATION_MILLIS = 200L;

  /** How long it takes to swap the noon and nadir inset positions. */
  private static final long INSET_ANIMATION_DURATION_MILLIS = 1000L;

  private static final int COMPASS_SIDE_DP = 100;

  private static final int COMPASS_RADIUS_DP = 44;

  private static final double RADIUS_INSET_SCALE = 0.82;

  private static final double RADIUS_SUN_MOVEMENT_SCALE = 0.6;

  private static final float OPAQUE_ALPHA = 1.0f;

  private static final float INSET_ALPHA = 0.5f;

  private static final float ALPHA_RANGE = OPAQUE_ALPHA - INSET_ALPHA;

  private static final ImmutableSet<SunEvent.Type> REQUIRED_EVENT_TYPES =
      ImmutableSet.of(SunEvent.Type.NOON, SunEvent.Type.NADIR);

  /** Shared preference key for compass lock status. */
  private static final String PREF_LOCK_COMPASS = "lockCompass";

  /** Shared preference key for (locked) compass south-at-top status. */
  private static final String PREF_SOUTH_AT_TOP = "southAtTop";

  private ActivityCompassBinding activityCompassBinding;
  private SensorManager sensorManager;
  private Sensor rotationVectorSensor = null;
  private LocationManager locationManager;

  private ImmutableMap<SunEvent.Type, ImageView> sunEventViews = null;
  private ImmutableList<ImageView> compassRadiusViews = null;
  private NoonNadirWrapper noonWrapper = null;
  private NoonNadirWrapper nadirWrapper = null;

  private double magneticDeclinationDeg = 0.0;
  private float lastRotationDeg = 0.0f;
  private int compassRadiusPx;
  private int radiusPxRange;

  /** Returns true iff noon and nadir are either both in the north or both in the south. */
  private static boolean noonNadirOverlap(SunEvent noonEvent, SunEvent nadirEvent) {
    // If we're starting near north (0 az), we'll end up near 90. If we start off near south
    // (180 az), we'll end up near -90.
    float noonOffsetDeg = DirectionUtil.zeroCenterDeg(noonEvent.getAzimuthDeg() + 90.0);
    float nadirOffsetDeg = DirectionUtil.zeroCenterDeg(nadirEvent.getAzimuthDeg() + 90.0);
    return Math.abs(noonOffsetDeg - nadirOffsetDeg) < 45.0;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activityCompassBinding = ActivityCompassBinding.inflate(getLayoutInflater());
    setContentView(activityCompassBinding.getRoot());
    setSupportActionBar(activityCompassBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    if (rotationVectorSensor == null) {
      activityCompassBinding.lockCompassControl.setChecked(true);
      activityCompassBinding.lockCompassControl.setEnabled(false);
    }

    var sunInfoViewModel = new ViewModelProvider(this).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, this::acceptSunInfo);

    locationManager = new LocationManager(this, sunInfoViewModel::acceptPlace);
    getLifecycle().addObserver(locationManager);

    sunEventViews =
        ImmutableMap.of(
            SunEvent.Type.RISE,
            activityCompassBinding.rise,
            SunEvent.Type.NOON,
            activityCompassBinding.noon,
            SunEvent.Type.SET,
            activityCompassBinding.set,
            SunEvent.Type.NADIR,
            activityCompassBinding.nadir);

    // Note that noon and nadir can be inset, and sunMovement is at a fraction of the compass
    // radius, so they're handled differently.
    compassRadiusViews =
        ImmutableList.of(
            activityCompassBinding.sun, activityCompassBinding.rise, activityCompassBinding.set);

    noonWrapper = new NoonNadirWrapper(activityCompassBinding.noon);
    nadirWrapper = new NoonNadirWrapper(activityCompassBinding.nadir);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_text);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onResume() {
    super.onResume();

    activityCompassBinding.compassComposite.post(this::obtainCompassRadius);

    var prefs = getPreferences();
    boolean isLocked = prefs.getBoolean(PREF_LOCK_COMPASS, false) || rotationVectorSensor == null;
    boolean southAtTop = prefs.getBoolean(PREF_SOUTH_AT_TOP, false);

    activityCompassBinding.lockCompassControl.setChecked(isLocked);
    activityCompassBinding.southAtTopControl.setChecked(southAtTop);
    activityCompassBinding.southAtTopControl.setEnabled(isLocked);

    applyCompassLockState();
  }

  private void obtainCompassRadius() {
    int widthPx = activityCompassBinding.compassFace.getWidth();
    double pxPerDp = (double) widthPx / COMPASS_SIDE_DP;
    compassRadiusPx = (int) (pxPerDp * COMPASS_RADIUS_DP);
    int insetRadiusPx = (int) (compassRadiusPx * RADIUS_INSET_SCALE);
    radiusPxRange = compassRadiusPx - insetRadiusPx;

    compassRadiusViews.forEach(
        view -> LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, compassRadiusPx));

    int sunMovementRadiusPx = (int) (compassRadiusPx * RADIUS_SUN_MOVEMENT_SCALE);
    LayoutParamsUtil.changeConstraintLayoutCircleRadius(
        activityCompassBinding.sunMovement, sunMovementRadiusPx);

    noonWrapper.redraw();
    nadirWrapper.redraw();
  }

  @Override
  protected void onPause() {
    super.onPause();

    sensorManager.unregisterListener(this);
    getPreferences()
        .edit()
        .putBoolean(PREF_LOCK_COMPASS, activityCompassBinding.lockCompassControl.isChecked())
        .putBoolean(PREF_SOUTH_AT_TOP, activityCompassBinding.southAtTopControl.isChecked())
        .apply();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    // NoonNadirWrapper instances hold references to views, so explicitly free them up for
    // garbage collection.
    noonWrapper = null;
    nadirWrapper = null;
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // Do nothing.
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
      return;
    }

    float[] rotationMatrix = new float[9];
    float[] orientationAngles = new float[3];

    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
    SensorManager.getOrientation(rotationMatrix, orientationAngles);

    double magneticAzimuthDeg = toDegrees(orientationAngles[0]);

    // This is how much the compass face as a whole will rotate, based on the device's orientation
    // with respect to true north.
    float compassAzimuthDeg =
        DirectionUtil.zeroCenterDeg(magneticAzimuthDeg + magneticDeclinationDeg);

    updateCompassState(compassAzimuthDeg);
  }

  /**
   * Executes an animated sweep from the old compass rotation to the new one, and updates the old
   * one to the new value to prepare for the next update.
   */
  private void updateCompassState(float compassAzimuthDeg) {
    float desiredRotationDeg = -compassAzimuthDeg;
    float deltaDeg = desiredRotationDeg - lastRotationDeg;

    // When animating from e.g. -179 to +179, we don't want to go the long way around the circle.

    float adjustedDesiredRotationDeg = desiredRotationDeg;

    if (deltaDeg > 180.0f) {
      adjustedDesiredRotationDeg -= 360.0f;
    } else if (deltaDeg < -180.0f) {
      adjustedDesiredRotationDeg += 360.0f;
    }

    var compassAnimator =
        ObjectAnimator.ofFloat(
                activityCompassBinding.compassRotating,
                "rotation",
                lastRotationDeg,
                adjustedDesiredRotationDeg)
            .setDuration(ROTATION_ANIMATION_DURATION_MILLIS);
    compassAnimator.setAutoCancel(true);
    compassAnimator.start();

    lastRotationDeg = desiredRotationDeg;
  }

  private void acceptSunInfo(SunInfo sunInfo) {
    magneticDeclinationDeg = sunInfo.getMagneticDeclinationDeg();
    var sunAzimuthInfo = sunInfo.getSunAzimuthInfo();
    float sunAzimuthDeg = sunAzimuthInfo.getAzimuthDeg();

    LayoutParamsUtil.changeConstraintLayoutCircleAngle(activityCompassBinding.sun, sunAzimuthDeg);
    LayoutParamsUtil.changeConstraintLayoutCircleAngle(
        activityCompassBinding.sunMovement, sunAzimuthDeg);

    float sunMovementRotation =
        sunAzimuthInfo.isClockwise() ? sunAzimuthDeg : sunAzimuthDeg + 180.0f;
    activityCompassBinding.sunMovement.setRotation(sunMovementRotation);

    var shownEvents = new HashMap<SunEvent.Type, SunEvent>();

    for (SunEvent sunEvent : sunInfo.getSunEvents()) {
      SunEvent.Type type = sunEvent.getType();

      if (!shownEvents.containsKey(type)) {
        shownEvents.put(type, sunEvent);
        var view = checkNotNull(sunEventViews.get(type));
        LayoutParamsUtil.changeConstraintLayoutCircleAngle(view, sunEvent.getAzimuthDeg());
        view.setVisibility(View.VISIBLE);
      }
    }

    var shownTypes = EnumSet.copyOf(shownEvents.keySet());
    var missingTypes = EnumSet.complementOf(shownTypes);

    missingTypes.forEach(
        type -> checkNotNull(sunEventViews.get(type)).setVisibility(View.INVISIBLE));

    if (!shownTypes.containsAll(REQUIRED_EVENT_TYPES)) {
      AppLogger.error(TAG, "Noon or nadir missing; shownEvents=%s", shownEvents);
      return;
    }

    // In the tropics, noon and nadir can be at the same azimuth, so inset and dim the icon for
    // the one that comes later. We also explicitly reset the non-offset event(s) to correct
    // previous later-event status.

    var noonEvent = checkNotNull(shownEvents.get(SunEvent.Type.NOON));
    var nadirEvent = checkNotNull(shownEvents.get(SunEvent.Type.NADIR));

    boolean isOverlap = noonNadirOverlap(noonEvent, nadirEvent);
    boolean isNoonEarlier = noonEvent.compareTo(nadirEvent) < 1;
    boolean isNoonInset = isOverlap && !isNoonEarlier;
    boolean isNadirInset = isOverlap && isNoonEarlier;

    noonWrapper.selectInset(isNoonInset);
    nadirWrapper.selectInset(isNadirInset);
  }

  public void onCheckboxClicked(View view) {
    // This call handles both checkboxes.
    applyCompassLockState();
  }

  private void applyCompassLockState() {
    if (activityCompassBinding.lockCompassControl.isChecked()) {
      activityCompassBinding.southAtTopControl.setEnabled(true);
      sensorManager.unregisterListener(this);
      updateCompassState(activityCompassBinding.southAtTopControl.isChecked() ? 180.0f : 0.0f);
    } else {
      activityCompassBinding.southAtTopControl.setEnabled(false);
      sensorManager.registerListener(
          this,
          rotationVectorSensor,
          SensorManager.SENSOR_DELAY_NORMAL,
          SensorManager.SENSOR_DELAY_UI);
    }
  }

  /** Obtains user preferences using the same path as the deprecated older way of doing this. */
  private SharedPreferences getPreferences() {
    return getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
  }

  /**
   * Wrapper for noon and nadir {@link ImageView} objects which provides a single object and
   * property for ObjectAnimator to use in doing overlap inset swaps.
   */
  private class NoonNadirWrapper {

    private final ImageView view;
    private int insetPct = 0;

    private NoonNadirWrapper(ImageView view) {
      this.view = view;
    }

    private void selectInset(boolean isInset) {
      int targetPct = isInset ? 100 : 0;

      if (targetPct != insetPct) {
        ObjectAnimator insetAnimator =
            ObjectAnimator.ofInt(this, "insetPct", targetPct)
                .setDuration(INSET_ANIMATION_DURATION_MILLIS);
        insetAnimator.setAutoCancel(true);
        insetAnimator.start();
      }
    }

    @Keep
    int getInsetPct() {
      return insetPct;
    }

    @Keep
    void setInsetPct(int pct) {
      insetPct = pct;

      float insetFraction = pct / 100.0f;
      float alpha = OPAQUE_ALPHA - insetFraction * ALPHA_RANGE;
      int radiusPx = (int) (compassRadiusPx - insetFraction * radiusPxRange);

      view.setAlpha(alpha);
      LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, radiusPx);
    }

    /**
     * Called from {@link CompassActivity#obtainCompassRadius()} to redraw this event at the
     * appropriate radius and alpha for its saved state.
     */
    private void redraw() {
      setInsetPct(insetPct);
    }
  }
}
