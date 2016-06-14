/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.v2.ip.TransferredResource;

public final class LinkingObjectUtils {
  private static final String ID_SEPARATOR = "-";
  private static final String LINKING_ID_SEPARATOR = "/";

  /** Private empty constructor */
  private LinkingObjectUtils() {

  }

  public static String getLinkingIdentifierId(RODA_TYPE type, String id) {
    return URNUtils.createRodaURN(type, id);
  }

  public static String getLinkingIdentifierId(RODA_TYPE type, TransferredResource transferredResource) {
    return URNUtils.createRodaURN(type, transferredResource.getRelativePath());
  }

  public static RODA_TYPE getLinkingIdentifierType(String value) {
    return URNUtils.getRodaType(value);
  }

  public static String getLinkingObjectPath(String value) {
    return URNUtils.getLinkingObjectPath(value);
   
  }

  public static String getFileIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    return path;
  }

  public static String getRepresentationIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    return path;
  }

  public static String getAipIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

  public static String getTransferredResourceIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

}
