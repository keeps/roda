package org.roda.wui.api.controllers;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginInfo;
import org.roda.core.data.v2.jobs.PluginParameter;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.wui.client.ingest.process.CreateIngestJobBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.roda.wui.client.ingest.process.JobBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author António Lindo <alindo@keep.pt>
 */
public class JobBundles {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobsHelper.class);

  JobBundles(){
    // do nothing
  }

  public static JobBundle retrieveJobBundle(Job job) {
    List<PluginInfo> pluginsInfo = new ArrayList<>();

    PluginInfo basePlugin = RodaCoreFactory.getPluginManager().getPluginInfo(job.getPlugin());

    if (basePlugin != null) {
      pluginsInfo.add(basePlugin);

      for (PluginParameter parameter : basePlugin.getParameters()) {
        if (PluginParameter.PluginParameterType.PLUGIN_SIP_TO_AIP.equals(parameter.getType())) {
          String pluginId = job.getPluginParameters().get(parameter.getId());
          if (pluginId == null) {
            pluginId = parameter.getDefaultValue();
          }
          if (pluginId != null) {
            PluginInfo refPlugin = RodaCoreFactory.getPluginManager().getPluginInfo(pluginId);
            if (refPlugin != null) {
              pluginsInfo.add(refPlugin);
            } else {
              LOGGER.warn("Could not find plugin: " + pluginId);
            }
          }
        }
      }
    }

    // FIXME nvieira 20170208 it could possibly, in the future, be necessary to
    // add more plugin types adding all AIP to AIP plugins for job report list
    List<PluginInfo> aipToAipPlugins = RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.AIP_TO_AIP);
    if (aipToAipPlugins != null) {
      pluginsInfo.addAll(aipToAipPlugins);
    }

    JobBundle bundle = new JobBundle();
    bundle.setJob(job);
    bundle.setPluginsInfo(pluginsInfo);
    return bundle;
  }

  public static CreateIngestJobBundle retrieveCreateIngestProcessBundle() {
    // TODO check permissions
    CreateIngestJobBundle bundle = new CreateIngestJobBundle();
    bundle.setIngestPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.INGEST));
    bundle.setSipToAipPlugins(RodaCoreFactory.getPluginManager().getPluginsInfo(PluginType.SIP_TO_AIP));
    return bundle;
  }

}
