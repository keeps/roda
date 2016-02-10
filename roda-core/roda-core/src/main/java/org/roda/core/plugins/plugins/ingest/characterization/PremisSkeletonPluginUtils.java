package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;

public class PremisSkeletonPluginUtils {

  public static void createPremisForRepresentation(ModelService model, StorageService storage, Path temp, AIP aip,
    String representationId) throws IOException, RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException, XmlException, ValidationException, AlreadyExistsException {

    ContentPayload representationPremis = PremisUtils.createBaseRepresentation(aip.getId(), representationId);
    model.createPreservationMetadata(PreservationMetadataType.OBJECT_REPRESENTATION, representationId, aip.getId(),
      representationId, representationPremis);
    ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representationId);
    for (File file : allFiles) {
      if (!file.isDirectory()) {
        ContentPayload filePreservation = PremisUtils.createBaseFile(file, model);
        model.createPreservationMetadata(PreservationMetadataType.OBJECT_FILE, aip.getId(), representationId,
          file.getPath(), file.getId(), filePreservation);
        ContentPayload updatedRepresentation = PremisUtils.linkFileToRepresentation(file, aip.getId(), representationId,
          RodaConstants.PREMIS_RELATIONSHIP_TYPE_STRUCTURAL, RodaConstants.PREMIS_RELATIONSHIP_SUBTYPE_HASPART, model);
        String id = representationId;
        PreservationMetadataType type = PreservationMetadataType.OBJECT_REPRESENTATION;
        model.updatePreservationMetadata(id, type, aip.getId(), representationId, null, null, updatedRepresentation);
        // TODO save updated representation
      }
    }
    IOUtils.closeQuietly(allFiles);
  }

}
