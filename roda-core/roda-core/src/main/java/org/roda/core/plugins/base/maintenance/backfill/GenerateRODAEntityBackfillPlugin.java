/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.NotSupportedException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAObjectsProcessingLogic;
import org.roda.core.plugins.base.maintenance.backfill.beans.Add;
import org.roda.core.plugins.base.maintenance.backfill.beans.Delete;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenerateRODAEntityBackfillPlugin<T extends IsRODAObject & IsModelObject>
  extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateRODAEntityBackfillPlugin.class);
  private int blockSize = 100000;
  private String validateAgainst = "None";

  private final HashSet<String> processedObjectIds = new HashSet<>();

  private final static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE, "Block size", PluginParameter.PluginParameterType.INTEGER)
        .withDefaultValue("100000").isMandatory(false)
        .withDescription("Number of documents in each index documents block.").build());
    pluginParameters
      .put(RodaConstants.PLUGIN_PARAMS_VALIDATE_AGAINST,
        PluginParameter
          .getBuilder(RodaConstants.PLUGIN_PARAMS_VALIDATE_AGAINST, "Check parity against",
            PluginParameter.PluginParameterType.DROPDOWN)
          .withPossibleValues(Arrays.asList(GenerateBackfillPluginUtils.VALIDATE_AGAINST_NONE,
            GenerateBackfillPluginUtils.VALIDATE_AGAINST_INDEX, GenerateBackfillPluginUtils.VALIDATE_AGAINST_STORAGE))
          .withDefaultValue(GenerateBackfillPluginUtils.VALIDATE_AGAINST_NONE).isMandatory(true)
          .withDescription(
            "Which collection the final result will be validated against to adjust for mid-execution changes.")
          .build());

  }

  protected GenerateRODAEntityBackfillPlugin() {
    super();
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
    // Get from pom.xml <name>
    return getClass().getPackage().getImplementationTitle();
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
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE));
    parameters.add(pluginParameters.get(RodaConstants.PLUGIN_PARAMS_VALIDATE_AGAINST));
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE)) {
      blockSize = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE));
    }
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_VALIDATE_AGAINST)) {
      validateAgainst = parameters.get(RodaConstants.PLUGIN_PARAMS_VALIDATE_AGAINST);
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this,
      (RODAObjectsProcessingLogic<T>) (index1, model1, storage1, report, cachedJob, jobPluginInfo, plugin,
        objects) -> generateBackfill(model1, index1, storage1, report, jobPluginInfo, cachedJob, objects),
      index, model, storage, liteList);
  }

  protected void generateBackfill(ModelService model, IndexService index, StorageService storage, Report report,
    JobPluginInfo jobPluginInfo, Job job, List<T> objects) {
    int totalBeans = 0;
    int count = 0;
    Add addType = new Add();

    for (T object : objects) {
      // TODO Handle exceptions
      try {
        processedObjectIds.add(object.getId());
        if (count == blockSize) {
          count = 0;
          totalBeans += 1;
          GenerateBackfillPluginUtils.writeAddBean(storage, pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID)
            + "/" + object.getClass().getName() + "_" + totalBeans + ".xml", addType);
        }
        addType.getDoc().add(GenerateBackfillPluginUtils.toDocBean(object, getIndexClass()));
        count += 1;
      } catch (AuthorizationDeniedException e) {
        throw new RuntimeException(e);
      } catch (RequestNotValidException e) {
        throw new RuntimeException(e);
      } catch (NotFoundException e) {
        throw new RuntimeException(e);
      } catch (NotSupportedException e) {
        throw new RuntimeException(e);
      } catch (GenericException e) {
        throw new RuntimeException(e);
      } catch (AlreadyExistsException e) {
        throw new RuntimeException(e);
      }
    }
    // TODO Handle exceptions
    try {
      totalBeans += 1;
      GenerateBackfillPluginUtils.writeAddBean(storage,
        objects.getFirst().getClass().getSimpleName() + "_" + totalBeans + ".xml", addType);
    } catch (AlreadyExistsException | RequestNotValidException | GenericException | AuthorizationDeniedException
      | NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract <I extends IsIndexed> Class<I> getIndexClass();

  protected List<T> retrieveModelObjects(ModelService model, List<String> ids)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException {
    List<T> result = new ArrayList<>();
    for (String id : ids) {
      result.add(retrieveModelObject(model, id));
    }
    return result;
  }

  protected abstract T retrieveModelObject(ModelService model, String id)
    throws AuthorizationDeniedException, NotFoundException, GenericException, RequestNotValidException;

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
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
    if (validateAgainst.equals(GenerateBackfillPluginUtils.VALIDATE_AGAINST_INDEX)) {
      validateAgainstIndex(index, model, storage);
    }
    return null;

  }

  protected Report validateAgainstIndex(IndexService index, ModelService model, StorageService storage) {
    // TODO Handle exceptions
    try {
      List<Add> originalAddBeans = GenerateBackfillPluginUtils.readAddBeans(storage, "test");
      List<String> deletedObjectIds = GenerateBackfillPluginUtils.checkIndexForDeletedObjects(index, getIndexClass(),
        processedObjectIds.stream().toList());
      List<String> addedObjects = GenerateBackfillPluginUtils.checkIndexForAddedObjects(index, getIndexClass(),
        processedObjectIds);
      Add addBean = new Add();
      int addBeanCount = 0;
      Delete deleteBean = new Delete();
      int deleteBeanCount = 0;
      while (!deletedObjectIds.isEmpty() || !addedObjects.isEmpty()) {
        for (String id : deletedObjectIds) {
          // Write delete query and remove from the processed ids
          deleteBean.getId().add(id);
          processedObjectIds.remove(id);
          if (deleteBean.getId().size() > blockSize) {
            GenerateBackfillPluginUtils.writeDeleteBean(storage,
              pluginParameters.get(RodaConstants.PLUGIN_PARAMS_JOB_ID) + "/index_deleted/"
                + getIndexClass().getSimpleName() + "_" + deleteBeanCount + ".xml",
              deleteBean);
            deleteBean = new Delete();
          }
        }
        for (String id : addedObjects) {
          // Write add query and add to the processed ids
          addBean.getDoc().add(GenerateBackfillPluginUtils.toDocBean(retrieveModelObject(model, id), getIndexClass()));
          processedObjectIds.add(id);
          if (addBean.getDoc().size() > blockSize) {
            GenerateBackfillPluginUtils.writeAddBean(storage,
              "index_added/" + getIndexClass().getSimpleName() + "_" + addBeanCount + ".xml", addBean);
            addBean = new Add();
          }
        }
        deletedObjectIds = GenerateBackfillPluginUtils.checkIndexForDeletedObjects(index, getIndexClass(),
          processedObjectIds.stream().toList());
        addedObjects = GenerateBackfillPluginUtils.checkIndexForAddedObjects(index, getIndexClass(),
          processedObjectIds);
      }
    } catch (GenericException e) {
      throw new RuntimeException(e);
    } catch (RequestNotValidException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (AuthorizationDeniedException e) {
      throw new RuntimeException(e);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    } catch (NotSupportedException e) {
      throw new RuntimeException(e);
    } catch (AlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    // TODO return report
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }
}
