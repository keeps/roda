/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import org.roda.core.RodaCoreFactory;

import com.codahale.metrics.MetricRegistry;

public abstract class RodaWuiController {
  protected static MetricRegistry getMetricRegistry() {
    return RodaCoreFactory.getMetrics();
  }
}
