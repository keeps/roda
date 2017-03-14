/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.ip.TransferredResource;

public final class LinkingObjectUtils {
  private static final String ID_SEPARATOR = "-";
  private static final String LINKING_ID_SEPARATOR = "/";

  /** Private empty constructor */
  private LinkingObjectUtils() {

  }

  public static String getLinkingIdentifierId(RODA_TYPE type, String uuid) {
    return URNUtils.createRodaURN(type, uuid);
  }

  public static String getLinkingIdentifierId(TransferredResource transferredResource) {
    return getLinkingIdentifierId(RODA_TYPE.TRANSFERRED_RESOURCE, transferredResource.getRelativePath());
  }

  /**
   * @return RODA_TYPE or null
   */
  public static RODA_TYPE getLinkingIdentifierType(String value) {
    if (value.contains(RodaConstants.URN_SEPARATOR) && value.split(RodaConstants.URN_SEPARATOR).length > 2) {
      return RODA_TYPE.valueOf(value.split(RodaConstants.URN_SEPARATOR)[2].toUpperCase());
    } else {
      return null;
    }
  }

  /**
   * @return String or null
   */
  public static String getLinkingObjectPath(String path) {
    if (path.contains(":")) {
      return path.substring(path.lastIndexOf(':') + 1);
    } else {
      return null;
    }
  }

  public static String[] splitLinkingId(String id) {
    return id.split(LINKING_ID_SEPARATOR);
  }

  /**
   * @return String or null
   */
  public static String getFileIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    if (path != null) {
      return path.replaceAll(LINKING_ID_SEPARATOR, ID_SEPARATOR);
    } else {
      return null;
    }
  }

  /**
   * @return String or null
   */
  public static String getRepresentationIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    if (path != null) {
      return path.replaceAll(LINKING_ID_SEPARATOR, ID_SEPARATOR);
    } else {
      return null;
    }
  }

  public static String getAipIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

  public static String getTransferredResourceIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

}
