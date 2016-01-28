/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.ORCHESTRATOR_METHOD;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class AkkaJobWorkerActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobWorkerActor.class);

  public AkkaJobWorkerActor() {

  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Job) {
      Job job = (Job) msg;
      Plugin<?> plugin = (Plugin<?>) RodaCoreFactory.getPluginManager().getPlugin(job.getPlugin());
      setPluginParameters(job, plugin);

      if (ORCHESTRATOR_METHOD.ON_TRANSFERRED_RESOURCES == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources((Plugin<TransferredResource>) plugin,
          getTransferredResourcesFromObjectIds(job.getObjectIds()));
      } else if (ORCHESTRATOR_METHOD.ON_ALL_AIPS == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnAllAIPs((Plugin<AIP>) plugin);
      } else if (ORCHESTRATOR_METHOD.ON_AIPS == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs((Plugin<AIP>) plugin, job.getObjectIds());
      } else if (ORCHESTRATOR_METHOD.RUN_PLUGIN == job.getOrchestratorMethod()) {
        RodaCoreFactory.getPluginOrchestrator().runPlugin(plugin);
      }
    }
  }

  private <T extends Serializable> void setPluginParameters(Job job, Plugin<T> plugin) {
    Map<String, String> parameters = new HashMap<String, String>(job.getPluginParameters());
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, job.getId());
    try {
      plugin.setParameterValues(parameters);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error setting plug-in parameters", e);
    }
  }

  public List<TransferredResource> getTransferredResourcesFromObjectIds(List<String> objectIds) {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    for (String objectId : objectIds) {
      try {
        // TODO review error handling logic
        res.add(RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, objectId));
      } catch (GenericException | NotFoundException e) {
        LOGGER.error("Error retrieving TransferredResource", e);
      }
    }
    return res;
  }

}