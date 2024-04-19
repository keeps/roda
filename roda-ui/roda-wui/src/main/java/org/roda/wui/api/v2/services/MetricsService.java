package org.roda.wui.api.v2.services;

import java.util.List;
import java.util.SortedMap;

import org.roda.core.data.v2.common.Metrics;
import org.roda.wui.common.RodaWuiController;
import org.springframework.stereotype.Service;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

@Service
public class MetricsService {

  public Metrics getMetrics(List<String> metricsToObtain) {
    // delegate
    MetricRegistry metricRegistry = RodaWuiController.getMetricRegistry();
    org.roda.core.data.v2.common.Metrics metrics = new org.roda.core.data.v2.common.Metrics();
    SortedMap<String, Counter> counters = metricRegistry
      .getCounters((metricName, metric) -> metricsToObtain.contains(metricName));
    counters.forEach((a, b) -> metrics.addMetric(a, Long.toString(b.getCount())));

    return metrics;
  }
}
