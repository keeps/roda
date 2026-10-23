/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

/**
 * Tests for the findings of the Solr usage review
 * (https://github.com/keeps/roda/issues/3782), one class per sub-issue. Each
 * test shows the problem its issue describes, so it fails until the issue is
 * fixed.
 * <p>
 * They are in their own group, which is not part of {@code all}, {@code dev} or
 * {@code travis}, so they only run when asked for (Docker must be available):
 *
 * <pre>
 * mvn -pl roda-core/roda-core-tests test -Dtestng.groups=solr-review
 * </pre>
 */
public final class SolrReview {
  public static final String GROUP = "solr-review";

  /**
   * The HTTP/2 deadlock check ({@link Http2InterShardStallTest}): long, and best
   * run on its own, {@code -Dtestng.groups=solr-review-stall}.
   */
  public static final String STALL_GROUP = "solr-review-stall";

  private SolrReview() {
    // constants only
  }
}
