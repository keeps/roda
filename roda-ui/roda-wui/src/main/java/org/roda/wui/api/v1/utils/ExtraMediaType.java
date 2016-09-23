package org.roda.wui.api.v1.utils;

import javax.ws.rs.core.MediaType;

/**
 * An abstraction for extra media types. Instances are immutable.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 * @see <a href=
 *      "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1
 *      section 3.7</a>
 * @see javax.ws.rs.core.MediaType
 * @since 2.0
 */
public final class ExtraMediaType {

  /**
   * Constructor.
   */
  private ExtraMediaType() {
  }

  /**
   * A {@code String} constant representing {@value #TEXT_CSV} media type.
   */
  public static final String TEXT_CSV = "text/csv";
  /**
   * A {@link MediaType} constant representing {@value #TEXT_CSV} media type.
   */
  public static final MediaType TEXT_CSV_TYPE = new MediaType("text", "csv");

}
