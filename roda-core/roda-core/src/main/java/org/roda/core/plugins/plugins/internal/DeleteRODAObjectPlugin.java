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
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;

public class DeleteRODAObjectPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
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

    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<T> plugin, T object) {
        if (object instanceof AIP) {
          processAIP(index, model, report, jobPluginInfo, cachedJob, (AIP) object);
        } else if (object instanceof File) {
          processFile(index, model, report, jobPluginInfo, cachedJob, (File) object);
        } else if (object instanceof Representation) {
          processRepresentation(index, model, report, jobPluginInfo, cachedJob, (Representation) object);
        } else if (object instanceof Risk) {
          processRisk(index, model, report, jobPluginInfo, cachedJob, (Risk) object);
        } else if (object instanceof Format) {
          processFormat(model, report, jobPluginInfo, cachedJob, (Format) object);
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(IndexService index, ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo,
    Job job, AIP aip) {
    Locale locale = PluginHelper.parseLocale(RodaConstants.DEFAULT_EVENT_LOCALE);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    PluginState state = PluginState.SUCCESS;

    final String eventDescription = messages.getTranslation(RodaConstants.EVENT_DELETE_ON_REPOSITORY);
    Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    final List<String> aipsDeleted = new ArrayList<>();

    try {
      model.deleteAIP(aip.getId());
      aipsDeleted.add(aip.getId());
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
    }

    try {
      Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
      index.execute(IndexedAIP.class, filter, Arrays.asList(RodaConstants.INDEX_UUID), new IndexRunnable<IndexedAIP>() {
        @Override
        public void run(IndexedAIP item)
          throws GenericException, RequestNotValidException, AuthorizationDeniedException {
          PluginState state = PluginState.SUCCESS;
          try {
            model.deleteAIP(item.getId());
            aipsDeleted.add(item.getId());
          } catch (NotFoundException e) {
            state = PluginState.FAILURE;
            reportItem.addPluginDetails("Could not delete AIP: " + e.getMessage());
          }

          String outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_DELETE_AIP_SUCCESS, aip.getId());
          model.createRepositoryEvent(PreservationEventType.DELETION, eventDescription, state, outcomeText, details,
            job.getUsername(), true);
        }
      });
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete sublevel AIPs: " + e.getMessage());
    }

    try {
      // removing related risk incidences
      Filter incidenceFilter = new Filter(
        new OneOfManyFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aipsDeleted));
      deleteRelatedIncidences(model, index, incidenceFilter);
    } catch (NotFoundException | GenericException | RequestNotValidException | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      reportItem.addPluginDetails("Could not delete AIP related incidences: " + e.getMessage());
    }

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);

    String outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_DELETE_AIP_SUCCESS, aip.getId());
    model.createRepositoryEvent(PreservationEventType.DELETION, eventDescription, state, outcomeText, details,
      job.getUsername(), true);
  }

  private void processFile(IndexService index, ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo,
    Job job, File file) {
    Locale locale = PluginHelper.parseLocale(RodaConstants.DEFAULT_EVENT_LOCALE);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    PluginState state = PluginState.SUCCESS;

    final String eventDescription = messages.getTranslation(RodaConstants.EVENT_DELETE_ON_REPOSITORY);
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

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);

    String outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_DELETE_FILE_SUCCESS, file.getId());
    model.createRepositoryEvent(PreservationEventType.DELETION, eventDescription, state, outcomeText, details,
      job.getUsername(), true);
  }

  private void processRepresentation(IndexService index, ModelService model, Report report,
    SimpleJobPluginInfo jobPluginInfo, Job job, Representation representation) {
    Locale locale = PluginHelper.parseLocale(RodaConstants.DEFAULT_EVENT_LOCALE);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    PluginState state = PluginState.SUCCESS;

    final String eventDescription = messages.getTranslation(RodaConstants.EVENT_DELETE_ON_REPOSITORY);
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

    report.addReport(reportItem.setPluginState(state));
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
    jobPluginInfo.incrementObjectsProcessed(state);

    String outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_DELETE_REPRESENTATION_SUCCESS,
      representation.getId());
    model.createRepositoryEvent(PreservationEventType.DELETION, eventDescription, state, outcomeText, details,
      job.getUsername(), true);
  }

  private void processRisk(IndexService index, ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo,
    Job job, Risk risk) {
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

  private void processFormat(ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo, Job job,
    Format format) {
    Report reportItem = PluginHelper.initPluginReportItem(this, format.getId(), Format.class);
    PluginState state = PluginState.SUCCESS;

    try {
      model.deleteFormat(format.getId(), true);
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

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    list.add(Risk.class);
    list.add(Format.class);
    return (List) list;
  }
}
