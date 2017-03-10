/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public final class XmlEncodeUtility {

  /**
   * Encode XML entities and unicode control chars
   * 
   * @param s
   *          the string to encode
   * @return the encoded string
   */
  public static String encode(String s) {
    StringBuilder ret = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '<') {
        ret.append("&lt;"); //$NON-NLS-1$
      } else if (ch == '>') {
        ret.append("&gt;"); //$NON-NLS-1$
      } else if (ch == '"') {
        ret.append("&quot;"); //$NON-NLS-1$
      } else if (ch == '\'') {
        ret.append("&apos;"); //$NON-NLS-1$
      } else if (ch == '&') {
        ret.append("&amp;"); //$NON-NLS-1$
      } else if (ch < 0x20 && ch != 0x9 && ch != 0xD && ch != 0xA) {
        ret.append((ch <= 0xf ? "\\u000" : "\\u00") //$NON-NLS-1$ //$NON-NLS-2$
          + Integer.toHexString(ch).toUpperCase());
      } else {
        ret.append(ch);
      }
    }

    return ret.toString();
  }

  /**
   * Escape invalid control characters from a text to be included in XML. String
   * types may contain C0 control characters that are not legal in XML. This
   * method convert these to the Unicode replacement character 0xFFFD.
   * 
   * @param text
   *          the text to be escaped.
   * @return the escaped text.
   */
  public static String escapeInvalidXmlChars(String text) {
    StringBuilder ret = new StringBuilder();

    for (int i = 0; i < text.length(); i++) {
      ret.append(escapeChar(text.charAt(i)));
    }

    return ret.toString();
  }

  private static char escapeChar(char c) {
    if (c >= 0x20 || c == '\n' || c == '\r' || c == '\t') {
      return c;
    } else {
      return '\uFFFD';
    }
  }

}
