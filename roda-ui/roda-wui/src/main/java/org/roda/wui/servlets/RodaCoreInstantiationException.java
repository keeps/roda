package org.roda.wui.servlets;

import javax.servlet.ServletException;

public class RodaCoreInstantiationException extends ServletException {

  /**
   * 
   */
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
