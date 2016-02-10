package org.roda.core.plugins.plugins.ingest.characterization;

import java.io.IOException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lc.xmlns.premisV2.Representation;

public class PremisSkeletonPluginUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPlugin.class);

  public static void createPremisForRepresentation(ModelService model, StorageService storage, AIP aip,
    String representationId, boolean notify) throws IOException, RequestNotValidException, GenericException,
      NotFoundException, AuthorizationDeniedException, XmlException, ValidationException, AlreadyExistsException {

    Representation representation = PremisUtils.createBaseRepresentation(aip.getId(), representationId);
    boolean notifyInSteps = false;

    ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), representationId);
    for (File file : allFiles) {
      if (!file.isDirectory()) {
        LOGGER.debug("Processing " + file);
        ContentPayload filePreservation = PremisUtils.createBaseFile(file, model);
        model.createPreservationMetadata(PreservationMetadataType.OBJECT_FILE, aip.getId(), representationId,
          file.getPath(), file.getId(), filePreservation, notifyInSteps);
        PremisUtils.linkFileToRepresentation(file, RodaConstants.PREMIS_RELATIONSHIP_TYPE_STRUCTURAL,
          RodaConstants.PREMIS_RELATIONSHIP_SUBTYPE_HASPART, representation);
      }
    }
    IOUtils.closeQuietly(allFiles);

    ContentPayload representationPayload = PremisUtils.representationToBinary(representation);
    model.createPreservationMetadata(PreservationMetadataType.OBJECT_REPRESENTATION, representationId, aip.getId(),
      representationId, representationPayload, notifyInSteps);

    if (notify) {
      model.notifyAIPUpdated(aip.getId());
    }
  }

}
