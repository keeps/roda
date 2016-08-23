/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Several utility methods to manipulate strings.
 * 
 * @author Luis Faria
 * @author Rui Castro
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public class StringUtility {

  /**
   * Remove leading whitespace from <code>source</code> string.
   * 
   * @param source
   *          the <code>source</code> string.
   */
  public static String ltrim(String source) {
    return source.replaceAll("^\\s+", "");
  }

  /**
   * Remove trailing whitespace from <code>source</code> string.
   * 
   * @param source
   *          the <code>source</code> string.
   */
  public static String rtrim(String source) {
    return source.replaceAll("\\s+$", "");
  }

  /**
   * Replace whitespace between words (internal) with single whitespace in
   * <code>source</code> string.
   * 
   * @param source
   *          the <code>source</code> string.
   */
  public static String itrim(String source) {
    return source.replaceAll("\\b\\s{2,}\\b", " ");
  }

  /**
   * Remove all leading, trailing and internal whitespace from
   * <code>source</code> string.
   * 
   * @param source
   *          the <code>source</code> string.
   */
  public static String trim(String source) {
    return itrim(ltrim(rtrim(source)));
  }

  /**
   * Remove leading and trailing whitespace in <code>source</code> string.
   * 
   * @param source
   *          the <code>source</code> string.
   * @return a String
   */
  public static String lrtrim(String source) {
    return ltrim(rtrim(source));
  }

  /**
   * Remove all newline characters from <code>source</code> string and replaces
   * each occurrence with a single whitespace.
   * 
   * @param source
   *          the <code>source</code> string.
   * @return a String
   */
  public static String nltrim(String source) {
    return source.replaceAll("[\n\r\t]", " ");
  }

  /**
   * Removes all leading, trailing, internal whitespace from <code>source</code>
   * string along with all newline characters and replaces them with a single
   * whitespace.
   * 
   * @param source
   *          the <code>source</code> string.
   * @return a String
   */
  public static String normalizeSpaces(String source) {
    return trim(nltrim(source));
  }

  /**
   * Joins a {@link Collection} of {@link String}s in a single {@link String}
   * using <code>delimiter</code> between each {@link String}.
   * 
   * @param strings
   * @param delimiter
   * @return a {@link String} with all the {@link String}s in
   *         <code>strings</code> concatenated.
   */
  public static String join(Collection<String> strings, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    Iterator<String> iter = strings.iterator();
    while (iter.hasNext()) {
      buffer.append(iter.next());
      if (iter.hasNext()) {
        buffer.append(delimiter);
      }
    }
    return buffer.toString();
  }

  /**
   * Joins a array of {@link String}s in a single {@link String} using
   * <code>delimiter</code> between each {@link String}.
   * 
   * @param strings
   * @param delimiter
   * @return a String
   */
  public static String join(String[] strings, String delimiter) {
    return join(Arrays.asList(strings), delimiter);
  }
}