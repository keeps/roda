package org.roda.core.plugins;

import java.util.Locale;
import java.util.ResourceBundle.Control;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginMessagesControl extends Control {

  public PluginMessagesControl() {
    // empty constructor
  }

  @Override
  public Locale getFallbackLocale(String baseName, Locale locale) {
    if (baseName == null) {
      throw new NullPointerException();
    }
    // 20160712 hsilva: the following line is needed otherwise default locale
    // is used and this can be incoherent with other parts of the code where
    // the default locale is ENGLISH
    Locale defaultLocale = Locale.ENGLISH;
    return locale.equals(defaultLocale) ? null : defaultLocale;
  }
}
