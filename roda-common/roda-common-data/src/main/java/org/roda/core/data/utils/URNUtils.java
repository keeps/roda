/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.utils;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.RODA_TYPE;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;

public final class URNUtils {

  /** Private empty constructor */
  private URNUtils() {
    // do nothing
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

  public static String createRodaPreservationURN(File file) {
    return createRodaPreservationURN(PreservationMetadataType.FILE, file.getPath(), file.getId());
  }

  public static String createRodaPreservationURN(PreservationMetadataType preservationType, String id) {
    return createRodaPreservationURN(preservationType, Collections.emptyList(), id);
  }

  public static String createRodaPreservationURN(PreservationMetadataType preservationType, List<String> path,
    String id) {
    StringBuilder sb = new StringBuilder();
    sb.append(getPremisPrefix(preservationType));
    // TODO: Add path if not empty
    if (!path.isEmpty()) {
      for (String dir : path) {
        sb.append(dir).append("-");
      }
    }
    sb.append(id);
    return sb.toString();
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

  public static PreservationMetadataType getPreservationMetadataTypeFromId(String id) {
    String[] fields = id.split(RodaConstants.URN_SEPARATOR);
    return PreservationMetadataType.valueOf(fields[3].toUpperCase());
  }
}
