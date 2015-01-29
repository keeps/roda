package pt.gov.dgarq.roda.wui.common.client;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.wui.common.client.widgets.MessagePopup;
import pt.gov.dgarq.roda.wui.main.client.CasForwardDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	private static final int CURRENT_LOG_LEVEL = WARN;

	private static boolean SHOW_ERROR_MESSAGES = false;

	private String classname;
	

	/**
	 * Create a new client logger
	 */
	public ClientLogger() {
	}

	/**
	 * Create a new client logger
	 * 
	 * @param classname
	 */
	public ClientLogger(String classname) {
		this.classname = classname;
	}

	/**
	 * Set the uncaught exception handler
	 */
	public static void setUncaughtExceptionHandler() {
		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			ClientLogger logger = new ClientLogger("Uncaught");

			public void onUncaughtException(Throwable e) {
				logger.fatal("Uncaught Exception", e);
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
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().trace(classname, message,
					errorcallback);
		}
	}

	/**
	 * Log a trace message and error
	 * 
	 * @param message
	 * @param error
	 */
	public void trace(final String message, final Throwable error) {
		if (CURRENT_LOG_LEVEL <= TRACE) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().trace(classname, message,
					error, errorcallback);
		}
	}

	/**
	 * Log a debug message
	 * 
	 * @param message
	 */
	public void debug(final String message) {
		if (CURRENT_LOG_LEVEL <= DEBUG) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().debug(classname, message,
					errorcallback);
		}
	}

	/**
	 * Log a debug message and error
	 * 
	 * @param object
	 * @param error
	 */
	public void debug(final String object, final Throwable error) {
		if (CURRENT_LOG_LEVEL <= DEBUG) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(object, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().debug(classname, object,
					error, errorcallback);
		}
	}

	/**
	 * Log a information message
	 * 
	 * @param message
	 */
	public void info(final String message) {
		if (CURRENT_LOG_LEVEL <= INFO) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().info(classname, message,
					errorcallback);
		}
	}

	/**
	 * Log an information message and error
	 * 
	 * @param message
	 * @param error
	 */
	public void info(final String message, final Throwable error) {
		if (CURRENT_LOG_LEVEL <= INFO) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().info(classname, message,
					error, errorcallback);
		}
	}

	/**
	 * Log a warning message
	 * 
	 * @param message
	 */
	public void warn(final String message) {
		if (CURRENT_LOG_LEVEL <= WARN) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().warn(classname, message,
					errorcallback);
		}
	}

	/**
	 * Log a warning message and error
	 * 
	 * @param message
	 * @param error
	 */
	public void warn(final String message, final Throwable error) {
		if (CURRENT_LOG_LEVEL <= WARN) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			ClientLoggerService.Util.getInstance().warn(classname, message,
					error, errorcallback);
		}
	}

	/**
	 * Log an error message
	 * 
	 * @param message
	 */
	public void error(final String message) {
		if (CURRENT_LOG_LEVEL <= ERROR) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			GWT.log(message, null);
			ClientLoggerService.Util.getInstance().error(classname, message,
					errorcallback);

			if (SHOW_ERROR_MESSAGES) {
				MessagePopup.showError(message);
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
			String windowLocation = Window.Location.getHref();
			CasForwardDialog cfd = new CasForwardDialog(windowLocation);
			cfd.show(); 
		} else if (CURRENT_LOG_LEVEL <= ERROR) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			GWT.log(message, error);
			ClientLoggerService.Util.getInstance().error(classname, message,
					error, errorcallback);
			if (SHOW_ERROR_MESSAGES) {
				MessagePopup.showError(message, error.getMessage()
						+ (error.getCause() != null ? "\nCause: "
								+ error.getCause().getMessage() : ""));
			}
		}
	}

	/**
	 * Log a fatal message
	 * 
	 * @param message
	 */
	public void fatal(final String message) {
		if (CURRENT_LOG_LEVEL <= FATAL) {
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, null);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			GWT.log(message, null);
			ClientLoggerService.Util.getInstance().fatal(classname, message,
					errorcallback);
			if (SHOW_ERROR_MESSAGES) {
				MessagePopup.showError(message);
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
			AsyncCallback<Void> errorcallback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					GWT.log(message, error);
					GWT.log("Error while logging another error", caught);
				}

				public void onSuccess(Void result) {
					// do nothing
				}
			};
			GWT.log(message, error);
			ClientLoggerService.Util.getInstance().fatal(classname, message,
					error, errorcallback);

			if (SHOW_ERROR_MESSAGES) {
				MessagePopup.showError(message, error.getMessage()
						+ (error.getCause() != null ? "\nCause: "
								+ error.getCause().getMessage() : ""));
			}
		}
	}

	/**
	 * Get logging class name
	 * 
	 * @return the name of the class being logged
	 */
	public String getClassname() {
		return classname;
	}

	/**
	 * Set class name
	 * 
	 * @param classname
	 *            the name of class being logged
	 */
	public void setClassname(String classname) {
		this.classname = classname;
	}

}
