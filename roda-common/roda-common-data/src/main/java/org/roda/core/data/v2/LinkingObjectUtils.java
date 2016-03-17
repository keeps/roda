/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import org.roda.core.data.v2.ip.TransferredResource;

public final class LinkingObjectUtils {
  private static final String ID_SEPARATOR = "-";
  private static final String LINKING_ID_SEPARATOR = "/";

  public enum LinkingObjectType {
    TRANSFERRED_RESOURCE, AIP, REPRESENTATION, FILE
  }

  /** Private empty constructor */
  private LinkingObjectUtils() {

  }

  public static String getLinkingIdentifierId(LinkingObjectType type, String uuid) {
    return type + ":" + uuid;
  }

  public static String getLinkingIdentifierId(LinkingObjectType type, TransferredResource transferredResource) {
    return type + ":" + transferredResource.getRelativePath();
  }

  public static LinkingObjectType getLinkingIdentifierType(String value) {
    if (value.contains(":")) {
      return LinkingObjectType.valueOf(value.split(":")[0]);
    } else {
      return null;
    }
  }

  public static String getLinkingObjectPath(String path) {
    if (path.contains(":")) {
      return path.substring(path.indexOf(":") + 1);
    } else {
      return null;
    }
  }

  public static String[] splitLinkingId(String id) {
    return id.split(LINKING_ID_SEPARATOR);
  }

  public static String getFileIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    return path.replaceAll(LINKING_ID_SEPARATOR, ID_SEPARATOR);
  }

  public static String getRepresentationIdFromLinkingId(String linkingId) {
    String path = getLinkingObjectPath(linkingId);
    return path.replaceAll(LINKING_ID_SEPARATOR, ID_SEPARATOR);
  }

  public static String getAipIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

  public static String getTransferredResourceIdFromLinkingId(String linkingId) {
    return getLinkingObjectPath(linkingId);
  }

}
