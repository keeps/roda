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
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;

import akka.actor.ActorRef;

public interface PluginOrchestrator {

  public <T extends Serializable> void runPluginFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> plugin);

  public List<Report> runPluginOnAIPs(Plugin<AIP> plugin, List<String> ids);

  public void runPluginOnAllAIPs(Plugin<AIP> plugin);

  public void runPluginOnAllRepresentations(Plugin<Representation> plugin);

  public void runPluginOnAllFiles(Plugin<File> plugin);

  public void runPluginOnTransferredResources(Plugin<TransferredResource> plugin, List<TransferredResource> paths);

  public <T extends Serializable> void runPlugin(Plugin<T> plugin);

  public <T extends Serializable> void runPluginOnObjects(Plugin<T> plugin, List<String> ids);

  public void setup();

  public void shutdown();

  public ActorRef getCoordinator();
}
