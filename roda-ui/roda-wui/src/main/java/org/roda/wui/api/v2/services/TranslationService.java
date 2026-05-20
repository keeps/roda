/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.jobs.CertificateInfo;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.Report;
import org.roda.wui.common.I18nUtility;
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

  public PluginInfo translatePlugin(PluginInfo originalPlugin, String localeString) {
    PluginInfo clonedPlugin = new PluginInfo(originalPlugin);
    if (originalPlugin.getCertificateInfo().getCertificateStatus().equals(CertificateInfo.CertificateStatus.INTERNAL)) {
      clonedPlugin.setName(I18nUtility.getMessage(originalPlugin.getName(), originalPlugin.getName(), localeString));
      clonedPlugin.setDescription(
              I18nUtility.getMessage(originalPlugin.getDescription(), originalPlugin.getDescription(), localeString));
    } else {
      Locale locale = ServerTools.parseLocale(localeString);
      ResourceBundle resourceBundle = RodaCoreFactory.getPluginMessages(originalPlugin.getId(), locale);

      String name;
      String description;
      try {
        name = resourceBundle.getString(originalPlugin.getName());
      } catch (MissingResourceException e) {
        name = originalPlugin.getName();
      }

      try {
        description = resourceBundle.getString(originalPlugin.getDescription());
      } catch (MissingResourceException e) {
        description = originalPlugin.getDescription();
      }

      clonedPlugin.setName(name);
      clonedPlugin.setDescription(description);
    }

    return clonedPlugin;
  }

  public List<PluginParameter> translatePluginParameters(PluginInfo originalPlugin, String localeString) {
    List<PluginParameter> translatedParameters = new ArrayList<>();
    for (PluginParameter originalParam : originalPlugin.getParameters()) {
      PluginParameter clonedParam = new PluginParameter(originalParam);

      if (originalPlugin.getCertificateInfo().getCertificateStatus()
              .equals(CertificateInfo.CertificateStatus.INTERNAL)) {

        clonedParam.setName(I18nUtility.getMessage(originalParam.getName(), originalParam.getName(), localeString));
        clonedParam.setDescription(
                I18nUtility.getMessage(originalParam.getDescription(), originalParam.getDescription(), localeString));

        translatedParameters.add(clonedParam);
      } else {
        Locale locale = ServerTools.parseLocale(localeString);
        ResourceBundle resourceBundle = RodaCoreFactory.getPluginMessages(originalPlugin.getId(), locale);

        String name;
        String description;
        try {
          name = resourceBundle.getString(originalParam.getName());
        } catch (MissingResourceException e) {
          name = originalParam.getName();
        }

        try {
          description = resourceBundle.getString(originalParam.getDescription());
        } catch (MissingResourceException e) {
          description = originalParam.getDescription();
        }

        clonedParam.setName(name);
        clonedParam.setDescription(description);
        translatedParameters.add(clonedParam);
      }
    }

    return translatedParameters;
  }
}
