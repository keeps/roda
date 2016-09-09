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

import config.i18n.client.ClientMessages;

public class Humanize {

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

  public enum DHMSFormat {
    LONG, SHORT;
  }

  public static String durationInDHMS(Date start, Date end, DHMSFormat format) {
    if (end == null) {
      end = new Date();
    }
    long duration = end.getTime() - start.getTime();

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
    int millis = (int) (duration % ONE_SECOND);
    duration /= ONE_SECOND;
    int seconds = (int) (duration % SECONDS);
    duration /= SECONDS;
    int minutes = (int) (duration % MINUTES);
    duration /= MINUTES;
    int hours = (int) (duration % HOURS);
    int days = (int) (duration / HOURS);

    String ret;

    if (days > 0) {
      ret = messages.durationDHMSShortDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      ret = messages.durationDHMSShortHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      ret = messages.durationDHMSShortMinutes(minutes, seconds);
    } else if(seconds > 0){
      ret = messages.durationDHMSShortSeconds(seconds);
    } else {
      ret = messages.durationDHMSShortMillis(millis);
    }

    return ret;

  }

  /**
   * converts time (in milliseconds) to human-readable format
   * "<w> days, <x> hours, <y> minutes and (z) seconds"
   */
  public static String durationMillisToLongDHMS(long duration) {
    duration /= ONE_SECOND;
    int seconds = (int) (duration % SECONDS);
    duration /= SECONDS;
    int minutes = (int) (duration % MINUTES);
    duration /= MINUTES;
    int hours = (int) (duration % HOURS);
    int days = (int) (duration / HOURS);

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

}
