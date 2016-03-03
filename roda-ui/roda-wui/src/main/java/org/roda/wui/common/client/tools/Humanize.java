/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import config.i18n.client.BrowseMessages;

public class Humanize {

  private static BrowseMessages messages = (BrowseMessages) GWT.create(BrowseMessages.class);

  public static final String BYTES = "B";
  public static final String KILOBYTES = "KB";
  public static final String MEGABYTES = "MB";
  public static final String GIGABYTES = "GB";
  public static final String TERABYTES = "TB";
  public static final String PETABYTES = "PB";

  public static final String[] UNITS = new String[] {BYTES, KILOBYTES, MEGABYTES, GIGABYTES, TERABYTES, PETABYTES};

  public static final double BYTES_IN_KILOBYTES = 1024L;
  public static final double BYTES_IN_MEGABYTES = 1048576L;
  public static final double BYTES_IN_GIGABYTES = 1073741824L;
  public static final double BYTES_IN_TERABYTES = 1099511627776L;
  public static final double BYTES_IN_PETABYTES = 1125899906842624L;

  public static final double[] BYTES_IN_UNITS = {1, BYTES_IN_KILOBYTES, BYTES_IN_MEGABYTES, BYTES_IN_GIGABYTES,
    BYTES_IN_TERABYTES, BYTES_IN_PETABYTES};

  protected static final NumberFormat SMALL_NUMBER_FORMAT = NumberFormat.getFormat("0.#");
  protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getFormat("#");

  public static Long parseFileSize(String size, String unit) {
    Long ret = null;
    if (size != null && !size.isEmpty()) {
      size = size.trim();
      if (unit.equals(PETABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_PETABYTES);
      } else if (unit.equals(TERABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_TERABYTES);
      } else if (unit.equals(GIGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_GIGABYTES);
      } else if (unit.equals(MEGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_MEGABYTES);
      } else if (unit.equals(KILOBYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_KILOBYTES);
      } else if (unit.equals(BYTES)) {
        ret = Long.parseLong(size);
      } else {
        throw new IllegalArgumentException(size);
      }
    }
    return ret;
  }

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0 B";
    }
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
  }

  public static String getDatesText(Date dateInitial, Date dateFinal, boolean extendedDate) {
    String ret;

    if (dateInitial == null && dateFinal == null) {
      ret = extendedDate ? messages.titleDatesEmpty() : messages.simpleDatesEmpty();
    } else if (dateInitial != null && dateFinal == null) {
      ret = extendedDate ? messages.titleDatesNoFinal(dateInitial) : messages.simpleDatesNoFinal(dateInitial);
    } else if (dateInitial == null && dateFinal != null) {
      ret = extendedDate ? messages.titleDatesNoInitial(dateFinal) : messages.simpleDatesNoInitial(dateFinal);
    } else {
      ret = extendedDate ? messages.titleDates(dateInitial, dateFinal) : messages.simpleDates(dateInitial, dateFinal);
    }

    return ret;
  }
}
