/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.maintenance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.roda.core.catalog.CatalogService;
import org.roda.core.catalog.Catalogs;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.Void;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginParameter.PluginParameterType;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.NonTransactionalPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.RODAProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rebuilds the AIP catalog from the main storage (or only verifies it). Run it
 * after upgrading, or whenever the catalog may be out of sync.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RebuildCatalogPlugin extends AbstractPlugin<Void> implements NonTransactionalPlugin {
  private static final Logger LOGGER = LoggerFactory.getLogger(RebuildCatalogPlugin.class);
  public static final String PLUGIN_PARAMS_VERIFY_ONLY = "parameter.verify_only";

  private boolean verifyOnly = false;

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
    return "Rebuild AIP catalog";
  }

  @Override
  public String getDescription() {
    return "Rebuilds the AIP catalog (structural information used by ingest and maintenance actions) from the main"
      + " storage, or only reports the differences when 'verify only' is set.";
  }

  @Override
  public String getVersionImpl() {
    return "1.0";
  }

  @Override
  public List<PluginParameter> getParameters() {
    return List.of(PluginParameter.getBuilder(PLUGIN_PARAMS_VERIFY_ONLY, "Verify only", PluginParameterType.BOOLEAN)
      .withDefaultValue("false").isMandatory(false)
      .withDescription("Only report differences between the catalog and the main storage").build());
  }

  @Override
  public void setParameterValues(java.util.Map<String, String> parameters) throws InvalidParameterException {
    super.setParameterValues(parameters);
    if (parameters != null && parameters.containsKey(PLUGIN_PARAMS_VERIFY_ONLY)) {
      verifyOnly = Boolean.parseBoolean(parameters.get(PLUGIN_PARAMS_VERIFY_ONLY));
    }
  }

  @Override
  public Report execute(IndexService index, ModelService model, List<LiteOptionalWithCause> list)
    throws PluginException {
    return PluginHelper.processVoids(this, new RODAProcessingLogic<Void>() {
      @Override
      public void process(IndexService index, ModelService model, Report report, Job cachedJob,
        JobPluginInfo jobPluginInfo, Plugin<Void> plugin) {
        rebuild(model, report, jobPluginInfo);
      }
    }, index, model);
  }

  private void rebuild(ModelService model, Report report, JobPluginInfo jobPluginInfo) {
    Optional<CatalogService> catalog = Catalogs.service();
    if (catalog.isEmpty()) {
      report.setPluginState(PluginState.FAILURE).setPluginDetails("The AIP catalog is not available");
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      return;
    }
    try {
      if (verifyOnly) {
        List<String> differences = catalog.get().verify(model);
        report.setPluginState(differences.isEmpty() ? PluginState.SUCCESS : PluginState.PARTIAL_SUCCESS)
          .setPluginDetails(differences.isEmpty() ? "The catalog matches the main storage"
            : differences.size() + " differences:\n" + String.join("\n", differences));
      } else {
        long count = catalog.get().rebuild(model);
        report.setPluginState(PluginState.SUCCESS).setPluginDetails("Catalog rebuilt with " + count + " AIPs");
      }
      jobPluginInfo.incrementObjectsProcessedWithSuccess();
    } catch (Exception e) {
      LOGGER.error("Error rebuilding the AIP catalog", e);
      report.setPluginState(PluginState.FAILURE).setPluginDetails(e.getMessage());
      jobPluginInfo.incrementObjectsProcessedWithFailure();
    }
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model) {
    return new Report();
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model) {
    return new Report();
  }

  @Override
  public Plugin<Void> cloneMe() {
    return new RebuildCatalogPlugin();
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
    return PreservationEventType.NONE;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Rebuild AIP catalog";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "The AIP catalog was rebuilt with success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "An error occurred while rebuilding the AIP catalog";
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_REINDEX);
  }

  @Override
  public List<Class<Void>> getObjectClasses() {
    return Arrays.asList(Void.class);
  }
}
