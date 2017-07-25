/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.List;

public class RepresentationState {
  public static final String ORIGINAL = "ORIGINAL";
  public static final String INGESTED = "INGESTED";
  public static final String PRESERVATION = "PRESERVATION";
  public static final String ACCESS = "ACCESS";
  public static final String OTHER = "OTHER";

  private static final List<String> VALUES = Arrays.asList(ORIGINAL, INGESTED, PRESERVATION, ACCESS);

  private RepresentationState() {
    // do nothing
  }

  public static List<String> values() {
    return VALUES;
  }

}
