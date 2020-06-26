package org.onereed.helios;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.ImmutableSet;

import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.LayoutParamsUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.location.LocationManager;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.toDegrees;

/** Displays directions to sun events. */
public class CompassActivity extends AbstractMenuActivity implements SensorEventListener {

  private static final String TAG = LogUtil.makeTag(CompassActivity.class);

  /**
   * How long the compass rotation would last, in theory. In practice it will be interrupted by a
   * new azimuth reading before then, but this serves to damp the motion of the compass, making it
   * visually smoother.
   */
  private static final long ROTATION_ANIMATION_DURATION_MILLIS = 200L;

  private static final int COMPASS_SIDE_DP = 100;

  private static final int COMPASS_RADIUS_DP = 44;

  private static final double RADIUS_INSET_SCALE = 0.82;

  private static final int INSET_ALPHA_PCT = 50;

  private static final ImmutableSet<SunEvent.Type> REQUIRED_EVENT_TYPES =
      ImmutableSet.of(SunEvent.Type.NOON, SunEvent.Type.NADIR);

  /** Shared preference key for compass lock status. */
  private static final String LOCK_COMPASS = "lockCompass";

  private ActivityCompassBinding activityCompassBinding;
  private SensorManager sensorManager;
  private Sensor rotationVectorSensor = null;
  private LocationManager locationManager;

  private Map<SunEvent.Type, ImageView> sunEventViews = new HashMap<>();
  private List<ImageView> circleViews = new ArrayList<>();

  private double magneticDeclinationDeg = 0.0;
  private float lastRotationDeg = 0.0f;
  int compassRadiusPx;

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
      activityCompassBinding.lockCompassControl.setVisibility(View.INVISIBLE);
    }

    ViewModelProvider.Factory factory = new ViewModelProvider.NewInstanceFactory();
    SunInfoViewModel sunInfoViewModel =
        new ViewModelProvider(this, factory).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, this::acceptSunInfo);

    locationManager = new LocationManager(this, sunInfoViewModel::acceptPlace);
    getLifecycle().addObserver(locationManager);

    sunEventViews.put(SunEvent.Type.RISE, activityCompassBinding.rise);
    sunEventViews.put(SunEvent.Type.NOON, activityCompassBinding.noon);
    sunEventViews.put(SunEvent.Type.SET, activityCompassBinding.set);
    sunEventViews.put(SunEvent.Type.NADIR, activityCompassBinding.nadir);

    circleViews.add(activityCompassBinding.sun);
    circleViews.addAll(sunEventViews.values());
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onResume() {
    super.onResume();
    activityCompassBinding.compassComposite.post(this::obtainCompassRadius);

    if (rotationVectorSensor != null) {
      boolean isLocked =
          PreferenceManager.getDefaultSharedPreferences(this).getBoolean(LOCK_COMPASS, false);
      activityCompassBinding.lockCompassControl.setChecked(isLocked);
      expandLockCheckboxHitRect();
    }

    applyCompassLockState();
  }

  /**
   * Google Play Store accessibility testing complained that the hit rect for the lock-compass
   * checkbox is too small, so expand it using a {@link TouchDelegate}.
   */
  private void expandLockCheckboxHitRect() {
    View parent = (View) activityCompassBinding.lockCompassControl.getParent();
    parent.post(
        () -> {
          Rect rect = new Rect();
          activityCompassBinding.lockCompassControl.getHitRect(rect);
          int extraPadding = rect.height();
          rect.top -= extraPadding;
          rect.left -= extraPadding;
          rect.right += extraPadding;
          rect.bottom += extraPadding;
          parent.setTouchDelegate(
              new TouchDelegate(rect, activityCompassBinding.lockCompassControl));
        });
  }

  private void obtainCompassRadius() {
    int widthPx = activityCompassBinding.compassFace.getWidth();
    double pxPerDp = (double) widthPx / COMPASS_SIDE_DP;
    compassRadiusPx = (int) (pxPerDp * COMPASS_RADIUS_DP);

    circleViews.forEach(
        view -> LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, compassRadiusPx));
  }

  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putBoolean(LOCK_COMPASS, activityCompassBinding.lockCompassControl.isChecked())
        .apply();
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

    ObjectAnimator compassAnimator =
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

    LayoutParamsUtil.changeConstraintLayoutCircleAngle(
        activityCompassBinding.sun, sunInfo.getSunAzimuthDeg());

    Map<SunEvent.Type, SunEvent> shownEvents = new HashMap<>();

    for (SunEvent sunEvent : sunInfo.getSunEvents()) {
      SunEvent.Type type = sunEvent.getType();

      if (!shownEvents.containsKey(type)) {
        shownEvents.put(type, sunEvent);
        ImageView view = checkNotNull(sunEventViews.get(type));
        LayoutParamsUtil.changeConstraintLayoutCircleAngle(view, sunEvent.getAzimuthDeg());
        view.setVisibility(View.VISIBLE);
      }
    }

    EnumSet<SunEvent.Type> shownTypes = EnumSet.copyOf(shownEvents.keySet());
    EnumSet<SunEvent.Type> missingTypes = EnumSet.complementOf(shownTypes);

    missingTypes.forEach(
        type -> checkNotNull(sunEventViews.get(type)).setVisibility(View.INVISIBLE));

    if (!shownTypes.containsAll(REQUIRED_EVENT_TYPES)) {
      AppLogger.error(TAG, "Noon or nadir missing; shownEvents=%s", shownEvents);
    }

    // In the tropics, noon and nadir can be at the same azimuth, so inset and dim the icon for
    // the one that comes later. We also explicitly reset the non-offset event(s) to correct
    // previous later-event status.

    int noonRadiusPx = compassRadiusPx;
    int nadirRadiusPx = compassRadiusPx;

    int noonAlphaPct = 100;
    int nadirAlphaPct = 100;

    SunEvent noonEvent = checkNotNull(shownEvents.get(SunEvent.Type.NOON));
    NoonNadirWrapper noonWrapper = new NoonNadirWrapper(noonEvent);
    SunEvent nadirEvent = checkNotNull(shownEvents.get(SunEvent.Type.NADIR));
    NoonNadirWrapper nadirWrapper = new NoonNadirWrapper(nadirEvent);

    if (noonWrapper.isNorth == nadirWrapper.isNorth) {
      int insetRadiusPx = (int) (compassRadiusPx * RADIUS_INSET_SCALE);

      if (noonEvent.compareTo(nadirEvent) < 1) {
        nadirRadiusPx = insetRadiusPx;
        nadirAlphaPct = INSET_ALPHA_PCT;
      } else {
        noonRadiusPx = insetRadiusPx;
        noonAlphaPct = INSET_ALPHA_PCT;
      }
    }

    // TODO: Animate these transitions.

    noonWrapper.setRadiusPx(noonRadiusPx);
    noonWrapper.setAlphaPct(noonAlphaPct);
    nadirWrapper.setRadiusPx(nadirRadiusPx);
    nadirWrapper.setAlphaPct(nadirAlphaPct);
  }

  public void onCheckboxClicked(View view) {
    // For now, there's just one checkbox, so we know what to do without checking further.
    applyCompassLockState();
  }

  private void applyCompassLockState() {
    if (activityCompassBinding.lockCompassControl.isChecked()) {
      sensorManager.unregisterListener(this);
      updateCompassState(0.0f);
    } else {
      sensorManager.registerListener(
          this,
          rotationVectorSensor,
          SensorManager.SENSOR_DELAY_NORMAL,
          SensorManager.SENSOR_DELAY_UI);
    }
  }

  /**
   * Wrapper providing properties for noon-nadir collision handling. We represent alpha as an
   * integer percent to simplify equality comparison.
   */
  private class NoonNadirWrapper {

    private final ImageView view;

    private final boolean isNorth;

    private NoonNadirWrapper(SunEvent sunEvent) {
      this.view = sunEventViews.get(sunEvent.getType());

      // If we're starting near north (0 az), we'll end up near 90. If we start off near south
      // (180 az), we'll end up near -90.
      float offsetDeg = DirectionUtil.zeroCenterDeg(sunEvent.getAzimuthDeg() + 90.0);
      this.isNorth = Math.abs(offsetDeg - 90.0f) < 45.0f;
    }

    private int getAlphaPct() {
      return Math.round(100 * view.getAlpha());
    }

    private void setAlphaPct(int alphaPct) {
      view.setAlpha(alphaPct / 100.0f);
    }

    private int getRadiusPx() {
      return LayoutParamsUtil.getConstraintLayoutCircleRadius(view);
    }

    private void setRadiusPx(int radiusPx) {
      LayoutParamsUtil.changeConstraintLayoutCircleRadius(view, radiusPx);
    }
  }
}
