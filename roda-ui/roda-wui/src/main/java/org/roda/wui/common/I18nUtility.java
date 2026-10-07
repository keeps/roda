/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class I18nUtility {
  private static final String PRESERVATION_EVENT_TYPE_PREFIX = "ui.preservationEvent.type.";
  private static final List<String> PRESERVATION_EVENT_TEXT_LOCALES = Arrays.asList("en", "pt_PT", "es", "hr", "hu",
    "sv_SE", "de_AT", "de_DE");
  private static Map<String, String> preservationEventTextKeys = null;
  private static List<Map<String, String>> preservationEventTextSources = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(I18nUtility.class);

  /** Private empty constructor */
  private I18nUtility() {

  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return translate(input, resultClass, locale);
  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    Locale locale) {
    IndexResult<T> output = input;
    if (output != null && output.getFacetResults() != null && !output.getFacetResults().isEmpty()) {
      for (FacetFieldResult ffr : output.getFacetResults()) {
        if (ffr != null && ffr.getValues() != null && !ffr.getValues().isEmpty()) {
          String field = ffr.getField();
          for (FacetValue fv : ffr.getValues()) {
            fv.setLabel(getFacetTranslation(field, fv.getValue(), locale, resultClass));
          }
        }
      }
    }
    return output;
  }

  private static <T extends Serializable> String getFacetTranslation(String facetField, String facetValue,
    Locale locale, Class<T> resultClass) {
    String ret;
    String bundleKey = RodaConstants.I18N_UI_FACETS_PREFIX + "." + resultClass.getSimpleName() + "." + facetField
      + (facetValue == null || facetValue.trim().length() == 0 ? "other" : "." + facetValue.trim());

    try {
      if (resultClass.equals(LogEntry.class) && facetField.equals(RodaConstants.LOG_ACTION_METHOD)
        && facetValue != null) {
        ret = StringUtils.getPrettifiedActionMethod(facetValue);
      } else if (resultClass.equals(RODAMember.class) && facetField.equals(RodaConstants.MEMBERS_GROUPS)
        && facetValue != null) {
        ArrayList<String> fields = new ArrayList<>();
        fields.add(RodaConstants.MEMBERS_ID);
        fields.add(RodaConstants.MEMBERS_GROUPS);
        fields.add(RodaConstants.MEMBERS_FULLNAME);
        RODAMember group = RodaCoreFactory.getIndexService().retrieve(RODAMember.class, facetValue, fields);
        ret = group.getFullName();
      } else if (resultClass.equals(DisposalSchedule.class)
        && facetField.equals(RodaConstants.DISPOSAL_SCHEDULE_FULL_RETENTION_PERIOD) && facetValue != null) {
        String prefixKey = RodaConstants.I18N_UI_FACETS_PREFIX + "." + resultClass.getSimpleName() + "." + facetField
          + ".";
        ret = getRetentionPeriodTranslation(facetValue, prefixKey, locale);
      } else if (resultClass.equals(IndexedPreservationEvent.class)
        && facetField.equals(RodaConstants.PRESERVATION_EVENT_TYPE) && facetValue != null) {
        ret = translatePreservationEventType(facetValue, locale);
      } else if (resultClass.equals(IndexedReport.class)
        && (facetField.equals("unsuccessfulPlugins") || facetField.equals("successfulPlugins"))) {
        ret = RodaCoreFactory.getI18NMessages(locale).getTranslation(facetValue);
      } else {
        ret = RodaCoreFactory.getI18NMessages(locale).getTranslation(bundleKey);
      }
    } catch (MissingResourceException | NotFoundException | GenericException e) {
      ret = facetValue;
      LOGGER.trace("Translation not found: " + bundleKey + " locale: " + locale, e);
    }

    return ret;
  }

  /**
   * Fills the translated labels of the preservation events type and detail. The
   * original values (as in PREMIS and in the index) are kept untouched.
   */
  public static IndexResult<IndexedPreservationEvent> translatePreservationEvents(
    IndexResult<IndexedPreservationEvent> result, String localeString) {
    if (result != null && result.getResults() != null) {
      Locale locale = ServerTools.parseLocale(localeString);
      for (IndexedPreservationEvent event : result.getResults()) {
        translatePreservationEvent(event, locale);
      }
    }
    return result;
  }

  public static IndexedPreservationEvent translatePreservationEvent(IndexedPreservationEvent event,
    String localeString) {
    return translatePreservationEvent(event, ServerTools.parseLocale(localeString));
  }

  public static IndexedPreservationEvent translatePreservationEvent(IndexedPreservationEvent event, Locale locale) {
    if (event != null) {
      event.setEventTypeLabel(translatePreservationEventType(event.getEventType(), locale));
      event.setEventDetailLabel(translatePreservationEventText(event.getEventDetail(), locale));
    }
    return event;
  }

  /**
   * Translates a preservation event text (detail or outcome detail) as stored in
   * PREMIS, which was written in the server language from the keys
   * {@code preservationEvent.*} and {@code plugin.*.event.*}. The stored text is
   * matched exactly (line by line) against the values of those keys in all
   * languages and the value of the matched key in the given locale is returned.
   * Texts that do not match (e.g. with variable values) are returned unchanged.
   */
  public static String translatePreservationEventText(String text, Locale locale) {
    if (StringUtils.isBlank(text)) {
      return text;
    }

    try {
      Map<String, String> keys = getPreservationEventTextKeys();
      Messages messages = RodaCoreFactory.getI18NMessages(locale);
      String[] lines = text.split("\n", -1);
      for (int i = 0; i < lines.length; i++) {
        String key = keys.get(lines[i].trim());
        if (key != null) {
          lines[i] = messages.getTranslation(key, lines[i]);
        }
      }
      return String.join("\n", lines);
    } catch (RuntimeException e) {
      LOGGER.debug("Could not translate preservation event text", e);
      return text;
    }
  }

  private static synchronized Map<String, String> getPreservationEventTextKeys() {
    List<Map<String, String>> sources = new ArrayList<>();
    for (String localeString : PRESERVATION_EVENT_TEXT_LOCALES) {
      Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));
      sources.add(messages.getTranslations("preservationEvent", String.class, false));
      sources.add(messages.getTranslations("plugin", String.class, false));
    }

    // rebuild only when the (cached) translations change
    if (!isSamePreservationEventTextSources(sources)) {
      Map<String, String> keys = new HashMap<>();
      for (Map<String, String> source : sources) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          if ((key.startsWith("preservationEvent.") || key.contains(".event.")) && StringUtils.isNotBlank(value)
            && !value.contains("{")) {
            keys.putIfAbsent(value.trim(), key);
          }
        }
      }
      preservationEventTextKeys = keys;
      preservationEventTextSources = sources;
    }
    return preservationEventTextKeys;
  }

  /**
   * Translates a preservation event type as stored (e.g. "fixity check") using
   * the key {@code ui.preservationEvent.type.<PreservationEventType name>}. If
   * the type is unknown or has no translation, the stored value is returned.
   */
  public static String translatePreservationEventType(String eventType, Locale locale) {
    if (StringUtils.isBlank(eventType)) {
      return eventType;
    }

    PreservationEventType type = null;
    for (PreservationEventType value : PreservationEventType.values()) {
      if (value.getOriginalText().equalsIgnoreCase(eventType) || value.toString().equalsIgnoreCase(eventType)) {
        type = value;
        break;
      }
    }

    if (type == null) {
      return eventType;
    }
    return RodaCoreFactory.getI18NMessages(locale).getTranslation(PRESERVATION_EVENT_TYPE_PREFIX + type.name(),
      eventType);
  }

  private static boolean isSamePreservationEventTextSources(List<Map<String, String>> sources) {
    List<Map<String, String>> cached = preservationEventTextSources;
    if (cached == null || cached.size() != sources.size()) {
      return false;
    }
    for (int i = 0; i < sources.size(); i++) {
      if (sources.get(i) != cached.get(i)) {
        return false;
      }
    }
    return true;
  }

  private static String getRetentionPeriodTranslation(String value, String prefixKey, Locale locale) {
    if (RetentionPeriodIntervalCode.NO_RETENTION_PERIOD.toString().equals(value)) {
      return RodaCoreFactory.getI18NMessages(locale).getTranslation(prefixKey + value);
    } else {
      String numberPart = value.replaceAll("\\D+", "");
      String intervalPart = value.replaceAll("\\d+", "");

      String intervalTranslation;
      if (Integer.parseInt(numberPart) == 1) {
        String replaced = intervalPart.replace("S", "");
        intervalTranslation = RodaCoreFactory.getI18NMessages(locale).getTranslation(prefixKey + replaced);
      } else {
        intervalTranslation = RodaCoreFactory.getI18NMessages(locale).getTranslation(prefixKey + intervalPart);
      }

      return numberPart + " " + intervalTranslation;
    }
  }

  public static String getMessage(String key, String defaultMessage, String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    return messages.getTranslation(key, defaultMessage);
  }
}
