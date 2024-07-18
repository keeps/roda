package org.roda.core.data.v2.log;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ClientLogCreateRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -560864427759571057L;

  private Level level;
  private String className;
  private String message;
  private Throwable throwable;

  public enum Level {
    DEBUG, ERROR, FATAL, INFO, TRACE, WARN
  }

  public ClientLogCreateRequest() {
    // empty constructor
  }

  public ClientLogCreateRequest(Level level, String className, String message) {
    this.level = level;
    this.className = className;
    this.message = message;
  }

  public ClientLogCreateRequest(Level level, String className, String message, Throwable throwable) {
    this.level = level;
    this.className = className;
    this.message = message;
    this.throwable = throwable;
  }

  public Level getLevel() {
    return level;
  }

  public String getClassName() {
    return className;
  }

  public String getMessage() {
    return message;
  }

  public Throwable getThrowable() {
    return throwable;
  }
}
