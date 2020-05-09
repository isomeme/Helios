package org.onereed.helios.logger;

import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Java-logger based implementation of {@link GeneralLogger}, intended for use only in tests.
 */
public class JavaLogger implements GeneralLogger {

  private final Logger logger = Logger.getLogger("test");

  public static JavaLogger create() {
    return new JavaLogger();
  }

  private JavaLogger() {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.FINEST);
    logger.addHandler(handler);
    logger.setLevel(Level.FINEST);
  }

  @Override
  public void verbose(String tag, String message) {
    logger.finest(messageSupplier(tag, message));
  }

  @Override
  public void debug(String tag, String message) {
    logger.fine(messageSupplier(tag, message));
  }

  @Override
  public void info(String tag, String message) {
    logger.info(messageSupplier(tag, message));
  }

  @Override
  public void warning(String tag, String message) {
    logger.warning(messageSupplier(tag, message));
  }

  @Override
  public void error(String tag, String message) {
    logger.severe(messageSupplier(tag, message));
  }

  @Override
  public void error(String tag, String message, Throwable tr) {
    logger.log(Level.SEVERE, tr, messageSupplier(tag, message));
  }

  private static Supplier<String> messageSupplier(String tag, String message) {
    return () -> String.format("<%s> %s", tag, message);
  }
}
