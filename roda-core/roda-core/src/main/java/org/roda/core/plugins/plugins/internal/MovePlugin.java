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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovePlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovePlugin.class);
  private static final String EVENT_DESCRIPTION = "The process of updating an object of the repository";

  private String destinationId = null;
  private String details = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, new PluginParameter(RodaConstants.PLUGIN_PARAMS_ID,
      "Destination object identifier", PluginParameterType.STRING, "", false, false, "Destination object identifier"));

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
    return "Move action over RODA entity";
  }

  @Override
  public String getDescription() {
    return "Executes move actions over RODA entity faster";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ID));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_ID)) {
      destinationId = parameters.get(RodaConstants.PLUGIN_PARAMS_ID);
    }

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_DETAILS)) {
      details = parameters.get(RodaConstants.PLUGIN_PARAMS_DETAILS);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        if (!objects.isEmpty()) {
          if (objects.get(0) instanceof AIP) {
            for (T object : objects) {
              processAIP(model, index, report, jobPluginInfo, cachedJob, (AIP) object);
            }
          } else if (objects.get(0) instanceof File) {
            for (T object : objects) {
              processFile(index, model, report, jobPluginInfo, cachedJob, (File) object);
            }
          } else if (objects.get(0) instanceof TransferredResource) {
            processTransferredResource(model, report, jobPluginInfo, cachedJob, (List<TransferredResource>) objects);
          }
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(ModelService model, IndexService index, Report report, JobPluginInfo jobPluginInfo, Job job,
    AIP aip) {
    PluginState state = PluginState.SUCCESS;

    if (!aip.getId().equals(destinationId)) {
      LOGGER.debug("Moving AIP {} under {}", aip.getId(), destinationId);

      try {
        IndexResult<IndexedAIP> result = new IndexResult<>();

        if (destinationId != null) {
          Filter filter = new Filter();
          filter.add(new SimpleFilterParameter(RodaConstants.INDEX_UUID, destinationId));
          filter.add(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aip.getId()));
          result = index.find(IndexedAIP.class, filter, Sorter.NONE, new Sublist(0, 1),
            Arrays.asList(RodaConstants.INDEX_UUID));
        }

        if (destinationId == null || result.getResults().isEmpty()) {
          model.moveAIP(aip.getId(), destinationId, job.getUsername());
        } else {
          state = PluginState.FAILURE;
          Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
          reportItem.addPluginDetails("Could not move AIP because the destination is a sublevel").setPluginState(state);
          report.addReport(reportItem);
          PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
        }
      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
        reportItem.addPluginDetails("Could not move AIP: " + e.getMessage()).setPluginState(state);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
      }
    }

    String outcomeText = "";

    try {
      IndexedAIP item = index.retrieve(IndexedAIP.class, aip.getId(),
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.AIP_TITLE));

      if (state.equals(PluginState.SUCCESS)) {
        outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has been manually moved");
      } else {
        outcomeText = PluginHelper.createOutcomeTextForAIP(item, "has not been manually moved");
      }
    } catch (NotFoundException | GenericException e1) {
      if (state.equals(PluginState.SUCCESS)) {
        outcomeText = "Archival Information Package [id: " + aip.getId() + "] has been manually moved";
      } else {
        outcomeText = "Archival Information Package [id: " + aip.getId() + "] has not been manually moved";
      }
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, EVENT_DESCRIPTION, state,
      outcomeText, details, job.getUsername(), true);
  }

  private void processFile(IndexService index, ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    File file) {
    PluginState state = PluginState.SUCCESS;
    StringBuilder outcomeText = new StringBuilder();

    try {
      String toAIP;
      String toRepresentation;
      List<String> toPath = new ArrayList<>();

      if (StringUtils.isNotBlank(destinationId)) {
        List<String> fileFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_AIP_ID,
          RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_PATH, RodaConstants.INDEX_ID);
        IndexedFile toFolder = index.retrieve(IndexedFile.class, destinationId, fileFields);

        LOGGER.debug("Moving File {} under {}", file.getId(), toFolder.getId());

        toAIP = toFolder.getAipId();
        toRepresentation = toFolder.getRepresentationId();
        if (toFolder.getPath() != null) {
          toPath.addAll(toFolder.getPath());
        }
        toPath.add(toFolder.getId());
      } else {
        toAIP = file.getAipId();
        toRepresentation = file.getRepresentationId();
      }

      File movedFile = model.moveFile(file, toAIP, toRepresentation, toPath, file.getId(), true);
      outcomeText.append("The file '").append(ModelUtils.getFileStoragePath(file).toString())
        .append("' has been manually moved to '").append(ModelUtils.getFileStoragePath(movedFile).toString())
        .append("'");

    } catch (GenericException | AlreadyExistsException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class);
      reportItem.addPluginDetails("Could not move file: " + e.getMessage()).setPluginState(state);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      outcomeText.append("The file '").append(file.getId()).append("' has not been manually moved: [")
        .append(e.getClass().getSimpleName()).append("] ").append(e.getMessage());
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null, PreservationEventType.UPDATE,
      EVENT_DESCRIPTION, state, outcomeText.toString(), details, job.getUsername(), true);
  }

  @SuppressWarnings("unchecked")
  private void processTransferredResource(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    List<TransferredResource> resources) {
    if (destinationId == null) {
      destinationId = "";
    }

    try {
      Map<String, String> moveResult = RodaCoreFactory.getTransferredResourcesScanner()
        .moveTransferredResource(resources, destinationId, false, true);

      for (TransferredResource resource : resources) {
        if (!moveResult.containsKey(resource.getUUID())) {
          addFailedReport(model, report, jobPluginInfo, job, resource.getUUID(), (Class<T>) TransferredResource.class);
        } else {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
        }
      }

    } catch (GenericException | IsStillUpdatingException | NotFoundException | RuntimeException
      | AuthorizationDeniedException e) {
      LOGGER.error("Could not move transferred resource list", e);

      for (TransferredResource resource : resources) {
        addFailedReport(model, report, jobPluginInfo, job, resource.getId(), (Class<T>) TransferredResource.class);
      }
    }
  }

  private void addFailedReport(ModelService model, Report report, JobPluginInfo jobPluginInfo, Job job,
    String resourceId, Class<T> objectClass) {
    jobPluginInfo.incrementObjectsProcessedWithFailure();
    Report reportItem = PluginHelper.initPluginReportItem(this, resourceId, objectClass);
    reportItem
      .addPluginDetails("Could not move transferred resource " + resourceId
        + " due to inapropriate move operation. A likely scenario is a move operation of a parent to a child.")
      .setPluginState(PluginState.FAILURE);
    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);
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
      if (TransferredResource.class.getName().equals(job.getSourceObjects().getSelectedClass())) {
        String relativePath = "";
        if (getParameterValues().containsKey(RodaConstants.PLUGIN_PARAMS_ID)) {
          relativePath = getParameterValues().get(RodaConstants.PLUGIN_PARAMS_ID);
        }

        RodaCoreFactory.getTransferredResourcesScanner().updateTransferredResources(Optional.of(relativePath), true);
      }

      index.commit((Class<? extends IsIndexed>) Class.forName(job.getSourceObjects().getSelectedClass()));
    } catch (NotFoundException | GenericException | IsStillUpdatingException | RequestNotValidException
      | AuthorizationDeniedException e) {
      LOGGER.error("Could not update new resource parent folder");
    } catch (ClassNotFoundException e) {
      LOGGER.error("Error commiting after move operation");
    }
    return new Report();
  }

  @Override
  public Plugin<T> cloneMe() {
    return new MovePlugin<>();
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
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Moves objects to a destination";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The objects were successfully moved";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "The objects were not successfully moved";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_NOT_LISTABLE);
  }

  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(File.class);
    list.add(TransferredResource.class);
    return (List) list;
  }
}
