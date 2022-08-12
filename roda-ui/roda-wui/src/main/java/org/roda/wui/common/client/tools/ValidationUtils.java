/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common.client.tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ValidationUtils {
  public ValidationUtils() {
    // do Nothing
  }

  public static boolean isValidURL(String url, boolean topLevelDomainRequired) {
    RegExp urlValidator = RegExp
      .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");
    RegExp urlPlusTldValidator = RegExp
      .compile("^((ftp|http|https)://[\\w@.\\-\\_]+\\.[a-zA-Z]{2,}(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");
    GWT.log("isValidURL: " + url + " " + (topLevelDomainRequired ? urlPlusTldValidator : urlValidator).exec(url));
    return (topLevelDomainRequired ? urlPlusTldValidator : urlValidator).exec(url) != null;
  }
}
