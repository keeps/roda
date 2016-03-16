/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MigrationException;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.utils.METSEnums.MetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPluginUtils.class);

  public static AIP earkSIPToAIP(SIP sip, Path sipPath, ModelService model, StorageService storage, String parentId)
    throws IOException, MigrationException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    boolean active = false;
    Permissions permissions = new Permissions();
    boolean notify = false;

    AIP aip = model.createAIP(active, parentId, permissions, notify);

    // process representations
    if (sip.getRepresentations() != null && !sip.getRepresentations().isEmpty()) {

      for (IPRepresentation sr : sip.getRepresentations()) {
        boolean original = true;
        model.createRepresentation(aip.getId(), sr.getObjectID(), original, notify);

        if (sr.getData() != null && !sr.getData().isEmpty()) {
          for (IPFile file : sr.getData()) {
            List<String> directoryPath = file.getRelativeFolders();
            String fileId = file.getFileName();
            ContentPayload payload = new FSPathContentPayload(file.getPath());
            model.createFile(aip.getId(), sr.getObjectID(), directoryPath, fileId, payload, notify);
          }
        }
      }
    }

    // process descriptive metadata
    if (sip.getDescriptiveMetadata() != null && !sip.getDescriptiveMetadata().isEmpty()) {
      for (IPDescriptiveMetadata dm : sip.getDescriptiveMetadata()) {
        String descriptiveMetadataId = dm.getMetadata().getFileName().toString();
        ContentPayload payload = new FSPathContentPayload(dm.getMetadata().getPath());
        String metadataType = getMetadataType(dm);
        String metadataVersion = dm.getMetadataVersion();
        model.createDescriptiveMetadata(aip.getId(), descriptiveMetadataId, payload, metadataType, metadataVersion,
          notify);
      }
    }

    // TODO add preservation metadata

    model.notifyAIPCreated(aip.getId());

    return model.retrieveAIP(aip.getId());

  }

  private static String getMetadataType(IPDescriptiveMetadata dm) {
    MetadataType metadataType = dm.getMetadataType();
    String type = "";
    if (metadataType != null) {
      if (metadataType == MetadataType.OTHER && StringUtils.isNotBlank(metadataType.getOtherType())) {
        type = metadataType.getOtherType();
      } else {
        type = metadataType.getType();
      }
    }
    return type;
  }
}
