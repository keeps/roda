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
import org.roda.core.data.v2.IdUtils.LinkingObjectType;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.JobReport.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.ReportItem;
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

  private static final String EVENT_DESCRIPTION = "Extracted objects from package in BagIt format.";
  private static final String EVENT_SUCESS_MESSAGE = "The SIP has been successfuly unpacked.";
  private static final String EVENT_FAILURE_MESSAGE = "The ingest process failed to unpack the SIP.";

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
    PluginState state;

    String jobDefinedParentId = PluginHelper.getParentIdFromParameters(getParameterValues());
    boolean jobDefinedForceParentId = PluginHelper.getForceParentIdFromParameters(getParameterValues());

    for (TransferredResource transferredResource : list) {
      Path bagitPath = Paths.get(transferredResource.getFullPath());

      ReportItem reportItem = PluginHelper.createPluginReportItem(transferredResource, this);

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

        state = PluginState.SUCCESS;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, aipCreated.getId(),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()));

        if (aipCreated.getParentId() == null) {
          reportItem = reportItem
            .addAttribute(new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, "Parent not found: " + parentId));
        }

        List<LinkingIdentifier> sources = Arrays.asList(PluginHelper.getLinkingIdentifier(transferredResource));
        List<LinkingIdentifier> outcomes = Arrays
          .asList(PluginHelper.getLinkingIdentifier(LinkingObjectType.AIP, aipCreated.getId(), null, null, null));
        boolean notify = true;
        String eventType = RodaConstants.PRESERVATION_EVENT_TYPE_INGEST_START;
        String outcome = PluginState.SUCCESS.name();
        PluginHelper.createPluginEvent(this, aipCreated.getId(), null, null, null, model, eventType, EVENT_DESCRIPTION,
          sources, outcomes, outcome, EVENT_SUCESS_MESSAGE, "", notify);

        LOGGER.debug("Done with converting " + bagitPath + " to AIP " + aipCreated.getId());
      } catch (Throwable e) {
        state = PluginState.FAILURE;
        reportItem = PluginHelper.setPluginReportItemInfo(reportItem, null,
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME, state.toString()),
          new Attribute(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS, e.getMessage()));

        LOGGER.error("Error converting " + bagitPath + " to AIP", e);
      }
      report.addItem(reportItem);
      PluginHelper.createJobReport(model, this, reportItem, state);
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

}
