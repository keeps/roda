package org.roda.wui.common.client.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.FacetParameter;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.RangeFacetParameter;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.wui.common.client.ClientLogger;

import com.google.gwt.core.client.GWT;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ConfigurationManager {
  private static final String DEBUG_MODE_PROPERTY = "ui.sharedProperties.debug";

  private static ClientLogger logger = new ClientLogger(ConfigurationManager.class.getName());

  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private static ConfigurationManager instance = null;

  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private Map<String, List<String>> configurationProperties = null;

  public static void initialize(Map<String, List<String>> properties) {
    instance = new ConfigurationManager(properties);
    FacetFactory.initialize();
    instance.debug();
  }

  private ConfigurationManager(Map<String, List<String>> properties) {
    configurationProperties = properties;
  }

  private static Map<String, List<String>> getConfigurationProperties() {
    if (instance == null || instance.configurationProperties == null) {
      logger.error("Requiring a shared property while they are not yet loaded");
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
   * Used when the configuration is setup as following:
   *
   * keyParts: i18nKey
   *
   * i18nKey: returnValue
   *
   * This method resolves the keyParts to an 18n key and retrieves its associated
   * translation.
   *
   * @return the translation for an i18n key that is the property value for the
   *         provided keyParts
   */
  public static String resolveTranslation(String... keyParts) {
    String i18nKey = getString(keyParts);
    if (i18nKey != null) {
      return getTranslation(i18nKey);
    }
    return null;
  }

  /**
   * @return The integer property value for the provided keyParts. Or
   *         {@code defaultValue} if the property value was null, not an integer
   *         or the key is not present.
   */
  public static Integer getInt(Integer defaultValue, String... keyParts) {
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
   * @return The integer property value for the provided keyParts. Or {@code null}
   *         if the property value was null, not an integer or the key is not
   *         present.
   */
  public static Integer getInt(String... keyParts) {
    return getInt(null, keyParts);
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
   *
   * @see RodaCoreFactory#getRodaConfigurationAsList
   */
  public static List<String> getStringList(String... keyParts) {
    String key = getConfigurationKey(keyParts);
    List<String> values = getConfigurationProperties().get(key);
    return values != null ? values : new ArrayList<>();
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

  public static class FacetFactory {
    private static List<String> enabledListIds = Collections.emptyList();

    private FacetFactory() {
      // do nothing
    }

    private static void initialize() {
      enabledListIds = getStringList(RodaConstants.UI_LISTS_PROPERTY);
    }

    public static Facets getFacets(String listId) {
      if (!enabledListIds.contains(listId)) {
        return Facets.NONE;
      }

      return buildFacets(listId);
    }

    private static Facets buildFacets(String listId) {
      String query = getString(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_QUERY_PROPERTY);

      List<String> parameterNames = getStringList(RodaConstants.UI_LISTS_PROPERTY, listId,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY);

      if (query != null) {
        return new Facets(buildParameters(listId, parameterNames), query);
      } else {
        return new Facets(buildParameters(listId, parameterNames));
      }
    }

    private static Map<String, FacetParameter> buildParameters(String listId, List<String> parameterNames) {
      Map<String, FacetParameter> parameters = new HashMap<>();

      if (parameterNames.isEmpty()) {
        GWT.log("ConfigurationManager: list '" + listId + "' has no parameters.");
      }

      for (String parameterName : parameterNames) {
        String type = getString(RodaConstants.UI_LISTS_PROPERTY, listId,
          RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY, parameterName,
          RodaConstants.UI_LISTS_FACETS_PARAMETERS_TYPE_PROPERTY);

        FacetParameter parameter = null;
        if (SimpleFacetParameter.class.getSimpleName().equalsIgnoreCase(type)) {
          parameter = buildSimpleFacetParameter(listId, parameterName);
        } else if (RangeFacetParameter.class.getSimpleName().equalsIgnoreCase(type)) {
          parameter = buildRangeFacetParameter(listId, parameterName);
        }

        if (parameter != null) {
          parameters.put(parameter.getName(), parameter);
        } else {
          GWT.log("ConfigurationManager: ignoring FacetParameter '" + parameterName + "' of type '" + type
            + "' which has an invalid type or invalid set of args.");
        }
      }

      return parameters;
    }

    private static SimpleFacetParameter buildSimpleFacetParameter(String listId, String parameterName) {
      String name = buildNameArg(listId, parameterName);
      Integer limit = buildLimitArg(listId, parameterName);
      FacetParameter.SORT sort = buildSortArg(listId, parameterName);
      Integer minCount = buildMinCountArg(listId, parameterName);

      // values is always not null, not need to check it
      List<String> values = buildValuesArg(listId, parameterName);

      // check which arguments are not null and use the appropriate constructor
      if (name != null && limit != null && sort != null) {
        return new SimpleFacetParameter(name, limit, sort);
      } else if (name != null && sort != null) {
        return new SimpleFacetParameter(name, sort);
      } else if (name != null && minCount != null && limit != null) {
        return new SimpleFacetParameter(name, values, minCount, limit);
      } else if (name != null && limit != null) {
        return new SimpleFacetParameter(name, limit);
      } else if (name != null && minCount != null) {
        return new SimpleFacetParameter(name, values, minCount);
      } else if (name != null) {
        return new SimpleFacetParameter(name, values);
      } else {
        // Assuming a FacetParameter is useless without a name
        return null;
      }
    }

    private static RangeFacetParameter buildRangeFacetParameter(String listId, String parameterName) {
      String name = buildNameArg(listId, parameterName);
      String start = buildStartArg(listId, parameterName);
      String end = buildEndArg(listId, parameterName);
      String gap = buildGapArg(listId, parameterName);

      // check which arguments are not null and use the appropriate constructor
      if (name != null && start != null && end != null && gap != null) {
        return new RangeFacetParameter(name, start, end, gap);
      } else if (name != null) {
        return new RangeFacetParameter(name);
      } else {
        // Assuming a FacetParameter is useless without a name
        return null;
      }
    }

    private static String buildNameArg(String listId, String parameterName) {
      return parameterName;
    }

    private static String buildStartArg(String listId, String parameterName) {
      return getString(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_START_PROPERTY);
    }

    private static String buildEndArg(String listId, String parameterName) {
      return getString(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_END_PROPERTY);
    }

    private static String buildGapArg(String listId, String parameterName) {
      return getString(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_GAP_PROPERTY);
    }

    private static Integer buildLimitArg(String listId, String parameterName) {
      return getInt(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_LIMIT_PROPERTY);
    }

    private static FacetParameter.SORT buildSortArg(String listId, String parameterName) {
      String possibleSort = getString(RodaConstants.UI_LISTS_PROPERTY, listId,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY, parameterName,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_SORT_PROPERTY);

      if (possibleSort != null) {
        for (FacetParameter.SORT sort : FacetParameter.SORT.values()) {
          if (possibleSort.equalsIgnoreCase(sort.name())) {
            return sort;
          }
        }
        GWT.log("ConfigurationManager: list '" + listId + "', parameter '" + parameterName
          + "' has an invalid SORT argument: '" + possibleSort + "'.");
      }

      return null;
    }

    private static List<String> buildValuesArg(String listId, String parameterName) {
      return getStringList(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_VALUES_PROPERTY);
    }

    private static Integer buildMinCountArg(String listId, String parameterName) {
      return getInt(RodaConstants.UI_LISTS_PROPERTY, listId, RodaConstants.UI_LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        RodaConstants.UI_LISTS_FACETS_PARAMETERS_ARGS_MINCOUNT_PROPERTY);
    }

  }
}
