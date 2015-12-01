/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.NotFoundException;
import org.roda.core.data.v2.Job;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.index.IndexServiceException;
import org.roda.core.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;

public class AkkaCoordinatorActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaCoordinatorActor.class);

  public AkkaCoordinatorActor() {

  }

  @Override
  public void onReceive(Object msg) throws Exception {
    if (msg instanceof Job) {
      Job job = (Job) msg;
      if ("runPluginOnTransferredResources".equalsIgnoreCase(job.getOrchestratorMethod())) {
        Plugin<TransferredResource> plugin = (Plugin<TransferredResource>) RodaCoreFactory.getPluginManager()
          .getPlugin(job.getPlugin());
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("job.id", job.getId());
        try {
          plugin.setParameterValues(parameters);
        } catch (InvalidParameterException e) {
          LOGGER.error("Error setting plug-in parameters", e);
        }

        RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(plugin,
          getTransferredResourcesFromObjectIds(job.getUsername(), job.getObjectIds()));
      }
    }
  }

  public List<TransferredResource> getTransferredResourcesFromObjectIds(String username, List<String> objectIds)
    throws NotFoundException {
    List<TransferredResource> res = new ArrayList<TransferredResource>();
    for (String objectId : objectIds) {
      try {
        res.add(RodaCoreFactory.getIndexService().retrieve(TransferredResource.class, username + "/" + objectId));
      } catch (IndexServiceException e) {
        LOGGER.error("Error retrieving TransferredResource", e);
      }
    }
    return res;
  }

}