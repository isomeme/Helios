package org.onereed.helios.logger;

/**
 * Static proxy for a {@link GeneralLogger} instance.
 */
public class AppLogger {

  private static GeneralLogger delegate = null;

  public static void init(GeneralLogger generalLogger) {
    delegate = generalLogger;
  }

  public static void verbose(String tag, String message) {
    delegate.verbose(tag, message);
  }

  public static void debug(String tag, String message) {
    delegate.debug(tag, message);
  }

  public static void info(String tag, String message) {
    delegate.info(tag, message);
  }

  public static void warning(String tag, String message) {
    delegate.warning(tag, message);
  }

  public static void error(String tag, String message) {
    delegate.error(tag, message);
  }

  public static void error(String tag, String message, Throwable tr) {
    delegate.error(tag, message, tr);
  }

  public static void silent(String tag, String message) {
    delegate.silent(tag, message);
  }

  private AppLogger() {
  }
}
