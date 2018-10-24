/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogicNew;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteRODAObjectPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRODAObjectPlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of deleting an object of the repository";
  private String details = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS,
      "Event details", PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));
  }

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
    return "Delete RODA entities";
  }

  @Override
  public String getDescription() {
    return "Delete any removable type of RODA entities";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogicNew<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, T object) {
        if (object instanceof AIP) {
          processAIP(index, model, report, jobPluginInfo, cachedJob, (AIP) object);
        } else if (object instanceof File) {
          processFile(index, model, report, jobPluginInfo, cachedJob, (File) object);
        } else if (object instanceof Representation) {
          processRepresentation(index, model, report, jobPluginInfo, cachedJob, (Representation) object);
        } else if (object instanceof Risk) {
          processRisk(index, model, report, jobPluginInfo, cachedJob, (Risk) object);
        } else if (object instanceof RepresentationInformation) {
          processRepresentationInformation(model, report, jobPluginInfo, cachedJob, (RepresentationInformation) object);
        } else if (object instanceof RiskIncidence) {
          processRiskIncidence(model, report, jobPluginInfo, cachedJob, (RiskIncidence) object);
        } else if (object instanceof DIP) {
          processDIP(model, report, jobPluginInfo, cachedJob, (DIP) object);
        } else if (object instanceof DIPFile) {
          processDIPFile(model, report, jobPluginInfo, cachedJob, (DIPFile) object);
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    AIP aip) {
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    reportItem.setPluginState(PluginState.SUCCESS);

    try {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      index
        .execute(
          IndexedAIP.class, filter, Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_ID,
            RodaConstants.AIP_LEVEL, RodaConstants.AIP_DATE_INITIAL, RodaConstants.AIP_DATE_FINAL),
          new IndexRunnable<IndexedAIP>() {
            @Override
            public void run(IndexedAIP item)
              throws GenericException, RequestNotValidException, AuthorizationDeniedException {
              PluginState state = PluginState.SUCCESS;
              try {
                model.deleteAIP(item.getId());
              } catch (NotFoundException e) {
                state = PluginState.FAILURE;
                reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
              }

              String outcomeText;
              if (state.equals(PluginState.SUCCESS)) {
                outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has been manually deleted");
              } else {
                outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has not been manually deleted");
              }

              List<LinkingIdentifier> sources = new ArrayList<>();
              sources
                .add(PluginHelper.getLinkingIdentifier(item.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

              model.createEvent(item.getId(), null, null, null, PreservationEventType.DELETION, EVENT_DESCRIPTION,
                sources, null, state, outcomeText, details, job.getUsername(), true);
            }
          }, e -> {
            reportItem.setPluginState(PluginState.FAILURE);
            reportItem.addPluginDetails("Could not delete sublevel AIPs: " + e.getMessage());
          });
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      reportItem.setPluginState(PluginState.FAILURE);
      reportItem.addPluginDetails("Could not delete sublevel AIPs: " + e.getMessage());
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(reportItem.getPluginState());

    IndexedAIP item = null;
    String outcomeText;

    try {
      item = index.retrieve(IndexedAIP.class, aip.getId(),
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_TITLE));
    } catch (NotFoundException | GenericException e) {
      // do nothing
    }

    try {
      model.deleteAIP(aip.getId());

      if (item != null) {
        outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has been manually deleted");
      } else {
        outcomeText = "Archival Information Package [id: " + aip.getId() + "] has been manually deleted";
      }
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      reportItem.setPluginState(PluginState.FAILURE);
      reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
      if (item != null) {
        outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has not been manually deleted");
      } else {
        outcomeText = "Archival Information Package [id: " + aip.getId() + "] has not been manually deleted";
      }
    }

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(aip.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    model.createEvent(aip.getId(), null, null, null, PreservationEventType.DELETION, EVENT_DESCRIPTION, sources, null,
      reportItem.getPluginState(), outcomeText, details, job.getUsername(), true);
  }

  private void processFile(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    File file) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class);

    try {
      model.deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), true);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete File: " + e.getMessage());
    }

    try {
      // removing related risk incidences
      Filter incidenceFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, file.getAipId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, file.getRepresentationId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED,
          StringUtils.join(file.getPath(), RodaConstants.RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR)),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_FILE_ID, file.getId()));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete file related incidences: " + e.getMessage());
    }

    // removing PREMIS file
    try {
      String pmId = URNUtils.getPremisPrefix(PreservationMetadataType.FILE) + file.getId();
      model.deletePreservationMetadata(PreservationMetadataType.FILE, file.getAipId(), file.getRepresentationId(), pmId,
        false);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      reportItem.addPluginDetails("Could not delete associated PREMIS file: " + e.getMessage());
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);

    String outcomeText;
    if (state.equals(PluginState.SUCCESS)) {
      outcomeText = "The file '" + file.getId() + "' has been manually deleted.";
    } else {
      outcomeText = "The file '" + file.getId() + "' has not been manually deleted.";
    }

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId(), RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    model.createEvent(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
      PreservationEventType.DELETION, EVENT_DESCRIPTION, sources, null, state, outcomeText, details, job.getUsername(),
      true);
  }

  private void processRepresentation(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo,
    Job job, Representation representation) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, representation.getId(), Representation.class);

    try {
      model.deleteRepresentation(representation.getAipId(), representation.getId());
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete representation: " + e.getMessage());
    }

    try {
      // removing related risk incidences
      Filter incidenceFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, representation.getAipId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_REPRESENTATION_ID, representation.getId()));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete representation related incidences: " + e.getMessage());
    }

    // removing PREMIS file
    try {
      String pmId = URNUtils.getPremisPrefix(PreservationMetadataType.REPRESENTATION) + representation.getId();
      model.deletePreservationMetadata(PreservationMetadataType.REPRESENTATION, representation.getAipId(),
        representation.getId(), pmId, false);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      reportItem.addPluginDetails("Could not delete associated PREMIS file: " + e.getMessage());
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);

    String outcomeText;
    if (state.equals(PluginState.SUCCESS)) {
      outcomeText = "The representation '" + representation.getId() + "' has been manually deleted.";
    } else {
      outcomeText = "The representation '" + representation.getId() + "' has not been manually deleted.";
    }

    List<LinkingIdentifier> sources = new ArrayList<>();
    sources.add(PluginHelper.getLinkingIdentifier(representation.getAipId(), representation.getId(),
      RodaConstants.PRESERVATION_LINKING_OBJECT_SOURCE));

    model.createEvent(representation.getAipId(), representation.getId(), null, null, PreservationEventType.DELETION,
      EVENT_DESCRIPTION, sources, null, state, outcomeText, details, job.getUsername(), true);
  }

  private void processRisk(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    Risk risk) {
    Report reportItem = PluginHelper.initPluginReportItem(this, risk.getId(), Risk.class);
    PluginState state = PluginState.SUCCESS;

    try {
      Filter incidenceFilter = new Filter(
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_RISK_ID, risk.getId()));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      reportItem.addPluginDetails("Could not delete representation related incidences: " + e.getMessage());
      state = PluginState.FAILURE;
    }

    try {
      model.deleteRisk(risk.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private void processRepresentationInformation(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    RepresentationInformation ri) {
    Report reportItem = PluginHelper.initPluginReportItem(this, ri.getId(), RepresentationInformation.class);
    PluginState state = PluginState.SUCCESS;

    try {
      model.deleteRepresentationInformation(ri.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private void deleteRelatedIncidences(ModelService model, IndexService index, Filter incidenceFilter)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    Long incidenceCounter = index.count(RiskIncidence.class, incidenceFilter);
    IndexResult<RiskIncidence> incidences = index.find(RiskIncidence.class, incidenceFilter, Sorter.NONE,
      new Sublist(0, incidenceCounter.intValue()),
      Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.RISK_INCIDENCE_ID));

    for (RiskIncidence incidence : incidences.getResults()) {
      model.deleteRiskIncidence(incidence.getId(), false);
    }
  }

  private void processRiskIncidence(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    RiskIncidence incidence) {
    Report reportItem = PluginHelper.initPluginReportItem(this, incidence.getId(), RiskIncidence.class);
    PluginState state = PluginState.SUCCESS;

    try {
      model.deleteRiskIncidence(incidence.getId(), true);
    } catch (GenericException | NotFoundException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private void processDIP(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job, DIP dip) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, dip.getId(), DIP.class);

    try {
      model.deleteDIP(dip.getId());
    } catch (NotFoundException | GenericException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete DIP: " + e.getMessage());
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  private void processDIPFile(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    DIPFile dipFile) {
    PluginState state = PluginState.SUCCESS;
    Report reportItem = PluginHelper.initPluginReportItem(this, dipFile.getId(), DIPFile.class);

    try {
      model.deleteDIPFile(dipFile.getDipId(), dipFile.getPath(), dipFile.getId(), false);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete DIP file: " + e.getMessage());
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      Job job = PluginHelper.getJob(this, index);
      index.commit((Class<? extends IsIndexed>) Class.forName(job.getSourceObjects().getSelectedClass()));
    } catch (NotFoundException | GenericException | ClassNotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      LOGGER.error("Could not commit after delete operation", e);
    }

    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new DeleteRODAObjectPlugin<>();
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
    return PreservationEventType.DELETION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Deletes RODA entities";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "RODA entities were successfully removed";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "RODA entities were not successfully removed";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    list.add(Risk.class);
    list.add(RepresentationInformation.class);
    list.add(RiskIncidence.class);
    list.add(DIP.class);
    list.add(DIPFile.class);
    return (List) list;
  }
}
