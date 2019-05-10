/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.characterization;

import java.io.IOException;
import java.util.Collection;

import org.apache.xmlbeans.XmlException;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PremisSkeletonPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(PremisSkeletonPluginUtils.class);

  private PremisSkeletonPluginUtils() {
    // do nothing
  }

  public static void createPremisSkeletonOnRepresentation(ModelService model, String aipId, String representationId,
    Collection<String> fixityAlgorithms) throws IOException, RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, XmlException, ValidationException {
    gov.loc.premis.v3.Representation representation;

    if (model.preservationRepresentationExists(aipId, representationId)) {
      Binary preservationObject = model.retrievePreservationRepresentation(aipId, representationId);
      representation = PremisV3Utils.binaryToRepresentation(preservationObject.getContent(), false);
    } else {
      representation = PremisV3Utils.createBaseRepresentation(aipId, representationId);
    }

    try (CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aipId, representationId, true)) {
      for (OptionalWithCause<File> oFile : allFiles) {
        if (oFile.isPresent()) {
          File file = oFile.get();
          if (!file.isDirectory()) {
            if (!model.preservationFileExists(aipId, representationId, file.getPath(), file.getId())) {
              PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, fixityAlgorithms, representation);
            }
          }
        } else {
          LOGGER.error("Cannot process File", oFile.getCause());
        }
      }
    }

    ContentPayload representationPayload = PremisV3Utils.representationToBinary(representation);
    try {
      model.createPreservationMetadata(PreservationMetadataType.REPRESENTATION, aipId, representationId,
        representationPayload, false);
    } catch (AlreadyExistsException e1) {
      String pmId = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null,
        null);
      model.updatePreservationMetadata(pmId, PreservationMetadataType.REPRESENTATION, aipId, representationId, null,
        null, representationPayload, false);
    }
  }

  public static void createPremisSkeletonOnFile(ModelService model, File file, Collection<String> fixityAlgorithms)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException,
    ValidationException, XmlException, IOException {
    gov.loc.premis.v3.Representation representation;

    try {
      // FIXME 20190509 hsilva: couldn't this be changed by an exists
      // invocation? more cheap?
      Binary preservationObject = model.retrievePreservationRepresentation(file.getAipId(), file.getRepresentationId());
      representation = PremisV3Utils.binaryToRepresentation(preservationObject.getContent(), false);
    } catch (NotFoundException e) {
      representation = PremisV3Utils.createBaseRepresentation(file.getAipId(), file.getRepresentationId());
    }

    PremisSkeletonPluginUtils.createPremisSkeletonOnFile(model, file, fixityAlgorithms, representation);
  }

  public static void createPremisSkeletonOnFile(ModelService model, File file, Collection<String> fixityAlgorithms,
    gov.loc.premis.v3.Representation representation) throws RequestNotValidException, GenericException,
    AuthorizationDeniedException, NotFoundException, ValidationException, XmlException, IOException {
    boolean notifyInSteps = false;

    if (!file.isDirectory()) {
      LOGGER.debug("Processing {}", file);
      if (!model.preservationFileExists(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId())) {
        ContentPayload filePreservation = PremisV3Utils.createBaseFile(file, model, fixityAlgorithms);
        String pmId;
        try {
          PreservationMetadata pm = model.createPreservationMetadata(PreservationMetadataType.FILE, file.getAipId(),
            file.getRepresentationId(), file.getPath(), file.getId(), filePreservation, notifyInSteps);
          pmId = pm.getId();
          model.notifyFileCreated(file).failOnError();
        } catch (AlreadyExistsException e1) {
          pmId = IdUtils.getPreservationFileId(file.getId());
          model.updatePreservationMetadata(pmId, PreservationMetadataType.FILE, file.getAipId(),
            file.getRepresentationId(), file.getPath(), file.getId(), filePreservation, notifyInSteps);
          model.notifyFileUpdated(file).failOnError();
        }

        PremisV3Utils.linkFileToRepresentation(pmId, RodaConstants.PREMIS_RELATIONSHIP_TYPE_STRUCTURAL,
          RodaConstants.PREMIS_RELATIONSHIP_SUBTYPE_HASPART, representation);
      }
    }
  }

}
