package org.roda.wui.common.client.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ConfigurationManager {
  private static String DEBUG_MODE_PROPERTY = "ui.sharedProperties.debug";

  private static ClientLogger logger = new ClientLogger(ConfigurationManager.class.getName());

  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private static ConfigurationManager instance = null;

  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private Map<String, List<String>> configurationProperties = null;

  public static void initialize(Map<String, List<String>> properties) {
    instance = new ConfigurationManager(properties);
    instance.debug();
  }

  private ConfigurationManager(Map<String, List<String>> properties) {
    configurationProperties = properties;
  }

  private static Map<String, List<String>> getConfigurationProperties() {
    if (instance == null || instance.configurationProperties == null) {
      logger.error("Requiring a shared property while their are not yet loaded");
      return Collections.emptyMap();
    } else {
      return instance.configurationProperties;
    }
  }

  /**
   * @return The property value for the provided keyParts. Or {@code null} if the
   *         property value was null or the key is not present.
   */
  public static String getString(String... keyParts) {
    List<String> values = getStringList(keyParts);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * @return The translation for the provided keyParts. Or {@code null} if the
   *         translation was null or the key is not present.
   */
  public static String getTranslation(String... keyParts) {
    String translationKey = "i18n." + getConfigurationKey(keyParts);
    List<String> values = getStringList(translationKey);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * @return The integer property value for the provided keyParts. Or
   *         {@code defaultValue} if the property value was null, not an integer
   *         or the key is not present.
   */
  public static int getInt(int defaultValue, String... keyParts) {
    String value = getString(keyParts);
    if (value != null) {
      try {
        return Integer.valueOf(value);
      } catch (NumberFormatException e) {
        // proceed with returning the default
      }
    }
    return defaultValue;
  }

  /**
   * @return The property value for the provided keyParts. Or {@code null} if the
   *         property value was null or the key is not present.
   */
  public static boolean getBoolean(boolean defaultValue, String... keyParts) {
    String value = getString(keyParts);
    if ("true".equalsIgnoreCase(value)) {
      return true;
    } else if ("false".equalsIgnoreCase(value)) {
      return false;
    } else {
      return defaultValue;
    }
  }

  /**
   * @return A list with the property values for the provided keyParts. Or an
   *         empty list if the key is not present).
   */
  public static List<String> getStringList(String... keyParts) {
    String key = getConfigurationKey(keyParts);
    List<String> values = getConfigurationProperties().get(key);
    if (values != null) {
      return new ArrayList<>(values);
    } else {
      return new ArrayList<>();
    }
  }

  private static String getConfigurationKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }

  private void debug() {
    if (getBoolean(true, DEBUG_MODE_PROPERTY)) {
      GWT.log("--- debugging configuration manager start");
      Map<String, List<String>> cfg = new TreeMap<>(getConfigurationProperties());

      StringBuilder debugInfo = new StringBuilder();
      for (Map.Entry<String, List<String>> property : cfg.entrySet()) {
        List<String> values = property.getValue();
        debugInfo.append(property.getKey()).append(":");
        if (values.isEmpty()) {
          debugInfo.append("  ''\n");
        } else {
          if (values.size() > 1) {
            debugInfo.append("\n");
          }
          for (String value : property.getValue()) {
            debugInfo.append("  '").append(value).append("'\n");
          }
        }
      }
      GWT.log(debugInfo.toString());
      GWT.log("--- debugging configuration manager end");
    }
  }
}
