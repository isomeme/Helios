package org.onereed.helios.common;

import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.function.Consumer;

/** Utility methods for dealing with {@code LayoutParams}. */
public class LayoutParamsUtil {

  /** Sets the radius of a view that is using circle-constrained layout. */
  public static void changeConstraintLayoutCircleRadius(View view, int radius) {
    changeConstraintLayoutParams(view, layoutParams -> layoutParams.circleRadius = radius);
  }

  /** Sets the angle of a view that is using circle-constrained layout. */
  public static void changeConstraintLayoutCircleAngle(View view, float angle) {
    changeConstraintLayoutParams(view, layoutParams -> layoutParams.circleAngle = angle);
  }

  /**
   * Obtains the {@link ConstraintLayout.LayoutParams} instance from {@code view}, passes it to
   * {@code modifier} to make changes, and applies the changed layout to the view.
   */
  private static void changeConstraintLayoutParams(
      View view, Consumer<ConstraintLayout.LayoutParams> modifier) {

    var layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
    modifier.accept(layoutParams);
    view.setLayoutParams(layoutParams);
  }

  private LayoutParamsUtil() {}
}
