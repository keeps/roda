/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@jakarta.xml.bind.annotation.XmlRootElement(name = "metrics")
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metrics {
  private final Map<String, String> metricList;

  public Metrics() {
    metricList = new HashMap<>();
  }

  public Map<String, String> getMetrics() {
    return metricList;
  }

  public void addMetric(String metricName, String metricValue) {
    metricList.put(metricName, metricValue);
  }

}
