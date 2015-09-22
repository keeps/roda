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
   * @param acceptFormat
   *          String with required format
   * @param acceptHeaders
   *          String with request headers
   * @return media type
   */
  public static String getMediaType(String acceptFormat, String acceptHeaders) {
    final String APPLICATION_JS = "application/javascript; charset=UTF-8";

    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    if (StringUtils.isNotBlank(acceptFormat)) {
      if (acceptFormat.equalsIgnoreCase("XML")) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptFormat.equalsIgnoreCase("JSONP")) {
        mediaType = APPLICATION_JS;
      } else if (acceptFormat.equalsIgnoreCase("bin")) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if (acceptFormat.equalsIgnoreCase("html")) {
        mediaType = MediaType.TEXT_HTML;
      }
    } else if (acceptHeaders.contains(MediaType.APPLICATION_XML)) {
      mediaType = MediaType.APPLICATION_XML;
    } else if (acceptHeaders.contains(APPLICATION_JS)) {
      mediaType = APPLICATION_JS;
    }

    return mediaType;
  }

}
