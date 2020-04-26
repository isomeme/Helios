package org.onereed.helios.common;

/** Static utility methods for logging. */
public final class LogUtil {

  private static final String PREFIX = "helios_";

  public static String makeTag(Class<?> clazz) {
    return PREFIX + clazz.getSimpleName();
  }

  private LogUtil() {}
}
