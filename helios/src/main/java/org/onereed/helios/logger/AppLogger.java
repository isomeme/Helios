package org.onereed.helios.logger;

import com.google.errorprone.annotations.FormatString;
import java.util.Locale;

/**
 * Static proxy for a {@link GeneralLogger} instance. Configured to use {@link NullLogger} by
 * default, which does no logging at all and is intended for production builds. When running a debug
 * app on a connected device or emulator, call {@link #useAndroidLogger()} to enable standard
 * Android logging using {@link android.util.Log}. Standalone tests can call {@link
 * #useJavaLogger()} to use a {@link java.util.logging.Logger} instead, which both avoids using
 * {@link android.util.Log} (which is not allowed in standalone tests) and writes log output to the
 * test console.
 *
 * <p>The proxy methods in this class allow message string argument substitution using {@link
 * String#format(Locale, String, Object...)}.
 */
public class AppLogger {

  private static GeneralLogger delegate = NullLogger.create();

  private AppLogger() {}

  public static void useAndroidLogger() {
    delegate = AndroidLogger.create();
  }

  public static void useJavaLogger() {
    delegate = JavaLogger.create();
  }

  public static void debug(String tag, @FormatString String message, Object... args) {
    delegate.debug(tag, format(message, args));
  }

  public static void warning(String tag, @FormatString String message, Object... args) {
    delegate.warning(tag, format(message, args));
  }

  public static void error(String tag, @FormatString String message, Object... args) {
    delegate.error(tag, format(message, args));
  }

  public static void error(String tag, Throwable t, @FormatString String message, Object... args) {
    delegate.error(tag, format(message, args), t);
  }

  private static String format(@FormatString String message, Object[] args) {
    return (args.length == 0) ? message : String.format(Locale.ENGLISH, message, args);
  }
}
