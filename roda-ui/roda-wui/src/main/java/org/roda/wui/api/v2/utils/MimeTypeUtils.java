/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.utils;

import jakarta.activation.MimetypesFileTypeMap;

public class MimeTypeUtils {

  private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
  private static final MimetypesFileTypeMap MIMEMAP = new MimetypesFileTypeMap();

  static {
    MIMEMAP.addMimeTypes("text/css css CSS");
    MIMEMAP.addMimeTypes("application/javascript js JS");
    MIMEMAP.addMimeTypes("image/svg+xml svg SVG");
    MIMEMAP.addMimeTypes("video/mp4 mp4 MP4");
  }

  public static String getContentType(String filename) {
    return getContentType(filename, APPLICATION_OCTET_STREAM);
  }

  public static String getContentType(String filename, String fallback) {
    String ret = MIMEMAP.getContentType(filename);

    if (APPLICATION_OCTET_STREAM.equals(ret)) {
      ret = fallback;
    }

    return ret;
  }
}
