package org.onereed.helios.common;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.FormatString;

import java.util.Locale;

/**
 * A {@link VerifyException} with message formatting.
 */
public class FormattedVerifyException extends VerifyException {

  public FormattedVerifyException(@FormatString String message, Object... args) {
    super(String.format(Locale.ENGLISH, message, args));
  }

  /**
   * Note argument order change from superclass.
   */
  public FormattedVerifyException(Throwable cause, @FormatString String message, Object... args) {
    super(String.format(Locale.ENGLISH, message, args), cause);
  }
}
