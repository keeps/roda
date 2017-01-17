/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.RODAObjectProcessingLogic;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda_project.commons_ip.model.ParseException;
import org.roda_project.commons_ip.model.impl.eark.EARKAIP;
import org.roda_project.commons_ip.utils.IPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that generates E-ARK AIP manifest files (METS.xml) from the exiting
 * AIP information in the storage layer.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CreateMetsPlugin extends AbstractPlugin<AIP> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMetsPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

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
    return "Create E-ARK AIP manifest files (METS.xml)";
  }

  @Override
  public String getDescription() {
    return "Plugin that generates E-ARK AIP manifest files (\"METS.xml\") from "
      + "existing AIP information in the storage layer. This plugin only works with filesystem as the storage service.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Plugin<AIP> cloneMe() {
    return new CreateMetsPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.MISC;
  }

  @Override
  public List<PluginParameter> getParameters() {
    return new ArrayList<>();
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.RISK_MANAGEMENT;
  }

  @Override
  public String getPreservationEventDescription() {
    return "TODO: Preservation event description";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "Success";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failure";
  }

  @Override
  public Report beforeAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(final IndexService index, final ModelService model, final StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Collections.singletonList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public List<Class<AIP>> getObjectClasses() {
    return Collections.singletonList(AIP.class);
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    final List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectProcessingLogic<AIP>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<AIP> plugin, AIP object) {
        executeOnAip(object, index, model, storage, jobPluginInfo, report, cachedJob);
      }
    }, index, model, storage, liteList);
  }

  /**
   * Execute on a single {@link AIP}.
   * 
   * @param aip
   *          the {@link AIP}.
   * @param index
   *          the {@link IndexService}.
   * @param model
   *          the {@link ModelService}.
   * @param storage
   *          the {@link StorageService}.
   * @param jobPluginInfo
   *          the {@link JobPluginInfo}
   * @param report
   *          the {@link Report}.
   */
  private void executeOnAip(final AIP aip, final IndexService index, final ModelService model,
    final StorageService storage, final JobPluginInfo jobPluginInfo, final Report report, final Job job) {
    LOGGER.debug("Processing AIP {}", aip.getId());

    final Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false, job);

    try {
      final Path aipPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(),
        ModelUtils.getAIPStoragePath(aip.getId()));
      LOGGER.debug(String.format("aipPath=%s", aipPath));

      new EARKAIP(RodaFolderAIP.parse(aipPath)).build(aipPath.getParent(), true);

      jobPluginInfo.incrementObjectsProcessedWithSuccess();
      reportItem.setPluginState(PluginState.SUCCESS);

    } catch (final RODAException | ParseException | IPException | InterruptedException e) {
      final String message = String.format("Error creating manifest files for AIP %s. Cause: %s.", aip.getId(),
        e.getMessage());
      LOGGER.debug(message, e);
      jobPluginInfo.incrementObjectsProcessedWithFailure();
      reportItem.setPluginState(PluginState.FAILURE);
      reportItem.setPluginDetails(message);
    }

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true, job);
  }
}
