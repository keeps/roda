/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.roda.core.TestContainersManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads Solr's own update metrics (Prometheus format): number of update
 * requests and time spent in them, per collection.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
final class SolrUpdateMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrUpdateMetrics.class);
  private static final Pattern REQUESTS = Pattern.compile(
    "^solr_core_requests_total\\{category=\"UPDATE\",collection=\"([^\"]+)\".*handler=\"/update\".*\\} ([0-9.eE+-]+)$");
  private static final Pattern TIME = Pattern.compile(
    "^solr_core_requests_times_milliseconds_sum\\{category=\"UPDATE\",collection=\"([^\"]+)\".*handler=\"/update\".*\\} ([0-9.eE+-]+)$");

  private SolrUpdateMetrics() {
    // utility class
  }

  /** @return "requests.<collection>", "ms.<collection>" and the totals */
  static Map<String, Double> read() {
    Map<String, Double> values = new TreeMap<>();
    String baseUrl = System.getProperty(TestContainersManager.SOLR_BASE_URL_PROPERTY);
    if (baseUrl == null) {
      return values;
    }
    try {
      HttpResponse<String> response = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder(URI.create(baseUrl + "/admin/metrics")).GET().build(),
        HttpResponse.BodyHandlers.ofString());
      for (String line : response.body().split("\n")) {
        add(values, REQUESTS.matcher(line), "requests.");
        add(values, TIME.matcher(line), "ms.");
      }
    } catch (Exception e) {
      LOGGER.warn("Could not read Solr metrics", e);
    }
    return values;
  }

  private static void add(Map<String, Double> values, Matcher matcher, String prefix) {
    if (matcher.matches()) {
      double value = Double.parseDouble(matcher.group(2));
      values.merge(prefix + matcher.group(1), value, Double::sum);
      values.merge(prefix + "total", value, Double::sum);
    }
  }

  static Map<String, Double> difference(Map<String, Double> before, Map<String, Double> after) {
    Map<String, Double> diff = new TreeMap<>();
    after.forEach((key, value) -> {
      double delta = value - before.getOrDefault(key, 0.0);
      if (delta != 0) {
        diff.put(key, delta);
      }
    });
    return diff;
  }
}
