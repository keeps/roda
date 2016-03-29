/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.orchestrate.JobPluginInfo;

public interface PluginOrchestrator {

  public <T extends IsIndexed> void runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin);

  public List<Report> runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids);

  public List<Report> runPluginOnAllAIPs(Plugin<AIP> plugin);

  public List<Report> runPluginOnAllRepresentations(Plugin<Representation> plugin);

  public List<Report> runPluginOnAllFiles(Plugin<File> plugin);

  public List<Report> runPluginOnTransferredResources(Plugin<TransferredResource> plugin,
    List<TransferredResource> paths);

  public <T extends Serializable> void runPlugin(Plugin<T> plugin);

  public <T extends Serializable> void runPluginOnObjects(Plugin<T> plugin, List<String> ids);

  public void setup();

  public void shutdown();

  /** 201603 hsilva: this method should be async */
  public void executeJob(Job job);

  public void stopJob(Job job);

  public <T extends Serializable> void updateJobInformation(Plugin<T> plugin, JobPluginInfo jobPluginInfo);

}
