/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip.utils.IPEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPlugin extends SIPToAIPPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPlugin.class);

  public static String UNPACK_DESCRIPTION = "Extracted objects from package in E-ARK SIP format.";

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
    return "E-ARK SIP";
  }

  @Override
  public String getDescription() {
    // return "E-ARK SIP as a zip file (with support for Update SIPs).";
    return "E-ARK SIP as a zip file.";
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
    String jobId = PluginHelper.getJobId(this);
    Optional<String> computedSearchScope = PluginHelper.getSearchScopeFromParameters(this, model);
    Path jobWorkingDirectory = PluginHelper.getJobWorkingDirectory(this);
    boolean forceSearchScope = PluginHelper.getForceParentIdFromParameters(this);

    for (TransferredResource transferredResource : list) {
      Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);

      Path earkSIPPath = Paths.get(transferredResource.getFullPath());
      LOGGER.debug("Converting {} to AIP", earkSIPPath);

      transformTransferredResourceIntoAnAIP(index, model, storage, transferredResource, earkSIPPath, createSubmission,
        reportItem, jobId, computedSearchScope, forceSearchScope, jobWorkingDirectory);
      report.addReport(reportItem);

      PluginHelper.createJobReport(this, model, reportItem);

    }
    return report;
  }

  private void transformTransferredResourceIntoAnAIP(IndexService index, ModelService model, StorageService storage,
    TransferredResource transferredResource, Path earkSIPPath, boolean createSubmission, Report reportItem,
    String jobId, Optional<String> computedSearchScope, boolean forceSearchScope, Path jobWorkingDirectory) {
    SIP sip = null;
    AIP aip;
    try {
      sip = EARKSIP.parse(earkSIPPath, jobWorkingDirectory);

      reportItem.setSourceObjectOriginalIds(sip.getIds());

      if (sip.getValidationReport().isValid()) {

        Optional<String> parentId = PluginHelper.getComputedParent(model, index, sip.getAncestors(),
          computedSearchScope, forceSearchScope, jobId);

        if (IPEnums.IPStatus.NEW == sip.getStatus()) {
          aip = processNewSIP(index, model, storage, reportItem, sip, parentId);
        } else if (IPEnums.IPStatus.UPDATE == sip.getStatus()) {
          aip = processUpdateSIP(index, model, storage, sip);
        } else {
          throw new GenericException("Unknown IP Status: " + sip.getStatus());
        }

        // put SIP inside the created AIP (if it is supposed to do so)
        PluginHelper.createSubmission(model, createSubmission, earkSIPPath, aip.getId());

        createUnpackingEventSuccess(model, index, transferredResource, aip, UNPACK_DESCRIPTION);
        reportItem.setOutcomeObjectId(aip.getId()).setPluginState(PluginState.SUCCESS);

        if (sip.getAncestors() != null && !sip.getAncestors().isEmpty() && aip.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
        }
        createWellformedEventSuccess(model, index, transferredResource, aip);
        LOGGER.debug("Done with converting {} to AIP {}", earkSIPPath, aip.getId());
      } else {
        reportItem.setPluginState(PluginState.FAILURE).setHtmlPluginDetails(true)
          .setPluginDetails(sip.getValidationReport().toHtml(true, true, true, false, false));
        LOGGER.debug("The SIP {} is not valid", earkSIPPath);
      }

    } catch (RODAException | ParseException | RuntimeException | IOException e) {
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      LOGGER.error("Error converting " + earkSIPPath + " to AIP", e);
    } finally {
      if (sip != null) {
        Path transferredResourcesAbsolutePath = RodaCoreFactory.getTransferredResourcesScanner().getBasePath()
          .toAbsolutePath();
        if (!sip.getBasePath().toAbsolutePath().toString().startsWith(transferredResourcesAbsolutePath.toString())) {
          FSUtils.deletePathQuietly(sip.getBasePath());
        }
      }
    }
  }

  private AIP processNewSIP(IndexService index, ModelService model, StorageService storage, Report reportItem, SIP sip,
    Optional<String> computedParentId) throws NotFoundException, GenericException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException, ValidationException, IOException {
    String jobUsername = PluginHelper.getJobUsername(this, index);
    Permissions fullPermissions = new Permissions();

    // Create the permissions object for the user that created the job
    fullPermissions.setUserPermissions(jobUsername,
      new HashSet<>(Arrays.asList(Permissions.PermissionType.CREATE, Permissions.PermissionType.READ,
        Permissions.PermissionType.UPDATE, Permissions.PermissionType.DELETE, Permissions.PermissionType.GRANT)));

    return EARKSIPToAIPPluginUtils.earkSIPToAIP(sip, jobUsername, fullPermissions, model, storage, sip.getIds(),
      reportItem.getJobId(), computedParentId);
  }

  private AIP processUpdateSIP(IndexService index, ModelService model, StorageService storage, SIP sip)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    AIP aip;
    IndexResult<IndexedAIP> result = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_IDS, sip.getId())), Sorter.NONE, new Sublist(0, 1));
    if (result.getTotalCount() == 1) {
      IndexedAIP indexedAIP = result.getResults().get(0);

      String jobUsername = PluginHelper.getJobUsername(this, index);
      // Update the AIP
      aip = EARKSIPToAIPPluginUtils.earkSIPToAIPUpdate(sip, indexedAIP.getId(), model, storage, jobUsername);
    } else {
      result = index.find(IndexedAIP.class, new Filter(new SimpleFilterParameter(RodaConstants.AIP_ID, sip.getId())),
        Sorter.NONE, new Sublist(0, 1));
      if (result.getTotalCount() == 1) {
        IndexedAIP indexedAIP = result.getResults().get(0);
        String jobUsername = PluginHelper.getJobUsername(this, index);
        // Update the AIP
        aip = EARKSIPToAIPPluginUtils.earkSIPToAIPUpdate(sip, indexedAIP.getId(), model, storage, jobUsername);
      } else {
        // Fail to update since there's no AIP
        throw new NotFoundException("Unable to find AIP created with SIP ID: " + sip.getId());
      }
    }
    return aip;
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

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<TransferredResource>> getObjectClasses() {
    return Arrays.asList(TransferredResource.class);
  }
}
