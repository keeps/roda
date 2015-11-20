/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.NotImplementedException;
import org.roda.core.data.v2.RodaUser;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.plugins.Plugin;
import org.roda.wui.api.v1.entities.Job;
import org.roda.wui.common.client.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobsHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  private static final List<String> ORCHESTRATOR_METHODS = Arrays.asList("runPluginOnTransferredResources");
  private static final List<String> RESOURCE_TYPES = Arrays.asList("bagit");

  protected static void validateCreateJob(Job job) throws NotImplementedException {
    boolean valid = false;
    if (ORCHESTRATOR_METHODS.contains(job.getOrchestratorMethod())) {
      valid = true;
    }
    if (RESOURCE_TYPES.contains(job.getResourceType())) {
      valid = valid && true;
    }

    if (!valid) {
      throw new NotImplementedException();
    }
  }

  protected static Job createJob(RodaUser user, Job job) {
    Job updatedJob = new Job(job);

    // send job to the orchestrator and return right away
    if ("runPluginOnTransferredResources".equalsIgnoreCase(job.getOrchestratorMethod())) {
      RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(
        (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager().getPlugin(updatedJob.getPlugin()),
        getTransferredResourcesFromObjectIds(user, updatedJob.getObjectIds()));
    }
    return updatedJob;
  }

  private static List<TransferredResource> getTransferredResourcesFromObjectIds(RodaUser user, List<String> objectIds) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    for (String objectId : objectIds) {
      try {
        res.add(BrowserHelper.retrieveTransferredResource(user.getId() + "/" + objectId));
      } catch (GenericException e) {
        LOGGER.error("Error retrieving transferred resource {}", objectId, e);
      }
    }
    return res;
  }

}
