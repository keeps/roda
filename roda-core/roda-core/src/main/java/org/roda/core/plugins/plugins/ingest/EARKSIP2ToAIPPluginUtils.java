/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.ingest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.roda_project.commons_ip2.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip2.model.IPFile;
import org.roda_project.commons_ip2.model.IPMetadata;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.RepresentationStatus;
import org.roda_project.commons_ip2.model.SIP;

public class EARKSIP2ToAIPPluginUtils {

  private EARKSIP2ToAIPPluginUtils() {
    // do nothing
  }

  public static AIP earkSIPToAIP(SIP sip, String username, Permissions fullPermissions, ModelService model,
    List<String> ingestSIPIds, String ingestJobId, Optional<String> parentId, String ingestSIPUUID, Plugin<?> plugin)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, ValidationException, IOException, LockingException {

    AIPState state = AIPState.INGEST_PROCESSING;
    Permissions permissions = new Permissions();
    boolean notify = false;

    String aipType = getType(sip);

    AIP aip = model.createAIP(state, parentId.orElse(null), aipType, permissions, ingestSIPUUID, ingestSIPIds,
      ingestJobId, notify, username);

    PluginHelper.acquireObjectLock(aip, plugin);

    // process IP information
    processIPInformation(model, sip, aip.getId(), notify, false);

    // process IPRepresentation information
    for (IPRepresentation representation : sip.getRepresentations()) {
      processIPRepresentationInformation(model, representation, aip.getId(), notify, false, username, null);
    }

    // INFO 20190509 hsilva: this is required as the previous instructions
    // update the AIP metadata
    AIP createdAIP = model.retrieveAIP(aip.getId());

    // Set Permissions
    Permissions readPermissions = PermissionUtils.grantReadPermissionToUserGroup(model, createdAIP,
      aip.getPermissions());
    Permissions finalPermissions = PermissionUtils.grantAllPermissions(username, readPermissions, fullPermissions);
    createdAIP.setPermissions(finalPermissions);

    return model.updateAIP(createdAIP, username);
  }

  public static AIP earkSIPToAIPUpdate(SIP sip, IndexedAIP indexedAIP, ModelService model, StorageService storage,
    String username, String ingestJobId, Report reportItem, Plugin<?> plugin)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, ValidationException, LockingException {
    return earkSIPToAIPUpdate(sip, indexedAIP, model, username, Optional.empty(), ingestJobId, reportItem, plugin);
  }

  public static AIP earkSIPToAIPUpdate(SIP sip, IndexedAIP indexedAIP, ModelService model, String username,
    Optional<String> searchScope, String ingestJobId, Report reportItem, Plugin<?> plugin)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, ValidationException, LockingException {
    boolean notify = false;
    AIP aip;

    PluginHelper.acquireObjectLock(indexedAIP, plugin);

    // type UPDATE
    if (reportItem != null) {
      reportItem.getSipInformation().setUpdate(true);
    }

    // process IP information
    processIPInformation(model, sip, indexedAIP.getId(), notify, true);

    // process IPRepresentation information
    for (IPRepresentation representation : sip.getRepresentations()) {
      processIPRepresentationInformation(model, representation, indexedAIP.getId(), notify, true, username, reportItem);
    }

    aip = model.retrieveAIP(indexedAIP.getId());
    aip.setGhost(false);
    if (searchScope.isPresent()) {
      aip.setParentId(searchScope.get());
    }
    aip.addIngestUpdateJobId(ingestJobId);

    for (String id : sip.getIds()) {
      if (!aip.getIngestSIPIds().contains(id)) {
        aip.getIngestSIPIds().add(id);
      }
    }

    return model.updateAIP(aip, username);
  }

  private static void processIPInformation(ModelService model, SIP sip, String aipId, boolean notify, boolean update)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException, ValidationException {
    // process descriptive metadata
    processDescriptiveMetadata(model, aipId, null, sip.getDescriptiveMetadata(), notify, update);

    // process other metadata
    processOtherMetadata(model, sip.getOtherMetadata(), aipId, Optional.empty(), notify);

    // process preservation metadata
    processPreservationMetadata(model, sip.getPreservationMetadata(), aipId, Optional.empty(), notify);

    // process documentation
    processDocumentation(model, sip.getDocumentation(), aipId, null, update);

    // process schemas
    processSchemas(model, sip.getSchemas(), aipId, null, update);
  }

  private static void processDescriptiveMetadata(ModelService model, String aipId, String representationId,
    List<IPDescriptiveMetadata> descriptiveMetadata, boolean notify, boolean update) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException, ValidationException {
    for (IPDescriptiveMetadata dm : descriptiveMetadata) {
      String descriptiveMetadataId = dm.getMetadata().getFileName();
      ContentPayload payload = new FSPathContentPayload(dm.getMetadata().getPath());
      String metadataType = dm.getMetadataType().asString();
      String metadataVersion = dm.getMetadataVersion();
      try {
        model.createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload, metadataType,
          metadataVersion, notify);
      } catch (AlreadyExistsException e) {
        if (update) {
          Map<String, String> properties = new HashMap<>();
          properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATE_FROM_SIP.toString());

          model.updateDescriptiveMetadata(aipId, descriptiveMetadataId, payload, metadataType, metadataVersion,
            properties);
        } else {
          throw e;
        }
      }
    }
  }

  private static void processOtherMetadata(ModelService model, List<IPMetadata> otherMetadata, String aipId,
    Optional<String> representationId, boolean notify) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {

    for (IPMetadata pm : otherMetadata) {
      IPFile file = pm.getMetadata();
      ContentPayload fileContentPayload = new FSPathContentPayload(file.getPath());

      model.createOrUpdateOtherMetadata(aipId, representationId.orElse(null), file.getRelativeFolders(),
        file.getFileName(), "", pm.getMetadataType().asString(), fileContentPayload, notify);
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
    String representationId, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFile doc : documentation) {
      List<String> directoryPath = doc.getRelativeFolders();
      String fileId = doc.getFileName();
      ContentPayload payload = new FSPathContentPayload(doc.getPath());
      try {
        model.createDocumentation(aipId, representationId, directoryPath, fileId, payload);
      } catch (AlreadyExistsException e) {
        // Tolerate duplicate documentation when updating
        if (!update)
          throw e;
      }
    }
  }

  private static void processSchemas(ModelService model, List<IPFile> schemas, String aipId, String representationId,
    boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFile schema : schemas) {
      List<String> directoryPath = schema.getRelativeFolders();
      String fileId = schema.getFileName();
      ContentPayload payload = new FSPathContentPayload(schema.getPath());

      try {
        model.createSchema(aipId, representationId, directoryPath, fileId, payload);
      } catch (AlreadyExistsException e) {
        // Tolerate duplicate schemas when updating
        if (!update)
          throw e;
      }
    }
  }

  private static void processIPRepresentationInformation(ModelService model, IPRepresentation sr, String aipId,
    boolean notify, boolean update, String username, Report reportItem) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException, ValidationException {
    String representationType = getType(sr);
    boolean isOriginal = RepresentationStatus.getORIGINAL().equals(sr.getStatus());

    Representation representation = null;
    if (update) {
      try {
        representation = model.retrieveRepresentation(aipId, sr.getObjectID());
      } catch (NotFoundException e) {
        // do nothing
      }
    }
    // Either we're not updating or the retrieve failed
    if (representation == null) {
      representation = model.createRepresentation(aipId, sr.getObjectID(), isOriginal, representationType, notify,
        username);
      if (reportItem != null && update) {
        reportItem.getSipInformation().addRepresentationData(aipId, IdUtils.getRepresentationId(representation));
      }
    }

    // process representation descriptive metadata
    processDescriptiveMetadata(model, aipId, representation.getId(), sr.getDescriptiveMetadata(), notify, update);

    // process other metadata
    processOtherMetadata(model, sr.getOtherMetadata(), aipId, Optional.ofNullable(representation.getId()), notify);

    // process representation preservation metadata
    processPreservationMetadata(model, sr.getPreservationMetadata(), aipId, Optional.ofNullable(representation.getId()),
      notify);

    // process representation files
    for (IPFile file : sr.getData()) {
      List<String> directoryPath = file.getRelativeFolders();
      String fileId = file.getFileName();
      ContentPayload payload = new FSPathContentPayload(file.getPath());
      try {
        File createdFile = model.createFile(aipId, representation.getId(), directoryPath, fileId, payload, notify);
        if (reportItem != null && update) {
          reportItem.getSipInformation().addFileData(aipId, IdUtils.getRepresentationId(representation),
            createdFile);
        }
      } catch (AlreadyExistsException e) {
        if (update) {
          File updatedFile = model.updateFile(aipId, representation.getId(), directoryPath, fileId, payload, true,
            notify);
          if (reportItem != null) {
            reportItem.getSipInformation().addFileData(aipId, IdUtils.getRepresentationId(representation),
              updatedFile);
          }
        } else
          throw e;
      }
    }

    // process representation documentation
    processDocumentation(model, sr.getDocumentation(), aipId, representation.getId(), false);

    // process representation schemas
    processSchemas(model, sr.getSchemas(), aipId, representation.getId(), false);
  }

  private static String getType(SIP sip) {
    return sip.getContentType().asString();
  }

  private static String getType(IPRepresentation sr) {
    return sr.getContentInformationType().asString();
  }
}
