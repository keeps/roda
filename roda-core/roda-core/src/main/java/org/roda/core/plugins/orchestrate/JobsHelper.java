/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
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
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final int DEFAULT_BLOCK_SIZE = 100;

  private JobsHelper() {

  }

  public static int getMaxNumberOfJobsInParallel() {
    int defaultMaxNumberOfJobsInParallel = Runtime.getRuntime().availableProcessors() + 1;

    return RodaCoreFactory.getRodaConfiguration().getInt("core.orchestrator.max_jobs_in_parallel",
      defaultMaxNumberOfJobsInParallel);
  }

  public static int getNumberOfJobsWorkers() {
    int defaultNumberOfJobsWorkers = Runtime.getRuntime().availableProcessors() + 1;

    return RodaCoreFactory.getRodaConfiguration().getInt("core.orchestrator.nr_of_jobs_workers",
      defaultNumberOfJobsWorkers);
  }

  public static int getBlockSize() {
    String key = "core.orchestrator.block_size";
    return RodaCoreFactory.getRodaConfiguration().getInt(key, DEFAULT_BLOCK_SIZE);
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

  public static List<TransferredResource> getTransferredResources(IndexService index, List<String> uuids)
    throws NotFoundException, GenericException {
    List<TransferredResource> ret = index.retrieve(TransferredResource.class, uuids);
    if (ret.isEmpty()) {
      throw new NotFoundException("Could not retrive the Transferred Resources");
    }
    return ret;
  }

  public static List<AIP> getAIPs(ModelService model, IndexService index, List<String> uuids, boolean retrieveFromModel)
    throws NotFoundException {
    List<AIP> aipsToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      if (retrieveFromModel) {
        for (String uuid : uuids) {
          try {
            aipsToReturn.add(model.retrieveAIP(uuid));
          } catch (RODAException | RuntimeException e) {
            LOGGER.error("Error while retrieving AIP from model", e);
          }
        }
      } else {
        try {
          List<IndexedAIP> retrieve = index.retrieve(IndexedAIP.class, uuids);

          for (IndexedAIP indexedAIP : retrieve) {
            aipsToReturn.add(model.retrieveAIP(indexedAIP.getId()));
          }

        } catch (RODAException | RuntimeException e) {
          LOGGER.error("Error while retrieving AIP from index", e);
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
    List<Representation> representationsToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      try {
        List<IndexedRepresentation> retrieve = index.retrieve(IndexedRepresentation.class, uuids);

        for (IndexedRepresentation indexedRepresentation : retrieve) {
          representationsToReturn
            .add(model.retrieveRepresentation(indexedRepresentation.getAipId(), indexedRepresentation.getId()));
        }

      } catch (RODAException | RuntimeException e) {
        LOGGER.error("Error while retrieving representations from index", e);
      }
    }

    if (representationsToReturn.isEmpty()) {
      throw new NotFoundException("Could not retrive the Representations");
    }

    return representationsToReturn;
  }

  public static List<File> getFiles(ModelService model, IndexService index, List<String> uuids)
    throws NotFoundException {
    List<File> filesToReturn = new ArrayList<>();

    if (!uuids.isEmpty()) {
      try {
        List<IndexedFile> retrieve = index.retrieve(IndexedFile.class, uuids);

        for (IndexedFile indexedFile : retrieve) {
          filesToReturn.add(model.retrieveFile(indexedFile.getAipId(), indexedFile.getRepresentationId(),
            indexedFile.getPath(), indexedFile.getId()));
        }

      } catch (RODAException | RuntimeException e) {
        LOGGER.error("Error while retrieving files from index", e);
      }
    }

    if (filesToReturn.isEmpty()) {
      throw new NotFoundException("Could not retrive the Files");
    }

    return filesToReturn;
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

  public static <T extends IsRODAObject> void doJobObjectsCleanup(Job job, ModelService modelService,
    IndexService indexService) {

    if (RodaCoreFactory.getNodeType() == NodeType.MASTER) {
      try {
        // make sure the index is up to date
        indexService.commit(IndexedAIP.class);
        // find all AIPs that should be removed
        Filter filter = new Filter();
        filter.add(new SimpleFilterParameter(RodaConstants.INGEST_JOB_ID, job.getId()));
        filter.add(new OneOfManyFilterParameter(RodaConstants.AIP_STATE,
          Arrays.asList(AIPState.CREATED.toString(), AIPState.INGEST_PROCESSING.toString())));
        Sublist sublist = new Sublist();

        IndexResult<IndexedAIP> aipsResult;
        int offset = 0;
        do {
          sublist.setFirstElementIndex(offset);
          aipsResult = indexService.find(IndexedAIP.class, filter, Sorter.NONE, sublist);
          offset += aipsResult.getLimit();
          doJobCleanup(modelService, aipsResult.getResults());
        } while (aipsResult.getTotalCount() > aipsResult.getOffset() + aipsResult.getLimit());
      } catch (GenericException | RequestNotValidException e) {
        LOGGER.error("Error doing Job cleanup", e);
      }
    }
  }

  private static void doJobCleanup(ModelService modelService, List<IndexedAIP> results) {
    for (IndexedAIP indexedAIP : results) {
      try {
        LOGGER.info("Job cleanup: deleting AIP {}", indexedAIP.getId());
        modelService.deleteAIP(indexedAIP.getId());
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
        LOGGER.error("Error doing deleting AIP during Job cleanup", e);
      }
    }
  }

}
