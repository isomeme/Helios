package org.onereed.helios.common;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.function.Function;

/**
 * Simplifies access to {@link TypedArray} resource data which will all be retrieved from the same
 * index..
 */
public class TypedArrayAccessor {

  private final Resources resources;
  private final int index;

  public static TypedArrayAccessor create(Context context, int index) {
    return new TypedArrayAccessor(context.getResources(), index);
  }

  private TypedArrayAccessor(Resources resources, int index) {
    this.resources = resources;
    this.index = index;
  }

  public int getColor(int arrayResId) {
    return lookup(arrayResId, typedArray -> typedArray.getColor(index, /* defValue= */ 0));
  }

  public int getResourceId(int arrayResId) {
    return lookup(arrayResId, typedArray -> typedArray.getResourceId(index, /* defValue= */ 0));
  }

  private <T> T lookup(int arrayResId, Function<TypedArray, T> function) {
    TypedArray typedArray = null;
    try {
      typedArray = resources.obtainTypedArray(arrayResId);
      return function.apply(typedArray);
    } finally {
      if (typedArray != null) {
        typedArray.recycle();
      }
    }
  }
}
