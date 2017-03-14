/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.util.List;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.orchestrate.SimpleJobPluginInfo;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAIPComponentsPlugin<T extends IsRODAObject> extends AbstractPlugin<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAIPComponentsPlugin.class);

  @Override
  public Report execute(IndexService index, ModelService model, StorageService storage,
    List<LiteOptionalWithCause> liteList) throws PluginException {
    return PluginHelper.processObjects(this, new RODAObjectsProcessingLogic<T>() {
      @Override
      public void process(IndexService index, ModelService model, StorageService storage, Report report, Job cachedJob,
        SimpleJobPluginInfo jobPluginInfo, Plugin<T> plugin, List<T> objects) {
        if (!objects.isEmpty()) {
          try {
            if (objects.get(0) instanceof AIP) {
              report = executeOnAIP(index, model, storage, report, jobPluginInfo, (List<AIP>) objects, cachedJob);
            } else if (objects.get(0) instanceof Representation) {
              report = executeOnRepresentation(index, model, storage, report, jobPluginInfo,
                (List<Representation>) objects, cachedJob);
            } else if (objects.get(0) instanceof File) {
              report = executeOnFile(index, model, storage, report, jobPluginInfo, (List<File>) objects, cachedJob);
            }
          } catch (PluginException e) {
            LOGGER.error("Error while executing 'executeOnX' method", e);
            report.setPluginState(PluginState.FAILURE);
          }
        }

      }
    }, index, model, storage, liteList);
  }

  protected abstract Report executeOnAIP(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<AIP> list, Job job) throws PluginException;

  protected abstract Report executeOnRepresentation(IndexService index, ModelService model, StorageService storage,
    Report report, SimpleJobPluginInfo jobPluginInfo, List<Representation> list, Job job) throws PluginException;

  protected abstract Report executeOnFile(IndexService index, ModelService model, StorageService storage, Report report,
    SimpleJobPluginInfo jobPluginInfo, List<File> list, Job job) throws PluginException;

}
