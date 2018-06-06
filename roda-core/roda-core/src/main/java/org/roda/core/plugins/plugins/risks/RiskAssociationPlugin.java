/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.risks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * https://docs.google.com/spreadsheets/d/
 * 1Ncu0My6tf19umSClIA6iXeYlJ4_FP6MygRwFCe0EzyM
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RiskAssociationPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RiskAssociationPlugin.class);

  private String riskIds = null;
  private String incidenceDescription = "";
  private String severity = "";

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_ID,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_ID, "Risks", PluginParameterType.RISK_ID, "", false, false,
        "Add the risks that will be associated with the objects above."));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION, "Incidence description",
        PluginParameterType.STRING, "", false, false, "Associate a description to the incidence(s) created"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY, "Incidence severity",
        PluginParameterType.SEVERITY, "", false, false, "Associate a severity to the incidence"));
  }

  @Override
  public String getName() {
    return "Risk association";
  }

  @Override
  public String getDescription() {
    return "Associates selected items to existing risks in the Risk registry (as risk incidences).\nThis task is convenient when the preservation "
      + "expert wants to associate a set of items (e.g. AIPs, representations or files) to a risk to be mitigated in the near future.\nAs an example, "
      + "if the designated community of the repository provides feedback that a given format under a certain collection is not being displayed properly "
      + "on the graphical user interface of the repository, then the preservation expert may want to mark these files to be targeted by a preservation"
      + " action (e.g. generate new representations for access purposes).";
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_ID)) {
      riskIds = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION)) {
      incidenceDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY)) {
      severity = parameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY);
    }
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY));
    return parameters;
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
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    LOGGER.debug("Creating risk incidences");
    Report pluginReport = PluginHelper.initPluginReport(this);

    try {
      JobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, liteList.size());
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);

      Job job = PluginHelper.getJob(this, model);
      List<T> list = PluginHelper.transformLitesIntoObjects(model, this, pluginReport, jobPluginInfo, liteList, job,
        false);

      if (!list.isEmpty() && riskIds != null) {
        List<String> risks = Arrays.asList(riskIds.split(","));
        Pair<JobPluginInfo, Report> jobInfo = new Pair<>();

        if (list.get(0) instanceof AIP) {
          jobInfo = addIncidenceToAIPList(model, index, (List<AIP>) list, risks, jobPluginInfo, pluginReport, job);
        } else if (list.get(0) instanceof Representation) {
          jobInfo = addIncidenceToRepresentationList(model, index, (List<Representation>) list, risks, jobPluginInfo,
            pluginReport, job);
        } else if (list.get(0) instanceof File) {
          jobInfo = addIncidenceToFileList(model, index, (List<File>) list, risks, jobPluginInfo, pluginReport, job);
        }

        jobPluginInfo = jobInfo.getFirst();
        pluginReport = jobInfo.getSecond();
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformationAsync(this, jobPluginInfo);
    } catch (JobException | AuthorizationDeniedException | NotFoundException | GenericException
      | RequestNotValidException | LockingException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return pluginReport;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    try {
      index.commit(RiskIncidence.class);
    } catch (GenericException e) {
      LOGGER.error("Error commiting risk incidences to index");
    }
    return new Report();
  }

  private Pair<JobPluginInfo, Report> addIncidenceToAIPList(ModelService model, IndexService index, List<AIP> list,
    List<String> risks, JobPluginInfo jobPluginInfo, Report pluginReport, Job job) throws JobException {

    for (AIP aip : list) {
      PluginState state = PluginState.SUCCESS;

      for (String riskId : risks) {
        try {
          RiskIncidence incidence = new RiskIncidence();
          incidence.setDetectedOn(new Date());
          incidence.setDetectedBy(job.getUsername());
          incidence.setRiskId(riskId);
          incidence.setAipId(aip.getId());
          incidence.setObjectClass(AIP.class.getSimpleName());
          incidence.setStatus(INCIDENCE_STATUS.UNMITIGATED);
          incidence.setSeverity(Risk.SEVERITY_LEVEL.valueOf(severity));
          incidence.setDescription(incidenceDescription);
          model.createRiskIncidence(incidence, false);
        } catch (GenericException e) {
          state = PluginState.FAILURE;
        }
      }

      jobPluginInfo.incrementObjectsProcessed(state);
      Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class);
      reportItem.setPluginState(state).setPluginDetails("Risk job plugin ran on an AIP");
      pluginReport.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      try {
        PluginHelper.createPluginEvent(this, aip.getId(), model, index, state, "", true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        LOGGER.error("Could not create a risk job plugin event");
      }
    }

    return Pair.of(jobPluginInfo, pluginReport);
  }

  private Pair<JobPluginInfo, Report> addIncidenceToRepresentationList(ModelService model, IndexService index,
    List<Representation> list, List<String> risks, JobPluginInfo jobPluginInfo, Report pluginReport, Job job)
    throws JobException {

    for (Representation representation : list) {
      PluginState state = PluginState.SUCCESS;

      for (String riskId : risks) {
        try {
          RiskIncidence incidence = new RiskIncidence();
          incidence.setDetectedOn(new Date());
          incidence.setDetectedBy(job.getUsername());
          incidence.setRiskId(riskId);
          incidence.setAipId(representation.getAipId());
          incidence.setRepresentationId(representation.getId());
          incidence.setObjectClass(Representation.class.getSimpleName());
          incidence.setStatus(INCIDENCE_STATUS.UNMITIGATED);
          incidence.setSeverity(SEVERITY_LEVEL.valueOf(severity));
          model.createRiskIncidence(incidence, false);
        } catch (GenericException e) {
          state = PluginState.FAILURE;
        }
      }

      jobPluginInfo.incrementObjectsProcessed(state);
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getRepresentationId(representation),
        Representation.class);
      reportItem.setPluginState(state).setPluginDetails("Risk job plugin ran on a representation");
      pluginReport.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      try {
        PluginHelper.createPluginEvent(this, representation.getAipId(), representation.getId(), model, index, null,
          null, state, "", true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        LOGGER.error("Could not create a risk job plugin event");
      }
    }

    return Pair.of(jobPluginInfo, pluginReport);
  }

  private Pair<JobPluginInfo, Report> addIncidenceToFileList(ModelService model, IndexService index, List<File> list,
    List<String> risks, JobPluginInfo jobPluginInfo, Report pluginReport, Job job) throws JobException {

    for (File file : list) {
      PluginState state = PluginState.SUCCESS;

      for (String riskId : risks) {
        try {
          RiskIncidence incidence = new RiskIncidence();
          incidence.setDetectedOn(new Date());
          incidence.setDetectedBy(job.getUsername());
          incidence.setRiskId(riskId);
          incidence.setAipId(file.getAipId());
          incidence.setRepresentationId(file.getRepresentationId());
          incidence.setFilePath(file.getPath());
          incidence.setFileId(file.getId());
          incidence.setObjectClass(File.class.getSimpleName());
          incidence.setStatus(INCIDENCE_STATUS.UNMITIGATED);
          incidence.setSeverity(SEVERITY_LEVEL.valueOf(severity));
          model.createRiskIncidence(incidence, false);
        } catch (GenericException e) {
          state = PluginState.FAILURE;
        }
      }

      jobPluginInfo.incrementObjectsProcessed(state);
      Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class);
      reportItem.setPluginState(state).setPluginDetails("Risk job plugin ran on a file");
      pluginReport.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      try {
        PluginHelper.createPluginEvent(this, file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(),
          model, index, null, null, state, "", true);
      } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException
        | ValidationException | AlreadyExistsException e) {
        LOGGER.error("Could not create a risk job plugin event");
      }

    }

    return Pair.of(jobPluginInfo, pluginReport);
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public Plugin<T> cloneMe() {
    return new RiskAssociationPlugin<>();
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.RISK_MANAGEMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return getDescription();
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Risk was successfully associated with objects";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Risk was not successfully associated with objects";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_RISK_MANAGEMENT);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(Representation.class);
    list.add(File.class);
    return (List) list;
  }
}
