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
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.impl.eark.EARKSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPlugin extends SIPToAIPPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPlugin.class);

  public static String UNPACK_DESCRIPTION = "Extracted objects from package in E-ARK SIP format.";

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "E-ARK SIP";
  }

  @Override
  public String getDescription() {
    return "E-ARK SIP as a zip file";
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

    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage, List<TransferredResource> list)
    throws PluginException {
    Report report = PluginHelper.createPluginReport(this);

    for (TransferredResource transferredResource : list) {
      Report reportItem = PluginHelper.createPluginReportItem(this, transferredResource);

      Path earkSIPPath = Paths.get(transferredResource.getFullPath());
      LOGGER.debug("Converting {} to AIP", earkSIPPath);

      transformTransferredResourceIntoAnAIP(index, model, storage, transferredResource, earkSIPPath, reportItem);
      report.addReport(reportItem);

      PluginHelper.createJobReport(this, model, reportItem);

    }
    return report;
  }

  private void transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    TransferredResource transferredResource, Path earkSIPPath, Report reportItem) {
    SIP sip = null;
    try {
      sip = EARKSIP.parse(earkSIPPath);
      if (sip.getValidationReport().isValid()) {
        String parentId = PluginHelper.getParentId(this, index, sip.getParentID());
        AIP aipCreated = EARKSIPToAIPPluginUtils.earkSIPToAIP(sip, earkSIPPath, model, storage, parentId);

        createUnpackingEventSuccess(model, index, transferredResource, aipCreated, UNPACK_DESCRIPTION);
        reportItem.setItemId(aipCreated.getId()).setPluginState(PluginState.SUCCESS);

        if (sip.getParentID() != null && aipCreated.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", sip.getParentID()));
        }
        createWellformedEventSuccess(model, index, transferredResource, aipCreated);
        LOGGER.debug("Done with converting {} to AIP {}", earkSIPPath, aipCreated.getId());
      } else {
        reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(sip.getValidationReport().toString());
        LOGGER.debug("The SIP {} is not valid", earkSIPPath);
      }

    } catch (Throwable e) {
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      LOGGER.error("Error converting " + earkSIPPath + " to AIP", e);
    } finally {
      if (sip != null) {
        FSUtils.deletePathQuietly(sip.getBasePath());
      }
    }
  }

  @Override
  public Report afterBlockExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {

    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new EARKSIPToAIPPlugin();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

}
