package org.onereed.helios;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.google.common.collect.ImmutableMap;

import org.onereed.helios.common.LogUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.logger.AppLogger;

import java.util.Locale;
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

  private ActivityCompassBinding activityCompassBinding;
  private SensorManager sensorManager;
  float oldAzimuth = 0.0f;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activityCompassBinding = ActivityCompassBinding.inflate(getLayoutInflater());
    setContentView(activityCompassBinding.getRoot());
    setSupportActionBar(activityCompassBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
  }

  @Override
  protected Map<Integer, Runnable> getMenuActions() {
    return ImmutableMap.of();
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    AppLogger.debug(TAG, "onAccuracyChanged: sensor=%s accuracy=%d", sensor, accuracy);
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
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
      return;
    }

    float[] rotationMatrix = new float[9];
    float[] orientationAngles = new float[3];

    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
    SensorManager.getOrientation(rotationMatrix, orientationAngles);

    float azimuth = (float) toDegrees(orientationAngles[0]);

    String info =
        String.format(
            Locale.ENGLISH,
            "azimuth=%.4f pitch=%.4f roll=%.4f",
            azimuth,
            toDegrees(orientationAngles[1]),
            toDegrees(orientationAngles[2]));

    activityCompassBinding.azimuth.setText(info);

    RotateAnimation rotateAnimation =
        new RotateAnimation(
            -oldAzimuth,
            -azimuth,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);

    rotateAnimation.setDuration(ROTATION_ANIMATION_DURATION_MILLIS);
    rotateAnimation.setFillAfter(true); // Hold end position after animation

    activityCompassBinding.compass.startAnimation(rotateAnimation);
    oldAzimuth = azimuth;
  }
}
