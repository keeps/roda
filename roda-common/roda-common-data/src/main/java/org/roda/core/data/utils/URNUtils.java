/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;

public final class URNUtils {

  /** Private empty constructor */
  private URNUtils() {

  }

  public static String createRodaURN(RODA_TYPE type, String id) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(id);
    return sb.toString().toLowerCase();
  }

  public static String createRodaPreservationURN(PreservationMetadataType preservationType, String id) {
    StringBuilder sb = new StringBuilder();
    sb.append(getPremisPrefix(preservationType));
    sb.append(id);
    return sb.toString();
  }

  public static String createOtherURN(String type, String id) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_OTHER);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(id);
    return sb.toString().toLowerCase();
  }

  public static String getPremisPrefix(PreservationMetadataType type) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_PREMIS);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    return sb.toString().toLowerCase();
  }

  public static RODA_TYPE getRodaType(String value) {
    if (value.contains(RodaConstants.URN_SEPARATOR) && value.split(RodaConstants.URN_SEPARATOR).length > 3) {
      return RODA_TYPE.valueOf(value.split(RodaConstants.URN_SEPARATOR)[2].toUpperCase());
    } else {
      return null;
    }
  }

  public static String getRodaPrefix(RODA_TYPE type) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    return sb.toString().toLowerCase();
  }

  public static String getLinkingObjectPath(String value) {
    for (RODA_TYPE type : RODA_TYPE.values()) {
      String prefix = getRodaPrefix(type);
      if (value.toLowerCase().startsWith(prefix)) {
        value = value.replace(prefix, "");
        break;
      }
    }
    return value;
  }

}
