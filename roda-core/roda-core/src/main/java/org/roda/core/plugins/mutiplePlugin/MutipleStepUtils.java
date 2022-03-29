package org.roda.core.plugins.mutiplePlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.orchestrate.MutipleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class MutipleStepUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(MutipleStepUtils.class);

  private MutipleStepUtils() {
    // do nothing
  }

  public static void executePlugin(final MutipleStepBlundle bundle, final MutipleStep step, final Class classOfplugin) {
    final Report pluginReport = MutipleStepUtils.executeStep(bundle, step, classOfplugin);
    mergeReports(bundle.getJobPluginInfo(), pluginReport);
  }

  public static Report executeStep(final MutipleStepBlundle bundle, final MutipleStep step, final Class classOfplugin) {
    Plugin<?> plugin = RodaCoreFactory.getPluginManager().getPlugin(step.getPluginName(), classOfplugin);
    plugin.setMandatory(step.isMandatory());
    Map<String, String> mergedParams = new HashMap<>(bundle.getParameterValues());
    if (step.getParameters() != null) {
      mergedParams.putAll(step.getParameters());
    }

    try {
      plugin.setParameterValues(mergedParams);
      List<LiteOptionalWithCause> lites = LiteRODAObjectFactory.transformIntoLiteWithCause(bundle.getModel(),
        bundle.getObjects());
      return plugin.execute(bundle.getIndex(), bundle.getModel(), bundle.getStorage(), lites);
    } catch (InvalidParameterException | PluginException | RuntimeException e) {
      LOGGER.error("Error executing plugin: {}", step.getPluginName(), e);

      Report report = PluginHelper.initPluginReport(plugin);
      for (IsRODAObject rodaObject : bundle.getObjects()) {
        Report reportItem = PluginHelper.initPluginReportItem(plugin, rodaObject.getId(), classOfplugin,
          AIPState.INGEST_PROCESSING);
        reportItem.setPluginDetails(e.getMessage());
        reportItem.setPluginState(PluginState.FAILURE);
        report.addReport(reportItem);
        PluginHelper.updatePartialJobReport(plugin, bundle.getModel(), reportItem, false, bundle.getCachedJob());
      }

      report.setPluginDetails(e.getMessage());
      report.setPluginState(PluginState.FAILURE);
      return report;
    }
  }

  public static void mergeReports(final MutipleJobPluginInfo jobPluginInfo, final Report pluginReport) {
    if (pluginReport != null) {
      for (Report reportItem : pluginReport.getReports()) {
        jobPluginInfo.addReport(reportItem);
      }
    }
  }

}
