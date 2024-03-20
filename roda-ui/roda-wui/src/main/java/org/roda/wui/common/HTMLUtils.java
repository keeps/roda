/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.TechnicalMetadataNotFoundException;
import org.roda.core.storage.Binary;

import com.google.common.io.CharStreams;

/**
 * HTML related utility class
 *
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * @author Sébastien Leroux <sleroux@keep.pt>
 */
public final class HTMLUtils {

  /** Private empty constructor */
  private HTMLUtils() {
    // do nothing
  }

  public static String descriptiveMetadataToHtml(Binary binary, String metadataType, String metadataVersion,
    final Locale locale) throws GenericException {
    Map<String, String> translations = getTranslations(metadataType, metadataVersion, locale);
    Reader reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
      metadataType, metadataVersion, translations);
    try {
      return CharStreams.toString(reader);
    } catch (IOException e) {
      throw new GenericException("Could not transform PREMIS to HTML", e);
    }
  }

  public static String technicalMetadataToHtml(Binary binary, String metadataType, String metadataVersion,
    final Locale locale) throws GenericException, TechnicalMetadataNotFoundException {
    Map<String, String> translations = getTranslations(metadataType, metadataVersion, locale);

    String lowerCaseMetadataTypeWithVersion = metadataType.toLowerCase() + RodaConstants.METADATA_VERSION_SEPARATOR
      + metadataVersion;

    if ((RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH + lowerCaseMetadataTypeWithVersion + ".xslt")) == null) {
      throw new TechnicalMetadataNotFoundException("Could not retrieve technical metadata stylesheet");
    }


    Reader reader = RodaUtils.applyMetadataStylesheet(binary, RodaConstants.CROSSWALKS_DISSEMINATION_HTML_PATH,
      metadataType, metadataVersion, translations);
    try {
      return CharStreams.toString(reader);
    } catch (IOException e) {
      throw new GenericException("Could not transform PREMIS to HTML", e);
    }
  }

  public static String preservationMetadataEventToHtml(Binary binary, boolean onlyDetails, final Locale locale)
    throws GenericException {

    Map<String, String> translations = getEventTranslations(locale);

    Reader reader = RodaUtils.applyEventStylesheet(binary, onlyDetails, translations,
      RodaConstants.CROSSWALKS_DISSEMINATION_HTML_EVENT_PATH);

    try {
      return CharStreams.toString(reader);
    } catch (IOException e) {
      throw new GenericException("Could not transform PREMIS to HTML", e);
    }
  }

  public static Map<String, String>   getTranslations(String descriptiveMetadataType, String descriptiveMetadataVersion,
    final Locale locale) {
    Map<String, String> translations = null;
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    if (descriptiveMetadataType != null) {
      String lowerCaseDescriptiveMetadataType = descriptiveMetadataType.toLowerCase();
      if (descriptiveMetadataVersion != null) {
        String lowerCaseDescriptiveMetadataTypeWithVersion = lowerCaseDescriptiveMetadataType
          + RodaConstants.METADATA_VERSION_SEPARATOR + descriptiveMetadataVersion;
        translations = messages.getTranslations(
          RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + lowerCaseDescriptiveMetadataTypeWithVersion,
          String.class, true);
      }

      if (translations == null || translations.isEmpty()) {
        translations = messages.getTranslations(
          RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + lowerCaseDescriptiveMetadataType, String.class,
          true);
      }

    } else {
      translations = new HashMap<>();
    }
    return translations;
  }

  public static Map<String, String> getEventTranslations(final Locale locale) {
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return messages.getTranslations(RodaConstants.I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX + "event", String.class,
      true);
  }

}
