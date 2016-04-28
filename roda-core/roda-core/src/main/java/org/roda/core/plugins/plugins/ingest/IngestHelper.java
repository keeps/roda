/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import org.apache.commons.lang3.StringUtils;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MetadataType;
import org.roda_project.commons_ip.model.MetadataType.MetadataTypeEnum;
import org.roda_project.commons_ip.model.SIP;

public final class IngestHelper {

  private IngestHelper() {

  }

  public static String getType(SIP sip) {
    return sip.getContentType().asString();
  }

  public static String getType(IPRepresentation sr) {
    return sr.getContentType().asString();
  }

  public static String getMetadataType(IPDescriptiveMetadata dm) {
    MetadataType metadataType = dm.getMetadataType();
    String type = "";
    if (metadataType != null) {
      if (metadataType.getType() == MetadataTypeEnum.OTHER && StringUtils.isNotBlank(metadataType.getOtherType())) {
        type = metadataType.getOtherType();
      } else {
        type = metadataType.getType().getType();
      }
    }
    return type;
  }
}
