package org.roda.core.index.utils;

import dev.failsafe.Fallback;
import dev.failsafe.RetryPolicy;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class RetryPolicyUtils {

  private static RetryPolicyUtils instance;
  private RetryPolicy<Object> retryPolicy = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryPolicyUtils.class);

  private RetryPolicyUtils() {
    int delay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_DELAY, 1);
    int maxDeplay = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_DELAY, 30);
    double delayFactor = RodaCoreFactory.getRodaConfiguration().getDouble(RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0);
    int maxRetries = RodaCoreFactory.getRodaConfiguration().getInt(RodaConstants.SOLR_RETRY_MAX_RETRIES, 5);

    retryPolicy = RetryPolicy.builder().handle(Arrays.asList(SolrServerException.class, IOException.class))
      .withBackoff(delay, maxDeplay, ChronoUnit.SECONDS, delayFactor).withMaxRetries(maxRetries).onRetry(event -> {
        LOGGER.debug("Attempt #{}", event.getAttemptCount());
      }).onRetriesExceeded(event -> {
        LOGGER.debug("Number of max retries exceeded");
      }).build();
  }

  public static RetryPolicyUtils getInstance() {
    if (instance == null) {
      instance = new RetryPolicyUtils();
    }

    return instance;
  }

  public RetryPolicy<Object> getRetryPolicy() {
    return retryPolicy;
  }
}
