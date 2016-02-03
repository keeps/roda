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
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda_project.commons_ip.model.MigrationException;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPDescriptiveMetadata;
import org.roda_project.commons_ip.model.SIPRepresentation;
import org.roda_project.commons_ip.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPluginUtils.class);

  public static AIP earkSIPToAIP(SIP sip, Path sipPath, ModelService model, StorageService storage, String parentId)
    throws IOException, MigrationException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    // TODO check if parent exists

    boolean active = false;
    AIPPermissions permissions = new AIPPermissions();
    boolean notify = true;

    AIP aip = model.createAIP(active, parentId, permissions, notify);

    if (sip.getRepresentations() != null && sip.getRepresentations().size() > 0) {

      for (SIPRepresentation sr : sip.getRepresentations()) {
        boolean original = true;
        model.createRepresentation(aip.getId(), sr.getObjectID(), original, false);

        // TODO check if this is needed
        PluginHelper.createDirectories(model, aip.getId(), sr.getObjectID());

        if (sr.getData() != null && sr.getData().size() > 0) {
          for (Pair<Path, List<String>> entry : sr.getData()) {
            Path filePath = entry.getFirst();
            List<String> directoryPath = entry.getSecond();
            String fileId = filePath.getFileName().toString();
            ContentPayload payload = new FSPathContentPayload(filePath);
            model.createFile(aip.getId(), sr.getObjectID(), directoryPath, fileId, payload);
          }
        }
      }
    }

    if (sip.getDescriptiveMetadata() != null && sip.getDescriptiveMetadata().size() > 0) {
      for (SIPDescriptiveMetadata dm : sip.getDescriptiveMetadata()) {
        String descriptiveMetadataId = dm.getMetadata().getFileName().toString();
        ContentPayload payload = new FSPathContentPayload(dm.getMetadata());
        String type = (dm.getMetadataType() != null) ? dm.getMetadataType().toString() : "";

        model.createDescriptiveMetadata(aip.getId(), descriptiveMetadataId, payload, type, false);
      }
    }

    // TODO add preservation metadata

    return model.retrieveAIP(aip.getId());

  }
}
