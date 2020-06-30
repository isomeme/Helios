package org.onereed.helios.common;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Static utility methods for working with {@link TypedArray}. For single-index access, see {@link
 * TypedArrayAccessor}.
 */
public class TypedArrayUtil {

  public static String[] getStringArray(Context context, int resId) {
    TypedArray typedArray = null;
    try {
      typedArray = context.getResources().obtainTypedArray(resId);
      String[] values = new String[typedArray.length()];
      for (int i = 0; i < typedArray.length(); ++i) {
        values[i] = typedArray.getString(i);
      }
      return values;
    } finally {
      if (typedArray != null) {
        typedArray.recycle();
      }
    }
  }

  private TypedArrayUtil() {}
}
