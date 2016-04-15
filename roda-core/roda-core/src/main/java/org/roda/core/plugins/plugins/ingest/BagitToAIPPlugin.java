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
import java.util.List;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.utilities.SimpleResult;

public class BagitToAIPPlugin extends SIPToAIPPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPlugin.class);

  private static String UNPACK_DESCRIPTION = "Extracted objects from package in Bagit format.";

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
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report beforeBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (TransferredResource transferredResource : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);
      Path bagitPath = Paths.get(transferredResource.getFullPath());

      try {
        LOGGER.debug("Converting {} to AIP", bagitPath);
        BagFactory bagFactory = new BagFactory();
        Bag bag = bagFactory.createBag(bagitPath.toFile());
        SimpleResult result = bag.verifyPayloadManifests();
        if (!result.isSuccess()) {
          throw new BagitNotValidException(result.getMessages() + "");
        }

        String parentId = PluginHelper.getParentId(this, index, bag.getBagInfoTxt().get("parent"));

        AIP aipCreated = BagitToAIPPluginUtils.bagitToAip(bag, bagitPath, model, "metadata.xml", parentId);
        createUnpackingEventSuccess(model, index, transferredResource, aipCreated, UNPACK_DESCRIPTION);
        reportItem.setItemId(aipCreated.getId()).setPluginState(PluginState.SUCCESS);

        if (aipCreated.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
        }

        createWellformedEventSuccess(model, index, transferredResource, aipCreated);
        LOGGER.debug("Done with converting {} to AIP {}", bagitPath, aipCreated.getId());
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
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new BagitToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
