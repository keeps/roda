package org.roda.core.plugins;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.storage.StorageService;

public interface RODAObjectProcessingLogic<T extends IsRODAObject> {
  public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
    SimpleJobPluginInfo jobPluginInfo, Plugin<T> plugin, T object);
}
