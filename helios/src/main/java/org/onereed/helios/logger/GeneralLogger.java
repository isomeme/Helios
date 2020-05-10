package org.onereed.helios.logger;

/**
 * Interface for a general logger. Implementations can use different logging frameworks. This is a
 * workaround for the problem that the Android static {@code Log} implementation cannot be used in
 * classes ander standalone testing.
 */
interface GeneralLogger {

  void verbose(String tag, String message);

  void debug(String tag, String message);

  void info(String tag, String message);

  void warning(String tag, String message);

  void error(String tag, String message);

  void error(String tag, String message, Throwable tr);

  default void silent(String tag, String message) {
    // Do nothing.
  }
}
