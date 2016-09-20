/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.GroupAlreadyExistsException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.ResourceParseUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.EmptyClosableIterable;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that "relates" Model & Storage
 * 
 * FIXME questions:
 * 
 * 1) how to undo things created/changed upon exceptions??? if using fedora
 * perhaps with transactions
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ModelService extends ModelObservable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

  private final StorageService storage;
  private Path logFile;
  private static final boolean FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA = false;

  public ModelService(StorageService storage) {
    super();
    this.storage = storage;
    ensureAllContainersExist();
    ensureAllDiretoriesExist();
  }

  private void ensureAllContainersExist() {
    try {
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_AIP);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_PRESERVATION);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_ACTIONLOG);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_JOB);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_JOB_REPORT);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_RISK);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error while ensuring that all containers exist", e);
    }

  }

  private void createContainerIfNotExists(String containerName)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    try {
      storage.createContainer(DefaultStoragePath.parse(containerName));
    } catch (AlreadyExistsException e) {
      // do nothing
    }
  }

  private void ensureAllDiretoriesExist() {
    try {
      createDirectoryIfNotExists(
        DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION, RodaConstants.STORAGE_DIRECTORY_AGENTS));
    } catch (RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error initializing directories", e);
    }
  }

  private void createDirectoryIfNotExists(StoragePath directoryPath)
    throws GenericException, AuthorizationDeniedException {
    try {
      storage.createDirectory(directoryPath);
    } catch (AlreadyExistsException e) {
      // do nothing
    }

  }

  public StorageService getStorage() {
    return storage;
  }

  /***************** AIP related *****************/
  /***********************************************/

  private void createAIPMetadata(AIP aip) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    createAIPMetadata(aip, ModelUtils.getAIPStoragePath(aip.getId()));
  }

  private void createAIPMetadata(AIP aip, StoragePath storagePath) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    String json = JsonUtils.getJsonFromObject(aip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    boolean asReference = false;
    storage.createBinary(metadataStoragePath, new StringContentPayload(json), asReference);
  }

  private void updateAIPMetadata(AIP aip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    updateAIPMetadata(aip, ModelUtils.getAIPStoragePath(aip.getId()));
  }

  private void updateAIPMetadata(AIP aip, StoragePath storagePath)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    String json = JsonUtils.getJsonFromObject(aip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(json), asReference, createIfNotExists);
  }

  public CloseableIterable<OptionalWithCause<AIP>> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final boolean recursive = false;

    final CloseableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getAIPcontainerPath(), recursive);

    return ResourceParseUtils.convert(getStorage(), resourcesIterable, AIP.class);
  }

  public AIP retrieveAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
  }

  /**
   * Create a new AIP
   * 
   * @param aipId
   *          Suggested ID for the AIP, if <code>null</code> then an ID will be
   *          automatically generated. If ID cannot be allowed because it
   *          already exists or is not valid, another ID will be provided.
   * @param sourceStorage
   * @param sourceContainer
   * @param sourcePath
   * @param sourceName
   * @return
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   * @throws AlreadyExistsException
   * @throws ValidationException
   */
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    // XXX possible optimization would be to allow move between storage
    // TODO support asReference
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {

      storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPStoragePath(aipId));
      Directory newDirectory = storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));

      aip = ResourceParseUtils.getAIPMetadata(getStorage(), newDirectory.getStoragePath());
      aip.setCreatedBy(createdBy);
      aip.setCreatedOn(new Date());
      aip.setUpdatedBy(createdBy);
      aip.setUpdatedOn(new Date());

      if (notify) {
        notifyAipCreated(aip);
      }
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  public AIP createAIP(String parentId, String type, Permissions permissions, boolean notify, String sipId,
    boolean isGhost, String createdBy) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    AIPState state = AIPState.ACTIVE;
    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    String id = directory.getStoragePath().getName();

    AIP aip = new AIP(id, parentId, type, state, permissions, createdBy);

    aip.setGhost(isGhost);
    aip.setIngestSIPId(sipId);

    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip);
    }

    return aip;
  }

  public AIP createAIP(String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    AIPState state = AIPState.ACTIVE;
    boolean notify = true;
    return createAIP(state, parentId, type, permissions, notify, createdBy);
  }

  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    boolean notify = true;
    return createAIP(state, parentId, type, permissions, notify, createdBy);
  }

  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify,
    String createdBy) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {

    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    String id = directory.getStoragePath().getName();

    AIP aip = new AIP(id, parentId, type, state, permissions, createdBy);
    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip);
    }

    return aip;
  }

  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPId,
    String ingestJobId, boolean notify, String createdBy) throws RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException {

    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    String id = directory.getStoragePath().getName();

    AIP aip = new AIP(id, parentId, type, state, permissions, createdBy).setIngestSIPId(ingestSIPId)
      .setIngestJobId(ingestJobId);
    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip);
    }

    return aip;
  }

  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    return createAIP(aipId, sourceStorage, sourcePath, true, createdBy);
  }

  public AIP notifyAIPCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipCreated(aip);
    return aip;
  }

  public AIP notifyAIPUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipUpdated(aip);
    return aip;
  }

  // TODO support asReference
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {
      StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);

      // XXX possible optimization only creating new files, updating
      // changed and
      // removing deleted ones.
      storage.deleteResource(aipPath);

      storage.copy(sourceStorage, sourcePath, aipPath);
      Directory directoryUpdated = storage.getDirectory(aipPath);

      aip = ResourceParseUtils.getAIPMetadata(getStorage(), directoryUpdated.getStoragePath());
      aip.setUpdatedBy(updatedBy);
      aip.setUpdatedOn(new Date());
      notifyAipUpdated(aip);
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    aip.setUpdatedBy(updatedBy);
    aip.setUpdatedOn(new Date());
    updateAIPMetadata(aip);
    notifyAipUpdated(aip);

    return aip;
  }

  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    aip.setUpdatedBy(updatedBy);
    aip.setUpdatedOn(new Date());
    updateAIPMetadata(aip);
    notifyAipStateUpdated(aip);

    return aip;
  }

  public AIP moveAIP(String aipId, String parentId)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (aipId.equals(parentId)) {
      throw new RequestNotValidException("Cannot set itself as its parent: " + aipId);
    }

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    String oldParentId = aip.getParentId();
    aip.setParentId(parentId);
    updateAIPMetadata(aip);

    notifyAipMoved(aip, oldParentId, parentId);

    return aip;
  }

  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    storage.deleteResource(aipPath);
    notifyAipDeleted(aipId);
  }

  private ValidationReport isAIPvalid(ModelService model, Directory directory,
    boolean failIfNoDescriptiveMetadataSchema)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ValidationReport report = new ValidationReport();

    // validate metadata (against schemas)
    ValidationReport descriptiveMetadataValidationReport = ValidationUtils.isAIPDescriptiveMetadataValid(model,
      directory.getStoragePath().getName(), failIfNoDescriptiveMetadataSchema);

    report.setValid(descriptiveMetadataValidationReport.isValid());
    report.setIssues(descriptiveMetadataValidationReport.getIssues());

    // FIXME validate others aspects

    return report;
  }

  /***************** Descriptive Metadata related *****************/
  /****************************************************************/

  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    return retrieveDescriptiveMetadataBinary(aipId, null, descriptiveMetadataId);
  }

  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    return storage.getBinary(binaryPath);
  }

  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrieveDescriptiveMetadata(aipId, null, descriptiveMetadataId);
  }

  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);

    DescriptiveMetadata ret = null;
    for (DescriptiveMetadata descriptiveMetadata : getDescriptiveMetadata(aip, representationId)) {
      if (descriptiveMetadata.getId().equals(descriptiveMetadataId)) {
        ret = descriptiveMetadata;
        break;
      }
    }

    if (ret == null) {
      throw new NotFoundException("Could not find descriptive metadata: " + descriptiveMetadataId);
    }

    return ret;
  }

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    return createDescriptiveMetadata(aipId, null, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, notify);
  }

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    return createDescriptiveMetadata(aipId, null, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, true);
  }

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, true);
  }

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    boolean asReference = false;

    storage.createBinary(binaryPath, payload, asReference);
    DescriptiveMetadata descriptiveMetadata = new DescriptiveMetadata(descriptiveMetadataId, aipId, representationId,
      descriptiveMetadataType, descriptiveMetadataVersion);

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.addDescriptiveMetadata(descriptiveMetadata);
    updateAIPMetadata(aip);

    if (notify) {
      notifyDescriptiveMetadataCreated(descriptiveMetadata);
    }

    return descriptiveMetadata;
  }

  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    String message) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    ValidationException {
    return updateDescriptiveMetadata(aipId, null, descriptiveMetadataId, descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, message);
  }

  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String message) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, ValidationException {
    DescriptiveMetadata ret = null;

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    // Create version snapshot
    storage.createBinaryVersion(binaryPath, message);

    // Update
    storage.updateBinaryContent(binaryPath, descriptiveMetadataPayload, asReference, createIfNotExists);

    // set descriptive metadata type
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    ret = updateDescriptiveMetadata(aip, representationId, descriptiveMetadataId, descriptiveMetadataType,
      descriptiveMetadataVersion);

    updateAIPMetadata(aip);
    notifyDescriptiveMetadataUpdated(ret);

    return ret;
  }

  private DescriptiveMetadata updateDescriptiveMetadata(AIP aip, String representationId, String descriptiveMetadataId,
    String descriptiveMetadataType, String descriptiveMetadataVersion) {
    DescriptiveMetadata descriptiveMetadata;

    Optional<DescriptiveMetadata> odm = getDescriptiveMetadata(aip, representationId).stream()
      .filter(dm -> dm.getId().equals(descriptiveMetadataId)).findFirst();
    if (odm.isPresent()) {
      descriptiveMetadata = odm.get();
      descriptiveMetadata.setType(descriptiveMetadataType);
      descriptiveMetadata.setVersion(descriptiveMetadataVersion);
    } else {
      descriptiveMetadata = new DescriptiveMetadata(descriptiveMetadataId, aip.getId(), representationId,
        descriptiveMetadataType, descriptiveMetadataVersion);
      aip.addDescriptiveMetadata(descriptiveMetadata);
    }

    return descriptiveMetadata;
  }

  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    deleteDescriptiveMetadata(aipId, null, descriptiveMetadataId);
  }

  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);

    storage.deleteResource(binaryPath);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    deleteDescriptiveMetadata(aip, representationId, descriptiveMetadataId);

    updateAIPMetadata(aip);
    notifyDescriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataId);

  }

  private void deleteDescriptiveMetadata(AIP aip, String representationId, String descriptiveMetadataId) {

    for (Iterator<DescriptiveMetadata> it = getDescriptiveMetadata(aip, representationId).iterator(); it.hasNext();) {
      DescriptiveMetadata descriptiveMetadata = it.next();
      if (descriptiveMetadata.getId().equals(descriptiveMetadataId)) {
        it.remove();
        break;
      }
    }
  }

  private List<DescriptiveMetadata> getDescriptiveMetadata(AIP aip, String representationId) {
    List<DescriptiveMetadata> descriptiveMetadataList = Collections.emptyList();
    if (representationId == null) {
      // AIP descriptive metadata
      descriptiveMetadataList = aip.getDescriptiveMetadata();
    } else {
      // Representation descriptive metadata
      Optional<Representation> oRep = aip.getRepresentations().stream()
        .filter(rep -> rep.getId().equals(representationId)).findFirst();
      if (oRep.isPresent()) {
        descriptiveMetadataList = oRep.get().getDescriptiveMetadata();
      }
    }
    return descriptiveMetadataList;
  }

  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return listDescriptiveMetadataVersions(aipId, null, descriptiveMetadataId);
  }

  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    return storage.listBinaryVersions(binaryPath);
  }

  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    String message) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return revertDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId, message);
  }

  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, String message)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);

    BinaryVersion currentVersion = storage.createBinaryVersion(binaryPath, message);
    storage.revertBinaryVersion(binaryPath, versionId);

    notifyDescriptiveMetadataUpdated(retrieveDescriptiveMetadata(aipId, descriptiveMetadataId));

    return currentVersion;
  }

  /***************** Representation related *****************/
  /**********************************************************/

  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);

    Representation ret = null;
    for (Representation representation : aip.getRepresentations()) {
      if (representation.getId().equals(representationId)) {
        ret = representation;
        break;
      }
    }

    if (ret == null) {
      throw new NotFoundException("Could not find representation: " + representationId);
    }

    return ret;
  }

  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    Representation representation = new Representation(representationId, aipId, original, type);

    StoragePath directoryPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);
    storage.createDirectory(directoryPath);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.getRepresentations().add(representation);
    updateAIPMetadata(aip);

    if (notify) {
      notifyRepresentationCreated(representation);
    }

    return representation;
  }

  // TODO support asReference
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    Representation representation;

    StoragePath directoryPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isRepresentationValid(sourceDirectory);
    if (validationReport.isValid()) {
      storage.copy(sourceStorage, sourcePath, directoryPath);

      representation = new Representation(representationId, aipId, original, type);

      // update AIP metadata
      AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
      aip.getRepresentations().add(representation);
      updateAIPMetadata(aip);

      notifyRepresentationCreated(representation);
    } else {
      throw new ValidationException(validationReport);
    }

    return representation;
  }

  public Representation updateRepresentationInfo(Representation representation) {
    notifyRepresentationUpdated(representation);
    return representation;
  }

  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, ValidationException {
    Representation representation;

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isRepresentationValid(sourceDirectory);

    if (validationReport.isValid()) {
      // XXX possible optimization only creating new files, updating
      // changed and
      // removing deleted

      StoragePath representationPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);
      storage.deleteResource(representationPath);
      try {
        storage.copy(sourceStorage, sourcePath, representationPath);
      } catch (AlreadyExistsException e) {
        throw new GenericException("Copying after delete gave an unexpected already exists exception", e);
      }

      // build return object
      representation = new Representation(representationId, aipId, original, type);

      super.notifyRepresentationUpdated(representation);
    } else {
      throw new ValidationException(validationReport);
    }

    return representation;
  }

  public void notifyRepresentationUpdated(Representation representation) {
    super.notifyRepresentationUpdated(representation);
  }

  public void deleteRepresentation(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath representationPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);

    storage.deleteResource(representationPath);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    for (Iterator<Representation> it = aip.getRepresentations().iterator(); it.hasNext();) {
      Representation representation = it.next();
      if (representation.getId().equals(representationId)) {
        it.remove();
        break;
      }
    }
    updateAIPMetadata(aip);

    notifyRepresentationDeleted(aipId, representationId);
  }

  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final StoragePath storagePath = ModelUtils.getRepresentationDataStoragePath(aipId, representationId);
    CloseableIterable<OptionalWithCause<File>> ret;
    try {
      final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), iterable, File.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<OptionalWithCause<File>>();
    }

    return ret;

  }

  private ValidationReport isRepresentationValid(Directory directory) {
    return new ValidationReport();
  }

  /***************** File related *****************/
  /************************************************/

  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return listFilesUnder(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId(), recursive);
  }

  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(filePath, recursive);
    return ResourceParseUtils.convert(getStorage(), iterable, File.class);
  }

  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    File file;
    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    Class<? extends Entity> entity = storage.getEntity(filePath);

    if (entity.equals(Binary.class) || entity.equals(DefaultBinary.class)) {
      Binary binary = storage.getBinary(filePath);
      file = ResourceParseUtils.convertResourceToFile(binary);
    } else {
      Directory directory = storage.getDirectory(filePath);
      file = ResourceParseUtils.convertResourceToFile(directory);
    }

    return file;
  }

  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return createFile(aipId, representationId, directoryPath, fileId, contentPayload, true);

  }

  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    File file;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);

    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    file = ResourceParseUtils.convertResourceToFile(createdBinary);

    if (notify) {
      notifyFileCreated(file);
    }

    return file;
  }

  public File updateFileInfo(File file) {
    notifyFileUpdated(file);
    return file;
  }

  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    Binary binary, boolean createIfNotExists, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    File file = null;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);

    storage.updateBinaryContent(filePath, binary.getContent(), asReference, createIfNotExists);
    Binary binaryUpdated = storage.getBinary(filePath);
    file = ResourceParseUtils.convertResourceToFile(binaryUpdated);
    if (notify) {
      notifyFileUpdated(file);
    }

    return file;
  }

  public void updateFile(File file) {
    // TODO

    notifyFileUpdated(file);
  }

  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    boolean notify) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    storage.deleteResource(filePath);
    if (notify) {
      notifyFileDeleted(aipId, representationId, directoryPath, fileId);
    }

  }

  /***************** Preservation related *****************/
  /********************************************************/

  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String urn = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null,
      null);
    StoragePath path = ModelUtils.getPreservationMetadataStoragePath(urn, PreservationMetadataType.REPRESENTATION,
      aipId, representationId);
    return storage.getBinary(path);
  }

  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    return retrievePreservationFile(aipId, representationId, fileDirectoryPath, fileId, PreservationMetadataType.FILE);
  }

  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, PreservationMetadataType type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    String id = IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, fileId);
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(id, type, aipId, representationId,
      fileDirectoryPath, fileId);
    return storage.getBinary(filePath);
  }

  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrievePreservationFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public Binary retrievePreservationEvent(String aipId, String representationId, String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationID,
      PreservationMetadataType.EVENT, aipId, representationId);
    return storage.getBinary(storagePath);
  }

  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationID,
      PreservationMetadataType.AGENT);
    return storage.getBinary(storagePath);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    String identifier = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
    String urn = URNUtils.createRodaPreservationURN(type, identifier);
    return createPreservationMetadata(type, urn, aipId, representationId, fileDirectoryPath, fileId, payload, notify);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    List<String> fileDirectoryPath, String fileId, ContentPayload payload, boolean notify) throws GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    String id = IdUtils.getPreservationId(type, aipId, null, fileDirectoryPath, fileId);
    return createPreservationMetadata(type, id, aipId, null, fileDirectoryPath, fileId, payload, notify);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    String representationId, ContentPayload payload, boolean notify) throws GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    String id = IdUtils.getPreservationId(type, aipId, representationId, null, null);
    return createPreservationMetadata(type, id, aipId, representationId, null, null, payload, notify);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    return createPreservationMetadata(type, id, null, null, null, null, payload, notify);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setType(type);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    boolean asReference = false;
    storage.createBinary(binaryPath, payload, asReference);

    if (notify) {
      notifyPreservationMetadataCreated(pm);
    }
    return pm;
  }

  public void notifyPreservationMetadataCreated(PreservationMetadata preservationMetadata) {
    super.notifyPreservationMetadataCreated(preservationMetadata);
  }

  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setType(type);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);

    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.updateBinaryContent(binaryPath, payload, false, true);

    if (notify) {
      notifyPreservationMetadataUpdated(pm);
    }
    return pm;
  }

  public void deletePreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);

    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.deleteResource(binaryPath);

    if (notify) {
      notifyPreservationMetadataDeleted(pm);
    }
  }

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getAIPPreservationMetadataStoragePath(aipId);

    CloseableIterable<OptionalWithCause<PreservationMetadata>> aipPreservationMetadata;
    try {
      boolean recursive = true;
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      aipPreservationMetadata = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));
      // if no exception was sent by above method, return empty list
      aipPreservationMetadata = new EmptyClosableIterable<OptionalWithCause<PreservationMetadata>>();
    }

    if (includeRepresentations) {
      List<CloseableIterable<OptionalWithCause<PreservationMetadata>>> list = new ArrayList<>();
      list.add(aipPreservationMetadata);
      // list from all representations
      AIP aip = retrieveAIP(aipId);
      for (Representation representation : aip.getRepresentations()) {
        CloseableIterable<OptionalWithCause<PreservationMetadata>> rpm = listPreservationMetadata(aipId,
          representation.getId());
        list.add(rpm);
      }
      return CloseableIterables.concat(list);
    } else {
      return aipPreservationMetadata;
    }

  }

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getRepresentationPreservationMetadataStoragePath(aipId, representationId);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> ret;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
    } catch (NotFoundException e) {
      // check if Representation exists
      storage.getDirectory(ModelUtils.getRepresentationStoragePath(aipId, representationId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<OptionalWithCause<PreservationMetadata>>();
    }

    return ret;
  }

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationAgents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<PreservationMetadata>> ret;
    StoragePath storagePath = ModelUtils.getPreservationAgentStoragePath();

    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, true);
      ret = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
    } catch (NotFoundException e) {
      ret = new EmptyClosableIterable<OptionalWithCause<PreservationMetadata>>();
    }

    return ret;
  }

  /***************** Other metadata related *****************/
  /**********************************************************/

  public Binary retrieveOtherMetadataBinary(OtherMetadata om)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrieveOtherMetadataBinary(om.getAipId(), om.getRepresentationId(), om.getFileDirectoryPath(),
      om.getFileId(), om.getFileSuffix(), om.getType());
  }

  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    binary = storage.getBinary(binaryPath);
    return binary;
  }

  public OtherMetadata createOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type, ContentPayload payload, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    OtherMetadata om = null;

    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    boolean asReference = false;
    boolean createIfNotExists = true;
    try {
      storage.createBinary(binaryPath, payload, asReference);
    } catch (AlreadyExistsException e) {
      storage.updateBinaryContent(binaryPath, payload, asReference, createIfNotExists);
    }

    String id = IdUtils.getOtherMetadataId(type, aipId, representationId, fileDirectoryPath, fileId);

    om = new OtherMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, fileSuffix);

    if (notify) {
      notifyOtherMetadataCreated(om);
    }

    return om;
  }

  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String type,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getAIPOtherMetadataStoragePath(aipId, type);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<OtherMetadata>> aipOtherMetadata;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      aipOtherMetadata = ResourceParseUtils.convert(getStorage(), resources, OtherMetadata.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));
      // if no exception was sent by above method, return empty list
      aipOtherMetadata = new EmptyClosableIterable<OptionalWithCause<OtherMetadata>>();
    }

    if (includeRepresentations) {
      List<CloseableIterable<OptionalWithCause<OtherMetadata>>> list = new ArrayList<>();
      list.add(aipOtherMetadata);
      // list from all representations
      AIP aip = retrieveAIP(aipId);
      for (Representation representation : aip.getRepresentations()) {
        CloseableIterable<OptionalWithCause<OtherMetadata>> representationOtherMetadata = listOtherMetadata(aipId,
          representation.getId(), type);
        list.add(representationOtherMetadata);
      }
      return CloseableIterables.concat(list);
    } else {
      return aipOtherMetadata;
    }

  }

  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getRepresentationOtherMetadataFolderStoragePath(aipId, representationId);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<OtherMetadata>> ret;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), resources, OtherMetadata.class);
    } catch (NotFoundException e) {
      // check if Representation exists
      storage.getDirectory(ModelUtils.getRepresentationStoragePath(aipId, representationId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<OptionalWithCause<OtherMetadata>>();
    }
    return ret;
  }

  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId,
    String type) throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    StoragePath storagePath = ModelUtils.getRepresentationOtherMetadataStoragePath(aipId, representationId, type);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<OtherMetadata>> ret;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), resources, OtherMetadata.class);
    } catch (NotFoundException e) {
      // check if Representation exists
      storage.getDirectory(ModelUtils.getRepresentationStoragePath(aipId, representationId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<OptionalWithCause<OtherMetadata>>();
    }
    return ret;
  }

  /***************** Log entry related *****************/
  /*****************************************************/
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String datePlusExtension = sdf.format(new Date()) + ".log";
    logFile = logDirectory.resolve(datePlusExtension);
    synchronized (logFile) {

      // verify if file exists and if not, if older files exist (in that case,
      // move them to storage)
      if (!Files.exists(logFile)) {
        findOldLogsAndMoveThemToStorage(logDirectory, logFile);
        try {
          Files.createFile(logFile);
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write log into", e);
        }
      }

      // write to log file
      JsonUtils.appendObjectToFile(logEntry, logFile);

      // emit event
      if (notify) {
        notifyLogEntryCreated(logEntry);
      }
    }
  }

  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    addLogEntry(logEntry, logDirectory, true);
  }

  // FIXME this should be synchronized (at least access to logFile)
  public synchronized void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDirectory);

      for (Path path : directoryStream) {
        if (!path.equals(currentLogFile)) {
          try {
            StoragePath logPath = ModelUtils.getLogStoragePath(path.getFileName().toString());
            storage.createBinary(logPath, new FSPathContentPayload(path), false);
            Files.delete(path);
          } catch (IOException | GenericException | AlreadyExistsException e) {
            LOGGER.error("Error archiving log file", e);
          }
        }
      }
      directoryStream.close();

    } catch (IOException e) {
      LOGGER.error("Error listing directory for log files", e);
    }
  }

  /***************** Users/Groups related *****************/
  /********************************************************/

  public User retrieveUserByName(String name) throws GenericException {
    try {
      return UserUtility.getLdapUtility().getUser(name);
    } catch (LdapUtilityException e) {
      // TODO 20160906 hsilva: change this by a more specific exception
      throw new GenericException("Unable to retrieve user", e);
    }
  }

  public User retrieveUserByEmail(String email) throws GenericException {
    try {
      return UserUtility.getLdapUtility().getUserWithEmail(email);
    } catch (LdapUtilityException e) {
      // TODO 20160906 hsilva: change this by a more specific exception
      throw new GenericException("Unable to retrieve user", e);
    }
  }

  public User registerUser(User user, String password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    try {
      User registeredUser = UserUtility.getLdapUtility().registerUser(user, password);
      if (notify) {
        notifyUserCreated(registeredUser);
      }
      return registeredUser;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error registering user to LDAP", e);
    } catch (UserAlreadyExistsException e) {
      throw new UserAlreadyExistsException("User already exists", e);
    } catch (EmailAlreadyExistsException e) {
      throw new EmailAlreadyExistsException("Email already exists", e);
    }
  }

  public User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    return createUser(user, null, notify);
  }

  public User createUser(User user, String password, boolean notify) throws GenericException,
    EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    try {
      User createdUser = UserUtility.getLdapUtility().addUser(user);

      if (password != null) {
        UserUtility.getLdapUtility().setUserPassword(createdUser.getId(), password);
      }
      if (notify) {
        notifyUserCreated(createdUser);
      }
      return createdUser;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error creating user in LDAP", e);
    } catch (UserAlreadyExistsException e) {
      throw new UserAlreadyExistsException("User already exists", e);
    } catch (EmailAlreadyExistsException e) {
      throw new EmailAlreadyExistsException("Email already exists", e);
    }
  }

  public User updateUser(User user, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return updateUser(user, null, notify);
  }

  public User updateUser(User user, String password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    try {
      if (password != null) {
        UserUtility.getLdapUtility().setUserPassword(user.getId(), password);
      }

      User updatedUser = UserUtility.getLdapUtility().modifyUser(user);
      if (notify) {
        notifyUserUpdated(updatedUser);
      }
      return updatedUser;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error updating user in LDAP", e);
    } catch (EmailAlreadyExistsException e) {
      throw new AlreadyExistsException("User already exists", e);
    } catch (NotFoundException e) {
      throw new NotFoundException("User doesn't exist", e);
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  public User updateMyUser(User user, String password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    try {
      User updatedUser = UserUtility.getLdapUtility().modifySelfUser(user, password);

      if (notify) {
        notifyUserUpdated(updatedUser);
      }
      return updatedUser;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error updating user in LDAP", e);
    } catch (EmailAlreadyExistsException e) {
      throw new AlreadyExistsException("User already exists", e);
    } catch (NotFoundException e) {
      throw new NotFoundException("User doesn't exist", e);
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }

  }

  public void deleteUser(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    try {
      UserUtility.getLdapUtility().removeUser(id);
      if (notify) {
        notifyUserDeleted(id);
      }
    } catch (LdapUtilityException e) {
      throw new GenericException("Error deleting user from LDAP", e);
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  public void notifyUserUpdated(User user) {
    super.notifyUserUpdated(user);
  }

  public List<User> listUsers() throws GenericException {
    try {
      return UserUtility.getLdapUtility().getUsers();
    } catch (LdapUtilityException e) {
      throw new GenericException("Unable to list users", e);
    }
  }

  public Group retrieveGroup(String name) throws GenericException, NotFoundException {
    return UserUtility.getLdapUtility().getGroup(name);
  }

  public Group createGroup(Group group, boolean notify) throws GenericException, AlreadyExistsException {
    try {
      Group createdGroup = UserUtility.getLdapUtility().addGroup(group);
      if (notify) {
        notifyGroupCreated(createdGroup);
      }
      return createdGroup;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error creating group in LDAP", e);
    } catch (GroupAlreadyExistsException e) {
      throw new AlreadyExistsException("Group already exists", e);
    }
  }

  public Group updateGroup(final Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      Group updatedGroup = UserUtility.getLdapUtility().modifyGroup(group);
      if (notify) {
        notifyGroupUpdated(updatedGroup);
      }
      return updatedGroup;
    } catch (LdapUtilityException e) {
      throw new GenericException("Error updating group in LDAP", e);
    } catch (NotFoundException e) {
      throw new NotFoundException("Group doesn't exist", e);
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }

  }

  public void deleteGroup(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    try {
      UserUtility.getLdapUtility().removeGroup(id);
      if (notify) {
        notifyGroupDeleted(id);
      }
    } catch (LdapUtilityException e) {
      throw new GenericException("Error deleting group from LDAP", e);
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  public void notifyGroupUpdated(Group group) {
    super.notifyGroupUpdated(group);
  }

  public List<Group> listGroups() throws GenericException {
    try {
      return UserUtility.getLdapUtility().getGroups();
    } catch (LdapUtilityException e) {
      throw new GenericException("Unable to list groups", e);
    }
  }

  public User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, GenericException {
    User user = null;
    boolean success = true;
    try {
      if (useModel) {
        user = UserUtility.getLdapUtility().confirmUserEmail(username, email, emailConfirmationToken);
      }
      success = true;
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error on password reset", e);
    } catch (NotFoundException e) {
      success = false;
      throw new NotFoundException("User doesn't exist", e);
    } catch (InvalidTokenException e) {
      success = false;
      throw new InvalidTokenException("Token exception", e);
    }
    if (success && user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  public User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException {
    User user = null;
    boolean success = true;
    try {
      if (useModel) {
        user = UserUtility.getLdapUtility().requestPasswordReset(username, email);
      }
      success = true;
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error requesting password reset", e);
    } catch (NotFoundException e) {
      success = false;
      throw new NotFoundException("User doesn't exist", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new IllegalOperationException("Illegal operation", e);
    }
    if (success && user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  public User resetUserPassword(String username, String password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException {
    User user = null;
    boolean success = true;
    try {
      if (useModel) {
        user = UserUtility.getLdapUtility().resetUserPassword(username, password, resetPasswordToken);
      }
      success = true;
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error on password reset", e);
    } catch (NotFoundException e) {
      success = false;
      throw new NotFoundException("User doesn't exist", e);
    } catch (InvalidTokenException e) {
      success = false;
      throw new InvalidTokenException("Token exception", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new IllegalOperationException("Illegal operation", e);
    }
    if (success && user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  /***************** Jobs related *****************/
  /************************************************/
  public void createJob(Job job) throws GenericException {
    createOrUpdateJob(job);

    // try to create directory for this job in job report container
    try {
      StoragePath jobReportsPath = ModelUtils.getJobReportsStoragePath(job.getId());
      storage.createDirectory(jobReportsPath);
    } catch (AlreadyExistsException e) {
      // do nothing & carry on
    } catch (RequestNotValidException | AuthorizationDeniedException e) {
      throw new GenericException("Error creating/updating job report", e);
    }
  }

  public void createOrUpdateJob(Job job) {
    // create or update job in storage
    try {
      String jobAsJson = JsonUtils.getJsonFromObject(job);
      StoragePath jobPath = ModelUtils.getJobStoragePath(job.getId());
      storage.updateBinaryContent(jobPath, new StringContentPayload(jobAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job in storage", e);
    }

    // index it
    notifyJobCreatedOrUpdated(job, false);
  }

  public Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath jobPath = ModelUtils.getJobStoragePath(jobId);
    Binary binary = storage.getBinary(jobPath);
    Job ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Job.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading job: " + jobId, e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  public void deleteJob(String jobId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath jobPath = ModelUtils.getJobStoragePath(jobId);

    // remove it from storage
    storage.deleteResource(jobPath);

    // remove it from index
    notifyJobDeleted(jobId);
  }

  public Report retrieveJobReport(String jobId, String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobId, IdUtils.getJobReportId(jobId, aipId));
    Binary binary = storage.getBinary(jobReportPath);
    Report ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Report.class);
    } catch (IOException e) {
      throw new GenericException("Error reading job report", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  public void createOrUpdateJobReport(Report jobReport) throws GenericException {
    // create job report in storage
    try {
      String jobReportAsJson = JsonUtils.getJsonFromObject(jobReport);
      StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobReport.getJobId(), jobReport.getId());
      storage.updateBinaryContent(jobReportPath, new StringContentPayload(jobReportAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job report in storage", e);
    }

    // index it
    notifyJobReportCreatedOrUpdated(jobReport);
  }

  public void deleteJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobId, jobReportId);

    // remove it from storage
    storage.deleteResource(jobReportPath);

    // remove it from index
    notifyJobReportDeleted(jobReportId);
  }

  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    aip.setUpdatedBy(updatedBy);
    aip.setUpdatedOn(new Date());
    updateAIPMetadata(aip);
    notifyAipPermissionsUpdated(aip);
  }

  public void deleteTransferredResource(TransferredResource transferredResource) {
    FSUtils.deletePathQuietly(Paths.get(transferredResource.getFullPath()));
    notifyTransferredResourceDeleted(transferredResource.getUUID());
  }

  /***************** Risk related *****************/
  /************************************************/

  public Risk createRisk(Risk risk, boolean commit) throws GenericException {
    try {
      if (risk.getId() == null) {
        risk.setId(UUID.randomUUID().toString());
      }

      risk.setCreatedOn(new Date());
      risk.setUpdatedOn(new Date());

      String riskAsJson = JsonUtils.getJsonFromObject(risk);
      StoragePath riskPath = ModelUtils.getRiskStoragePath(risk.getId());
      storage.createBinary(riskPath, new StringContentPayload(riskAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating risk in storage", e);
    }

    notifyRiskCreatedOrUpdated(risk, commit);
    return risk;
  }

  public Risk updateRisk(Risk risk, String message, boolean commit) throws GenericException {
    try {
      risk.setUpdatedOn(new Date());
      String riskAsJson = JsonUtils.getJsonFromObject(risk);
      StoragePath riskPath = ModelUtils.getRiskStoragePath(risk.getId());

      // Create version snapshot
      if (message != null) {
        storage.createBinaryVersion(riskPath, message);
      }

      storage.updateBinaryContent(riskPath, new StringContentPayload(riskAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating risk in storage", e);
    }

    notifyRiskCreatedOrUpdated(risk, commit);
    return risk;
  }

  public void updateRisks(List<Risk> risks, String message, boolean commit) throws GenericException {
    for (int i = 0; i < risks.size(); i++) {
      if (i == risks.size() - 1) {
        updateRisk(risks.get(i), message, false);
      } else {
        updateRisk(risks.get(i), message, commit);
      }
    }
  }

  public void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    StoragePath riskPath = ModelUtils.getRiskStoragePath(riskId);
    storage.deleteResource(riskPath);
    notifyRiskDeleted(riskId, commit);
  }

  public Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath riskPath = ModelUtils.getRiskStoragePath(riskId);
    Binary binary = storage.getBinary(riskPath);
    Risk ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Risk.class);
    } catch (IOException e) {
      throw new GenericException("Error reading risk", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  public CloseableIterable<OptionalWithCause<Risk>> listRisks()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final boolean recursive = false;

    final CloseableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getRiskContainerPath(), recursive);

    return ResourceParseUtils.convert(getStorage(), resourcesIterable, Risk.class);
  }

  public BinaryVersion retrieveVersion(String id, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getRiskStoragePath(id);
    return storage.getBinaryVersion(binaryPath, versionId);
  }

  public BinaryVersion revertRiskVersion(String riskId, String versionId, String message, boolean commit)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getRiskStoragePath(riskId);

    BinaryVersion currentVersion = storage.createBinaryVersion(binaryPath, message);
    storage.revertBinaryVersion(binaryPath, versionId);

    notifyRiskCreatedOrUpdated(retrieveRisk(riskId), commit);

    return currentVersion;
  }

  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit) throws GenericException {
    try {
      riskIncidence.setId(UUID.randomUUID().toString());
      riskIncidence.setDetectedOn(new Date());

      String riskIncidenceAsJson = JsonUtils.getJsonFromObject(riskIncidence);
      StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidence.getId());
      storage.createBinary(riskIncidencePath, new StringContentPayload(riskIncidenceAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating risk incidence in storage", e);
    }

    notifyRiskIncidenceCreatedOrUpdated(riskIncidence, commit);
    return riskIncidence;
  }

  public RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit) throws GenericException {
    try {
      riskIncidence.setRiskId(riskIncidence.getRiskId().replace("[", "").replace("]", ""));
      String riskIncidenceAsJson = JsonUtils.getJsonFromObject(riskIncidence);
      StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidence.getId());
      storage.updateBinaryContent(riskIncidencePath, new StringContentPayload(riskIncidenceAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating risk incidence in storage", e);
    }

    notifyRiskIncidenceCreatedOrUpdated(riskIncidence, commit);
    return riskIncidence;
  }

  public void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidenceId);
    storage.deleteResource(riskIncidencePath);
    notifyRiskIncidenceDeleted(riskIncidenceId, commit);
  }

  public RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(incidenceId);
    Binary binary = storage.getBinary(riskIncidencePath);
    RiskIncidence ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, RiskIncidence.class);
    } catch (IOException e) {
      throw new GenericException("Error reading risk incidence", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  /***************** Agent related *****************/
  /************************************************/

  public Agent createAgent(Agent agent, boolean commit) throws GenericException {
    try {
      agent.setId(UUID.randomUUID().toString());
      String agentAsJson = JsonUtils.getJsonFromObject(agent);
      StoragePath agentPath = ModelUtils.getAgentStoragePath(agent.getId());
      storage.createBinary(agentPath, new StringContentPayload(agentAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating agent in storage", e);
    }

    notifyAgentCreatedOrUpdated(agent, commit);
    return agent;
  }

  public Agent updateAgent(Agent agent, boolean commit) throws GenericException {
    try {
      String agentAsJson = JsonUtils.getJsonFromObject(agent);
      StoragePath agentPath = ModelUtils.getAgentStoragePath(agent.getId());
      storage.updateBinaryContent(agentPath, new StringContentPayload(agentAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating agent in storage", e);
    }

    notifyAgentCreatedOrUpdated(agent, commit);
    return agent;
  }

  public void deleteAgent(String agentId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {

    StoragePath agentPath = ModelUtils.getAgentStoragePath(agentId);
    storage.deleteResource(agentPath);
    notifyAgentDeleted(agentId, commit);
  }

  public Agent retrieveAgent(String agentId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath agentPath = ModelUtils.getAgentStoragePath(agentId);
    Binary binary = storage.getBinary(agentPath);
    Agent ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Agent.class);
    } catch (IOException e) {
      throw new GenericException("Error reading agent", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  /***************** Format related *****************/
  /************************************************/

  public Format createFormat(Format format, boolean commit) throws GenericException {
    try {
      format.setId(UUID.randomUUID().toString());
      String formatAsJson = JsonUtils.getJsonFromObject(format);
      StoragePath formatPath = ModelUtils.getFormatStoragePath(format.getId());
      storage.createBinary(formatPath, new StringContentPayload(formatAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating format in storage", e);
    }

    notifyFormatCreatedOrUpdated(format, commit);
    return format;
  }

  public Format updateFormat(Format format, boolean commit) throws GenericException {
    try {
      String formatAsJson = JsonUtils.getJsonFromObject(format);
      StoragePath formatPath = ModelUtils.getFormatStoragePath(format.getId());
      storage.updateBinaryContent(formatPath, new StringContentPayload(formatAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating format in storage", e);
    }

    notifyFormatCreatedOrUpdated(format, commit);
    return format;
  }

  public void deleteFormat(String formatId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {

    StoragePath formatPath = ModelUtils.getFormatStoragePath(formatId);
    storage.deleteResource(formatPath);
    notifyFormatDeleted(formatId, commit);
  }

  public Format retrieveFormat(String formatId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath formatPath = ModelUtils.getFormatStoragePath(formatId);
    Binary binary = storage.getBinary(formatPath);
    Format ret;
    InputStream inputStream = null;
    try {
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Format.class);
    } catch (IOException e) {
      throw new GenericException("Error reading format", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  /***************** Format-Agent related *****************/
  /************************************************/

  public List<Format> retrieveFormatsFromAgent(String agentId) {
    try {
      Agent agent = retrieveAgent(agentId);
      return retrieveFormatsFromAgent(agent);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error getting agent formats");
      return new ArrayList<Format>();
    }
  }

  public List<Format> retrieveFormatsFromAgent(Agent agent) {
    try {
      CloseableIterable<Resource> allFormats = storage.listResourcesUnderDirectory(ModelUtils.getFormatContainerPath(),
        true);
      List<Format> formats = new ArrayList<Format>();

      for (Resource resource : allFormats) {
        String resourceName = resource.getStoragePath().getName();
        Format format = this.retrieveFormat(resourceName.substring(0, resourceName.lastIndexOf('.')));

        if (agent.getFormatIds().contains(format.getId())) {
          formats.add(format);
        }

        for (int i = 0; i < format.getPronoms().size() && formats.isEmpty(); i++) {
          if (agent.getPronoms().contains(format.getPronoms().get(i))) {
            formats.add(format);
          }
        }

        for (int i = 0; i < format.getMimetypes().size() && formats.isEmpty(); i++) {
          if (agent.getMimetypes().contains(format.getMimetypes().get(i))) {
            formats.add(format);
          }
        }

        for (int i = 0; i < format.getExtensions().size() && formats.isEmpty(); i++) {
          if (agent.getExtensions().contains(format.getExtensions().get(i))) {
            formats.add(format);
          }
        }

        for (int i = 0; i < format.getUtis().size() && formats.isEmpty(); i++) {
          if (agent.getUtis().contains(format.getUtis().get(i))) {
            formats.add(format);
          }
        }
      }

      return formats;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Error getting agent formats");
      return new ArrayList<Format>();
    }
  }

  public List<Agent> retrieveAgentsFromFormat(String formatId) {
    try {
      Format format = retrieveFormat(formatId);
      return retrieveAgentsFromFormat(format);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error getting format agents");
      return new ArrayList<Agent>();
    }
  }

  public List<Agent> retrieveAgentsFromFormat(Format format) {
    try {
      CloseableIterable<Resource> allAgents = storage.listResourcesUnderDirectory(ModelUtils.getAgentContainerPath(),
        true);
      List<Agent> agents = new ArrayList<Agent>();

      for (Resource resource : allAgents) {
        String resourceName = resource.getStoragePath().getName();
        Agent agent = this.retrieveAgent(resourceName.substring(0, resourceName.lastIndexOf('.')));

        if (agent.getFormatIds().contains(format.getId())) {
          agents.add(agent);
        }

        for (int i = 0; i < format.getPronoms().size() && agents.isEmpty(); i++) {
          if (agent.getPronoms().contains(format.getPronoms().get(i))) {
            agents.add(agent);
          }
        }

        for (int i = 0; i < format.getMimetypes().size() && agents.isEmpty(); i++) {
          if (agent.getMimetypes().contains(format.getMimetypes().get(i))) {
            agents.add(agent);
          }
        }

        for (int i = 0; i < format.getExtensions().size() && agents.isEmpty(); i++) {
          if (agent.getExtensions().contains(format.getExtensions().get(i))) {
            agents.add(agent);
          }
        }

        for (int i = 0; i < format.getUtis().size() && agents.isEmpty(); i++) {
          if (agent.getUtis().contains(format.getUtis().get(i))) {
            agents.add(agent);
          }
        }
      }

      return agents;
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Error getting format agents");
      return new ArrayList<Agent>();
    }
  }

  /***************** Notification related *****************/
  /**
   * @throws AuthorizationDeniedException
   **********************************************/

  public Notification createNotification(final Notification notification, final NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {

    notification.setId(UUID.randomUUID().toString());
    notification.setAcknowledgeToken(UUID.randomUUID().toString());
    Notification processedNotification = processor.processNotification(this, notification);

    try {
      String notificationAsJson = JsonUtils.getJsonFromObject(processedNotification);
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(processedNotification.getId());
      storage.createBinary(notificationPath, new StringContentPayload(notificationAsJson), false);
      notifyNotificationCreatedOrUpdated(processedNotification);
    } catch (NotFoundException | RequestNotValidException | AlreadyExistsException e) {
      LOGGER.error("Error creating notification in storage", e);
      throw new GenericException(e);
    }
    return processedNotification;
  }

  public Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      String notificationAsJson = JsonUtils.getJsonFromObject(notification);
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notification.getId());
      storage.updateBinaryContent(notificationPath, new StringContentPayload(notificationAsJson), false, true);
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error updating notification in storage", e);
      throw new GenericException(e);
    }

    notifyNotificationCreatedOrUpdated(notification);
    return notification;
  }

  public void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notificationId);
      storage.deleteResource(notificationPath);
      notifyNotificationDeleted(notificationId);
    } catch (RequestNotValidException e) {
      LOGGER.error("Error deleting notification", e);
      throw new GenericException(e);
    }
  }

  public Notification retrieveNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    InputStream inputStream = null;
    Notification ret;
    try {
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notificationId);
      Binary binary = storage.getBinary(notificationPath);
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, Notification.class);
    } catch (IOException | RequestNotValidException e) {
      throw new GenericException("Error reading notification", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  public void acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {

    Notification notification = this.retrieveNotification(notificationId);
    String ackToken = token.substring(0, 36);
    String emailToken = token.substring(36);

    if (notification.getAcknowledgeToken().equals(ackToken)) {
      for (String recipient : notification.getRecipientUsers()) {
        if (recipient.equals(emailToken)) {
          DateFormat df = DateFormat.getDateTimeInstance();
          String ackDate = df.format(new Date());
          notification.addAcknowledgedUser(recipient, ackDate);
          notification.setAcknowledged(true);
          this.updateNotification(notification);
        }
      }
    }
  }

  /****************************************************************
   * 
   * OTHER DIRECTORIES (submission, documentation, schemas)
   * 
   *********************************************************/

  /**
   * 
   */
  public Directory getSubmissionDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSubmissionStoragePath(aipId));
  }

  public void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    storage.copy(submissionStorage, submissionStoragePath, ModelUtils.getSubmissionStoragePath(aipId));
  }

  public void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    StoragePath submissionStoragePath = DefaultStoragePath.parse(ModelUtils.getSubmissionStoragePath(aipId),
      submissionPath.getFileName().toString());
    storage.createBinary(submissionStoragePath, new FSPathContentPayload(submissionPath), false);
  }

  public Directory getDocumentationDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getDocumentationStoragePath(aipId));
  }

  public Directory getDocumentationDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getDocumentationStoragePath(aipId, representationId));
  }

  public File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    File file;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getDocumentationStoragePath(aipId, representationId, directoryPath, fileId);

    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    file = ResourceParseUtils.convertResourceToFile(createdBinary);

    return file;
  }

  public Directory getSchemasDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSchemasStoragePath(aipId));
  }

  public Directory getSchemasDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSchemasStoragePath(aipId, representationId));
  }

  public File createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    File file;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getSchemaStoragePath(aipId, representationId, directoryPath, fileId);

    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    file = ResourceParseUtils.convertResourceToFile(createdBinary);

    return file;
  }

  private <T extends Serializable> CloseableIterable<OptionalWithCause<T>> listRepresentations(Class<T> objectClass)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<AIP>> aips = listAIPs();
    Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
    List<CloseableIterable<OptionalWithCause<T>>> representations = new ArrayList<CloseableIterable<OptionalWithCause<T>>>();

    while (aipIter.hasNext()) {
      OptionalWithCause<AIP> oaip = aipIter.next();
      AIP aip = oaip.get();
      if (oaip.isPresent() && !aip.getRepresentations().isEmpty()) {
        final CloseableIterable<Resource> resourcesIterable = storage
          .listResourcesUnderContainer(ModelUtils.getRepresentationsContainerPath(aip.getId()), false);
        CloseableIterable<OptionalWithCause<T>> fs = ResourceParseUtils.convert(getStorage(), resourcesIterable,
          objectClass);
        representations.add(fs);
      }
    }

    return CloseableIterables.concat(representations);
  }

  private <T extends Serializable> CloseableIterable<OptionalWithCause<T>> listFiles(Class<T> objectClass)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<AIP>> aips = listAIPs();
    Iterator<OptionalWithCause<AIP>> aipIter = aips.iterator();
    List<CloseableIterable<OptionalWithCause<T>>> files = new ArrayList<CloseableIterable<OptionalWithCause<T>>>();

    while (aipIter.hasNext()) {
      OptionalWithCause<AIP> aip = aipIter.next();
      if (aip.isPresent()) {
        for (Representation representation : aip.get().getRepresentations()) {
          final CloseableIterable<Resource> resourcesIterable = storage.listResourcesUnderContainer(
            ModelUtils.getRepresentationDataStoragePath(aip.get().getId(), representation.getId()), true);
          CloseableIterable<OptionalWithCause<T>> fs = ResourceParseUtils.convert(getStorage(), resourcesIterable,
            objectClass);
          files.add(fs);
        }
      }
    }

    return CloseableIterables.concat(files);
  }

  public <T extends Serializable> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    if (Representation.class.equals(objectClass)) {
      return listRepresentations(objectClass);
    } else if (File.class.equals(objectClass)) {
      return listFiles(objectClass);
    } else {
      StoragePath containerPath = ModelUtils.getContainerPath(objectClass);
      final CloseableIterable<Resource> resourcesIterable = storage.listResourcesUnderContainer(containerPath, false);
      return ResourceParseUtils.convert(getStorage(), resourcesIterable, objectClass);
    }
  }

  public boolean hasObjects(Class<? extends IsRODAObject> objectClass) {
    try {
      if (!TransferredResource.class.equals(objectClass)) {
        StoragePath storagePath = ModelUtils.getContainerPath(objectClass);
        try {
          return RodaCoreFactory.getStorageService().countResourcesUnderContainer(storagePath, false).intValue() > 0;
        } catch (NotFoundException e) {
          // 20160913 hsilva: we want to handle the non-existence of a container
        }
      }

      return false;
    } catch (RODAException e) {
      return false;
    }
  }

}
