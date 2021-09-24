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
import org.roda.core.data.v2.synchronization.local.LocalInstanceIdentifierState;

import java.util.Arrays;
import java.util.List;

public final class URNUtils {

  /** Private empty constructor */
  private URNUtils() {
    // do nothing
  }

  private static final int URN_INSTANCE_IDENTIFIER_POSITION = 2;

  private static final int URN_LENGTH_WITH_INSTANCE_IDENTIFIER = 6;
  private static final int URN_LENGTH_WITHOUT_INSTANCE_IDENTIFIER = 5;

  public static String createRodaURN(RODA_TYPE type, String id, String instanceId) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);

    if (instanceId != null) {
      sb.append(instanceId);
      sb.append(RodaConstants.URN_SEPARATOR);
    }
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(id);
    return sb.toString().toLowerCase();
  }

  public static String createRodaPreservationURN(PreservationMetadataType preservationType, String id,
    String instanceId) {
    StringBuilder sb = new StringBuilder();
    sb.append(getPremisPrefix(preservationType, instanceId));
    sb.append(id);
    return sb.toString();
  }

  public static String getPremisPrefix(PreservationMetadataType type, String instanceId) {
    StringBuilder sb = new StringBuilder();
    sb.append(RodaConstants.URN_BASE);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(RodaConstants.URN_RODA);
    sb.append(RodaConstants.URN_SEPARATOR);

    if (instanceId != null) {
      sb.append(instanceId);
      sb.append(RodaConstants.URN_SEPARATOR);
    }

    sb.append(RodaConstants.URN_PREMIS);
    sb.append(RodaConstants.URN_SEPARATOR);
    sb.append(type);
    sb.append(RodaConstants.URN_SEPARATOR);
    return sb.toString().toLowerCase();
  }

  public static PreservationMetadataType getPreservationMetadataTypeFromId(String id) {
    String[] fields = id.split(RodaConstants.URN_SEPARATOR);
    if (fields.length == URN_LENGTH_WITH_INSTANCE_IDENTIFIER) {
      return PreservationMetadataType.valueOf(fields[4].toUpperCase());
    } else if (fields.length == URN_LENGTH_WITHOUT_INSTANCE_IDENTIFIER) {
      return PreservationMetadataType.valueOf(fields[3].toUpperCase());
    } else {
      List<PreservationMetadataType> values = Arrays.asList(PreservationMetadataType.values());
      for (PreservationMetadataType type : values) {
        if (type.toString().equals(fields[3].toUpperCase())) {
          return PreservationMetadataType.valueOf(fields[3].toUpperCase());
        } else if (type.toString().equals(fields[4].toUpperCase())) {
          return PreservationMetadataType.valueOf(fields[4].toUpperCase());
        }
      }
    }
    return null;
  }

  public static String getIdHashCodeFromEventId(String id, String instanceId) {
    String[] fields = id.split(RodaConstants.URN_SEPARATOR);
    if (fields.length == URN_LENGTH_WITH_INSTANCE_IDENTIFIER) {
      return fields[5];
    } else if (fields.length == URN_LENGTH_WITHOUT_INSTANCE_IDENTIFIER) {
      return fields[4];
    } else if (fields[2].equals(instanceId)) {
      String result = "";
      for (int i = 5; i < fields.length; i++) {
        result = result + fields[i];
      }
      return result;
    } else {
      String result = "";
      for (int i = 4; i < fields.length; i++) {
        result = result + fields[i];
      }
      return result;
    }
  }

  public static String getAgentUsernameFromURN(String urnId) {
    String[] fields = urnId.split(RodaConstants.URN_SEPARATOR);
    return fields[fields.length - 1];
  }

  public static boolean verifyInstanceIdentifier(String id, String instanceId) {
    if (extractInstanceIdentifierFromId(id).equals(instanceId)) {
      return true;
    } else {
      return false;
    }
  }

  public static String extractInstanceIdentifierFromId(String id) {
    String[] fields = id.split(RodaConstants.URN_SEPARATOR);
    return fields[URN_INSTANCE_IDENTIFIER_POSITION];
  }

  public static boolean hasInstanceId(String id) {
    String[] fields = id.split(RodaConstants.URN_SEPARATOR);
    if (fields.length == URN_LENGTH_WITH_INSTANCE_IDENTIFIER) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean verifyPremisPrefix(PreservationMetadataType type, String filename) {
    if (hasInstanceId(filename)) {
      return filename.startsWith(getPremisPrefix(type, extractInstanceIdentifierFromId(filename)));
    } else {
      return filename.startsWith(getPremisPrefix(type, null));
    }
  }

}
