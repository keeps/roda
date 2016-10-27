/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.findunknownfiles;

import java.util.Arrays;
import java.util.List;

import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.JobException;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.AbstractPlugin;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin that finds files with an unknown format.
 * 
 * @author Rui Castro <rui.castro@gmail.com>
 */
public class FindUnknownFilesPlugin extends AbstractPlugin<File> {
  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(FindUnknownFilesPlugin.class);
  /** Plugin version. */
  private static final String VERSION = "1.0";

  @Override
  public void init() throws PluginException {
  }

  @Override
  public void shutdown() {
    // do nothing
  }

  @Override
  public String getName() {
    return "AIP unknown file detector";
  }

  @Override
  public String getDescription() {
    return "AIP unknown file detector";
  }

  @Override
  public String getVersionImpl() {
    return VERSION;
  }

  @Override
  public Report execute(final IndexService index, final ModelService model, final StorageService storage,
    final List<File> list) throws PluginException {

    final Report report = PluginHelper.initPluginReport(this);

    try {
      final SimpleJobPluginInfo jobPluginInfo = PluginHelper.getInitialJobInformation(this, list.size());
      PluginHelper.updateJobInformation(this, jobPluginInfo);

      for (File file : list) {
        LOGGER.debug("Processing file {}", file.getId());

        final Report reportItem = PluginHelper.initPluginReportItem(this, IdUtils.getFileId(file), File.class,
          AIPState.ACTIVE);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, false);
        final PluginState reportState = PluginState.SUCCESS;

        if (reportState.equals(PluginState.SUCCESS)) {
          jobPluginInfo.incrementObjectsProcessedWithSuccess();
          reportItem.setPluginState(PluginState.SUCCESS);
        } else {
          jobPluginInfo.incrementObjectsProcessedWithFailure();
          reportItem.setHtmlPluginDetails(true).setPluginState(PluginState.FAILURE);
        }

        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(this, model, index, reportItem, true);
      }

      jobPluginInfo.finalizeInfo();
      PluginHelper.updateJobInformation(this, jobPluginInfo);
    } catch (final JobException e) {
      throw new PluginException("A job exception has occurred", e);
    }

    return report;
  }

  @Override
  public Plugin<File> cloneMe() {
    return new FindUnknownFilesPlugin();
  }

  @Override
  public PluginType getType() {
    return PluginType.AIP_TO_AIP;
  }

  @Override
  public boolean areParameterValuesValid() {
    return true;
  }

  // TODO FIX
  @Override
  public PreservationEventType getPreservationEventType() {
    return PreservationEventType.FORMAT_IDENTIFICATION;
  }

  @Override
  public String getPreservationEventDescription() {
    return "Identified the object's file formats and versions using DROID.";
  }

  @Override
  public String getPreservationEventSuccessMessage() {
    return "File formats were identified and recorded in PREMIS objects.";
  }

  @Override
  public String getPreservationEventFailureMessage() {
    return "Failed to identify file formats in the package.";
  }

  @Override
  public Report beforeAllExecute(IndexService index, ModelService model, StorageService storage)
    throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public Report afterAllExecute(IndexService index, ModelService model, StorageService storage) throws PluginException {
    // do nothing
    return null;
  }

  @Override
  public List<String> getCategories() {
    return Arrays.asList(RodaConstants.PLUGIN_CATEGORY_MISC);
  }

  @Override
  public List<Class<File>> getObjectClasses() {
    return Arrays.asList(File.class);
  }

}
