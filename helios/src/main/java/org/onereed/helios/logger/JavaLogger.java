package org.onereed.helios.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Java {@link Logger} based implementation of {@link GeneralLogger}, intended for use only in
 * tests.
 */
class JavaLogger implements GeneralLogger {

  private final Logger logger = Logger.getLogger("test");

  private JavaLogger() {
    var handler = new ConsoleHandler();
    handler.setLevel(Level.FINEST);
    logger.addHandler(handler);
    logger.setLevel(Level.FINEST);
  }

  static JavaLogger create() {
    return new JavaLogger();
  }

  private static String formatEntry(String tag, String message) {
    return String.format("<%s> %s", tag, message);
  }

  @Override
  public void debug(String tag, String message) {
    logger.fine(formatEntry(tag, message));
  }

  @Override
  public void warning(String tag, String message) {
    logger.warning(formatEntry(tag, message));
  }

  @Override
  public void error(String tag, String message) {
    logger.severe(formatEntry(tag, message));
  }

  @Override
  public void error(String tag, String message, Throwable t) {
    logger.log(Level.SEVERE, formatEntry(tag, message), t);
  }
}
