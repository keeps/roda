/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.base.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.IdUtils;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class FailAcceptPlugin extends AbstractPlugin<AIP> {
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
    return "plugin.longAccetPlugin.name";
  }

  @Override
  public String getDescription() {
    return "Accept AIPs under appraisal with a long sleep timer at the end of execution.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> liteList)
    throws PluginException {
    return PluginHelper.processObjects(this, (RODAObjectsProcessingLogic<AIP>) (index1, model1, report, cachedJob,
      jobPluginInfo, plugin, objects) -> processAIP(model1, index1, report, jobPluginInfo, cachedJob, objects), index,
      model, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<AIP> aips) {
    try {
      Date now = new Date();
      LinkingIdentifier linkingIdentifierAgent = new LinkingIdentifier();
      try {
        PreservationMetadata pm = PremisV3Utils.createIfNotExistsPremisUserAgentBinary(job.getUsername(), model, index,
          true, job.getJobUsersDetails());
        linkingIdentifierAgent.setValue(pm.getId());
      } catch (AlreadyExistsException e) {
        linkingIdentifierAgent
          .setValue(IdUtils.getUserAgentId(job.getUsername(), RODAInstanceUtils.getLocalInstanceIdentifier()));
      } catch (ValidationException e) {
        throw new GenericException(e);
      }

      for (AIP aip : aips) {
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
        PluginHelper.updatePartialJobReport(this, model, reportItem, false, job);
        PluginState state = PluginState.SUCCESS;

        try {
          String aipId = aip.getId();

          // Accept AIP
          aip.setState(AIPState.ACTIVE);
          model.updateAIPState(aip, job.getUsername());

          // create preservation event
          String id = IdUtils.createPreservationMetadataId(PreservationMetadata.PreservationMetadataType.EVENT,
            RODAInstanceUtils.getLocalInstanceIdentifier());
          PreservationEventType type = PreservationEventType.ACCESSION;
          String preservationEventDescription = AutoAcceptSIPPlugin.DESCRIPTION;
          List<LinkingIdentifier> sources = new ArrayList<>();
          List<LinkingIdentifier> outcomes = List
            .of(PluginHelper.getLinkingIdentifier(aipId, RodaConstants.PRESERVATION_LINKING_OBJECT_OUTCOME));
          PluginState outcome = PluginState.SUCCESS;
          String outcomeDetailNote = AutoAcceptSIPPlugin.SUCCESS_MESSAGE;

          try {
            ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, now, type.toString(),
              preservationEventDescription, sources, outcomes, outcome.name(), outcomeDetailNote, null,
              List.of(linkingIdentifierAgent));

            model.createPreservationMetadata(PreservationMetadata.PreservationMetadataType.EVENT, id, aipId, null, null,
              null, premisEvent, job.getUsername(), true);
          } catch (AlreadyExistsException e) {
            throw new GenericException(e);
          }
        } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e1) {
          state = PluginState.FAILURE;
        } finally {
          jobPluginInfo.incrementObjectsProcessed(state);

          StringBuilder outcomeText = new StringBuilder().append("The AIP '").append(aip.getId()).append("' was ")
            .append("accepted into the repository.");
          model.createRepositoryEvent(PreservationEventType.APPRAISAL,
            "The process of updating an non active object of the repository", state, outcomeText.toString(), null,
            job.getUsername(), true, null);
          reportItem.setPluginState(state).setPluginDetails(outcomeText.toString());
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }
      }

      index.commit(IndexedAIP.class, IndexedJob.class, IndexedReport.class, IndexedPreservationEvent.class);
      throw new RuntimeException("Object processing logic finished without issue, but we gotta fail!");
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("Failed to update job counters");
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) throws PluginException {
    // Time to fail!
    throw new PluginException("Expected hard-coded exception for test plugin. Please handle.");
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new FailAcceptPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.INTERNAL;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.APPRAISAL;
  }

  @Override
  public String getPreservationEventDescription() {
    return "TEST - Long auto appraisal";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "TEST - Long auto appraisal was successful";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "TEST - Long auto appraisal failed";
  }

  @Override
  public List<String> getCategories() {
    return List.of(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return List.of(AIP.class);
  }
}
