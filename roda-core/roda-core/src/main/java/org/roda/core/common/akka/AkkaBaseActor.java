/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.akka;

import org.roda.core.RodaCoreFactory;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.storage.StorageService;

import com.codahale.metrics.MetricRegistry;

import akka.actor.UntypedAbstractActor;

public abstract class AkkaBaseActor extends UntypedAbstractActor {

  public AkkaBaseActor() {
    setup();
  }

  private void setup() {
    try {
      setup(null);
    } catch (Exception e) {
      // do nothing
    }
  }

  public void setup(Object msg) throws Exception {
    org.slf4j.MDC.put("akkaSourceActor", self().path().toString());
    org.slf4j.MDC.put("akkaSourceThread", Thread.currentThread().getName());
  }

  public StorageService getStorage() {
    return RodaCoreFactory.getStorageService();
  }

  public ModelService getModel() {
    return RodaCoreFactory.getModelService();
  }

  public IndexService getIndex() {
    return RodaCoreFactory.getIndexService();
  }

  public PluginOrchestrator getPluginOrchestrator() {
    return RodaCoreFactory.getPluginOrchestrator();
  }

  public PluginManager getPluginManager() {
    return RodaCoreFactory.getPluginManager();
  }

  public MetricRegistry getMetricRegistry() {
    return RodaCoreFactory.getMetrics();
  }

}
