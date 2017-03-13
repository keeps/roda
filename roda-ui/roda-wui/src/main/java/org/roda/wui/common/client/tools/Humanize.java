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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

import config.i18n.client.ClientMessages;

public class Humanize {

  private Humanize() {

  }

  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

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

  public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");
  public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
  public static final DateTimeFormat DATE_TIME_MS_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS");

  public static Long parseFileSize(String size, String unit) {
    Long ret = null;
    if (size != null && !size.isEmpty()) {
      String trimmedSize = size.trim();
      if (unit.equals(PETABYTES)) {
        ret = Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_PETABYTES);
      } else if (unit.equals(TERABYTES)) {
        ret = Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_TERABYTES);
      } else if (unit.equals(GIGABYTES)) {
        ret = Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_GIGABYTES);
      } else if (unit.equals(MEGABYTES)) {
        ret = Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_MEGABYTES);
      } else if (unit.equals(KILOBYTES)) {
        ret = Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_KILOBYTES);
      } else if (unit.equals(BYTES)) {
        ret = Long.parseLong(trimmedSize);
      } else {
        throw new IllegalArgumentException(trimmedSize);
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
    } else if (dateInitial == null) {
      ret = extendedDate ? messages.titleDatesNoInitial(dateFinal) : messages.simpleDatesNoInitial(dateFinal);
    } else {
      ret = extendedDate ? messages.titleDates(dateInitial, dateFinal) : messages.simpleDates(dateInitial, dateFinal);
    }

    return ret;
  }

  public enum DHMSFormat {
    LONG, SHORT;
  }

  public static String durationInDHMS(Date start, Date end, DHMSFormat format) {
    Date endDate = end == null ? new Date() : end;
    long duration = endDate.getTime() - start.getTime();
    return DHMSFormat.LONG.equals(format) ? durationMillisToLongDHMS(duration) : durationMillisToShortDHMS(duration);
  }

  public final static long ONE_SECOND = 1000;
  public final static long SECONDS = 60;

  public final static long ONE_MINUTE = ONE_SECOND * 60;
  public final static long MINUTES = 60;

  public final static long ONE_HOUR = ONE_MINUTE * 60;
  public final static long HOURS = 24;

  public final static long ONE_DAY = ONE_HOUR * 24;

  /**
   * converts time (in milliseconds) to human-readable format "<dd:>hh:mm:ss"
   */
  public static String durationMillisToShortDHMS(long duration) {
    long d = duration;
    int millis = (int) (d % ONE_SECOND);
    d /= ONE_SECOND;
    int seconds = (int) (d % SECONDS);
    d /= SECONDS;
    int minutes = (int) (d % MINUTES);
    d /= MINUTES;
    int hours = (int) (d % HOURS);
    int days = (int) (d / HOURS);

    String ret;

    if (days > 0) {
      ret = messages.durationDHMSShortDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      ret = messages.durationDHMSShortHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      ret = messages.durationDHMSShortMinutes(minutes, seconds);
    } else if (seconds > 0) {
      ret = messages.durationDHMSShortSeconds(seconds);
    } else {
      ret = messages.durationDHMSShortMillis(millis);
    }

    return ret;
  }

  /**
   * converts time (in milliseconds) to human-readable format "<w> days,
   * <x> hours, <y> minutes and (z) seconds"
   */
  public static String durationMillisToLongDHMS(long duration) {
    long d = duration;
    d /= ONE_SECOND;
    int seconds = (int) (d % SECONDS);
    d /= SECONDS;
    int minutes = (int) (d % MINUTES);
    d /= MINUTES;
    int hours = (int) (d % HOURS);
    int days = (int) (d / HOURS);

    String ret;

    if (days > 0) {
      ret = messages.durationDHMSLongDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      ret = messages.durationDHMSLongHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      ret = messages.durationDHMSLongMinutes(minutes, seconds);
    } else {
      ret = messages.durationDHMSLongSeconds(seconds);
    }

    return ret;

  }

  /**
   * Format a value in milliseconds as a string
   * 
   * @param value
   * @param showMillis
   * @return a formatted string for time duration
   */
  public static String formatValueMilliseconds(long value, boolean showMillis) {
    String ret;

    long hours = value / 3600000;
    long minutes = (value % 3600000) / 60000;
    long seconds = ((value % 3600000) % 60000) / 1000;
    long millis = value % 1000;

    NumberFormat numberFormat = NumberFormat.getFormat("00");
    NumberFormat millisFormat = NumberFormat.getFormat("000");

    if (showMillis) {
      ret = numberFormat.format(hours) + ":" + numberFormat.format(minutes) + ":" + numberFormat.format(seconds) + "."
        + millisFormat.format(millis);
    } else {
      ret = numberFormat.format(hours) + ":" + numberFormat.format(minutes) + ":" + numberFormat.format(seconds);
    }

    return ret;
  }

  public static String formatDate(Date date) {
    return DATE_FORMAT.format(date);
  }

  public static String formatDateTime(Date date) {
    return DATE_TIME_FORMAT.format(date);
  }

  public static String formatDateTimeMs(Date date) {
    return DATE_TIME_MS_FORMAT.format(date);
  }

}
