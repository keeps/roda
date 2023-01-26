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
  private Histogram retries90Histo;
  private Histogram retries80Histo;
  private Histogram retries50Histo;
  private Histogram retriesHisto;
  private Counter retries90Count;
  private Counter retries80Count;
  private Counter retries50Count;
  private Counter retriesCount;
  private Counter successfulRetriesCounter;
  private Histogram successfulRetriesHistogram;
  private static final Logger LOGGER = LoggerFactory.getLogger(RetryPolicyBuilder.class);

  private RetryPolicyBuilder() {
    int delay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_DELAY, 1);
    int maxDelay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_DELAY,
      RodaConstants.NodeType.TEST.equals(RodaCoreFactory.getNodeType()) ? 2 : 180);
    double delayFactor = RodaCoreFactory.getRodaConfiguration().getDouble(RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0);
    int maxRetries = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_RETRIES, 10);

    initMetrics();

    policy = RetryPolicy.builder().handle(getHandleExceptionsFromConfiguration())
      .withBackoff(delay, maxDelay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(maxRetries).onRetry(event -> {
        LOGGER.warn("Attempt #{}: {} [{}]", event.getAttemptCount(), event.getLastException().getMessage(),
          event.getLastException().getClass().getSimpleName());
        computeMetrics(event.getAttemptCount(), maxRetries);
      }).onRetriesExceeded(event -> LOGGER.warn("Number of max retries exceeded", event.getException()))
      .onFailure(event -> LOGGER.error("Failed due to: {}", event.getException().getMessage(), event.getException()))
      .onSuccess(event -> {
        if (event.getAttemptCount() > 1) {
          LOGGER.debug("Successful retry after {} attempts", event.getAttemptCount());
          computeSuccessfulRetriesMetrics();
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

  private void initMetrics() {
    retriesHisto = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "allRetriesHisto"));
    retriesCount = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "allRetriesCounter"));
    retries90Histo = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries90Histo"));
    retries80Histo = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries80Histo"));
    retries50Histo = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries50Histo"));
    retries90Count = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries90Counter"));
    retries80Count = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries80Counter"));
    retries50Count = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retries50Counter"));
    successfulRetriesCounter = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "successfulRetriesCounter"));
    successfulRetriesHistogram = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "successfulRetriesHisto"));

  }

  private void computeSuccessfulRetriesMetrics() {
    successfulRetriesCounter.inc();
    successfulRetriesHistogram.update(successfulRetriesCounter.getCount());
  }

  private void computeMetrics(int numberOfAttempts, int maxRetries) {
    retriesCount.inc();
    retriesHisto.update(retriesCount.getCount());

    if (numberOfAttempts == Math.round(maxRetries * 0.5)) {
      retries50Count.inc();
      retries50Histo.update(retries50Count.getCount());
    }

    if (numberOfAttempts == Math.round(maxRetries * 0.8)) {
      retries80Count.inc();
      retries80Histo.update(retries80Count.getCount());
    }

    if (numberOfAttempts == Math.round(maxRetries * 0.9)) {
      retries90Count.inc();
      retries90Histo.update(retries90Count.getCount());
    }
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
