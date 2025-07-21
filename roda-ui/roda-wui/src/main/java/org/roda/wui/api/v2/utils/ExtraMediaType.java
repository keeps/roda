/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.utils;

/**
 * An abstraction for extra media types. Instances are immutable.
 *
 * @author Rui Castro <rui.castro@gmail.com>
 * @see <a href= "http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">
 *      HTTP/1.1 section 3.7</a>
 * @since 2.0
 */
public final class ExtraMediaType {

  /**
   * A {@code String} constant representing {@value #TEXT_CSV} media type.
   */
  public static final String TEXT_CSV = "text/csv";
  public static final String APPLICATION_ZIP = "application/zip";
  public static final String APPLICATION_JAVASCRIPT = "application/javascript";

  /**
   * Constructor.
   */
  private ExtraMediaType() {
    // do nothing
  }

}
