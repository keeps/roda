/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class URLUtils {
  private URLUtils() {
    // do nothing
  }

  public static String decode(String toDecode) {
    String decoded;
    try {
      decoded = URLDecoder.decode(toDecode, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      decoded = toDecode;
    }

    return decoded;
  }
}
