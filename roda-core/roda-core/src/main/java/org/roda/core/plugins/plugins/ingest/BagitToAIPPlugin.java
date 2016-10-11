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
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
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

  private boolean createSubmission = false;

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
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION)) {
      createSubmission = Boolean.parseBoolean(getParameterValues().get(RodaConstants.PLUGIN_PARAMS_CREATE_SUBMISSION));
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.initPluginReport(this);

    try {
      String username = PluginHelper.getJobUsername(this, index);

      for (TransferredResource transferredResource : list) {
        Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);
        Path bagitPath = Paths.get(transferredResource.getFullPath());

        try {
          LOGGER.debug("Converting {} to AIP", bagitPath);
          BagFactory bagFactory = new BagFactory();
          Bag bag = bagFactory.createBag(bagitPath.toFile());
          SimpleResult result = bag.verifyPayloadManifests();
          if (result.isSuccess()) {
            String parentId = PluginHelper.computeParentId(this, index, bag.getBagInfoTxt().get("parent"));

            AIP aipCreated = BagitToAIPPluginUtils.bagitToAip(bag, bagitPath, model, "metadata.xml",
              Arrays.asList(transferredResource.getName()), reportItem.getJobId(), parentId, username);

            PluginHelper.createSubmission(model, createSubmission, bagitPath, aipCreated.getId());

            createUnpackingEventSuccess(model, index, transferredResource, aipCreated, UNPACK_DESCRIPTION);
            reportItem.setOutcomeObjectId(aipCreated.getId()).setPluginState(PluginState.SUCCESS);

            if (aipCreated.getParentId() == null) {
              reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
            }

            createWellformedEventSuccess(model, index, transferredResource, aipCreated);
            LOGGER.debug("Done with converting {} to AIP {}", bagitPath, aipCreated.getId());
          } else {
            reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(result.getMessages() + "");
          }
        } catch (RODAException | RuntimeException e) {
          reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());

          LOGGER.error("Error converting " + bagitPath + " to AIP", e);
        }
        report.addReport(reportItem);
        PluginHelper.createJobReport(this, model, reportItem);
      }
    } catch (RODAException e) {
      LOGGER.error("Error getting job from plugin", e);
    }

    return report;
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

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }
}
