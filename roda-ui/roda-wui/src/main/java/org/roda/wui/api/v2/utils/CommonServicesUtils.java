package org.roda.wui.api.v2.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.generics.select.SelectedItemsFilterRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.util.IdUtils;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonServicesUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonServicesUtils.class);

  private CommonServicesUtils() {
    // empty constructor
  }

  public static <T extends IsRODAObject> Job createAndExecuteInternalJob(String name, SelectedItems<T> sourceObjects,
    Class<?> plugin, User user, Map<String, String> pluginParameters, String exceptionMessage)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return createAndExecuteJob(name, sourceObjects, plugin, PluginType.INTERNAL, user, pluginParameters,
      exceptionMessage);
  }

  public static <T extends IsRODAObject> Job createAndExecuteJob(String name, SelectedItems<T> sourceObjects,
    Class<?> plugin, PluginType pluginType, User user, Map<String, String> pluginParameters, String exceptionMessage)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName(name);
    job.setSourceObjects(sourceObjects);
    job.setPlugin(plugin.getCanonicalName());
    job.setPluginType(pluginType);
    job.setUsername(user.getName());
    job.setPluginParameters(pluginParameters);
    job.setPriority(getJobPriorityFromConfiguration());
    job.setParallelism(getJobParallelismFromConfiguration());

    try {
      RodaCoreFactory.getPluginOrchestrator().createAndExecuteJobs(job, true);
    } catch (JobAlreadyStartedException e) {
      LOGGER.error(exceptionMessage, e);
    }

    return job;
  }

  private static JobPriority getJobPriorityFromConfiguration() {
    // Fetch priority
    String priority = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_ORCHESTRATOR_PREFIX,
      RodaConstants.CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PRIORITY);

    if (priority == null) {
      return JobPriority.MEDIUM;
    }

    try {
      return JobPriority.valueOf(priority);
    } catch (IllegalArgumentException e) {
      return JobPriority.MEDIUM;
    }
  }

  private static JobParallelism getJobParallelismFromConfiguration() {
    // Fetch priority
    String parallelism = RodaCoreFactory.getRodaConfigurationAsString(RodaConstants.CORE_ORCHESTRATOR_PREFIX,
      RodaConstants.CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PARALLELISM);

    if (parallelism == null) {
      return JobParallelism.NORMAL;
    }

    try {
      return JobParallelism.valueOf(parallelism);
    } catch (IllegalArgumentException e) {
      return JobParallelism.NORMAL;
    }
  }

  public static <T extends IsIndexed> SelectedItems<T> convertSelectedItems(SelectedItemsRequest request,
    Class<T> tClass) throws RequestNotValidException {
    if (request instanceof SelectedItemsListRequest itemsListRequest) {
      return SelectedItemsList.create(tClass, itemsListRequest.getIds());
    } else if (request instanceof SelectedItemsFilterRequest filterRequest) {
      return new SelectedItemsFilter<>(filterRequest.getFilter(), tClass.getName(), filterRequest.getJustActive());
    }

    throw new RequestNotValidException("Selected items must be either a list or a filter");
  }

  public static void getMetadataValueI18nPrefix(MetadataValue metadataValue, Locale locale, Messages messages)
      throws GenericException {
    String i18nPrefix = metadataValue.get("optionsLabelI18nKeyPrefix");
    if (i18nPrefix != null) {
      Map<String, String> terms = messages.getTranslations(i18nPrefix, String.class, false);
      if (!terms.isEmpty()) {
        try {
          String options = metadataValue.get("options");
          List<String> optionsList = JsonUtils.getListFromJson(options, String.class);

          if (optionsList != null) {
            Map<String, Map<String, String>> i18nMap = new HashMap<>();
            for (String value : optionsList) {
              String translation = terms.get(i18nPrefix + "." + value);
              if (translation == null) {
                translation = value;
              }
              Map<String, String> term = new HashMap<>();
              term.put(locale.toString(), translation);
              i18nMap.put(value, term);
            }
            metadataValue.set("optionsLabels", JsonUtils.getJsonFromObject(i18nMap));
          }
        } catch (MissingResourceException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    }
  }

  public static void getMetadataValueLabels(MetadataValue metadataValue, Locale locale, Messages messages) {
    String labels = metadataValue.get("label");
    String labelI18N = metadataValue.get("labeli18n");
    if (labels != null && labelI18N != null) {
      Map<String, String> labelsMaps = JsonUtils.getMapFromJson(labels);
      try {
        labelsMaps.put(locale.toString(), messages.getTranslation(labelI18N));
      } catch (MissingResourceException e) {
        LOGGER.debug("Missing resource: {}", labelI18N);
      }
      labels = JsonUtils.getJsonFromObject(labelsMaps);
      metadataValue.set("label", labels);
    }
  }
}
