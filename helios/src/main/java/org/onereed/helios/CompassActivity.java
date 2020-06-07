package org.onereed.helios;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.LogUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.location.LocationManager;
import org.onereed.helios.logger.AppLogger;
import org.onereed.helios.sun.SunInfo;

import java.util.Locale;

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

  private ActivityCompassBinding activityCompassBinding;
  private SensorManager sensorManager;
  private LocationManager locationManager;

  double magneticDeclinationDeg = 0.0;
  float oldCompassAzimuthDeg = 0.0f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activityCompassBinding = ActivityCompassBinding.inflate(getLayoutInflater());
    setContentView(activityCompassBinding.getRoot());
    setSupportActionBar(activityCompassBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    ViewModelProvider.Factory factory = new ViewModelProvider.NewInstanceFactory();
    SunInfoViewModel sunInfoViewModel =
        new ViewModelProvider(this, factory).get(SunInfoViewModel.class);

    sunInfoViewModel.getSunInfoLiveData().observe(this, this::acceptSunInfo);

    locationManager = new LocationManager(this, sunInfoViewModel::acceptPlace);
    getLifecycle().addObserver(locationManager);
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

    Sensor rotationVectorSensor =
        checkNotNull(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
    sensorManager.registerListener(
        this,
        rotationVectorSensor,
        SensorManager.SENSOR_DELAY_NORMAL,
        SensorManager.SENSOR_DELAY_UI);
  }

  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    AppLogger.debug(TAG, "onAccuracyChanged: sensor=%s accuracy=%d", sensor, accuracy);
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
    double pitchDeg = toDegrees(orientationAngles[1]);
    double rollDeg = toDegrees(orientationAngles[2]);

    // This is how much the compass face as a whole will rotate, based on the device's orientation
    // with respect to true north.
    float compassAzimuthDeg =
        (float) DirectionUtil.zeroCenterDeg(magneticAzimuthDeg + magneticDeclinationDeg);

    String info =
        String.format(
            Locale.ENGLISH,
            "magAz=%.1f comAz=%.1f pitch=%.1f roll=%.1f",
            magneticAzimuthDeg,
            compassAzimuthDeg,
            pitchDeg,
            rollDeg);

    activityCompassBinding.azimuth.setText(info);

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

    activityCompassBinding.compassComposite.startAnimation(rotateAnimation);
    oldCompassAzimuthDeg = compassAzimuthDeg;
  }

  private void acceptSunInfo(SunInfo sunInfo) {
    activityCompassBinding.sun.setRotation((float) sunInfo.getSunAzimuthDeg());
    magneticDeclinationDeg = sunInfo.getMagneticDeclinationDeg();
  }
}
