package org.roda.wui.api.v2.services;

import java.util.Locale;
import java.util.Objects;

import org.roda.core.RodaCoreFactory;
import org.roda.wui.common.server.ServerTools;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
public class TranslationService {

  public String getTranslation(String localeString, String key) {
    return getTranslation(localeString, key, key);
  }

  public String getTranslation(String localeString, String key, String fallback) {
    Locale locale = ServerTools.parseLocale(localeString);
    try {
      return Objects.requireNonNull(RodaCoreFactory.getI18NMessages(locale)).getTranslation(key, fallback);
    } catch (NullPointerException e) {
      return key;
    }
  }
}
