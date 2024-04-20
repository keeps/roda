package org.roda.wui.api.v2.utils;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.JobAlreadyStartedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CommonServicesUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonServicesUtils.class);

  public static <T extends IsRODAObject> Job createAndExecuteInternalJob(String name, SelectedItems<T> sourceObjects,
    Class<?> plugin, User user, Map<String, String> pluginParameters, String exceptionMessage)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return createAndExecuteJob(name, sourceObjects, plugin, PluginType.INTERNAL, user, pluginParameters,
      exceptionMessage);
  }

  private static <T extends IsRODAObject> Job createAndExecuteJob(String name, SelectedItems<T> sourceObjects,
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
}
