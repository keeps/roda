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
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.impl.bagit.BagitSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagitToAIPPlugin extends SIPToAIPPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitToAIPPlugin.class);
  private static final String UNPACK_DESCRIPTION = "Extracted objects from package in BagIt format.";
  private static final String METADATA_FILE = "metadata.xml";

  private boolean createSubmission = false;

  private Optional<String> computedSearchScope;
  private boolean forceSearchScope;

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "BagIt";
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
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    computedSearchScope = PluginHelper.getSearchScopeFromParameters(this, model);
    forceSearchScope = PluginHelper.getForceParentIdFromParameters(this);

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<TransferredResource>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<TransferredResource> plugin, TransferredResource object) {
        processTransferredResource(index, model, report, cachedJob, object);
      }
    }, index, model, storage, liteList);
  }

  private void processTransferredResource(IndexService index, ModelService model, Report report, Job job,
    TransferredResource transferredResource) {
    Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);
    Path bagitPath = Paths.get(transferredResource.getFullPath());

    try {
      LOGGER.debug("Converting {} to AIP", bagitPath);
      SIP bagit = BagitSIP.parse(bagitPath);

      Optional<String> computedParentId = PluginHelper.getComputedParent(model, index, bagit.getAncestors(),
        computedSearchScope, forceSearchScope, job.getId());

      AIP aipCreated = BagitToAIPPluginUtils.bagitToAip(bagit, model, METADATA_FILE,
        Arrays.asList(transferredResource.getName()), reportItem.getJobId(), computedParentId, job.getUsername(),
        PermissionUtils.getIngestPermissions(job.getUsername()), transferredResource.getUUID());

      PluginHelper.createSubmission(model, createSubmission, bagitPath, aipCreated.getId());

      createUnpackingEventSuccess(model, index, transferredResource, aipCreated, UNPACK_DESCRIPTION);
      reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(), aipCreated.getId())
        .setPluginState(PluginState.SUCCESS);

      if (aipCreated.getParentId() == null && computedParentId.isPresent()) {
        reportItem.setPluginDetails(String.format("Parent with id '%s' not found", computedParentId.get()));
      }

      createWellformedEventSuccess(model, index, transferredResource, aipCreated);
      LOGGER.debug("Done with converting {} to AIP {}", bagitPath, aipCreated.getId());
    } catch (RODAException | RuntimeException | ParseException e) {
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      LOGGER.error("Error converting " + bagitPath + " to AIP", e);
    }

    report.addReport(reportItem);
    PluginHelper.createJobReport(this, model, reportItem);
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
