package org.onereed.helios.common;

import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.function.Consumer;

/** Utility methods for working with resources. */
public class ResourceUtil {

  /**
   * Creates a {@link TypedArray} from {@code resources} and {@code arrayResourceId}, and gives
   * it to {@code consumer}. This method takes care of recycling the {@link TypedArray} after use.
   */
  public static void withTypedArray(
      Resources resources, int arrayResourceId, Consumer<TypedArray> consumer) {

    TypedArray typedArray = null;
    try {
      typedArray = resources.obtainTypedArray(arrayResourceId);
      consumer.accept(typedArray);
    } finally {
      if (typedArray != null) {
        typedArray.recycle();
      }
    }
  }

  private ResourceUtil() {}
}
