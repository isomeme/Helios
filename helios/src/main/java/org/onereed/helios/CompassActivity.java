package org.onereed.helios;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

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

  private SensorManager sensorManager;

  private final float[] accelerometerReading = new float[3];
  private final float[] magnetometerReading = new float[3];

  private final float[] rotationMatrix = new float[9];
  private final float[] orientationAngles = new float[3];

  private boolean isAccelerometerReadingSet = false;
  private boolean isMagnetometerReadingSet = false;

  private ActivityCompassBinding activityCompassBinding;

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

    Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    if (accelerometer != null) {
      sensorManager.registerListener(
          this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }
    Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    if (magneticField != null) {
      sensorManager.registerListener(
          this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    sensorManager.unregisterListener(this);
  }

  // Get readings from accelerometer and magnetometer. To simplify calculations,
  // consider storing these readings as unit vectors.
  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
      isAccelerometerReadingSet = true;
    } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
      System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
      isMagnetometerReadingSet = true;
    }

    if (isAccelerometerReadingSet && isMagnetometerReadingSet) {
      updateOrientationAngles();
    }
  }

  // Compute the three orientation angles based on the most recent readings from
  // the device's accelerometer and magnetometer.
  public void updateOrientationAngles() {
    // Update rotation matrix, which is needed to update orientation angles.
    SensorManager.getRotationMatrix(
        rotationMatrix, null, accelerometerReading, magnetometerReading);

    // "mRotationMatrix" now has up-to-date information.

    SensorManager.getOrientation(rotationMatrix, orientationAngles);

    // "orientationAngles" now has up-to-date information.

    String info =
        String.format(
            Locale.ENGLISH,
            "azimuth=%.4f pitch=%.4f roll=%.4f",
            toDegrees(rotationMatrix[0]),
            toDegrees(rotationMatrix[1]),
            toDegrees(rotationMatrix[2]));

    activityCompassBinding.azimuth.setText(info);
  }
}
