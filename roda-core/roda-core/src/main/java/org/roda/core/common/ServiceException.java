/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

public class ServiceException extends Exception {

  private static final long serialVersionUID = -3970536792438366410L;

  public static final int BAD_REQUEST = 400;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int ALREADY_EXISTS = 409;
  public static final int INTERNAL_SERVER_ERROR = 500;
  public static final int NOT_IMPLEMENTED = 501;

  private final int code;

  public ServiceException(String message, int code) {
    super(message);
    this.code = code;
  }

  public ServiceException(String message, int code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

}
