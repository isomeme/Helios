package org.onereed.helios;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.location.LocationManager;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;

import java.util.EnumSet;
import java.util.HashMap;
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

  /** Shared preference key for compass lock status. */
  private static final String LOCK_COMPASS = "lockCompass";

  private ActivityCompassBinding activityCompassBinding;
  private SensorManager sensorManager;
  private Sensor rotationVectorSensor = null;
  private LocationManager locationManager;

  private Map<SunEvent.Type, ImageView> sunEventViews = new HashMap<>();

  private double magneticDeclinationDeg = 0.0;
  private float oldCompassAzimuthDeg = 0.0f;

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
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    AppLogger.debug(TAG, "onRequestPermissionsResult");
    locationManager.acceptPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  protected void onResume() {
    super.onResume();
    activityCompassBinding.compassComposite.post(this::applyCompassRadius);

    if (rotationVectorSensor != null) {
      boolean isLocked =
          PreferenceManager.getDefaultSharedPreferences(this).getBoolean(LOCK_COMPASS, false);
      activityCompassBinding.lockCompassControl.setChecked(isLocked);
    }

    applyCompassLockState();
  }

  private void applyCompassRadius() {
    int widthPx = activityCompassBinding.compassFace.getWidth();
    double pxPerDp = (double) widthPx / COMPASS_SIDE_DP;
    int compassRadiusPx = (int) (pxPerDp * COMPASS_RADIUS_DP);
    AppLogger.debug(TAG, "radius=%d pxPerDp=%.2f", compassRadiusPx, pxPerDp);

    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) activityCompassBinding.smallSquare.getLayoutParams();
    layoutParams.circleRadius = compassRadiusPx;
    activityCompassBinding.smallSquare.setLayoutParams(layoutParams);
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
        (float) DirectionUtil.zeroCenterDeg(magneticAzimuthDeg + magneticDeclinationDeg);

    updateCompassState(compassAzimuthDeg);
  }

  /**
   * Executes an animated sweep from the old compass rotation to the new one, and updates the old
   * one to the new value to prepare for the next update.
   */
  private void updateCompassState(float compassAzimuthDeg) {
    float azimuthDeltaDeg = compassAzimuthDeg - oldCompassAzimuthDeg;

    // When animating from e.g. -179 to +179, we don't want to go the long way around the circle.

    float adjustedAzimuthDeg = compassAzimuthDeg;

    if (azimuthDeltaDeg > 180.0f) {
      adjustedAzimuthDeg -= 360.0f;
    } else if (azimuthDeltaDeg < -180.0f) {
      adjustedAzimuthDeg += 360.0f;
    }

    RotateAnimation rotateAnimation =
        new RotateAnimation(
            -oldCompassAzimuthDeg,
            -adjustedAzimuthDeg,
            Animation.RELATIVE_TO_SELF,
            /* pivotXValue= */ 0.5f,
            Animation.RELATIVE_TO_SELF,
            /* pivotYValue= */ 0.5f);

    rotateAnimation.setDuration(ROTATION_ANIMATION_DURATION_MILLIS);
    rotateAnimation.setFillAfter(true); // Hold end position after animation

    activityCompassBinding.compassRotating.startAnimation(rotateAnimation);
    oldCompassAzimuthDeg = compassAzimuthDeg;
  }

  private void acceptSunInfo(SunInfo sunInfo) {
    activityCompassBinding.sun.setRotation((float) sunInfo.getSunAzimuthDeg());
    magneticDeclinationDeg = sunInfo.getMagneticDeclinationDeg();

    EnumSet<SunEvent.Type> unusedTypes = EnumSet.allOf(SunEvent.Type.class);

    for (SunEvent sunEvent : sunInfo.getSunEvents()) {
      SunEvent.Type type = sunEvent.getType();

      if (unusedTypes.remove(type)) {
        ImageView view = checkNotNull(sunEventViews.get(type));
        view.setRotation((float) sunEvent.getAzimuthDeg());
        view.setVisibility(View.VISIBLE);
      }
    }

    for (SunEvent.Type type : unusedTypes) {
      checkNotNull(sunEventViews.get(type)).setVisibility(View.INVISIBLE);
    }
  }

  public void onCheckboxClicked(View view) {
    AppLogger.debug(TAG, "onCheckboxClicked");

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
}
