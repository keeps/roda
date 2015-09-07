package org.roda.action.orchestrate;

import java.io.Serializable;

import org.roda.model.AIP;
import org.roda.model.File;

import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.v2.Representation;

public interface ActionOrchestrator {

  public <T extends Serializable> void runActionFromIndex(Class<T> classToActOn, Filter filter, Plugin<T> action);

  public void runActionOnAllAIPs(Plugin<AIP> action);

  public void runActionOnAllRepresentations(Plugin<Representation> action);

  public void runActionOnAllFiles(Plugin<File> action);

}
