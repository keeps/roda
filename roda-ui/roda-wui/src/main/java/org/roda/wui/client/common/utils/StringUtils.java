/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.Collection;
import java.util.List;

public class StringUtils {

  private StringUtils() {
    // do nothing
  }

  public static final boolean isNotBlank(String s) {
    return s != null && s.trim().length() > 0;
  }

  public static final boolean isBlank(String s) {
    return !isNotBlank(s);
  }

  // method to prettify method names
  public static String getPrettifiedActionMethod(String actionMethod) {
    String method = actionMethod.substring(0, 1).toUpperCase() + actionMethod.substring(1);
    method = method.replaceAll("([A-Z])", " $1").trim();
    return method.replaceAll("A I P", "AIP");
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
    StringBuilder history = new StringBuilder();
    if (tokens.length > 0) {
      history.append(tokens[0]);
    }
    for (int i = 1; i < tokens.length; i++) {
      history.append(separator).append(tokens[i]);
    }
    return history.toString();
  }

  public static String join(List<String> tokens, String separator) {
    StringBuilder history = new StringBuilder();
    if (!tokens.isEmpty()) {
      history.append(tokens.get(0));
    }
    for (int i = 1; i < tokens.size(); i++) {
      history.append(separator).append(tokens.get(i));
    }
    return history.toString();
  }

  /**
   * remove leading whitespace
   * 
   * @param source
   * @return string without leading whitespace
   */
  public static String ltrim(String source) {
    return source.replaceAll("^\\s+", "");
  }

  /**
   * remove trailing whitespace
   * 
   * @param source
   * @return string without trailing whitespace
   */
  public static String rtrim(String source) {
    return source.replaceAll("\\s+$", "");
  }

  /**
   * replace multiple whitespaces between words with single blank
   * 
   * @param source
   * @return string without multiple whitespaces between words
   * 
   */
  public static String itrim(String source) {
    return source.replaceAll("\\b\\s{2,}\\b", " ");
  }

  /**
   * remove all superfluous whitespaces in source string
   * 
   * @param source
   * @return string without superfluos whitespaces
   */
  public static String trim(String source) {
    return itrim(ltrim(rtrim(source)));
  }

  /**
   * Remove leading and trailing whitespace
   * 
   * @param source
   * @return string without leading or trailing whitespace
   */
  public static String lrtrim(String source) {
    return ltrim(rtrim(source));
  }

  /**
   * Replace new line, line feed and tab by a single white space
   * 
   * @param source
   * @return string without new lines, line feeds nor tabs
   */
  public static String nltrim(String source) {
    return source.replaceAll("[\n\r\t]", " ");
  }

  /**
   * Normalize string spaces
   * 
   * @param source
   * @return string without new lines, line feeds, tabs or superfluous white
   *         spaces
   */
  public static String normalizeSpaces(String source) {
    return source == null ? null : trim(nltrim(source));
  }

  public static String prettyPrint(Collection<String> allGroups) {
    String toString = allGroups.toString();
    return toString.substring(1, toString.length() - 1);
  }
}
