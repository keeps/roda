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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
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
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovePlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovePlugin.class);

  private String destinationId = null;
  private String details = null;
  private String eventDescription = null;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();
  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ID, new PluginParameter(RodaConstants.PLUGIN_PARAMS_ID,
      "Destination object identifier", PluginParameterType.STRING, "", false, false, "Destination object identifier"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, new PluginParameter(RodaConstants.PLUGIN_PARAMS_DETAILS,
      "Event details", PluginParameterType.STRING, "", false, false, "Details that will be used when creating event"));

    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION,
      new PluginParameter(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION, "Event description",
        PluginParameterType.STRING, "", false, false, "Description that will be used when creating event"));
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION));
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

    if (parameters.containsKey(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION)) {
      eventDescription = parameters.get(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {

    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        if (!objects.isEmpty()) {
          if (objects.get(0) instanceof AIP) {
            for (T object : objects) {
              processAIP(model, report, jobPluginInfo, cachedJob, (AIP) object);
            }
          } else if (objects.get(0) instanceof File) {
            for (T object : objects) {
              processFile(index, model, report, jobPluginInfo, cachedJob, (File) object);
            }
          } else if (objects.get(0) instanceof TransferredResource) {
            processTransferredResource(jobPluginInfo, (List<TransferredResource>) objects);
          }
        }
      }
    }, index, model, storage, liteList);
  }

  private void processAIP(ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo, Job job, AIP aip) {
    Locale locale = PluginHelper.parseLocale(RodaConstants.DEFAULT_EVENT_LOCALE);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    PluginState state = PluginState.SUCCESS;
    String outcomeText = "";

    if (!aip.getId().equals(destinationId)) {
      LOGGER.debug("Moving AIP {} under {}", aip.getId(), destinationId);

      try {
        model.moveAIP(aip.getId(), destinationId);

        outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_MOVE_AIP_SUCCESS, aip.getId());
      } catch (GenericException | NotFoundException | RequestNotValidException | AuthorizationDeniedException e) {
        state = PluginState.FAILURE;
        Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
        reportItem.addPluginDetails("Could not move AIP: " + e.getMessage()).setPluginState(state);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

        outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_MOVE_AIP_SUCCESS, aip.getId());
      }
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    model.createUpdateAIPEvent(aip.getId(), null, null, null, PreservationEventType.UPDATE, eventDescription, state,
      outcomeText, details, job.getUsername(), true);
  }

  private void processFile(IndexService index, ModelService model, Report report, SimpleJobPluginInfo jobPluginInfo,
    Job job, File file) {
    Locale locale = PluginHelper.parseLocale(RodaConstants.DEFAULT_EVENT_LOCALE);
    Messages messages = RodaCoreFactory.getI18NMessages(locale);
    PluginState state = PluginState.SUCCESS;
    String outcomeText = "";

    try {
      String toAIP;
      String toRepresentation;
      List<String> toPath = null;

      if (StringUtils.isNotBlank(destinationId)) {
        List<String> fileFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_AIP_ID,
          RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_PATH, RodaConstants.FILE_FILE_ID);
        IndexedFile toFolder = index.retrieve(IndexedFile.class, destinationId, fileFields);

        LOGGER.debug("Moving File {} under {}", file.getId(), toFolder.getId());

        toAIP = toFolder.getAipId();
        toRepresentation = toFolder.getRepresentationId();
        toPath = new ArrayList<>();
        if (toFolder.getPath() != null) {
          toPath.addAll(toFolder.getPath());
        }
        toPath.add(toFolder.getId());
      } else {
        toAIP = file.getAipId();
        toRepresentation = file.getRepresentationId();
      }

      File movedFile = model.moveFile(file, toAIP, toRepresentation, toPath, file.getId(), true, true);

      outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_MOVE_FILE_SUCCESS,
        ModelUtils.getFileStoragePath(file).toString(), ModelUtils.getFileStoragePath(movedFile).toString());
    } catch (GenericException | AlreadyExistsException | NotFoundException | RequestNotValidException
      | AuthorizationDeniedException e) {
      state = PluginState.FAILURE;
      Report reportItem = PluginHelper.initPluginReportItem(this, file.getId(), File.class);
      reportItem.addPluginDetails("Could not move file: " + e.getMessage()).setPluginState(state);
      report.addReport(reportItem);
      PluginHelper.updatePartialJobReport(this, model, reportItem, true, job);

      outcomeText = messages.getTranslationWithArgs(RodaConstants.EVENT_MOVE_FILE_FAILURE, file.getId(),
        e.getClass().getSimpleName(), e.getMessage());
    }

    jobPluginInfo.incrementObjectsProcessed(state);
    model.createUpdateAIPEvent(file.getAipId(), file.getRepresentationId(), null, null, PreservationEventType.UPDATE,
      eventDescription, state, outcomeText, details, job.getUsername(), true);
  }

  private void processTransferredResource(SimpleJobPluginInfo jobPluginInfo, List<TransferredResource> resources) {
    if (destinationId == null) {
      destinationId = "";
    }

    try {
      RodaCoreFactory.getTransferredResourcesScanner().moveTransferredResource(resources, destinationId, false, true);
      jobPluginInfo.incrementObjectsProcessedWithSuccess(resources.size());
    } catch (AlreadyExistsException | GenericException | IsStillUpdatingException | NotFoundException e) {
      LOGGER.error("Could not move transferred resource list");
      jobPluginInfo.incrementObjectsProcessedWithFailure(resources.size());
    }
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
    } catch (NotFoundException | GenericException | IsStillUpdatingException e) {
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<Class<T>> getObjectClasses() {
    List<Class<? extends IsRODAObject>> list = new ArrayList<>();
    list.add(AIP.class);
    list.add(File.class);
    list.add(TransferredResource.class);
    return (List) list;
  }
}
