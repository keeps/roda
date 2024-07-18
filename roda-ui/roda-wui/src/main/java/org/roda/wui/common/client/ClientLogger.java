/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.log.ClientLogCreateRequest;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.logging.client.DevelopmentModeLogHandler;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Luis Faria
 *
 */
public class ClientLogger implements IsSerializable {
  // log levels
  /**
   * Trace level
   */
  public static final int TRACE = 0;

  /**
   * Debug level
   */
  public static final int DEBUG = 1;

  /**
   * Information level
   */
  public static final int INFO = 2;

  /**
   * Warning level
   */
  public static final int WARN = 3;

  /**
   * Error level
   */
  public static final int ERROR = 4;

  /**
   * Fatal error level
   */
  public static final int FATAL = 5;

  private static final int CURRENT_LOG_LEVEL = TRACE;
  private static final boolean SHOW_ERROR_MESSAGES = false;
  private static final String LOGGING_ERROR = "Error while logging another error";

  private final Logger logger;
  private String className;

  /**
   * Create a new client logger
   */
  public ClientLogger() {
    logger = Logger.getLogger("");
    logger.addHandler(new DevelopmentModeLogHandler());
  }

  /**
   * Create a new client logger
   *
   * @param className
   */
  public ClientLogger(String className) {
    this.className = className;
    logger = Logger.getLogger(className);
    logger.addHandler(new DevelopmentModeLogHandler());
  }

  /**
   * Set the uncaught exception handler
   */
  public static void setUncaughtExceptionHandler() {
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      final ClientLogger clientlogger = new ClientLogger("Uncaught");

      @Override
      public void onUncaughtException(Throwable e) {
        clientlogger.fatal("Uncaught Exception: [" + e.getClass().getName() + "] " + e.getMessage(), e);
      }

    });
  }

  /**
   * Log a trace message
   *
   * @param message
   */
  public void trace(final String message) {
    if (CURRENT_LOG_LEVEL <= TRACE) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.TRACE, className,
        message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });
    }
  }

  /**
   * Log a debug message
   *
   * @param message
   */
  public void debug(final String message) {
    if (CURRENT_LOG_LEVEL <= DEBUG) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.DEBUG, className,
        message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });
    }
  }

  /**
   * Log a information message
   *
   * @param message
   */
  public void info(final String message) {
    if (CURRENT_LOG_LEVEL <= INFO) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.INFO, className,
        message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });
    }
  }

  /**
   * Log a warning message
   *
   * @param message
   */
  public void warn(final String message) {
    if (CURRENT_LOG_LEVEL <= WARN) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.WARN, className,
        message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });
    }
  }

  /**
   * Log an error message
   *
   * @param message
   */
  public void error(final String message) {
    if (CURRENT_LOG_LEVEL <= ERROR) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.ERROR, className,
        message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });

      if (SHOW_ERROR_MESSAGES) {
        Toast.showError(message);
      }
    }
  }

  /**
   * Log an error message and error
   *
   * @param message
   * @param error
   */
  public void error(final String message, final Throwable error) {
    if (error instanceof AuthorizationDeniedException) {
      UserLogin.getInstance().showSuggestLoginDialog();
    } else if (CURRENT_LOG_LEVEL <= ERROR && error != null) {
      String errorDetails = extractErrorDetails(error);

      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.ERROR, className,
        message + ", error: " + errorDetails, error);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {

        if (throwable != null) {
          logger.log(Level.SEVERE, message, error);
          logger.log(Level.SEVERE, LOGGING_ERROR, throwable);
        } else {
          logger.log(Level.SEVERE, message, error);
        }
      });

      if (SHOW_ERROR_MESSAGES) {
        Toast.showError(message,
          error.getMessage() + (error.getCause() != null ? "\nCause: " + error.getCause().getMessage() : ""));
      }
    }
  }

  private String extractErrorDetails(final Throwable error) {

    StringBuilder b = new StringBuilder();

    Throwable e = error;
    while (e != null) {
      b.append("[").append(e.getClass().getName()).append("] ").append(e.getMessage());
      StackTraceElement[] stackTrace = e.getStackTrace();
      if (stackTrace != null) {
        for (StackTraceElement stackTraceElement : stackTrace) {
          b.append("\n").append(stackTraceElement.toString());
        }
      }

      e = e.getCause();
    }

    return b.toString();
  }

  /**
   * Log a fatal message
   *
   * @param message
   */
  public void fatal(final String message) {
    if (CURRENT_LOG_LEVEL <= FATAL) {
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.FATAL, className,
        message);
      logger.log(Level.SEVERE, message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });

      if (SHOW_ERROR_MESSAGES) {
        Toast.showError(message);
      }
    }
  }

  /**
   * Log a fatal message and error
   *
   * @param message
   * @param error
   */
  public void fatal(final String message, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= FATAL) {
      String errorDetails = extractErrorDetails(error);
      Services services = new Services("Log client message", "log");
      ClientLogCreateRequest request = new ClientLogCreateRequest(ClientLogCreateRequest.Level.FATAL, className,
        message + ", error: " + errorDetails, error);
      logger.log(Level.SEVERE, message);
      services.clientLoggerResource(s -> s.log(request)).whenComplete((object, throwable) -> {
        if (throwable != null) {
          responseOnFailure(message, null, throwable);
        }
      });

      if (SHOW_ERROR_MESSAGES) {
        Toast.showError(message,
          error.getMessage() + (error.getCause() != null ? "\nCause: " + error.getCause().getMessage() : ""));
      }
    }
  }

  /**
   * Get logging class name
   *
   * @return the name of the class being logged
   */
  public String getClassName() {
    return className;
  }

  /**
   * Set class name
   *
   * @param className
   *          the name of class being logged
   */
  public void setClassName(String className) {
    this.className = className;
  }

  public void responseOnFailure(String message, Throwable error, Throwable caught) {
    GWT.log(message, error);
    GWT.log(LOGGING_ERROR, caught);
  }
}
