/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.v2.IdUtils.LinkingObjectType;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class BagitToAIPPlugin extends AbstractPlugin<TransferredResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPlugin.class);

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Bagit";
  }

  @Override
  public String getDescription() {
    return "BagIt as a zip file";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    String jobDefinedParentId = PluginHelper.getParentIdFromParameters(this);
    boolean jobDefinedForceParentId = PluginHelper.getForceParentIdFromParameters(this);

    for (TransferredResource transferredResource : list) {
      Path bagitPath = Paths.get(transferredResource.getFullPath());

      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);

      try {
        LOGGER.debug("Converting " + bagitPath + " to AIP");
        BagFactory bagFactory = new BagFactory();
        Bag bag = bagFactory.createBag(bagitPath.toFile());
        SimpleResult result = bag.verifyPayloadManifests();
        if (!result.isSuccess()) {
          throw new BagitNotValidException(result.getMessages() + "");
        }

        String parentId = PluginHelper.getParentId(bag.getBagInfoTxt().get("parent"), jobDefinedParentId,
          jobDefinedForceParentId);

        AIP aipCreated = BagitToAIPPluginUtils.bagitToAip(bag, bagitPath, model, "metadata.xml", parentId);

        reportItem.setItemId(aipCreated.getId()).setPluginState(PluginState.SUCCESS);

        if (aipCreated.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
        }

        List<LinkingIdentifier> sources = Arrays.asList(
          PluginHelper.getLinkingIdentifier(transferredResource, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
        List<LinkingIdentifier> outcomes = Arrays.asList(PluginHelper.getLinkingIdentifier(LinkingObjectType.AIP,
          aipCreated.getId(), null, null, null, RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));
        boolean notify = true;
        PluginHelper.createPluginEvent(this, aipCreated.getId(), null, null, null, model, sources, outcomes,
          reportItem.getPluginState(), "", notify);

        LOGGER.debug("Done with converting " + bagitPath + " to AIP " + aipCreated.getId());
      } catch (Throwable e) {
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());

        LOGGER.error("Error converting " + bagitPath + " to AIP", e);
      }
      report.addReport(reportItem);
      PluginHelper.createJobReport(this, model, reportItem);
    }

    return report;
  }

  @Override
  public Report beforeExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Report afterExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {

    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new BagitToAIPPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.SIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.INGEST_START;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Extracted objects from package in BagIt format.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The SIP has been successfuly unpacked.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The ingest process failed to unpack the SIP.";
  }

}
