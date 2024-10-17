/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE.md file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance.backfill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenerateBackfillPlugin<T extends IsRODAObject & IsModelObject> extends AbstractPlugin<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenerateBackfillPlugin.class);
  private int blockSize = 100000;

  private static Map<String, PluginParameter> pluginParameters = new HashMap<>();

  static {
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE,
      PluginParameter
        .getBuilder(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE, "Block size", PluginParameter.PluginParameterType.INTEGER)
        .withDefaultValue("100000").isMandatory(false)
        .withDescription("Number of documents in each index documents block.").build());
  }

  protected GenerateBackfillPlugin() {
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
    return parameters;
  }

  @Override
  public void setParameterValues(Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE)) {
      blockSize = Integer.parseInt(parameters.get(RodaConstants.PLUGIN_PARAMS_BLOCK_SIZE));
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
        if (count == blockSize) {
          count = 0;
          totalBeans += 1;
          GenerateBackfillPluginUtils.writeAddBean(storage, object.getClass().getName() + "_" + totalBeans + ".xml",
            addType);
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
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_EXPERIMENTAL);
  }
}
