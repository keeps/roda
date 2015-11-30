package org.roda.core.data.common;

public class StoragePathException extends RODAException {

  private static final long serialVersionUID = -3242810946238751526L;

  public StoragePathException() {
    super();
  }

  public StoragePathException(String message) {
    super(message);
  }

  public StoragePathException(String message, GenericException e) {
    super(message, e);
  }
}
