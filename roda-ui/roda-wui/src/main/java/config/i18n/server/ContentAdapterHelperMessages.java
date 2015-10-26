/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package config.i18n.server;

import java.util.Locale;
import java.util.ResourceBundle;

public class ContentAdapterHelperMessages {
  private static final String BUNDLE_NAME = "config.i18n.server.ContentAdapterHelperMessages";

  private final ResourceBundle resourceBundle;

  public ContentAdapterHelperMessages(Locale locale) {
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  public String getString(String key) {
    return resourceBundle.getString(key);
  }
}
