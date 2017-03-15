/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.antivirus;

/**
 * This class contains the result of a virus check.
 * 
 * @author Rui Castro
 */
public class VirusCheckResult {

  /**
   * <code>true</code> if no virus/warning was found, <code>false</code>
   * otherwise.
   */
  private boolean clean = false;

  /**
   * The output of the anti-virus tool during process.
   */
  private String report = null;

  /**
   * Constructs an empty {@link VirusCheckResult}.
   */
  public VirusCheckResult() {
    // do nothing
  }

  /**
   * Constructs a new {@link VirusCheckResult} with the given parameters.
   * 
   * @param clean
   *          <code>true</code> if no virus/warning was found,
   *          <code>false</code> otherwise.
   * @param report
   *          the output of the anti-virus tool.
   */
  public VirusCheckResult(boolean clean, String report) {
    setClean(clean);
    setReport(report);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "( clean=" + isClean() + ", report=" + getReport() + " )";
  }

  /**
   * @return the clean
   */
  public boolean isClean() {
    return clean;
  }

  /**
   * @param clean
   *          the clean to set
   */
  public void setClean(boolean clean) {
    this.clean = clean;
  }

  /**
   * @return the report
   */
  public String getReport() {
    return report;
  }

  /**
   * @param report
   *          the report to set
   */
  public void setReport(String report) {
    this.report = report;
  }

}
