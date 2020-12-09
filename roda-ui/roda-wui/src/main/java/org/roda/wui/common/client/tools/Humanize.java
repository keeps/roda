/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.roda.core.data.common.RodaConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.TimeZone;

import config.i18n.client.ClientMessages;

public class Humanize {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final long ONE_SECOND = 1000;
  public static final long SECONDS = 60;
  public static final long ONE_MINUTE = ONE_SECOND * 60;
  public static final long MINUTES = 60;
  public static final long ONE_HOUR = ONE_MINUTE * 60;
  public static final long HOURS = 24;
  public static final long ONE_DAY = ONE_HOUR * 24;

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
  public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss z");

  public static final boolean FORMAT_UTC = false;

  private Humanize() {
    // do nothing
  }

  public static Long parseFileSize(String size, String unit) {
    if (size != null && !size.isEmpty()) {
      String trimmedSize = size.trim();
      if (unit.equals(PETABYTES)) {
        return Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_PETABYTES);
      } else if (unit.equals(TERABYTES)) {
        return Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_TERABYTES);
      } else if (unit.equals(GIGABYTES)) {
        return Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_GIGABYTES);
      } else if (unit.equals(MEGABYTES)) {
        return Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_MEGABYTES);
      } else if (unit.equals(KILOBYTES)) {
        return Math.round(Double.parseDouble(trimmedSize) * BYTES_IN_KILOBYTES);
      } else if (unit.equals(BYTES)) {
        return Long.parseLong(trimmedSize);
      } else {
        throw new IllegalArgumentException(trimmedSize);
      }
    }

    return null;
  }

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0 B";
    }

    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
  }

  public static String getDatesText(Date dateInitial, Date dateFinal, boolean extendedDate) {
    if (dateInitial == null && dateFinal == null) {
      return extendedDate ? messages.titleDatesEmpty() : messages.simpleDatesEmpty();
    } else if (dateInitial != null && dateFinal == null) {
      String dateInitialString = formatDate(dateInitial, extendedDate);
      return extendedDate ? messages.titleDatesNoFinal(dateInitialString)
        : messages.simpleDatesNoFinal(dateInitialString);
    } else if (dateInitial == null) {
      String dateFinalString = formatDate(dateFinal, extendedDate);
      return extendedDate ? messages.titleDatesNoInitial(dateFinalString)
        : messages.simpleDatesNoInitial(dateFinalString);
    } else {
      String dateInitialString = formatDate(dateInitial, extendedDate);
      String dateFinalString = formatDate(dateFinal, extendedDate);
      return extendedDate ? messages.titleDates(dateInitialString, dateFinalString)
        : messages.simpleDates(dateInitialString, dateFinalString);
    }
  }

  public enum DHMSFormat {
    LONG, SHORT;
  }

  public static String durationInDHMS(Date start, Date end, DHMSFormat format) {
    Date endDate = end == null ? new Date() : end;
    long duration = endDate.getTime() - start.getTime();
    return DHMSFormat.LONG.equals(format) ? durationMillisToLongDHMS(duration) : durationMillisToShortDHMS(duration);
  }

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

    if (days > 0) {
      return messages.durationDHMSShortDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      return messages.durationDHMSShortHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      return messages.durationDHMSShortMinutes(minutes, seconds);
    } else if (seconds > 0) {
      return messages.durationDHMSShortSeconds(seconds);
    } else {
      return messages.durationDHMSShortMillis(millis);
    }
  }

  /**
   * converts time (in milliseconds) to human-readable format "<w> days, <x>
   * hours, <y> minutes and (z) seconds"
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

    if (days > 0) {
      return messages.durationDHMSLongDays(days, hours, minutes, seconds);
    } else if (hours > 0) {
      return messages.durationDHMSLongHours(hours, minutes, seconds);
    } else if (minutes > 0) {
      return messages.durationDHMSLongMinutes(minutes, seconds);
    } else {
      return messages.durationDHMSLongSeconds(seconds);
    }
  }

  /**
   * Format a value in milliseconds as a string
   * 
   * @param value
   * @param showMillis
   * @return a formatted string for time duration
   */
  public static String formatValueMilliseconds(long value, boolean showMillis) {
    long hours = value / 3600000;
    long minutes = (value % 3600000) / 60000;
    long seconds = ((value % 3600000) % 60000) / 1000;
    long millis = value % 1000;

    NumberFormat numberFormat = NumberFormat.getFormat("00");
    NumberFormat millisFormat = NumberFormat.getFormat("000");

    if (showMillis) {
      return numberFormat.format(hours) + ":" + numberFormat.format(minutes) + ":" + numberFormat.format(seconds) + "."
        + millisFormat.format(millis);
    } else {
      return numberFormat.format(hours) + ":" + numberFormat.format(minutes) + ":" + numberFormat.format(seconds);
    }
  }

  public static String formatDate(Date date) {
    return formatDate(date, false);
  }

  public static String formatDate(Date date, boolean extended) {
    String formatPropertyName = extended ? RodaConstants.UI_DATE_FORMAT_TITLE : RodaConstants.UI_DATE_FORMAT_SIMPLE;
    return applyDateTimeFormat(date, ConfigurationManager.getString(formatPropertyName), DATE_FORMAT);
  }

  public static String formatDateTime(Date date) {
    return applyDateTimeFormat(date, ConfigurationManager.getString(RodaConstants.UI_DATE_TIME_FORMAT_SIMPLE),
      DATE_TIME_FORMAT);
  }

  private static String applyDateTimeFormat(Date date, String stringFormat, DateTimeFormat defaultValue) {
    DateTimeFormat format;

    if (stringFormat != null) {
      try {
        if (stringFormat.startsWith("predef:")) {
          DateTimeFormat.PredefinedFormat predefinedFormat = DateTimeFormat.PredefinedFormat
            .valueOf(stringFormat.substring(7));
          format = DateTimeFormat.getFormat(predefinedFormat);
        } else {
          format = DateTimeFormat.getFormat(stringFormat);
        }
      } catch (IllegalArgumentException e) {
        format = defaultValue;
        GWT.log("Could not parse date/time format '" + stringFormat + "', using default", e);
      }
    } else {
      format = defaultValue;
    }

    if (ConfigurationManager.getBoolean(FORMAT_UTC, RodaConstants.UI_DATE_TIME_FORMAT_UTC)) {
      return format.format(date, TimeZone.createTimeZone(0));
    }

    return format.format(date);
  }
}
