/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import dev.failsafe.RetryPolicy;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class RetryPolicyBuilder {

  private static RetryPolicyBuilder instance;
  private final RetryPolicy<Object> policy;
  private final Histogram retriesHisto;
  private final Counter retriesCount;

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryPolicyBuilder.class);

  private RetryPolicyBuilder() {
    int delay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_DELAY, 1);
    int maxDelay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_DELAY,
      RodaConstants.NodeType.TEST.equals(RodaCoreFactory.getNodeType()) ? 2 : 180);
    double delayFactor = RodaCoreFactory.getRodaConfiguration().getDouble(RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0);
    int maxRetries = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_RETRIES, 10);

    this.retriesHisto = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retriesHisto"));
    this.retriesCount = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retriesCounter"));

    policy = RetryPolicy.builder().handle(getHandleExceptionsFromConfiguration())
      .withBackoff(delay, maxDelay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(maxRetries).onRetry(event -> {
        LOGGER.warn("Attempt #{}: {} [{}]", event.getAttemptCount(), event.getLastException().getMessage(),
          event.getLastException().getClass().getSimpleName());
        retriesCount.inc();
        retriesHisto.update(retriesCount.getCount());
      }).onRetriesExceeded(event -> {
        LOGGER.warn("Number of max retries exceeded", event.getException());
      }).onFailure(e -> {
        LOGGER.error("Failed due to: {}", e.getException().getMessage(), e.getException());
      }).onSuccess(event -> {
        if (event.getAttemptCount() > 1) {
          LOGGER.debug("Success retry after {} attempts", event.getAttemptCount());
        }
      }).build();
  }

  public static RetryPolicyBuilder getInstance() {
    if (instance == null) {
      instance = new RetryPolicyBuilder();
    }

    return instance;
  }

  public RetryPolicy<Object> getRetryPolicy() {
    return policy;
  }

  private List<Class<? extends Throwable>> getHandleExceptionsFromConfiguration() {
    List<Class<? extends Throwable>> classes = new ArrayList<>();
    List<String> exceptions = RodaUtils
      .copyList(RodaCoreFactory.getRodaConfiguration().getList(RodaConstants.SOLR_RETRY_HANDLE_EXCEPTIONS));

    if (exceptions.isEmpty()) {
      return Arrays.asList(SolrServerException.class, IOException.class);
    } else {
      for (String exception : exceptions) {
        try {
          Class<?> aClass = Class.forName(exception);
          classes.add(aClass.asSubclass(Throwable.class));
        } catch (ClassNotFoundException e) {
          LOGGER.debug("Class not found: {}", exception, e);
        }
      }

      return classes;
    }
  }
}
