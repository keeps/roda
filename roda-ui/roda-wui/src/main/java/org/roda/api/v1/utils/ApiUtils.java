package org.roda.api.v1.utils;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

/**
 * API Utils
 * 
 */
public class ApiUtils {

  /**
   * Get media type
   * 
   * @param format
   *          String with required format
   * @param headers
   *          String with request headers
   * @return media type
   */
  public static String getMediaType(String format, String headers) {
    final String APPLICATION_JS = "application/javascript; charset=UTF-8";

    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    if (StringUtils.isNotBlank(format)) {
      if (format.equalsIgnoreCase("XML")) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (format.equalsIgnoreCase("JSONP")) {
        mediaType = APPLICATION_JS;
      } else if (format.equalsIgnoreCase("bin")) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if (format.equalsIgnoreCase("html")) {
        mediaType = MediaType.TEXT_HTML;
      }
    } else if (headers.contains(MediaType.APPLICATION_XML)) {
      mediaType = MediaType.APPLICATION_XML;
    } else if (headers.contains(APPLICATION_JS)) {
      mediaType = APPLICATION_JS;
    }

    return mediaType;
  }

}
