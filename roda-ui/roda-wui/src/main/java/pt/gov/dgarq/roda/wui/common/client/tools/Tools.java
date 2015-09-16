/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;

import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;

/**
 * Useful methods
 * 
 * @author Luis Faria
 */
public class Tools {

  /**
   * Remove the first item from the string array
   * 
   * @param array
   * @return a copy of the array without the first item
   */
  public static String[] tail(String[] array) {
    String[] tail = new String[array.length - 1];
    for (int i = 1; i < array.length; i++) {
      tail[i - 1] = array[i];
    }
    return tail;
  }

  public static <T> List<T> tail(List<T> list) {
    return list.subList(1, list.size());
  }

  /**
   * Remove last item from the string array
   * 
   * @param array
   * @return a copy of the array without the last item
   */
  public static String[] removeLast(String[] array) {
    String[] ret = new String[array.length - 1];
    for (int i = 0; i < array.length - 1; i++) {
      ret[i] = array[i];
    }
    return ret;
  }

  public static <T> List<T> removeLast(List<T> list) {
    return list.subList(0, list.size() - 1);

  }

  /**
   * Split history string to history path using '.' as the separator
   * 
   * @param history
   * @return the history path
   */
  public static List<String> splitHistory(String history) {
    List<String> historyPath;
    if (history.indexOf('.') == -1) {
      historyPath = Arrays.asList(history);
    } else {
      historyPath = Arrays.asList(history.split("\\."));
    }
    return historyPath;
  }

  public static List<String> getCurrentHistoryPath() {
    String hash = Window.Location.getHash();
    if (hash.length() > 0) {
      hash = hash.substring(1);
    }

    List<String> splitted = Arrays.asList(hash.split("\\."));
    List<String> tokens = new ArrayList<String>();
    for (String item : splitted) {
      tokens.add(URL.decode(item));
    }
    return tokens;
  }

  private static String createHistoryToken(List<String> tokens) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (String token : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(".");
      }

      String encodedToken = URL.encode(token).replaceAll("\\.", "%2E");
      builder.append(encodedToken);
    }

    return builder.toString();

  }

  public static void newHistory(List<String> path) {
    // History.newItem(createHistoryToken(path)
    String hash = createHistoryToken(path);
    Window.Location.assign("#" + hash);
  }

  public static void newHistory(HistoryResolver resolver) {
    newHistory(resolver.getHistoryPath());
  }

  public static <T> List<T> concat(List<T> list, T item) {
    List<T> ret = new ArrayList<>(list);
    ret.add(item);
    return ret;
  }

  /**
   * Join all tokens dividing by a separator
   * 
   * @param tokens
   *          the string tokens
   * @param separator
   *          the separator to use between all tokens
   * @return a string will all tokens separated by the defined separator
   */
  public static String join(String[] tokens, String separator) {
    String history = "";
    if (tokens.length > 0) {
      history = tokens[0];
    }
    for (int i = 1; i < tokens.length; i++) {
      history += separator + tokens[i];
    }
    return history;
  }

  public static String join(List<String> tokens, String separator) {
    String history = "";
    if (tokens.size() > 0) {
      history = tokens.get(0);
    }
    for (int i = 1; i < tokens.size(); i++) {
      history += separator + tokens.get(i);
    }
    return history;
  }

  /**
   * String representation of a string array
   * 
   * @param array
   *          the string array
   * @return the string representation
   */
  public static String toString(String[] array) {
    String ret = "[";
    for (int i = 0; i < array.length; i++) {
      ret += (i > 0) ? ", " + array[i] : array[i];
    }
    ret += "]";
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

  public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd");

  public static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");

  public static final DateTimeFormat DATE_TIME_MS_FORMAT = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.SSS");

  public static String formatDate(Date date) {
    return DATE_FORMAT.format(date);
  }

  public static String formatDateTime(Date date) {
    return DATE_TIME_FORMAT.format(date);
  }

  public static String formatDateTimeMs(Date date) {
    return DATE_TIME_MS_FORMAT.format(date);
  }

  /**
   * Set the ListBox selected index based on a value
   * 
   * @param listbox
   * @param value
   * @return the index of the selected value, or -1 if not found
   */
  public static int setSelectedValue(ListBox listbox, String value) {
    int selectedIndex = -1;
    for (int i = 0; i < listbox.getItemCount(); i++) {
      if (listbox.getValue(i).equals(value)) {
        selectedIndex = i;
        listbox.setSelectedIndex(i);
        break;
      }
    }
    return selectedIndex;

  }
}
