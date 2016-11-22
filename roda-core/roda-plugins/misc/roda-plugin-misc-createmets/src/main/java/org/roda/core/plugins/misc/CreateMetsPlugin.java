/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.misc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.JobPluginInfo;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.roda_project.commons_ip.model.impl.eark.EARKMETSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that generates the root "METS.xml" from the exiting AIP information in
 * the storage layer.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class CreateMetsPlugin extends AbstractPlugin<AIP> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMetsPlugin.class);

  /** Plugin version. */
  private static final String VERSION = "1.0";

  /** Plugin parameter 'mimetype'. */
  private static final PluginParameter PARAM_MIMETYPE = new PluginParameter("mimetype", "Mimetype",
    PluginParameter.PluginParameterType.BOOLEAN, "true", false, false, "Check Mimetype?");

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
    return "Create METS";
  }

  @Override
  public String getDescription() {
    return "Plugin that generates the root \"METS.xml\" from the exiting AIP information in the storage layer.";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    final List<AIP> list) throws PluginException {

    try {
      final Report report = PluginHelper.initPluginReport(this);
      final SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());

      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (AIP aip : list) {
        executeOnAip(aip, index, model, storage, jobPluginInfo, report);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      return report;

    } catch (final JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }
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
    return Arrays.asList(PARAM_MIMETYPE);
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
    final StorageService storage, final JobPluginInfo jobPluginInfo, final Report report) {
    LOGGER.debug("Processing AIP {}", aip.getId());

    final Report reportItem = PluginHelper.initPluginReportItem(this, aip.getId(), AIP.class, AIPState.ACTIVE);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);

    // Create METS
    EARKMETSUtils earkmetsUtils;

    report.addReport(reportItem);
    PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
  }
}
