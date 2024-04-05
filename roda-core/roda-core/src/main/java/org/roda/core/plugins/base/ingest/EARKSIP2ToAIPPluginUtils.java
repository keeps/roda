/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.ingest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
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
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.ShallowFiles;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.ExternalFileManifestContentPayload;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.util.IdUtils;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip2.model.IPFileInterface;
import org.roda_project.commons_ip2.model.IPFileShallow;
import org.roda_project.commons_ip2.model.IPMetadata;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.RepresentationStatus;
import org.roda_project.commons_ip2.model.SIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EARKSIP2ToAIPPluginUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIP2ToAIPPluginUtils.class);

  private EARKSIP2ToAIPPluginUtils() {
    // do nothing
  }

  public static AIP earkSIPToAIP(SIP sip, String username, ModelService model, List<String> ingestSIPIds,
    String ingestJobId, Optional<String> parentId, String ingestSIPUUID, Plugin<?> plugin)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, ValidationException, LockingException {

    AIPState state = AIPState.INGEST_PROCESSING;
    Permissions permissions = new Permissions();
    boolean notify = false;

    String aipType = getType(sip);

    AIP aip = model.createAIP(state, parentId.orElse(null), aipType, permissions, ingestSIPUUID, ingestSIPIds,
      ingestJobId, notify, username);

    PluginHelper.acquireObjectLock(aip, plugin);

    // process IP information
    processIPInformation(model, sip, aip.getId(), notify, false, username);

    // process IPRepresentation information
    for (IPRepresentation representation : sip.getRepresentations()) {
      processIPRepresentationInformation(model, representation, aip.getId(), notify, false, username, null);
    }

    // INFO 20190509 hsilva: this is required as the previous instructions
    // update the AIP metadata
    AIP createdAIP = model.retrieveAIP(aip.getId());

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
    processIPInformation(model, sip, indexedAIP.getId(), notify, true, username);

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

  private static void processIPInformation(ModelService model, SIP sip, String aipId, boolean notify, boolean update,
    String username) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException, ValidationException {
    // process descriptive metadata
    processDescriptiveMetadata(model, aipId, null, sip.getDescriptiveMetadata(), notify, update, username);

    // process other metadata
    processOtherMetadata(model, sip.getOtherMetadata(), aipId, Optional.empty(), notify, username);

    // process preservation metadata
    processPreservationMetadata(model, sip.getPreservationMetadata(), aipId, Optional.empty(), username, notify);

    // process documentation
    processDocumentation(model, sip.getDocumentation(), aipId, null, update);

    // process schemas
    processSchemas(model, sip.getSchemas(), aipId, null, update);
  }

  private static void processDescriptiveMetadata(ModelService model, String aipId, String representationId,
    List<IPDescriptiveMetadata> descriptiveMetadata, boolean notify, boolean update, String username)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException, ValidationException {
    for (IPDescriptiveMetadata dm : descriptiveMetadata) {
      String descriptiveMetadataId = dm.getMetadata().getFileName();
      ContentPayload payload = new FSPathContentPayload(dm.getMetadata().getPath());
      String metadataType = dm.getMetadataType().asString();
      String metadataVersion = dm.getMetadataVersion();
      try {
        model.createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload, metadataType,
          metadataVersion, username, notify);
      } catch (AlreadyExistsException e) {
        if (update) {
          Map<String, String> properties = new HashMap<>();
          properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATE_FROM_SIP.toString());

          model.updateDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload, metadataType, metadataVersion,
            properties, username);
        } else {
          throw e;
        }
      }
    }
  }

  private static void processOtherMetadata(ModelService model, List<IPMetadata> otherMetadata, String aipId,
    Optional<String> representationId, boolean notify, String username) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {

    for (IPMetadata pm : otherMetadata) {
      IPFileInterface file = pm.getMetadata();
      ContentPayload fileContentPayload = new FSPathContentPayload(file.getPath());

      model.createOrUpdateOtherMetadata(aipId, representationId.orElse(null), file.getRelativeFolders(),
        file.getFileName(), "", pm.getMetadataType().asString(), fileContentPayload, username, notify);
    }
  }

  private static void processPreservationMetadata(ModelService model, List<IPMetadata> preservationMetadata,
    String aipId, Optional<String> representationId, String username, boolean notify) throws GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    for (IPMetadata pm : preservationMetadata) {
      IPFileInterface file = pm.getMetadata();
      ContentPayload fileContentPayload = new FSPathContentPayload(file.getPath());

      if (representationId.isPresent()) {
        model.createPreservationMetadata(PreservationMetadataType.OTHER, aipId, representationId.get(),
          file.getRelativeFolders(), file.getFileName(), fileContentPayload, username, notify);
      } else {
        model.createPreservationMetadata(PreservationMetadataType.OTHER, aipId, file.getRelativeFolders(),
          file.getFileName(), fileContentPayload, username, notify);
      }
    }
  }

  private static void processDocumentation(ModelService model, List<IPFileInterface> documentation, String aipId,
    String representationId, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFileInterface doc : documentation) {
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

  private static void processSchemas(ModelService model, List<IPFileInterface> schemas, String aipId,
    String representationId, boolean update) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    for (IPFileInterface schema : schemas) {
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
    processDescriptiveMetadata(model, aipId, representation.getId(), sr.getDescriptiveMetadata(), notify, update,
      username);

    // process other metadata
    processOtherMetadata(model, sr.getOtherMetadata(), aipId, Optional.ofNullable(representation.getId()), notify,
      username);

    // process representation preservation metadata
    processPreservationMetadata(model, sr.getPreservationMetadata(), aipId, Optional.ofNullable(representation.getId()),
      username, notify);

    // process representation files
    boolean hasShallowFile = false;
    for (IPFileInterface file : sr.getData()) {
      List<String> directoryPath = null;
      String fileId = null;
      ContentPayload payload = null;
      if (file instanceof IPFileShallow) {
        if (((IPFileShallow) file).getFileLocation() != null) {
          // this is an actual shallow file
          fileId = RodaConstants.RODA_MANIFEST_EXTERNAL_FILES;
          directoryPath = file.getRelativeFolders();
          payload = processIPFileShallow(aipId, representation.getId(), (IPFileShallow) file);
          hasShallowFile = true;
        } else {
          // this is an empty folder
          final StoragePath emptyDirectoryStoragePath = ModelUtils.getDirectoryStoragePath(aipId,
            representation.getId(), file.getRelativeFolders());
          model.getStorage().createDirectory(emptyDirectoryStoragePath);
          // TODO jgomes 2022-03-09: Create model service method to create empty directory
        }

      } else {
        fileId = file.getFileName();
        directoryPath = file.getRelativeFolders();
        payload = new FSPathContentPayload(file.getPath());
      }

      if (payload != null) {

        try {
          final File createdFile = model.createFile(aipId, representation.getId(), directoryPath, fileId, payload,
            username, notify);
          if (reportItem != null && update) {
            reportItem.getSipInformation().addFileData(aipId, IdUtils.getRepresentationId(representation), createdFile);
          }
        } catch (final AlreadyExistsException e) {
          if (update) {
            final File updatedFile = model.updateFile(aipId, representation.getId(), directoryPath, fileId, payload,
              true, username, notify);
            if (reportItem != null) {
              reportItem.getSipInformation().addFileData(aipId, IdUtils.getRepresentationId(representation),
                updatedFile);
            }
          } else {
            throw e;
          }
        }
      }
    }

    if (hasShallowFile) {
      model.changeRepresentationShallowFileFlag(aipId, representation.getId(), true, username, false);
    }

    // process representation documentation
    processDocumentation(model, sr.getDocumentation(), aipId, representation.getId(), false);

    // process representation schemas
    processSchemas(model, sr.getSchemas(), aipId, representation.getId(), false);
  }

  private static ContentPayload processIPFileShallow(String aipId, String representationId, IPFileShallow file)
    throws GenericException {
    try {
      final String decode = URLDecoder.decode(file.getFileLocation().toString(), "UTF-8");
      FileType fileType = file.getFileType();
      ShallowFile shallowFile = new ShallowFile();
      shallowFile.setName(FilenameUtils.getName(decode));
      shallowFile.setUUID(IdUtils.getFileId(aipId, representationId, null, shallowFile.getName()));
      shallowFile.setLocation(file.getFileLocation());
      shallowFile.setSize(fileType.getSIZE());
      shallowFile.setCreated(fileType.getCREATED());
      shallowFile.setMimeType(fileType.getMIMETYPE());
      shallowFile.setChecksum(fileType.getCHECKSUM());
      shallowFile.setChecksumType(fileType.getCHECKSUMTYPE());

      ShallowFiles shallowFiles = new ShallowFiles();
      shallowFiles.addObject(shallowFile);
      return new ExternalFileManifestContentPayload(shallowFiles);

    } catch (final UnsupportedEncodingException e) {
      throw new GenericException("Unable to identify the resource name", e);
    }
  }

  private static String getType(SIP sip) {
    return sip.getContentType().asString();
  }

  private static String getType(IPRepresentation sr) {
    if (sr.getContentType().isOtherAndOtherTypeIsDefined()) {
      return sr.getContentType().getOtherType();
    } else {
      return sr.getContentType().getType().asString();
    }
  }
}
