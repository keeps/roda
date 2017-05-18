/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.List;
import java.util.SortedMap;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class Metrics extends RodaWuiController {

  private Metrics() {
    super();
  }

  public static org.roda.core.data.v2.common.Metrics getMetrics(User user, List<String> metricsToObtain)
    throws AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // check user permissions
    controllerAssistant.checkRoles(user);

    // delegate
    MetricRegistry metricRegistry = RodaWuiController.getMetricRegistry();
    org.roda.core.data.v2.common.Metrics metrics = new org.roda.core.data.v2.common.Metrics();
    SortedMap<String, Counter> counters = metricRegistry.getCounters(new MetricFilter() {
      @Override
      public boolean matches(String metricName, Metric metric) {
        return metricsToObtain.contains(metricName);
      }
    });
    counters.forEach((a, b) -> metrics.addMetric(a, Long.toString(b.getCount())));

    controllerAssistant.registerAction(user, LOG_ENTRY_STATE.SUCCESS);
    return metrics;
  }
}
