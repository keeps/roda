/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client.tools;

import java.util.Collection;

/**
 * @author Luis Faria
 * 
 */
public class StringUtility {

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
