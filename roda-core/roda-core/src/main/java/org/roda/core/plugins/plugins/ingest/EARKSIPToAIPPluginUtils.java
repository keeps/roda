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
import java.util.Optional;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPMetadata;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MigrationException;
import org.roda_project.commons_ip.model.SIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIPToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPToAIPPluginUtils.class);

  public static AIP earkSIPToAIP(SIP sip, Path sipPath, ModelService model, StorageService storage, String ingestSIPId,
    String ingestJobId, String parentId) throws IOException, MigrationException, RequestNotValidException,
          NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException, ValidationException {

    AIPState state = AIPState.INGEST_PROCESSING;
    Permissions permissions = new Permissions();
    boolean notify = false;

    String aipType = IngestHelper.getType(sip);

    AIP aip = model.createAIP(state, parentId, aipType, permissions, ingestSIPId, ingestJobId, notify);

    // process IP information
    processIPInformation(model, sip, aip.getId(), notify, false);

    // process IPRepresentation information
    for (IPRepresentation representation : sip.getRepresentations()) {
      processIPRepresentationInformation(model, representation, aip.getId(), notify, false);
    }

    // FIXME 20160516 hsilva: put SIP inside the AIP

    model.notifyAIPCreated(aip.getId());

    return model.retrieveAIP(aip.getId());

  }

  public static AIP earkSIPToAIPUpdate(SIP sip, String aipId, Path sipPath, ModelService model, String ingestSIPId,
                                       String ingestJobId, String parentId) throws IOException, MigrationException, RequestNotValidException,
          NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException, ValidationException {
    boolean notify = false;

    // process IP information
    processIPInformation(model, sip, aipId, notify, true);

    // process IPRepresentation information
    for (IPRepresentation representation : sip.getRepresentations()) {
      processIPRepresentationInformation(model, representation, aipId, notify, true);
    }

    // FIXME 20160516 hsilva: put SIP inside the AIP

    model.notifyAIPCreated(aipId);

    return model.retrieveAIP(aipId);

  }

  private static void processIPInformation(ModelService model, SIP sip, String aipId, boolean notify, boolean update)
          throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
          NotFoundException, ValidationException {
    // process descriptive metadata
    processDescriptiveMetadata(model, aipId, sip.getDescriptiveMetadata(), notify, update);

    // process preservation metadata
    processPreservationMetadata(model, sip.getPreservationMetadata(), aipId, Optional.empty(), notify);

    // process documentation
    processDocumentation(model, sip.getDocumentation(), aipId, null, notify, update);

    // process schemas
    processSchemas(model, sip.getSchemas(), aipId, null, notify, update);

  }

  private static void processDescriptiveMetadata(ModelService model, String aipId,
    List<IPDescriptiveMetadata> descriptiveMetadata, boolean notify, boolean update) throws RequestNotValidException, GenericException,
          AlreadyExistsException, AuthorizationDeniedException, NotFoundException, ValidationException {
    for (IPDescriptiveMetadata dm : descriptiveMetadata) {
      String descriptiveMetadataId = dm.getMetadata().getFileName().toString();
      ContentPayload payload = new FSPathContentPayload(dm.getMetadata().getPath());
      String metadataType = IngestHelper.getMetadataType(dm);
      String metadataVersion = dm.getMetadataVersion();
      try {
        model.createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, metadataType, metadataVersion, notify);
      }  catch (AlreadyExistsException e) {
        if(update)
          model.updateDescriptiveMetadata(aipId, descriptiveMetadataId, payload, metadataType, metadataVersion, "Update from SIP");
        else throw e;
      }
    }
  }

  private static void processPreservationMetadata(ModelService model, List<IPMetadata> preservationMetadata,
    String aipId, Optional<String> representationId, boolean notify) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    for (IPMetadata pm : preservationMetadata) {
      IPFile file = pm.getMetadata();
      ContentPayload fileContentPayload = new FSPathContentPayload(file.getPath());

      if (representationId.isPresent()) {
        model.createPreservationMetadata(PreservationMetadataType.OTHER, aipId, representationId.get(),
          file.getRelativeFolders(), file.getFileName(), fileContentPayload, notify);
      } else {
        model.createPreservationMetadata(PreservationMetadataType.OTHER, aipId, file.getRelativeFolders(),
          file.getFileName(), fileContentPayload, notify);
      }
    }
  }

  private static void processDocumentation(ModelService model, List<IPFile> documentation, String aipId,
    String representationId, boolean notify, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFile doc : documentation) {
      List<String> directoryPath = doc.getRelativeFolders();
      String fileId = doc.getFileName();
      ContentPayload payload = new FSPathContentPayload(doc.getPath());
      try {
        model.createDocumentation(aipId, representationId, directoryPath, fileId, payload);
      } catch (AlreadyExistsException e) {
        // Tolerate duplicate documentation when updating
        if(!update)
          throw e;
      }
    }
  }

  private static void processSchemas(ModelService model, List<IPFile> schemas, String aipId, String representationId,
    boolean notify, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFile schema : schemas) {
      List<String> directoryPath = schema.getRelativeFolders();
      String fileId = schema.getFileName();
      ContentPayload payload = new FSPathContentPayload(schema.getPath());

      try {
        model.createSchema(aipId, representationId, directoryPath, fileId, payload);
      } catch (AlreadyExistsException e) {
        // Tolerate duplicate schemas when updating
        if(!update)
          throw e;
      }
    }
  }

  private static void processIPRepresentationInformation(ModelService model, IPRepresentation sr, String aipId,
    boolean notify, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    boolean original = true;
    String representationType = IngestHelper.getType(sr);

    Representation representation = null;
    if(update) {
      try {
        representation = model.retrieveRepresentation(aipId, sr.getObjectID());
      } catch (NotFoundException e) {}
    }
    // Either we're not updating or the retrieve failed
    if(representation == null){
      representation = model.createRepresentation(aipId, sr.getObjectID(), original, representationType, notify);
    }
      // process representation descriptive metadata
      // XXX 20160504 hsilva: there's no "space" in the model to accommodate this
      // information

    try {
      // process representation preservation metadata
      processPreservationMetadata(model, sr.getPreservationMetadata(), aipId, Optional.ofNullable(representation.getId()),
              notify);

      // process representation files
      for (IPFile file : sr.getData()) {
        List<String> directoryPath = file.getRelativeFolders();
        String fileId = file.getFileName();
        ContentPayload payload = new FSPathContentPayload(file.getPath());
        model.createFile(aipId, representation.getId(), directoryPath, fileId, payload, notify);
      }

      // process representation documentation
      processDocumentation(model, sr.getDocumentation(), aipId, representation.getId(), notify, false);

      // process representation schemas
      processSchemas(model, sr.getSchemas(), aipId, representation.getId(), notify, false);
    } catch (AlreadyExistsException e) {
      // apereira: Do nothing for now
    }
  }
}
