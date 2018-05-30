/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.orchestrate.akka.Messages;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final String NUMBER_OF_JOB_WORKERS_PROPERTY = "core.orchestrator.nr_of_jobs_workers";
  private static final String BLOCK_SIZE_PROPERTY = "core.orchestrator.block_size";
  private static final int DEFAULT_BLOCK_SIZE = 100;
  private static final String SYNC_TIMEOUT_PROPERTY = "core.orchestrator.sync_timeout";
  private static final int DEFAULT_SYNC_TIMEOUT = 600;
  private static final String MAX_JOBS_IN_PARALLEL_PROPERTY = "core.orchestrator.max_jobs_in_parallel";

  private JobsHelper() {
    // do nothing
  }

  public static int getMaxNumberOfJobsInParallel() {
    int defaultMaxNumberOfJobsInParallel = Runtime.getRuntime().availableProcessors() + 1;

    return RodaCoreFactory.getRodaConfiguration().getInt(MAX_JOBS_IN_PARALLEL_PROPERTY,
      defaultMaxNumberOfJobsInParallel);
  }

  public static void setNumberOfJobsWorkers(int numberOfJobWorkers) {
    RodaCoreFactory.getRodaConfiguration().setProperty(NUMBER_OF_JOB_WORKERS_PROPERTY, numberOfJobWorkers);
  }

  public static int getNumberOfJobsWorkers() {
    int defaultNumberOfJobsWorkers = Runtime.getRuntime().availableProcessors() + 1;
    return RodaCoreFactory.getRodaConfiguration().getInt(NUMBER_OF_JOB_WORKERS_PROPERTY, defaultNumberOfJobsWorkers);
  }

  public static int getBlockSize() {
    return RodaCoreFactory.getRodaConfiguration().getInt(BLOCK_SIZE_PROPERTY, DEFAULT_BLOCK_SIZE);
  }

  public static void setBlockSize(int blockSize) {
    RodaCoreFactory.getRodaConfiguration().setProperty(BLOCK_SIZE_PROPERTY, blockSize);
  }

  public static int getSyncTimeout() {
    return RodaCoreFactory.getRodaConfiguration().getInt(SYNC_TIMEOUT_PROPERTY, DEFAULT_SYNC_TIMEOUT);
  }

  public static void setSyncTimeout(int syncTimeout) {
    RodaCoreFactory.getRodaConfiguration().setProperty(SYNC_TIMEOUT_PROPERTY, syncTimeout);
  }

  public static <T extends IsRODAObject> void updateJobState(Plugin<T> plugin, ModelService model, JOB_STATE state,
    Optional<String> stateDetails) {
    try {
      Job job = PluginHelper.getJob(plugin, model);
      job.setState(state);
      if (stateDetails.isPresent()) {
        job.setStateDetails(stateDetails.get());
      }
      if (job.isInFinalState()) {
        job.setEndDate(new Date());
      }

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  public static void updateJobState(Job job, ModelService model, JOB_STATE state, Optional<String> stateDetails) {
    try {
      Job jobFromModel = PluginHelper.getJob(job.getId(), model);
      jobFromModel.setState(state);
      if (stateDetails.isPresent()) {
        jobFromModel.setStateDetails(stateDetails.get());
      }
      if (jobFromModel.isInFinalState()) {
        jobFromModel.setEndDate(new Date());
      }

      model.createOrUpdateJob(jobFromModel);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  public static <T extends IsRODAObject> void updateJobObjectsCount(Plugin<T> plugin, ModelService model,
    Long objectsCount) {
    try {
      Job job = PluginHelper.getJob(plugin, model);
      job.getJobStats().setSourceObjectsCount(objectsCount.intValue())
        .setSourceObjectsWaitingToBeProcessed(objectsCount.intValue());

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  public static <T extends IsRODAObject> void updateJobInformation(Plugin<T> plugin, ModelService model,
    JobPluginInfo jobPluginInfo) {

    // update job
    try {
      LOGGER.debug("New job completionPercentage: {}", jobPluginInfo.getCompletionPercentage());
      Job job = PluginHelper.getJob(plugin, model);
      job = setJobCounters(job, jobPluginInfo);

      model.createOrUpdateJob(job);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.error("Unable to get or update Job from model", e);
    }
  }

  private static Job setJobCounters(Job job, JobPluginInfo jobPluginInfo) {
    JobStats jobStats = job.getJobStats();

    jobStats.setCompletionPercentage(jobPluginInfo.getCompletionPercentage());
    jobStats.setSourceObjectsCount(jobPluginInfo.getSourceObjectsCount());
    jobStats.setSourceObjectsBeingProcessed(jobPluginInfo.getSourceObjectsBeingProcessed());
    jobStats.setSourceObjectsProcessedWithSuccess(jobPluginInfo.getSourceObjectsProcessedWithSuccess());
    jobStats.setSourceObjectsProcessedWithFailure(jobPluginInfo.getSourceObjectsProcessedWithFailure());
    jobStats
      .setSourceObjectsWaitingToBeProcessed(jobStats.getSourceObjectsCount() - jobStats.getSourceObjectsBeingProcessed()
        - jobStats.getSourceObjectsProcessedWithFailure() - jobStats.getSourceObjectsProcessedWithSuccess());
    jobStats.setOutcomeObjectsWithManualIntervention(jobPluginInfo.getOutcomeObjectsWithManualIntervention());
    return job;
  }

  /**
   * Updates the job state
   */
  public static <T extends IsRODAObject> void updateJobStateAsync(Plugin<T> plugin, JOB_STATE state,
    Optional<String> stateDetails) {
    RodaCoreFactory.getPluginOrchestrator().updateJobAsync(plugin,
      new Messages.JobStateUpdated(plugin, state, stateDetails));
  }

  public static <T extends IsRODAObject> void updateJobStateAsync(Plugin<T> plugin, JOB_STATE state,
    Throwable throwable) {
    updateJobStateAsync(plugin, state,
      Optional.ofNullable(throwable.getClass().getName() + ": " + throwable.getMessage()));
  }

  public static Job updateJobInTheStateStartedOrCreated(Job job) {
    job.setState(JOB_STATE.FAILED_TO_COMPLETE);
    JobStats jobStats = job.getJobStats();
    jobStats.setSourceObjectsBeingProcessed(0);
    jobStats.setSourceObjectsWaitingToBeProcessed(0);
    jobStats.setSourceObjectsProcessedWithFailure(
      jobStats.getSourceObjectsCount() - jobStats.getSourceObjectsProcessedWithSuccess());
    job.setEndDate(new Date());
    return job;
  }

  public static <T extends IsRODAObject> void setPluginParameters(Plugin<T> plugin, Job job) {
    Map<String, String> parameters = new HashMap<>(job.getPluginParameters());
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    try {
      plugin.setParameterValues(parameters);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error setting plugin parameters", e);
    }
  }

  public static List<TransferredResource> getTransferredResources(IndexService index, List<String> uuids)
    throws NotFoundException, GenericException, RequestNotValidException {
    List<TransferredResource> ret = index.retrieve(TransferredResource.class, uuids, new ArrayList<>());
    if (ret.isEmpty()) {
      throw new NotFoundException("Could not retrieve the Transferred Resources");
    }
    return ret;
  }

  public static List<AIP> getAIPs(ModelService model, List<String> uuids) throws NotFoundException {
    List<AIP> aipsToReturn = new ArrayList<>();
    if (!uuids.isEmpty()) {
      for (String uuid : uuids) {
        try {
          aipsToReturn.add(model.retrieveAIP(uuid));
        } catch (RODAException | RuntimeException e) {
          LOGGER.error("Error while retrieving AIP from model", e);
        }
      }
    }

    if (aipsToReturn.isEmpty()) {
      throw new NotFoundException("Could not retrieve the AIPs");
    }

    return aipsToReturn;
  }

  public static List<Representation> getRepresentations(ModelService model, IndexService index, List<String> uuids)
    throws NotFoundException {

    if (!uuids.isEmpty()) {
      try {
        List<IndexedRepresentation> retrieve = index.retrieve(IndexedRepresentation.class, uuids, new ArrayList<>());
        List<Representation> representationsToReturn = getRepresentationFromList(model, retrieve);

        if (representationsToReturn.isEmpty()) {
          throw new NotFoundException("Could not retrieve the Representations");
        }

        return representationsToReturn;
      } catch (RODAException | RuntimeException e) {
        LOGGER.error("Error while retrieving representations from index", e);
      }
    }

    throw new NotFoundException("Could not retrieve the Representations");
  }

  private static List<Representation> getRepresentationFromList(ModelService model,
    List<IndexedRepresentation> retrieve)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<Representation> representationsToReturn = new ArrayList<>();
    for (IndexedRepresentation indexedRepresentation : retrieve) {
      representationsToReturn
        .add(model.retrieveRepresentation(indexedRepresentation.getAipId(), indexedRepresentation.getId()));
    }
    return representationsToReturn;
  }

  public static List<File> getFiles(ModelService model, IndexService index, List<String> uuids)
    throws NotFoundException {
    if (!uuids.isEmpty()) {
      try {
        List<IndexedFile> retrieve = index.retrieve(IndexedFile.class, uuids, new ArrayList<>());
        List<File> filesToReturn = getFilesFromList(model, retrieve);

        if (filesToReturn.isEmpty()) {
          throw new NotFoundException("Could not retrieve the Files");
        }

        return filesToReturn;

      } catch (RODAException | RuntimeException e) {
        LOGGER.error("Error while retrieving files from index", e);
      }
    }

    throw new NotFoundException("Could not retrieve the Files");
  }

  private static List<File> getFilesFromList(ModelService model, List<IndexedFile> retrieve)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    List<File> filesToReturn = new ArrayList<>();
    for (IndexedFile indexedFile : retrieve) {
      filesToReturn.add(model.retrieveFile(indexedFile.getAipId(), indexedFile.getRepresentationId(),
        indexedFile.getPath(), indexedFile.getId()));
    }
    return filesToReturn;
  }

  public static <T extends IsRODAObject> List<T> getObjectsFromUUID(ModelService model, IndexService index,
    Class<T> objectClass, List<String> uuids) throws NotFoundException, GenericException, RequestNotValidException {
    if (AIP.class.equals(objectClass)) {
      return (List<T>) getAIPs(model, uuids);
    } else if (Representation.class.equals(objectClass)) {
      return (List<T>) getRepresentations(model, index, uuids);
    } else if (File.class.equals(objectClass)) {
      return (List<T>) getFiles(model, index, uuids);
    } else {
      return getObjectsFromIndex(index, objectClass, uuids);
    }
  }

  public static <T extends IsRODAObject, T1 extends IsIndexed> List<T> getObjectsFromIndexObjects(ModelService model,
    Class<T> objectClass, List<T1> indexObjects)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    if (AIP.class.equals(objectClass)) {
      return (List<T>) getAIPs(model, indexObjects.stream().map(e -> e.getUUID()).collect(Collectors.toList()));
    } else if (Representation.class.equals(objectClass)) {
      return (List<T>) getRepresentationFromList(model, (List<IndexedRepresentation>) indexObjects);
    } else if (File.class.equals(objectClass)) {
      return (List<T>) getFilesFromList(model, (List<IndexedFile>) indexObjects);
    } else {
      return (List<T>) indexObjects;
    }
  }

  public static <T extends IsRODAObject> List<T> getObjectsFromIndex(IndexService index, Class<T> objectClass,
    List<String> uuids) throws NotFoundException, GenericException, RequestNotValidException {
    List<T> ret = (List<T>) index.retrieve((Class<IsIndexed>) objectClass, uuids, new ArrayList<>());
    if (ret.isEmpty()) {
      throw new NotFoundException("Could not retrieve the " + objectClass.getSimpleName());
    }
    return ret;
  }

  public static Class<IsRODAObject> getSelectedClassFromString(String selectedClass) throws GenericException {
    try {
      Class<?> clazz = Class.forName(selectedClass);
      if (IsRODAObject.class.isAssignableFrom(clazz)) {
        return (Class<IsRODAObject>) clazz;
      } else {
        throw new GenericException("Error while getting class from string");
      }
    } catch (ClassNotFoundException e) {
      throw new GenericException("Error while getting class from string");
    }
  }

  public static Class<IsIndexed> getIsIndexedSelectedClassFromString(String selectedClass) throws GenericException {
    try {
      Class<?> clazz = Class.forName(selectedClass);
      if (IsIndexed.class.isAssignableFrom(clazz)) {
        try {
          return (Class<IsIndexed>) clazz;
        } catch (ClassCastException e) {
          LOGGER.error("Error while casting class to IsIndexed", e);
          // do nothing and let exception in the end of the method be thrown
        }
      }
    } catch (ClassNotFoundException e) {
      // do nothing and let exception in the end of the method be thrown
      LOGGER.error("Class not found", e);
    }

    throw new GenericException("Error while getting class from string");
  }

  public static IterableIndexResult<Job> findUnfinishedJobs(IndexService index)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new OneOfManyFilterParameter(RodaConstants.JOB_STATE, Job.nonFinalStateList()));
    return index.findAll(Job.class, filter, Collections.emptyList());
  }

  public static void cleanJobObjects(Job job, ModelService model, IndexService index) {
    if (RodaCoreFactory.getNodeType() == NodeType.MASTER) {
      // find all AIPs that should be removed
      Filter filter = new Filter();
      // FIXME 20161128 hsilva: perhaps we should avoid ghosts???
      // FIXME 20170308 nvieira: it should rollback if job is update
      filter.add(new SimpleFilterParameter(RodaConstants.INGEST_JOB_ID, job.getId()));
      filter.add(new OneOfManyFilterParameter(RodaConstants.AIP_STATE,
        Arrays.asList(AIPState.CREATED.toString(), AIPState.INGEST_PROCESSING.toString())));

      try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, filter, false,
        Arrays.asList(RodaConstants.INDEX_UUID))) {

        for (IndexedAIP aip : result) {
          String aipId = aip.getUUID();
          try {
            LOGGER.info("Deleting AIP {} during job {} cleanup", aipId, job.getId());
            model.deleteAIP(aipId);
          } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
            LOGGER.error("Error deleting AIP {} during job {} cleanup", aipId, job.getId(), e);
          }
        }
      } catch (IOException | GenericException | RequestNotValidException e) {
        LOGGER.error("Error getting AIP iterator when cleaning job objects", e);
      }
    }
  }

  public static void createJobWorkingDirectory(String jobId) {
    Path path = RodaCoreFactory.getWorkingDirectory().resolve(jobId);
    try {
      Files.createDirectory(path);
    } catch (IOException e) {
      LOGGER.error("Error while creating job working directory (path='{}')", path);
    }
  }

  public static void deleteJobWorkingDirectory(String jobId) {
    Path path = RodaCoreFactory.getWorkingDirectory().resolve(jobId);
    try {
      FSUtils.deletePath(path);
    } catch (NotFoundException | GenericException e) {
      LOGGER.error("Error while deleting job working directory (path='{}')", path);
    }
  }

}
