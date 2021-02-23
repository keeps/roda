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
