/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.servlets;

import jakarta.servlet.ServletException;

import java.io.Serial;

public class RodaCoreInstantiationException extends ServletException {
  @Serial
  private static final long serialVersionUID = -271098088066975713L;

  public RodaCoreInstantiationException() {
    super();
  }

  public RodaCoreInstantiationException(String message, Throwable rootCause) {
    super(message, rootCause);
  }

  public RodaCoreInstantiationException(String message) {
    super(message);
  }

  public RodaCoreInstantiationException(Throwable rootCause) {
    super(rootCause);
  }

}
