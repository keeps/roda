/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.maintenance.backfill.beans.Add;
import org.roda.core.plugins.base.maintenance.backfill.beans.DocType;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateRODAMemberBackfillPlugin extends AbstractPlugin<Void> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRODAMemberBackfillPlugin.class);
  private String outputDirectory = ".";
  private boolean onlyGenerateInventory = false;
  private Date startDate = null;

  private static final Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY, "Output directory",
          PluginParameter.PluginParameterType.STRING)
        .withDefaultValue(".").isMandatory(true).withDescription("This job's output directory path").build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY, "Only generate inventory",
          PluginParameter.PluginParameterType.BOOLEAN)
        .withDefaultValue("false").isMandatory(true)
        .withDescription(
          "Whether this job should only generate the inventory of RODA objects and not the index backfill files")
        .build());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_START_DATE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_START_DATE, "Object starting date",
          PluginParameter.PluginParameterType.STRING)
        .isMandatory(false).withDescription(
          "The last modified data for source objects to process. If not set, all objects will be processed.")
        .build());
  }

  @Override
  public void init() throws PluginException {
    // do nothing
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return List.of(Void.class);
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "Generate complete users and groups index backfill";
  }

  @Override
  public String getDescription() {
    return "Description of example plugin";
  }

  @Override
  public String getVersionImpl() {
    // Get from pom.xml <version>
    return getClass().getPackage().getImplementationVersion();
  }

  @Override
  public List<PluginParameter> getParameters() {
    ArrayList<PluginParameter> parameters = new ArrayList<>();
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY)) {
      outputDirectory = parameters.get(RodaConstants.PLUGIN_PARAMS_OUTPUT_DIRECTORY);
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY)) {
      onlyGenerateInventory = Boolean.parseBoolean(parameters.get(RodaConstants.PLUGIN_PARAMS_ONLY_GENERATE_INVENTORY));
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_START_DATE)) {
      String dateString = parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE);
      if (!dateString.isEmpty()) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
          startDate = formatter.parse(parameters.get(RodaConstants.PLUGIN_PARAMS_START_DATE));
        } catch (ParseException e) {
          throw new InvalidParameterException(e);
        }
      }
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processVoids(this, (index1, model1, storage1, report, cachedJob, jobPluginInfo,
      plugin) -> generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob), index, model, storage);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) throws PluginException {
    generateUsersBackfill(model, index, storage, report, jobPluginInfo, cachedJob);
    generateGroupsBackfill(model, index, storage, report, jobPluginInfo, cachedJob);
  }

  protected void generateUsersBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) throws PluginException {
    List<String> processedIds = new LinkedList<>();
    List<User> userObjects;
    try {
      userObjects = model.listUsers();
    } catch (GenericException e) {
      report.setPluginState(PluginState.FAILURE);
      report.setPluginDetails("Could not list users: " + e.getMessage());
      throw new PluginException(e);
    }
    int blockSize = RodaCoreFactory.getRodaConfigurationAsInt("core", "plugins", "internal", "backfill", "blockSize");
    if (blockSize == 0) {
      blockSize = 1000;
    }
    Add addBean = new Add();
    int docCount = 0;
    int addCount = 0;
    for (User user : userObjects) {
      try {
        if (!onlyGenerateInventory) {
          DocType docBean = GenerateBackfillPluginUtils.toDocBean(user, RODAMember.class);
          addBean.getDoc().add(docBean);
        }
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | NotSupportedException
        | GenericException e) {
        report.setPluginState(PluginState.FAILURE);
        report.setPluginDetails("Exception while processing object: " + e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        throw new PluginException(e);
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      processedIds.addLast(user.getId());
      docCount++;

      if (docCount >= blockSize && !onlyGenerateInventory) {
        try {
          StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory, User.class,
            Integer.toString(addCount));
          GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
          addBean = new Add();
          addCount++;
          docCount = 0;
        } catch (AuthorizationDeniedException | RequestNotValidException | GenericException | AlreadyExistsException
          | NotFoundException e) {
          report.setPluginState(PluginState.FAILURE);
          report.setPluginDetails("Exception while creating XML: " + e.getMessage());
          throw new PluginException(e);
        }
      }
    }
    try {
      if (docCount > 0) {
        StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory, User.class,
          Integer.toString(addCount));
        GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
      }
      StoragePath inventoryPath = GenerateBackfillPluginUtils.constructInventoryOutputPath(outputDirectory, User.class);
      GenerateBackfillPluginUtils.writeInventoryPartial(storage, inventoryPath, processedIds);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      report.setPluginState(PluginState.FAILURE);
      report.setPluginDetails("Exception while creating XML: " + e.getMessage());
      throw new PluginException(e);
    }
    report.setPluginState(PluginState.SUCCESS);
  }

  private void generateGroupsBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job cachedJob) throws PluginException {
    List<String> processedIds = new LinkedList<>();
    List<Group> groupObjects;
    try {
      groupObjects = model.listGroups();
    } catch (GenericException e) {
      report.setPluginState(PluginState.FAILURE);
      report.setPluginDetails("Could not list groups: " + e.getMessage());
      throw new PluginException(e);
    }
    int blockSize = RodaCoreFactory.getRodaConfigurationAsInt("core", "plugins", "internal", "backfill", "blockSize");
    if (blockSize == 0) {
      blockSize = 1000;
    }
    Add addBean = new Add();
    int docCount = 0;
    int addCount = 0;
    for (Group group : groupObjects) {
      try {
        if (!onlyGenerateInventory) {
          DocType docBean = GenerateBackfillPluginUtils.toDocBean(group, RODAMember.class);
          addBean.getDoc().add(docBean);
        }
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | NotSupportedException
        | GenericException e) {
        report.setPluginState(PluginState.FAILURE);
        report.setPluginDetails("Exception while processing object: " + e.getMessage());
        jobPluginInfo.incrementObjectsProcessedWithFailure();
        throw new PluginException(e);
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      processedIds.addLast(group.getId());
      docCount++;

      if (docCount >= blockSize && !onlyGenerateInventory) {
        try {
          StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory, Group.class,
            Integer.toString(addCount));
          GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
          addBean = new Add();
          addCount++;
          docCount = 0;
        } catch (AuthorizationDeniedException | RequestNotValidException | GenericException | AlreadyExistsException
          | NotFoundException e) {
          report.setPluginState(PluginState.FAILURE);
          report.setPluginDetails("Exception while creating XML: " + e.getMessage());
          throw new PluginException(e);
        }
      }
    }
    try {
      if (docCount > 0) {
        StoragePath addPath = GenerateBackfillPluginUtils.constructAddOutputPath(outputDirectory, Group.class,
          Integer.toString(addCount));
        GenerateBackfillPluginUtils.writeAddBean(storage, addPath, addBean);
      }
      StoragePath inventoryPath = GenerateBackfillPluginUtils.constructInventoryOutputPath(outputDirectory,
        Group.class);
      GenerateBackfillPluginUtils.writeInventoryPartial(storage, inventoryPath, processedIds);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      report.setPluginState(PluginState.FAILURE);
      report.setPluginDetails("Exception while creating XML: " + e.getMessage());
      throw new PluginException(e);
    }
    report.setPluginState(PluginState.SUCCESS);
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
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.MIGRATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Checked if ...";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "... with success.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to ...";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new GenerateRODAMemberBackfillPlugin();
  }
}
