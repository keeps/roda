/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.action.orchestrate;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.Representation;
import org.roda.model.AIP;
import org.roda.model.File;

public interface ActionOrchestrator {

  public <T extends Serializable> void runActionFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> action);

  public void runActionOnAIPs(Plugin<AIP> action, List<String> ids);

  public void runActionOnAllAIPs(Plugin<AIP> action);

  public void runActionOnAllRepresentations(Plugin<Representation> action);

  public void runActionOnAllFiles(Plugin<File> action);

  public void setup();

  public void shutdown();

  public void runActionOnFiles(Plugin<String> action, List<Path> paths);

}
