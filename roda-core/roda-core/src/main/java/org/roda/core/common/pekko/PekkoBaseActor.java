/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko;

import org.apache.pekko.actor.UntypedAbstractActor;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.transaction.RODATransactionManager;

import com.codahale.metrics.MetricRegistry;

public abstract class PekkoBaseActor extends UntypedAbstractActor {

  public PekkoBaseActor() {
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
    org.slf4j.MDC.put("pekkoSourceActor", self().path().toString());
    org.slf4j.MDC.put("pekkoSourceThread", Thread.currentThread().getName());
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

  public RODATransactionManager getStorageTransactionManager() throws GenericException {
    return RodaCoreFactory.getTransactionManager();
  }

}
