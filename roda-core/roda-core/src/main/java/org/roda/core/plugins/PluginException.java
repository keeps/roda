/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.jobs.Report;

/**
 * Thrown to indicate that some went wrong inside a {@link Plugin}.
 * 
 * @author Rui Castro
 */
public class PluginException extends RODAException {
  private static final long serialVersionUID = -4913064017146606623L;

  private Report report = null;

  /**
   * Constructs a new {@link PluginException}.
   */
  public PluginException() {
  }

  /**
   * Constructs a new {@link PluginException} with the given error message.
   * 
   * @param message
   *          the error message.
   */
  public PluginException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@link PluginException} with the given cause exception.
   * 
   * @param cause
   *          the cause exception.
   */
  public PluginException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link PluginException} with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public PluginException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new {@link PluginException} with the given error message and
   * cause exception.
   * 
   * @param message
   *          the error message.
   * @param cause
   *          the cause exception.
   */
  public PluginException(String message, Throwable cause, Report report) {
    this(message, cause);
    setReport(report);
  }

  /**
   * @return the report
   */
  public Report getReport() {
    return report;
  }

  /**
   * @param report
   *          the report to set
   */
  public void setReport(Report report) {
    this.report = report;
  }

}
