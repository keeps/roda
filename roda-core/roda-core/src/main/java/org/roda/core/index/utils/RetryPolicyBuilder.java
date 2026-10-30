/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.CircuitBreakerOpenException;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeExecutor;
import dev.failsafe.Fallback;
import dev.failsafe.RetryPolicy;

/**
 * The retry policy of the Solr calls, and the circuit breaker they share.
 *
 * A failed call is retried, with a backoff, according to why it failed. When
 * Solr is unavailable (the handled exceptions, as a refused or timed out
 * connection, a Solr that is not available or limits its requests, or the
 * circuit breaker is open), it is retried until an overall deadline
 * ({@code core.solr.retry.maxDuration}), however many attempts it takes, so an
 * outage shorter than the deadline does not fail the call, while a longer one
 * does not hold its worker, and the locks and transaction of the object it
 * indexes, any longer. When Solr fails with an internal error (5xx), which may
 * be caused by the request itself, it is retried at most
 * {@code core.solr.retry.serverError.maxRetries} times, within the deadline.
 * When Solr rejects the request (4xx), it is not retried, as it fails the same
 * way every time.
 *
 * After {@code core.solr.circuit_breaker.failures} failures in a row, from any
 * worker, the circuit breaker opens: the calls keep waiting for Solr within
 * their deadline, but without sending it requests; after
 * {@code core.solr.circuit_breaker.delay} it lets a call through, and closes
 * when it succeeds (#3785).
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RetryPolicyBuilder {

  private static final int DEFAULT_SERVER_ERROR_MAX_RETRIES = 3;
  private static final int DEFAULT_MAX_DELAY_SECONDS = 15;
  private static final int DEFAULT_MAX_DURATION_SECONDS = 300;
  private static final int DEFAULT_CIRCUIT_BREAKER_FAILURES = 10;
  private static final int DEFAULT_CIRCUIT_BREAKER_DELAY_SECONDS = 15;

  /** Why a Solr call failed, which sets how it is retried. */
  private enum Failure {
    UNAVAILABLE, SERVER_ERROR, REQUEST_ERROR, OTHER
  }

  private static RetryPolicyBuilder instance;
  private final List<Class<? extends Throwable>> handledExceptions;
  private final RetryPolicy<Object> unavailablePolicy;
  private final RetryPolicy<Object> serverErrorPolicy;
  // null when disabled
  private final CircuitBreaker<Object> circuitBreaker;
  private final Histogram retriesHisto;
  private final Counter retriesCount;
  private static final Logger LOGGER = LoggerFactory.getLogger(RetryPolicyBuilder.class);

  private RetryPolicyBuilder() {
    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    int delay = configuration.getInt(RodaConstants.SOLR_RETRY_DELAY, 1);
    int maxDelay = configuration.getInt(RodaConstants.SOLR_RETRY_MAX_DELAY,
      RodaConstants.NodeType.TEST.equals(RodaCoreFactory.getNodeType()) ? 2 : DEFAULT_MAX_DELAY_SECONDS);
    double delayFactor = configuration.getDouble(RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0);
    int serverErrorMaxRetries = getServerErrorMaxRetries(configuration);
    int maxDuration = getMaxDuration(configuration);
    int circuitBreakerFailures = configuration.getInt(RodaConstants.SOLR_CIRCUIT_BREAKER_FAILURES,
      DEFAULT_CIRCUIT_BREAKER_FAILURES);
    int circuitBreakerDelay = configuration.getInt(RodaConstants.SOLR_CIRCUIT_BREAKER_DELAY,
      DEFAULT_CIRCUIT_BREAKER_DELAY_SECONDS);
    handledExceptions = getHandleExceptionsFromConfiguration();

    this.retriesHisto = RodaCoreFactory.getMetrics()
      .histogram(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retriesHisto"));
    this.retriesCount = RodaCoreFactory.getMetrics()
      .counter(MetricRegistry.name(RetryPolicyBuilder.class.getSimpleName(), "retriesCounter"));

    // an open circuit breaker is waited for, as Solr being unavailable
    unavailablePolicy = RetryPolicy.builder()
      .handleIf(failure -> failure instanceof CircuitBreakerOpenException
        || classify(failure, handledExceptions) == Failure.UNAVAILABLE)
      .withBackoff(delay, maxDelay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(-1)
      .withMaxDuration(Duration.ofSeconds(maxDuration)).onRetry(event -> {
        LOGGER.warn("Solr unavailable, retrying ({} s of {} s): {} [{}]", event.getElapsedTime().toSeconds(),
          maxDuration, event.getLastException().getMessage(), event.getLastException().getClass().getSimpleName());
        countRetry();
      }).onRetriesExceeded(
        event -> LOGGER.warn("Solr still unavailable after {} s, giving up", maxDuration, event.getException()))
      .build();

    serverErrorPolicy = RetryPolicy.builder()
      .handleIf(failure -> classify(failure, handledExceptions) == Failure.SERVER_ERROR)
      .withBackoff(delay, maxDelay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(serverErrorMaxRetries)
      .withMaxDuration(Duration.ofSeconds(maxDuration)).onRetry(event -> {
        LOGGER.warn("Solr internal error, retrying (at most {} times): {} [{}]", serverErrorMaxRetries,
          event.getLastException().getMessage(), event.getLastException().getClass().getSimpleName());
        countRetry();
      }).onRetriesExceeded(event -> LOGGER.warn("Solr internal error after {} retries, giving up",
        serverErrorMaxRetries, event.getException()))
      .build();

    if (circuitBreakerFailures > 0) {
      circuitBreaker = CircuitBreaker.builder().handleIf(failure -> {
        Failure kind = classify(failure, handledExceptions);
        return kind == Failure.UNAVAILABLE || kind == Failure.SERVER_ERROR;
      }).withFailureThreshold(circuitBreakerFailures).withSuccessThreshold(1)
        .withDelay(Duration.ofSeconds(circuitBreakerDelay))
        .onOpen(event -> LOGGER.warn("Solr calls are failing: sending no requests for {} s", circuitBreakerDelay))
        .onHalfOpen(event -> LOGGER.info("Trying a Solr call again"))
        .onClose(event -> LOGGER.info("Solr calls are succeeding again")).build();
    } else {
      circuitBreaker = null;
    }
  }

  public static synchronized RetryPolicyBuilder getInstance() {
    if (instance == null) {
      instance = new RetryPolicyBuilder();
    }
    return instance;
  }

  /**
   * Runs a Solr call with the retry policies and the shared circuit breaker,
   * falling back to the given fallback when it fails.
   */
  public FailsafeExecutor<Object> executor(Fallback<Object> fallback) {
    return circuitBreaker != null
      ? Failsafe.with(fallback, unavailablePolicy, serverErrorPolicy, circuitBreaker)
      : Failsafe.with(fallback, unavailablePolicy, serverErrorPolicy);
  }

  /**
   * Whether Solr rejected the request (4xx), which fails the same way every time:
   * in a request of several documents, because of one of them.
   */
  boolean isRejected(Throwable failure) {
    return classify(failure, handledExceptions) == Failure.REQUEST_ERROR;
  }

  private void countRetry() {
    retriesCount.inc();
    retriesHisto.update(retriesCount.getCount());
  }

  /**
   * The retries of the internal errors: {@code core.solr.retry.maxRetries} used
   * to limit the retries of every failure, and still sets them when the new
   * setting is not.
   */
  private static int getServerErrorMaxRetries(Configuration configuration) {
    if (configuration.containsKey(RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES)) {
      return configuration.getInt(RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES);
    } else if (configuration.containsKey(RodaConstants.SOLR_RETRY_MAX_RETRIES)) {
      LOGGER.warn("{} is deprecated: it now limits the retries of Solr internal errors only, as {} does",
        RodaConstants.SOLR_RETRY_MAX_RETRIES, RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES);
      return configuration.getInt(RodaConstants.SOLR_RETRY_MAX_RETRIES);
    }
    return DEFAULT_SERVER_ERROR_MAX_RETRIES;
  }

  /** The deadline, which is the only limit of the retries while Solr is unavailable. */
  private static int getMaxDuration(Configuration configuration) {
    int maxDuration = configuration.getInt(RodaConstants.SOLR_RETRY_MAX_DURATION, DEFAULT_MAX_DURATION_SECONDS);
    if (maxDuration <= 0) {
      LOGGER.warn("{} must be positive, using {} s", RodaConstants.SOLR_RETRY_MAX_DURATION,
        DEFAULT_MAX_DURATION_SECONDS);
      return DEFAULT_MAX_DURATION_SECONDS;
    }
    return maxDuration;
  }

  /**
   * Why the call failed, from the causes: the cloud client reports a failed
   * request as a Solr error with the status of the first failure, which may be
   * a client error that could not reach Solr (as no live servers).
   */
  private static Failure classify(Throwable failure, List<Class<? extends Throwable>> handledExceptions) {
    boolean unavailable = false;
    boolean serverError = false;
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      if (cause instanceof SolrException solrException) {
        int code = solrException.code();
        if (code == SolrException.ErrorCode.TOO_MANY_REQUESTS.code
          || code == SolrException.ErrorCode.SERVICE_UNAVAILABLE.code) {
          unavailable = true;
        } else if (code >= 400 && code < 500) {
          return Failure.REQUEST_ERROR;
        } else {
          serverError = true;
        }
      }
      Throwable current = cause;
      if (handledExceptions.stream().anyMatch(handled -> handled.isInstance(current))) {
        unavailable = true;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    if (unavailable) {
      return Failure.UNAVAILABLE;
    }
    return serverError ? Failure.SERVER_ERROR : Failure.OTHER;
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
