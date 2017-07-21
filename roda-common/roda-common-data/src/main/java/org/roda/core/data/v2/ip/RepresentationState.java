/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

public enum RepresentationState {
  ORIGINAL, INGESTED, MASTER, ACCESS, NONE;

  public static RepresentationState getDefault() {
    return ORIGINAL;
  }
}
