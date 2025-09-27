package org.onereed.helios.common;

import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * Simplifies access to {@link TypedArray} resource data which will all be retrieved from the same
 * index.
 */
public class TypedArrayAccessor {

  private final Resources resources;
  private final int index;

  public static TypedArrayAccessor create(Resources resources, int index) {
    return new TypedArrayAccessor(resources, index);
  }

  private TypedArrayAccessor(Resources resources, int index) {
    this.resources = resources;
    this.index = index;
  }

  public int getColor(int arrayResId) {
    return lookup(arrayResId, TypedArray::getColor);
  }

  public int getResourceId(int arrayResId) {
    return lookup(arrayResId, TypedArray::getResourceId);
  }

  private <T> T lookup(int arrayResId, Accessor<T> accessor) {
    try (TypedArray typedArray = resources.obtainTypedArray(arrayResId)) {
      return accessor.access(typedArray, index, /* defValue= */ 0);
    }
  }
  
  @FunctionalInterface
  private interface Accessor<T> {
    T access(TypedArray typedArray, int index, int defValue);
  }
}
