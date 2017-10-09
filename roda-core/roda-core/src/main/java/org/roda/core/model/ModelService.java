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
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.dips.DIPUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.iterables.LogEntryFileSystemIterable;
import org.roda.core.model.iterables.LogEntryStorageIterable;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.ResourceListUtils;
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
import org.roda.core.util.HTTPUtility;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that "relates" Model & Storage
 * 
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ModelService extends ModelObservable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);
  private static final DateTimeFormatter LOG_NAME_DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
  private static final boolean FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA = false;
  private final StorageService storage;
  private Object logFileLock = new Object();

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
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DIP);
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

  private void updateDIPMetadata(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    updateDIPMetadata(dip, ModelUtils.getDIPStoragePath(dip.getId()));
  }

  public CloseableIterable<OptionalWithCause<AIP>> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final boolean recursive = false;

    final CloseableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getAIPContainerPath(), recursive);

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
   * @param sourcePath
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

  public AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy, boolean isGhost) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {

    AIPState state = AIPState.ACTIVE;
    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    String id = directory.getStoragePath().getName();
    Permissions inheritedPermissions = this.addParentPermissions(permissions, parentId);

    AIP aip = new AIP(id, parentId, type, state, inheritedPermissions, createdBy);

    aip.setGhost(isGhost);
    aip.setIngestSIPIds(ingestSIPIds);
    aip.setIngestJobId(ingestJobId);

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
    Permissions inheritedPermissions = this.addParentPermissions(permissions, parentId);

    AIP aip = new AIP(id, parentId, type, state, inheritedPermissions, createdBy);
    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip);
    }

    return aip;
  }

  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy) throws RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException {

    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    String id = directory.getStoragePath().getName();
    Permissions inheritedPermissions = this.addParentPermissions(permissions, parentId);

    AIP aip = new AIP(id, parentId, type, state, inheritedPermissions, createdBy).setIngestSIPIds(ingestSIPIds)
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

  public AIP notifyAipCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipCreated(aip);
    return aip;
  }

  public AIP notifyAipUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipUpdated(aip);
    return aip;
  }

  private Permissions addParentPermissions(Permissions permissions, String parentId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    if (parentId != null) {
      AIP parentAIP = this.retrieveAIP(parentId);
      Set<String> parentGroupnames = parentAIP.getPermissions().getGroupnames();
      Set<String> parentUsernames = parentAIP.getPermissions().getUsernames();
      Set<String> groupnames = permissions.getGroupnames();
      Set<String> usernames = permissions.getUsernames();

      for (String user : parentUsernames) {
        if (!usernames.contains(user)) {
          permissions.setUserPermissions(user, parentAIP.getPermissions().getUserPermissions(user));
        }
      }

      for (String group : parentGroupnames) {
        if (!groupnames.contains(group)) {
          permissions.setGroupPermissions(group, parentAIP.getPermissions().getGroupPermissions(group));
        }
      }
    }

    return permissions;
  }

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
      // changed and removing deleted ones.
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

  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    if (aipId.equals(parentId)) {
      throw new RequestNotValidException("Cannot set itself as its parent: " + aipId);
    }

    // TODO ADD RESTRICTIONS
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    String oldParentId = aip.getParentId();
    aip.setParentId(parentId);
    aip.setUpdatedOn(new Date());
    aip.setUpdatedBy(updatedBy);
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

  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    AIP aip = retrieveAIP(aipId);
    aip.setType(type);
    aip.setUpdatedOn(new Date());
    aip.setUpdatedBy(updatedBy);
    notifyAipUpdated(aip);
    updateAIPMetadata(aip);
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
    Map<String, String> properties) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, ValidationException {
    return updateDescriptiveMetadata(aipId, null, descriptiveMetadataId, descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, properties);
  }

  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, ValidationException {
    DescriptiveMetadata ret;

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    // Create version snapshot
    storage.createBinaryVersion(binaryPath, properties);

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
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return revertDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId, properties);
  }

  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);

    BinaryVersion currentVersion = storage.createBinaryVersion(binaryPath, properties);
    storage.revertBinaryVersion(binaryPath, versionId);

    notifyDescriptiveMetadataUpdated(retrieveDescriptiveMetadata(aipId, descriptiveMetadataId));

    return currentVersion;
  }

  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<CloseableIterable<OptionalWithCause<DescriptiveMetadata>>> list = new ArrayList<>();

    CloseableIterable<OptionalWithCause<AIP>> aips = listAIPs();

    for (OptionalWithCause<AIP> oaip : aips) {
      if (oaip.isPresent()) {
        AIP aip = oaip.get();
        StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aip.getId(), null);

        CloseableIterable<OptionalWithCause<DescriptiveMetadata>> aipDescriptiveMetadata;
        try {
          boolean recursive = true;
          CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
          aipDescriptiveMetadata = ResourceParseUtils.convert(getStorage(), resources, DescriptiveMetadata.class);
        } catch (NotFoundException e) {
          // check if AIP exists
          storage.getDirectory(ModelUtils.getAIPStoragePath(aip.getId()));
          // if no exception was sent by above method, return empty list
          aipDescriptiveMetadata = new EmptyClosableIterable<>();
        }

        list.add(aipDescriptiveMetadata);

        // list from all representations
        for (Representation representation : aip.getRepresentations()) {
          CloseableIterable<OptionalWithCause<DescriptiveMetadata>> rpm = listDescriptiveMetadata(aip.getId(),
            representation.getId());
          list.add(rpm);
        }
      }
    }

    return CloseableIterables.concat(list);
  }

  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    boolean includeRepresentations)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, null);

    CloseableIterable<OptionalWithCause<DescriptiveMetadata>> aipDescriptiveMetadata;
    try {
      boolean recursive = true;
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      aipDescriptiveMetadata = ResourceParseUtils.convert(getStorage(), resources, DescriptiveMetadata.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));
      // if no exception was sent by above method, return empty list
      aipDescriptiveMetadata = new EmptyClosableIterable<>();
    }

    if (includeRepresentations) {
      List<CloseableIterable<OptionalWithCause<DescriptiveMetadata>>> list = new ArrayList<>();
      list.add(aipDescriptiveMetadata);

      // list from all representations
      AIP aip = retrieveAIP(aipId);
      for (Representation representation : aip.getRepresentations()) {
        CloseableIterable<OptionalWithCause<DescriptiveMetadata>> rpm = listDescriptiveMetadata(aipId,
          representation.getId());
        list.add(rpm);
      }
      return CloseableIterables.concat(list);
    } else {
      return aipDescriptiveMetadata;
    }

  }

  public CloseableIterable<OptionalWithCause<DescriptiveMetadata>> listDescriptiveMetadata(String aipId,
    String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataDirectoryStoragePath(aipId, representationId);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<DescriptiveMetadata>> ret;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), resources, DescriptiveMetadata.class);
    } catch (NotFoundException e) {
      // check if Representation exists
      storage.getDirectory(ModelUtils.getRepresentationStoragePath(aipId, representationId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<>();
    }

    return ret;
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
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    Representation representation = new Representation(representationId, aipId, original, type);
    representation.setCreatedBy(createdBy);
    representation.setUpdatedBy(createdBy);

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

  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    Representation representation;

    if (justData) {
      StoragePath dataPath = ModelUtils.getRepresentationDataStoragePath(aipId, representationId);
      StoragePath sourceDataPath = DefaultStoragePath.parse(sourcePath, RodaConstants.STORAGE_DIRECTORY_DATA);
      storage.copy(sourceStorage, sourceDataPath, dataPath);
    } else {
      StoragePath directoryPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);

      // verify structure of source representation
      // 20170324 should we validate the representation???
      storage.copy(sourceStorage, sourcePath, directoryPath);
    }

    representation = new Representation(representationId, aipId, original, type);
    representation.setCreatedBy(createdBy);
    representation.setUpdatedBy(createdBy);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.getRepresentations().add(representation);
    updateAIPMetadata(aip);

    notifyRepresentationCreated(representation);
    return representation;
  }

  public Representation updateRepresentationInfo(Representation representation) {
    notifyRepresentationUpdated(representation);
    return representation;
  }

  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    AIP aip = retrieveAIP(aipId);
    Iterator<Representation> it = aip.getRepresentations().iterator();

    while (it.hasNext()) {
      Representation representation = it.next();
      if (representation.getId().equals(representationId)) {
        representation.setType(type);
        representation.setUpdatedOn(new Date());
        representation.setUpdatedBy(updatedBy);
        notifyRepresentationUpdated(representation);
        break;
      }
    }

    updateAIPMetadata(aip);
  }

  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    AIP aip = retrieveAIP(aipId);
    Iterator<Representation> it = aip.getRepresentations().iterator();
    Optional<Representation> representation = Optional.empty();

    while (it.hasNext()) {
      Representation next = it.next();
      if (next.getId().equals(representationId)) {
        representation = Optional.of(next);
        break;
      }
    }

    if (representation.isPresent()) {
      representation.get().setRepresentationStates(newStates);
      representation.get().setUpdatedOn(new Date());
      representation.get().setUpdatedBy(updatedBy);
      updateAIPMetadata(aip);
      notifyRepresentationUpdated(representation.get());
    }
  }

  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    Representation representation;

    // XXX possible optimization only creating new files, updating
    // changed and removing deleted

    StoragePath representationPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);
    storage.deleteResource(representationPath);
    try {
      storage.copy(sourceStorage, sourcePath, representationPath);
    } catch (AlreadyExistsException e) {
      throw new GenericException("Copying after delete gave an unexpected already exists exception", e);
    }

    // build return object
    representation = new Representation(representationId, aipId, original, type);
    representation.setUpdatedBy(updatedBy);
    notifyRepresentationUpdated(representation);
    return representation;
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
      ret = new EmptyClosableIterable<>();
    }

    return ret;

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
    boolean asReference = false;
    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);

    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    File file = ResourceParseUtils.convertResourceToFile(createdBinary);

    if (notify) {
      notifyFileCreated(file);
    }

    return file;
  }

  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    final Directory createdDirectory = storage.createDirectory(DefaultStoragePath.parse(filePath, dirName));
    File file = ResourceParseUtils.convertResourceToFile(createdDirectory);

    if (notify) {
      notifyFileCreated(file);
    }

    return file;
  }

  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    boolean asReference = false;
    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);

    storage.updateBinaryContent(filePath, contentPayload, asReference, createIfNotExists);
    Binary binaryUpdated = storage.getBinary(filePath);
    File file = ResourceParseUtils.convertResourceToFile(binaryUpdated);

    if (notify) {
      notifyFileUpdated(file);
    }

    return file;
  }

  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return updateFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), contentPayload,
      createIfNotExists, notify);
  }

  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    boolean notify) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    storage.deleteResource(filePath);

    if (notify) {
      notifyFileDeleted(aipId, representationId, directoryPath, fileId);
    }
  }

  public File renameFolder(File folder, String newName, boolean replaceExisting, boolean reindexResources)
    throws AlreadyExistsException, GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException {

    Path basePath = RodaCoreFactory.getStoragePath();
    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(folder);
    Path fullPath = basePath.resolve(FSUtils.getStoragePathAsString(fileStoragePath, false));

    if (FSUtils.exists(fullPath)) {
      FSUtils.move(fullPath, fullPath.getParent().resolve(newName), replaceExisting);

      if (reindexResources) {
        notifyAipUpdated(folder.getAipId());
      }

      return retrieveFile(folder.getAipId(), folder.getRepresentationId(), folder.getPath(), newName);
    } else {
      throw new NotFoundException("Folder was moved or does not exist");
    }
  }

  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean replaceExisting, boolean reindexResources) throws AlreadyExistsException, GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException {

    Path basePath = RodaCoreFactory.getStoragePath();

    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    Path fullPath = basePath.resolve(FSUtils.getStoragePathAsString(fileStoragePath, false));

    if (!FSUtils.exists(fullPath)) {
      throw new NotFoundException("Some files/folders were moved or do not exist");
    }

    File newFile = new File(newId, newAipId, newRepresentationId, newDirectoryPath, file.isDirectory());
    StoragePath newFileStoragePath = ModelUtils.getFileStoragePath(newFile);
    Path newFullPath = basePath.resolve(FSUtils.getStoragePathAsString(newFileStoragePath, false));

    FSUtils.move(fullPath, newFullPath, replaceExisting);

    if (reindexResources) {
      notifyRepresentationUpdated(retrieveRepresentation(newAipId, newRepresentationId));
      if (!newAipId.equals(file.getAipId()) || !newRepresentationId.equals(file.getRepresentationId())) {
        notifyRepresentationUpdated(retrieveRepresentation(file.getAipId(), file.getRepresentationId()));
      }
    }

    return newFile;

  }

  /***************** Preservation related *****************/
  /********************************************************/

  public void createRepositoryEvent(PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify) {
    createRepositoryEvent(eventType, eventDescription, null, null, outcomeState, outcomeText, outcomeDetail, agentName,
      notify);
  }

  public void createRepositoryEvent(PreservationEventType eventType, String eventDescription,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText,
    String outcomeDetail, String agentName, boolean notify) {
    createEvent(null, null, null, null, eventType, eventDescription, sources, targets, outcomeState, outcomeText,
      outcomeDetail, agentName, notify);
  }

  public void createUpdateAIPEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, PluginState outcomeState, String outcomeText,
    String outcomeDetail, String agentName, boolean notify) {
    createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, null, null, outcomeState,
      outcomeText, outcomeDetail, agentName, notify);
  }

  public void createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify) {
    try {
      StringBuilder builder = new StringBuilder(outcomeText);
      if (StringUtils.isNotBlank(outcomeDetail) && outcomeState.equals(PluginState.SUCCESS)) {
        builder.append("\n").append("The following reason has been reported by the user: ").append(agentName)
          .append("\n").append(outcomeDetail);
      }

      createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources, targets,
        outcomeState, builder.toString(), "", Arrays.asList(IdUtils.getUserAgentId(agentName)), notify);
    } catch (ValidationException | AlreadyExistsException | GenericException | NotFoundException
      | RequestNotValidException | AuthorizationDeniedException e1) {
      LOGGER.error("Could not create an event for: " + eventDescription, e1);
    }
  }

  public void createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<String> agentIds, boolean notify) throws GenericException, ValidationException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {

    String id = IdUtils.createPreservationMetadataId(PreservationMetadataType.EVENT);
    ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(id, new Date(), eventType.toString(),
      eventDescription, sources, targets, outcomeState.toString(), outcomeDetail, outcomeExtension, agentIds);
    createPreservationMetadata(PreservationMetadataType.EVENT, id, aipId, representationId, filePath, fileId,
      premisEvent, notify);
  }

  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadataType type) {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, fileId));
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setType(type);
    return pm;
  }

  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String urn = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null,
      null);
    StoragePath path = ModelUtils.getPreservationMetadataStoragePath(urn, PreservationMetadataType.REPRESENTATION,
      aipId, representationId);
    return storage.getBinary(path);
  }

  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrievePreservationFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String identifier = IdUtils.getPreservationFileId(fileId);
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(identifier, PreservationMetadataType.FILE,
      aipId, representationId, fileDirectoryPath, fileId);
    return storage.getBinary(filePath);
  }

  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationID,
      PreservationMetadataType.EVENT, aipId, representationId, filePath, fileId);
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
    String identifier = fileId;
    if (!PreservationMetadataType.FILE.equals(type)) {
      identifier = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
    }
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

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationMetadata()
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<CloseableIterable<OptionalWithCause<PreservationMetadata>>> list = new ArrayList<>();

    try {
      StoragePath storagePath = ModelUtils.getPreservationRepositoryEventStoragePath();
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, true);
      CloseableIterable<OptionalWithCause<PreservationMetadata>> pms = ResourceParseUtils.convert(getStorage(),
        resources, PreservationMetadata.class);
      list.add(pms);
    } catch (NotFoundException e) {
      // do nothing
    }

    try {
      StoragePath storagePath = ModelUtils.getPreservationAgentStoragePath();
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, true);
      CloseableIterable<OptionalWithCause<PreservationMetadata>> pms = ResourceParseUtils.convert(getStorage(),
        resources, PreservationMetadata.class);
      list.add(pms);
    } catch (NotFoundException e) {
      // do nothing
    }

    CloseableIterable<OptionalWithCause<AIP>> aips = listAIPs();

    for (OptionalWithCause<AIP> oaip : aips) {
      if (oaip.isPresent()) {
        AIP aip = oaip.get();
        StoragePath storagePath = ModelUtils.getAIPPreservationMetadataStoragePath(aip.getId());

        CloseableIterable<OptionalWithCause<PreservationMetadata>> aipPreservationMetadata;
        try {
          boolean recursive = true;
          CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
          aipPreservationMetadata = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
        } catch (NotFoundException e) {
          // check if AIP exists
          storage.getDirectory(ModelUtils.getAIPStoragePath(aip.getId()));
          // if no exception was sent by above method, return empty list
          aipPreservationMetadata = new EmptyClosableIterable<>();
        }

        list.add(aipPreservationMetadata);

        // list from all representations
        for (Representation representation : aip.getRepresentations()) {
          CloseableIterable<OptionalWithCause<PreservationMetadata>> rpm = listPreservationMetadata(aip.getId(),
            representation.getId());
          list.add(rpm);
        }
      }
    }

    return CloseableIterables.concat(list);
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
      aipPreservationMetadata = new EmptyClosableIterable<>();
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
      ret = new EmptyClosableIterable<>();
    }

    return ret;
  }

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationAgents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<PreservationMetadata>> ret;
    StoragePath storagePath = ModelUtils.getPreservationAgentStoragePath();

    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, false);
      ret = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
    } catch (NotFoundException e) {
      ret = new EmptyClosableIterable<>();
    }

    return ret;
  }

  public CloseableIterable<OptionalWithCause<PreservationMetadata>> listPreservationRepositoryEvents()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<PreservationMetadata>> ret;
    StoragePath storagePath = ModelUtils.getPreservationRepositoryEventStoragePath();

    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, false);
      ret = ResourceParseUtils.convert(getStorage(), resources, PreservationMetadata.class);
    } catch (NotFoundException e) {
      ret = new EmptyClosableIterable<>();
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

  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      retrieveOtherMetadataBinary(aipId, representationId, fileDirectoryPath, fileId, fileSuffix, type);
      String id = IdUtils.getOtherMetadataId(aipId, representationId, fileDirectoryPath, fileId);
      return new OtherMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, fileSuffix);
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      throw e;
    }
  }

  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    boolean asReference = false;
    boolean createIfNotExists = true;

    try {
      storage.createBinary(binaryPath, payload, asReference);
    } catch (AlreadyExistsException e) {
      storage.updateBinaryContent(binaryPath, payload, asReference, createIfNotExists);
    }

    String id = IdUtils.getOtherMetadataId(aipId, representationId, fileDirectoryPath, fileId);
    OtherMetadata om = new OtherMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, fileSuffix);

    if (notify) {
      notifyOtherMetadataCreated(om);
    }

    return om;
  }

  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    storage.deleteResource(binaryPath);
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
      aipOtherMetadata = new EmptyClosableIterable<>();
    }

    if (includeRepresentations) {
      List<CloseableIterable<OptionalWithCause<OtherMetadata>>> list = new ArrayList<>();
      list.add(aipOtherMetadata);
      // list from all representations
      AIP aip = retrieveAIP(aipId);
      for (Representation representation : aip.getRepresentations()) {
        CloseableIterable<OptionalWithCause<OtherMetadata>> representationOtherMetadata = listOtherMetadata(aipId,
          representation.getId(), null, null, type);
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
      ret = new EmptyClosableIterable<>();
    }
    return ret;
  }

  public CloseableIterable<OptionalWithCause<OtherMetadata>> listOtherMetadata(String aipId, String representationId,
    List<String> filePath, String fileId, String type)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    List<String> metadataPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, filePath, fileId, type);
    StoragePath storagePath = DefaultStoragePath.parse(metadataPath);

    boolean recursive = true;
    CloseableIterable<OptionalWithCause<OtherMetadata>> ret;
    try {
      CloseableIterable<Resource> resources = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), resources, OtherMetadata.class);
    } catch (NotFoundException e) {
      // check if Representation or AIP exists
      if (representationId != null) {
        storage.getDirectory(ModelUtils.getRepresentationStoragePath(aipId, representationId));
      } else {
        storage.getDirectory(ModelUtils.getAIPStoragePath(aipId));
      }
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<>();
    }

    return ret;
  }

  /***************** Log entry related *****************/
  /*****************************************************/
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String datePlusExtension = sdf.format(new Date()) + ".log";
    Path logFile = logDirectory.resolve(datePlusExtension);
    synchronized (logFileLock) {

      // verify if file exists and if not, if older files exist (in that case,
      // move them to storage)
      if (!FSUtils.exists(logFile)) {
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

  public synchronized void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDirectory)) {

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
    } catch (IOException e) {
      LOGGER.error("Error listing directory for log files", e);
    }
  }

  /***************** Users/Groups related *****************/
  /********************************************************/

  public User retrieveAuthenticatedUser(String name, String password)
    throws GenericException, AuthenticationDeniedException {
    return UserUtility.getLdapUtility().getAuthenticatedUser(name, password);
  }

  public User retrieveUserByName(String name) throws GenericException {
    return UserUtility.getLdapUtility().getUser(name);
  }

  public User retrieveUserByEmail(String email) throws GenericException {
    return UserUtility.getLdapUtility().getUserWithEmail(email);
  }

  public User registerUser(User user, String password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    User registeredUser = UserUtility.getLdapUtility().registerUser(user, password);
    if (notify) {
      notifyUserCreated(registeredUser);
    }
    return registeredUser;
  }

  public User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    return createUser(user, null, notify);
  }

  public User createUser(User user, String password, boolean notify) throws GenericException,
    EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    User createdUser = UserUtility.getLdapUtility().addUser(user);
    if (password != null) {
      UserUtility.getLdapUtility().setUserPassword(createdUser.getId(), password);
    }
    if (notify) {
      notifyUserCreated(createdUser);
    }
    return createdUser;
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
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  public List<User> listUsers() throws GenericException {
    return UserUtility.getLdapUtility().getUsers();
  }

  public RODAMember retrieveRODAMember(String name) throws GenericException {
    RODAMember member = null;
    try {
      member = UserUtility.getLdapUtility().getUser(name);
    } catch (GenericException e) {
      try {
        member = UserUtility.getLdapUtility().getGroup(name);
      } catch (GenericException | NotFoundException e1) {
        LOGGER.error("Could not retrieve any user or group with name: {}", name);
        throw e;
      }
    }
    return member;
  }

  public User retrieveUser(String name) throws GenericException {
    return UserUtility.getLdapUtility().getUser(name);
  }

  public Group retrieveGroup(String name) throws GenericException, NotFoundException {
    return UserUtility.getLdapUtility().getGroup(name);
  }

  public Group createGroup(Group group, boolean notify) throws GenericException, AlreadyExistsException {
    Group createdGroup = UserUtility.getLdapUtility().addGroup(group);
    if (notify) {
      notifyGroupCreated(createdGroup);
    }
    return createdGroup;
  }

  public Group updateGroup(final Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      Group updatedGroup = UserUtility.getLdapUtility().modifyGroup(group);
      if (notify) {
        notifyGroupUpdated(updatedGroup);
      }
      return updatedGroup;
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
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  public List<Group> listGroups() throws GenericException {
    return UserUtility.getLdapUtility().getGroups();
  }

  public User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, GenericException {
    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().confirmUserEmail(username, email, emailConfirmationToken);
    }
    if (user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  public User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException {
    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().requestPasswordReset(username, email);
    }
    if (user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  public User resetUserPassword(String username, String password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException {
    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().resetUserPassword(username, password, resetPasswordToken);
    }
    if (user != null && notify) {
      notifyUserUpdated(user);
    }
    return user;
  }

  /***************** Jobs related *****************/
  /************************************************/
  public void createJob(Job job)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
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

  public void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    // create or update job in storage
    String jobAsJson = JsonUtils.getJsonFromObject(job);
    StoragePath jobPath = ModelUtils.getJobStoragePath(job.getId());
    storage.updateBinaryContent(jobPath, new StringContentPayload(jobAsJson), false, true);

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

  public Report retrieveJobReport(String jobId, String givenId, boolean generateId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String id = givenId;
    if (generateId) {
      id = IdUtils.getJobReportId(jobId, givenId);
    }
    StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobId, id);
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

  public void createOrUpdateJobReport(Report jobReport, Job job) throws GenericException {
    // create job report in storage
    try {
      String jobReportAsJson = JsonUtils.getJsonFromObject(jobReport);
      StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobReport.getJobId(), jobReport.getId());
      storage.updateBinaryContent(jobReportPath, new StringContentPayload(jobReportAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job report in storage", e);
    }

    // index it
    notifyJobReportCreatedOrUpdated(jobReport, job);
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

  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    dip.setLastModified(new Date());
    updateDIPMetadata(dip);
    notifyDipPermissionsUpdated(dip);
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
        risk.setId(IdUtils.createUUID());
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

    notifyRiskCreatedOrUpdated(risk, 0, commit);
    return risk;
  }

  public Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException {
    try {
      risk.setUpdatedOn(new Date());
      String riskAsJson = JsonUtils.getJsonFromObject(risk);
      StoragePath riskPath = ModelUtils.getRiskStoragePath(risk.getId());

      // Create version snapshot
      if (properties != null && !properties.isEmpty()) {
        storage.createBinaryVersion(riskPath, properties);
      }

      storage.updateBinaryContent(riskPath, new StringContentPayload(riskAsJson), false, true);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating risk in storage", e);
    }

    notifyRiskCreatedOrUpdated(risk, incidences, commit);
    return risk;
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

  public BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties,
    boolean commit, int incidences)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getRiskStoragePath(riskId);

    BinaryVersion currentVersion = storage.createBinaryVersion(binaryPath, properties);
    storage.revertBinaryVersion(binaryPath, versionId);

    notifyRiskCreatedOrUpdated(retrieveRisk(riskId), incidences, commit);
    return currentVersion;
  }

  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit) throws GenericException {
    try {
      riskIncidence.setId(IdUtils.createUUID());
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
      riskIncidence.setRiskId(riskIncidence.getRiskId());
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

  /***************** Format related *****************/
  /************************************************/

  public Format createFormat(Format format, boolean commit) throws GenericException {
    try {
      format.setId(IdUtils.createUUID());
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

  /***************** Notification related *****************/
  /**********************************************************/

  public Notification createNotification(final Notification notification, final NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {

    notification.setId(IdUtils.createUUID());
    notification.setAcknowledgeToken(IdUtils.createUUID());
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

  public Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {

    Notification notification = this.retrieveNotification(notificationId);
    String ackToken = token.substring(0, 36);
    String emailToken = token.substring(36);

    if (notification.getAcknowledgeToken().equals(ackToken)) {
      for (String recipient : notification.getRecipientUsers()) {
        String recipientUUID = IdUtils.createUUID(recipient);
        if (recipientUUID.equals(emailToken)) {
          DateFormat df = DateFormat.getDateTimeInstance();
          String ackDate = df.format(new Date());
          notification.addAcknowledgedUser(recipient, ackDate);
          notification.setAcknowledged(true);
          this.updateNotification(notification);
        }
      }
    }

    return notification;
  }

  /***************** DIP related *****************/
  /**********************************************************/

  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return listDIPFilesUnder(f.getDipId(), f.getPath(), f.getId(), recursive);
  }

  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(filePath, recursive);
    return ResourceParseUtils.convert(getStorage(), iterable, DIPFile.class);
  }

  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final StoragePath storagePath = ModelUtils.getDIPDataStoragePath(dipId);
    CloseableIterable<OptionalWithCause<DIPFile>> ret;
    try {
      final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(storagePath, recursive);
      ret = ResourceParseUtils.convert(getStorage(), iterable, DIPFile.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getDIPStoragePath(dipId));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<>();
    }

    return ret;

  }

  private void createDIPMetadata(DIP dip, StoragePath storagePath) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    String json = JsonUtils.getJsonFromObject(dip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_DIP_METADATA_FILENAME);
    boolean asReference = false;
    storage.createBinary(metadataStoragePath, new StringContentPayload(json), asReference);
  }

  private void updateDIPMetadata(DIP dip, StoragePath storagePath)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    String json = JsonUtils.getJsonFromObject(dip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_DIP_METADATA_FILENAME);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(json), asReference, createIfNotExists);
  }

  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    try {
      Directory directory;

      if (StringUtils.isNotBlank(dip.getId())) {
        try {
          directory = storage
            .createDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP, dip.getId()));
        } catch (AlreadyExistsException | GenericException | AuthorizationDeniedException e) {
          directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP));
          dip.setId(directory.getStoragePath().getName());
        }
      } else {
        directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP));
        dip.setId(directory.getStoragePath().getName());
      }

      dip.setDateCreated(new Date());
      dip.setLastModified(new Date());
      createDIPMetadata(dip, directory.getStoragePath());

      if (notify) {
        notifyDIPCreated(dip, false);
      }

      return dip;
    } catch (NotFoundException | RequestNotValidException | AlreadyExistsException e) {
      LOGGER.error("Error creating DIP in storage", e);
      throw new GenericException(e);
    }
  }

  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      dip.setLastModified(new Date());
      updateDIPMetadata(dip, DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP, dip.getId()));
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error updating DIP in storage", e);
      throw new GenericException(e);
    }

    notifyDIPUpdated(dip, false);
    return dip;
  }

  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      // deleting external service if existing
      DIP dip = retrieveDIP(dipId);
      OptionalWithCause<String> deleteURL = DIPUtils.getCompleteDeleteExternalURL(dip);
      Optional<String> httpMethod = DIPUtils.getDeleteMethod(dip);
      if (deleteURL.isPresent() && httpMethod.isPresent()) {
        String url = deleteURL.get();
        Optional<Pair<String, String>> credentials = DIPUtils.getDeleteCredentials(dip);
        String method = httpMethod.get();
        HTTPUtility.doMethod(url, method, credentials);
      }

      StoragePath dipPath = ModelUtils.getDIPStoragePath(dipId);
      storage.deleteResource(dipPath);
      notifyDIPDeleted(dipId, false);
    } catch (RequestNotValidException e) {
      LOGGER.error("Error deleting DIP", e);
      throw new GenericException(e);
    }
  }

  public DIP retrieveDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    InputStream inputStream = null;
    DIP ret;
    try {
      StoragePath dipPath = ModelUtils.getDIPMetadataStoragePath(dipId);
      Binary binary = storage.getBinary(dipPath);
      inputStream = binary.getContent().createInputStream();
      ret = JsonUtils.getObjectFromJson(inputStream, DIP.class);
    } catch (IOException | RequestNotValidException e) {
      throw new GenericException("Error reading DIP", e);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    return ret;
  }

  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    DIPFile file = ResourceParseUtils.convertResourceToDIPFile(createdBinary);
    file.setUUID(IdUtils.getDIPFileId(file));
    file.setSize(size);

    if (notify) {
      notifyDIPFileCreated(file);
    }

    return file;
  }

  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Directory createdDirectory = storage.createDirectory(DefaultStoragePath.parse(filePath, dirName));
    DIPFile file = ResourceParseUtils.convertResourceToDIPFile(createdDirectory);

    if (notify) {
      notifyDIPFileCreated(file);
    }

    return file;
  }

  public DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    DIPFile file;
    boolean asReference = false;

    StoragePath oldFilePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, oldFileId);
    storage.deleteResource(oldFilePath);

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Binary binary = storage.createBinary(filePath, contentPayload, asReference);
    file = ResourceParseUtils.convertResourceToDIPFile(binary);

    if (notify) {
      notifyDIPFileDeleted(dipId, directoryPath, oldFileId);
      notifyDIPFileCreated(file);
    }

    return file;
  }

  public void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    storage.deleteResource(filePath);
    if (notify) {
      notifyDIPFileDeleted(dipId, directoryPath, fileId);
    }
  }

  public DIPFile retrieveDIPFile(String dipId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DIPFile file;
    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    Class<? extends Entity> entity = storage.getEntity(filePath);

    if (entity.equals(Binary.class) || entity.equals(DefaultBinary.class)) {
      Binary binary = storage.getBinary(filePath);
      file = ResourceParseUtils.convertResourceToDIPFile(binary);
    } else {
      Directory directory = storage.getDirectory(filePath);
      file = ResourceParseUtils.convertResourceToDIPFile(directory);
    }

    return file;
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
    boolean asReference = false;
    StoragePath filePath = ModelUtils.getDocumentationStoragePath(aipId, representationId, directoryPath, fileId);
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    return ResourceParseUtils.convertResourceToFile(createdBinary);
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
    boolean asReference = false;
    StoragePath filePath = ModelUtils.getSchemaStoragePath(aipId, representationId, directoryPath, fileId);
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    return ResourceParseUtils.convertResourceToFile(createdBinary);
  }

  private CloseableIterable<OptionalWithCause<Representation>> listRepresentations()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<AIP>> aips = listAIPs();

    return CloseableIterables.concat(aips, aip -> {
      if (aip.isPresent()) {
        List<Representation> representations = aip.get().getRepresentations();
        return CloseableIterables
          .fromList(representations.stream().map(rep -> OptionalWithCause.of(rep)).collect(Collectors.toList()));
      } else {
        return CloseableIterables.empty();
      }
    });
  }

  private CloseableIterable<OptionalWithCause<File>> listFiles()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    CloseableIterable<OptionalWithCause<Representation>> representations = listRepresentations();

    return CloseableIterables.concat(representations, rep -> {
      if (rep.isPresent()) {
        Representation representation = rep.get();
        try {
          return listFilesUnder(representation.getAipId(), representation.getId(), true);
        } catch (RODAException e) {
          LOGGER.error("Error listing files under representation: {}", representation.getId(), e);
          return CloseableIterables.empty();
        }
      } else {
        return CloseableIterables.empty();
      }
    });

  }

  private CloseableIterable<OptionalWithCause<DIPFile>> listDIPFiles() throws RODAException {
    CloseableIterable<OptionalWithCause<DIP>> dips = list(DIP.class);

    return CloseableIterables.concat(dips, odip -> {
      CloseableIterable<?> dipFiles = CloseableIterables.empty();

      if (odip.isPresent()) {
        DIP dip = odip.get();
        try {
          dipFiles = listDIPFilesUnder(dip.getId(), true);
        } catch (RODAException e) {
          LOGGER.error("Error getting DIP files under a DIP " + dip.getId());
        }
      }

      return (CloseableIterable<OptionalWithCause<DIPFile>>) dipFiles;
    });

  }

  public <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object) {
    return LiteRODAObjectFactory.get(object);
  }

  public <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject) {
    return LiteRODAObjectFactory.get(this, liteRODAObject);
  }

  public TransferredResource retrieveTransferredResource(String fullPath) {
    TransferredResourcesScanner transferredResourcesScanner = RodaCoreFactory.getTransferredResourcesScanner();
    return transferredResourcesScanner.instantiateTransferredResource(Paths.get(fullPath),
      transferredResourcesScanner.getBasePath());
  }

  @SuppressWarnings("unchecked")
  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<T>> list(Class<T> objectClass)
    throws RODAException {
    CloseableIterable<? extends OptionalWithCause<?>> ret;

    if (Representation.class.equals(objectClass)) {
      ret = listRepresentations();
    } else if (File.class.equals(objectClass)) {
      ret = listFiles();
    } else if (TransferredResource.class.equals(objectClass)) {
      ret = LiteRODAObjectFactory.transformFromLite(this,
        RodaCoreFactory.getTransferredResourcesScanner().listTransferredResources());
    } else if (RODAMember.class.equals(objectClass)) {
      ret = listMembers();
    } else if (LogEntry.class.equals(objectClass)) {
      ret = listLogEntries();
    } else if (DIPFile.class.equals(objectClass)) {
      ret = listDIPFiles();
    } else if (PreservationMetadata.class.equals(objectClass)) {
      ret = listPreservationMetadata();
    } else if (DescriptiveMetadata.class.equals(objectClass)) {
      ret = listDescriptiveMetadata();
    } else if (Report.class.equals(objectClass)) {
      ret = ResourceParseUtils.convert(getStorage(), listReportResources(), objectClass);
    } else {
      StoragePath containerPath = ModelUtils.getContainerPath(objectClass);
      final CloseableIterable<Resource> resourcesIterable = storage.listResourcesUnderContainer(containerPath, false);
      ret = ResourceParseUtils.convert(getStorage(), resourcesIterable, objectClass);
    }

    return (CloseableIterable<OptionalWithCause<T>>) ret;
  }

  public <T extends IsRODAObject> CloseableIterable<OptionalWithCause<LiteRODAObject>> listLite(Class<T> objectClass)
    throws RODAException {
    CloseableIterable<OptionalWithCause<LiteRODAObject>> ret;

    if (Representation.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), ResourceListUtils.listRepresentationResources(storage),
        objectClass);
    } else if (File.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), ResourceListUtils.listFileResources(storage), objectClass);
    } else if (TransferredResource.class.equals(objectClass)) {
      ret = RodaCoreFactory.getTransferredResourcesScanner().listTransferredResources();
    } else if (RODAMember.class.equals(objectClass)) {
      ret = LiteRODAObjectFactory.transformIntoLite(listMembers());
    } else if (LogEntry.class.equals(objectClass)) {
      ret = LiteRODAObjectFactory.transformIntoLite(listLogEntries());
    } else if (DIPFile.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), ResourceListUtils.listDIPFileResources(storage), objectClass);
    } else if (PreservationMetadata.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), ResourceListUtils.listPreservationMetadataResources(storage),
        objectClass);
    } else if (DescriptiveMetadata.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), ResourceListUtils.listDescriptiveMetadataResources(storage),
        objectClass);
    } else if (Report.class.equals(objectClass)) {
      ret = ResourceParseUtils.convertLite(getStorage(), listReportResources(), objectClass);
    } else {
      StoragePath containerPath = ModelUtils.getContainerPath(objectClass);
      final CloseableIterable<Resource> resourcesIterable = storage.listResourcesUnderContainer(containerPath, false);
      ret = ResourceParseUtils.convertLite(getStorage(), resourcesIterable, objectClass);
    }

    return ret;
  }

  private CloseableIterable<Resource> listReportResources() throws RODAException {
    CloseableIterable<Resource> resources = storage
      .listResourcesUnderContainer(ModelUtils.getContainerPath(Report.class), true);
    return CloseableIterables.filter(resources, resource -> !(resource instanceof Directory));
  }

  private CloseableIterable<OptionalWithCause<RODAMember>> listMembers() {
    List<OptionalWithCause<RODAMember>> members = new ArrayList<>();

    try {
      List<OptionalWithCause<RODAMember>> users = listUsers().stream()
        .map(user -> OptionalWithCause.of((RODAMember) user)).collect(Collectors.toList());
      members.addAll(users);

      List<OptionalWithCause<RODAMember>> groups = listGroups().stream()
        .map(group -> OptionalWithCause.of((RODAMember) group)).collect(Collectors.toList());
      members.addAll(groups);
    } catch (GenericException e) {
      LOGGER.error("Error getting user and/or groups list");
    }

    return CloseableIterables.fromList(members);
  }

  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries() {
    return listLogEntries(0);
  }

  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex) {
    boolean recursive = false;
    CloseableIterable<OptionalWithCause<LogEntry>> inStorage = null;
    CloseableIterable<OptionalWithCause<LogEntry>> notStorage = null;

    try {
      final CloseableIterable<Resource> actionLogs = getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), recursive);

      if (daysToIndex > 0) {
        inStorage = new LogEntryStorageIterable(
          CloseableIterables.filter(actionLogs, r -> isToIndex(r.getStoragePath().getName(), daysToIndex)));
      } else {
        inStorage = new LogEntryStorageIterable(actionLogs);
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Error getting action log from storage", e);
    }

    try {
      if (daysToIndex > 0) {
        notStorage = new LogEntryFileSystemIterable(RodaCoreFactory.getLogPath(),
          p -> isToIndex(p.getFileName().toString(), daysToIndex));
      } else {
        notStorage = new LogEntryFileSystemIterable(RodaCoreFactory.getLogPath());
      }

    } catch (IOException e) {
      LOGGER.error("Error getting action log from storage", e);
    }

    return CloseableIterables.concat(inStorage, notStorage);
  }

  private boolean isToIndex(String fileName, int daysToIndex) {
    boolean isToIndex = false;
    String fileNameWithoutExtension = fileName.replaceFirst(".log$", "");

    try {
      DateTime dt = LOG_NAME_DATE_FORMAT.parseDateTime(fileNameWithoutExtension);

      if (dt.plusDays(daysToIndex + 1).isAfterNow()) {
        isToIndex = true;
      }

    } catch (IllegalArgumentException | UnsupportedOperationException e) {
      LOGGER.error("Could not parse log file name", e);
    }
    return isToIndex;
  }

  public boolean hasObjects(Class<? extends IsRODAObject> objectClass) {
    try {
      if (LogEntry.class.equals(objectClass) || RODAMember.class.equals(objectClass)
        || TransferredResource.class.equals(objectClass) || IndexedPreservationAgent.class.equals(objectClass)
        || IndexedAIP.class.equals(objectClass)) {
        return true;
      } else {
        StoragePath storagePath = ModelUtils.getContainerPath(objectClass);
        try {
          return RodaCoreFactory.getStorageService().countResourcesUnderContainer(storagePath, false).intValue() > 0;
        } catch (NotFoundException e) {
          // TODO 20160913 hsilva: we want to handle the non-existence of a
          // container
        }
      }

      return false;
    } catch (RODAException e) {
      return false;
    }
  }

  public boolean checkObjectPermission(String username, String permissionType, String objectClass, String id)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    if (UserUtility.isAdministrator(username)) {
      return true;
    }

    boolean hasPermission = false;
    Set<String> groups = this.retrieveUser(username).getGroups();

    try {
      if (DIP.class.getName().equals(objectClass)) {
        DIP dip = this.retrieveDIP(id);
        Permissions permissions = dip.getPermissions();
        Set<PermissionType> userPermissions = permissions.getUserPermissions(username);

        for (String group : groups) {
          userPermissions.addAll(permissions.getGroupPermissions(group));
        }

        PermissionType type = PermissionType.valueOf(permissionType.toUpperCase());
        hasPermission = userPermissions.contains(type);
      } else if (AIP.class.getName().equals(objectClass)) {
        AIP aip = this.retrieveAIP(id);
        Permissions permissions = aip.getPermissions();
        Set<PermissionType> userPermissions = permissions.getUserPermissions(username);

        for (String group : groups) {
          userPermissions.addAll(permissions.getGroupPermissions(group));
        }

        PermissionType type = PermissionType.valueOf(permissionType.toUpperCase());
        hasPermission = userPermissions.contains(type);
      } else {
        throw new RequestNotValidException(objectClass + " permission verification is not supported");
      }
    } catch (IllegalArgumentException e) {
      throw new RequestNotValidException(e);
    }

    return hasPermission;
  }

}
