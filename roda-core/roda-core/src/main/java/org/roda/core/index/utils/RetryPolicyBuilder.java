/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import dev.failsafe.RetryPolicy;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class RetryPolicyBuilder {

  private static RetryPolicyBuilder instance;
  private dev.failsafe.RetryPolicyBuilder<Object> builder;

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryPolicyBuilder.class);

  private RetryPolicyBuilder() {
    int delay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_DELAY, 1);
    int maxDelay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_DELAY, 180);
    double delayFactor = RodaCoreFactory.getRodaConfiguration().getDouble(RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0);
    int maxRetries = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_RETRIES, 10);

    builder = RetryPolicy.builder().handle(Arrays.asList(SolrServerException.class, IOException.class))
            .withBackoff(delay, maxDelay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(maxRetries).onRetry(event -> {
              LOGGER.debug("Attempt #{}", event.getAttemptCount());
            }).onRetriesExceeded(event -> {
              LOGGER.debug("Number of max retries exceeded");
            });
  }

  public static RetryPolicyBuilder getInstance() {
    if (instance == null) {
      instance = new RetryPolicyBuilder();
    }

    return instance;
  }

  public RetryPolicy<Object> getRetryPolicy() {
    return builder.build();
  }
}
