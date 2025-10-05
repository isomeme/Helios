package org.onereed.helios;

import static com.google.common.base.Preconditions.checkNotNull;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.google.android.gms.location.DeviceOrientation;
import com.google.android.gms.location.DeviceOrientationListener;
import com.google.android.gms.location.DeviceOrientationRequest;
import com.google.android.gms.location.FusedOrientationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import org.onereed.helios.common.DirectionUtil;
import org.onereed.helios.common.LayoutParamsUtil;
import org.onereed.helios.databinding.ActivityCompassBinding;
import org.onereed.helios.sun.SunEvent;
import org.onereed.helios.sun.SunInfo;
import timber.log.Timber;

/** Displays directions to sun events. */
public class CompassActivity extends BaseSunInfoActivity
    implements DeviceOrientationListener, Observer<SunInfo> {

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

  private static final DeviceOrientationRequest DEVICE_ORIENTATION_REQUEST =
      new DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build();

  private ActivityCompassBinding activityCompassBinding;

  private FusedOrientationProviderClient fusedOrientationProviderClient;

  private ImmutableMap<SunEvent.Type, ImageView> sunEventViews = null;
  private ImmutableList<ImageView> compassRadiusViews = null;
  private NoonNadirWrapper noonWrapper = null;
  private NoonNadirWrapper nadirWrapper = null;

  private float lastRotationDeg = 0.0f;
  private int compassRadiusPx;
  private int radiusPxRange;

  private enum CompassDisplayState {
    LOCKED,
    UNLOCK_PENDING,
    UNLOCKING,
    UNLOCKED
  }

  private CompassDisplayState compassDisplayState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Timber.d("onCreate");
    super.onCreate(savedInstanceState);

    activityCompassBinding = ActivityCompassBinding.inflate(getLayoutInflater());
    setContentView(activityCompassBinding.getRoot());
    setSupportActionBar(activityCompassBinding.toolbar);
    checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    sunInfoViewModel.getSunInfoLiveData().observe(/* owner= */ this, /* observer= */ this);

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

    fusedOrientationProviderClient = LocationServices.getFusedOrientationProviderClient(this);
  }

  @Override
  protected Set<Integer> getOptionsMenuItems() {
    return ImmutableSet.of(R.id.action_text);
  }

  @Override
  protected void onResume() {
    Timber.d("onResume");
    super.onResume();

    activityCompassBinding.compassComposite.post(this::obtainCompassRadius);

    var prefs = getPreferences(Context.MODE_PRIVATE);
    boolean isLocked = prefs.getBoolean(PREF_LOCK_COMPASS, false);
    boolean southAtTop = prefs.getBoolean(PREF_SOUTH_AT_TOP, false);

    compassDisplayState = isLocked ? CompassDisplayState.LOCKED : CompassDisplayState.UNLOCKED;

    activityCompassBinding.lockCompassControl.setChecked(isLocked);
    activityCompassBinding.lockCompassControl.setEnabled(true);

    activityCompassBinding.southAtTopControl.setChecked(southAtTop);
    activityCompassBinding.southAtTopControl.setEnabled(isLocked);

    applyCompassControls();
  }

  @Override
  protected void onPause() {
    Timber.d("onPause");
    super.onPause();

    fusedOrientationProviderClient.removeOrientationUpdates(this);

    getPreferences(Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_LOCK_COMPASS, activityCompassBinding.lockCompassControl.isChecked())
        .putBoolean(PREF_SOUTH_AT_TOP, activityCompassBinding.southAtTopControl.isChecked())
        .apply();
  }

  @Override
  public void onDestroy() {
    Timber.d("onDestroy");
    super.onDestroy();

    // NoonNadirWrapper instances hold references to views, so explicitly free them up for
    // garbage collection.
    noonWrapper = null;
    nadirWrapper = null;
  }

  @Override
  public void onChanged(@NonNull SunInfo sunInfo) {
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
      Timber.e("Noon or nadir missing; shownEvents=%s", shownEvents);
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

  @Override
  public void onDeviceOrientationChanged(@NonNull DeviceOrientation deviceOrientation) {
    float compassAzimuthDeg = deviceOrientation.getHeadingDegrees();

    switch (compassDisplayState) {
      case LOCKED, UNLOCKING -> {}
      case UNLOCK_PENDING -> {
        // We use a single orientation update to animate rotating the compass to the reported
        // heading. We block further orientation updates until the animation is complete.

        compassDisplayState = CompassDisplayState.UNLOCKING;

        rotateCompass(
            compassAzimuthDeg,
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationCancel(Animator unused) {
                compassDisplayState = CompassDisplayState.UNLOCKED;
              }

              @Override
              public void onAnimationEnd(Animator unused) {
                compassDisplayState = CompassDisplayState.UNLOCKED;
              }
            });
      }
      case UNLOCKED -> rotateCompass(compassAzimuthDeg, /* listener= */ null);
    }
  }

  @Keep
  public void onLockCompassClicked(View unused) {
    applyCompassControls();
  }

  @Keep
  public void onSouthAtTopClicked(View unused) {
    rotateCompassToLockedPosition();
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

  private void applyCompassControls() {
    if (activityCompassBinding.lockCompassControl.isChecked()) {
      compassDisplayState = CompassDisplayState.LOCKED;
      fusedOrientationProviderClient.removeOrientationUpdates(this);
      activityCompassBinding.southAtTopControl.setEnabled(true);
      rotateCompassToLockedPosition();
    } else {
      fusedOrientationProviderClient
          .requestOrientationUpdates(DEVICE_ORIENTATION_REQUEST, mainExecutor, this)
          .addOnSuccessListener(
              unusedVoid -> {
                Timber.i("Request for orientation updates succeeded.");
                compassDisplayState = CompassDisplayState.UNLOCK_PENDING;
                activityCompassBinding.southAtTopControl.setEnabled(false);
              })
          .addOnFailureListener(
              e -> {
                Timber.e(e, "Failed to request orientation updates.");
                activityCompassBinding.lockCompassControl.setChecked(true);
                activityCompassBinding.lockCompassControl.setEnabled(false);
                applyCompassControls();
              });
    }
  }

  private void rotateCompassToLockedPosition() {
    rotateCompass(
        activityCompassBinding.southAtTopControl.isChecked() ? 180.0f : 0.0f, /* listener= */ null);
  }

  /**
   * Executes an animated sweep from the old compass rotation to the new one, and updates the old
   * one to the new value to prepare for the next update.
   */
  private void rotateCompass(
      float compassAzimuthDeg, @Nullable AnimatorListener listener) {

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

    if (listener != null) {
      compassAnimator.addListener(listener);
    }

    compassAnimator.setAutoCancel(true);
    compassAnimator.start();

    lastRotationDeg = desiredRotationDeg;
  }

  /** Returns true iff noon and nadir are either both in the north or both in the south. */
  private static boolean noonNadirOverlap(SunEvent noonEvent, SunEvent nadirEvent) {
    // If we're starting near north (0 az), we'll end up near 90. If we start off near south
    // (180 az), we'll end up near -90.
    float noonOffsetDeg = DirectionUtil.zeroCenterDeg(noonEvent.getAzimuthDeg() + 90.0);
    float nadirOffsetDeg = DirectionUtil.zeroCenterDeg(nadirEvent.getAzimuthDeg() + 90.0);
    return Math.abs(noonOffsetDeg - nadirOffsetDeg) < 45.0;
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

    /** Redraws this event at the appropriate radius and alpha for its saved state. */
    private void redraw() {
      setInsetPct(insetPct);
    }
  }
}
