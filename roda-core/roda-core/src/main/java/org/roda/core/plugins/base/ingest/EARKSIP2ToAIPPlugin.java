/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.utils.IPEnums;
import org.roda_project.commons_ip2.model.SIP;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIP2ToAIPPlugin extends SIPToAIPPlugin {
  public static final String UNPACK_DESCRIPTION = "Extracted objects from package in E-ARK SIP 2 format.";
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIP2ToAIPPlugin.class);
  private boolean createSubmission = false;

  private Optional<String> computedSearchScope;
  private boolean forceSearchScope;
  private Path jobWorkingDirectory;

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
    return "E-ARK SIP 2";
  }

  @Override
  public String getDescription() {
    return "E-ARK SIP 2 as a zip file.";
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
    jobWorkingDirectory = PluginHelper.getJobWorkingDirectory(this);

    return PluginHelper.processObjects(this,
      (RODAObjectProcessingLogic<TransferredResource>) (index1, model1, storage1, report, cachedJob, jobPluginInfo,
        plugin, object) -> processTransferredResource(index1, model1, report, cachedJob, object),
      index, model, storage, liteList);
  }

  private void processTransferredResource(IndexService index, ModelService model, Report report, Job cachedJob,
    TransferredResource transferredResource) {
    Report reportItem = PluginHelper.initPluginReportItem(this, transferredResource);

    Path earkSIPPath = Paths.get(FilenameUtils.normalize(transferredResource.getFullPath()));
    LOGGER.debug("Converting {} to AIP", earkSIPPath);

    transformTransferredResourceIntoAnAIP(index, model, transferredResource, earkSIPPath, createSubmission, reportItem,
      cachedJob, computedSearchScope, forceSearchScope, jobWorkingDirectory);
    report.addReport(reportItem);

    PluginHelper.createJobReport(this, model, reportItem, cachedJob);
  }

  private void transformTransferredResourceIntoAnAIP(IndexService index, ModelService model,
    TransferredResource transferredResource, Path earkSIPPath, boolean createSubmission, Report reportItem,
    Job cachedJob, Optional<String> computedSearchScope, boolean forceSearchScope, Path jobWorkingDirectory) {
    SIP sip = null;
    AIP aip = null;

    try {
      sip = new EARKSIP().parse(earkSIPPath, FSUtils.createRandomDirectory(jobWorkingDirectory));
      reportItem.setSourceObjectOriginalIds(sip.getIds());
      reportItem.setIngestType(sip.getStatus().toString());

      if (sip.getValidationReport().isValid()) {
        Optional<String> parentId = Optional.empty();
        if (IPEnums.IPStatus.NEW == sip.getStatus()) {
          parentId = PluginHelper.getComputedParent(model, index, sip.getAncestors(), computedSearchScope,
            forceSearchScope, cachedJob.getId());
          aip = processNewSIP(index, model, reportItem, sip, parentId, transferredResource.getUUID());
        } else if (IPEnums.IPStatus.UPDATE == sip.getStatus()) {
          aip = processUpdateSIP(index, model, sip, computedSearchScope, forceSearchScope);
        } else {
          throw new GenericException("Unknown IP Status: " + sip.getStatus());
        }

        PluginHelper.acquireObjectLock(aip, this);

        // put SIP inside the created AIP (if it is supposed to do so)
        PluginHelper.createSubmission(model, createSubmission, earkSIPPath, aip.getId());

        createUnpackingEventSuccess(model, index, transferredResource, aip, UNPACK_DESCRIPTION, cachedJob);
        reportItem.setSourceAndOutcomeObjectId(reportItem.getSourceObjectId(), aip.getId())
          .setPluginState(PluginState.SUCCESS);

        if (sip.getAncestors() != null && !sip.getAncestors().isEmpty() && aip.getParentId() == null) {
          reportItem.setPluginDetails(String.format("Parent with id '%s' not found", parentId));
        }
        createWellformedEventSuccess(model, index, transferredResource, aip, cachedJob);
        LOGGER.debug("Done with converting {} to AIP {}", earkSIPPath, aip.getId());
      } else {
        reportItem.setPluginState(PluginState.FAILURE).setHtmlPluginDetails(true)
          .setPluginDetails(sip.getValidationReport().toHtml(true, true, true, false, false));
        LOGGER.debug("The SIP {} is not valid", earkSIPPath);
      }

    } catch (RODAException | ParseException | RuntimeException | IOException e) {
      reportItem.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      LOGGER.error("Error converting {} to AIP", earkSIPPath, e);
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

  private AIP processNewSIP(IndexService index, ModelService model, Report reportItem, SIP sip,
    Optional<String> computedParentId, String ingestSIPUUID)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException, LockingException {
    String jobUsername = PluginHelper.getJobUsername(this, index);
    return EARKSIP2ToAIPPluginUtils.earkSIPToAIP(sip, jobUsername, model, sip.getIds(), reportItem.getJobId(),
      computedParentId, ingestSIPUUID, this);
  }

  private AIP processUpdateSIP(IndexService index, ModelService model, SIP sip, Optional<String> searchScope,
    boolean forceSearchScope) throws GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException, ValidationException, LockingException {
    String searchScopeString = searchScope.orElse(null);

    List<FilterParameter> possibleStates = new ArrayList<>();
    possibleStates.add(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.toString()));
    possibleStates.add(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.UNDER_APPRAISAL.toString()));

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_IDS, sip.getId()),
      new OrFiltersParameters(possibleStates));
    if (searchScopeString != null && !forceSearchScope) {
      filter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, searchScopeString));
    }

    IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, filter, Sorter.NONE, new Sublist(0, 1),
      List.of(RodaConstants.INDEX_UUID));
    IndexedAIP indexedAIP;

    long amountOfAipsFoundById = result.getTotalCount();
    if (amountOfAipsFoundById == 1) {
      indexedAIP = result.getResults().getFirst();
    } else {
      filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, sip.getId()));
      if (searchScopeString != null && !forceSearchScope) {
        filter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, searchScopeString));
      }
      result = index.find(IndexedAIP.class, filter, Sorter.NONE, new Sublist(0, 1), List.of(RodaConstants.INDEX_UUID));
      if (result.getTotalCount() == 1) {
        indexedAIP = result.getResults().getFirst();
      } else {
        // Fail to update since there's no AIP
        throw new NotFoundException("Unable to find one & only one AIP created with SIP ID or AIP ID " + sip.getId()
          + (searchScopeString != null ? " under AIP " + searchScopeString : "") + " (found " + amountOfAipsFoundById
          + " when searching by id and " + result.getTotalCount() + " when searching by SIP id or anscestor id)");
      }
    }

    String jobUsername = PluginHelper.getJobUsername(this, index);
    String jobId = PluginHelper.getJobId(this);

    // Update the AIP
    return EARKSIP2ToAIPPluginUtils.earkSIPToAIPUpdate(sip, indexedAIP, model, jobUsername, searchScope, jobId, null,
      this);
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Plugin<TransferredResource> cloneMe() {
    return new EARKSIP2ToAIPPlugin();
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
