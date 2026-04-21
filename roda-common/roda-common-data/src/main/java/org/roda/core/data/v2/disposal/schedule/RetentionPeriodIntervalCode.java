/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.disposal.schedule;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public enum RetentionPeriodIntervalCode {
  NO_RETENTION_PERIOD, DAYS, WEEKS, MONTHS, YEARS;

  public static RetentionPeriodIntervalCode getDefault() {
    return NO_RETENTION_PERIOD;
  }
}
