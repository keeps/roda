/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.roda.core.common.DownloadUtils.ZIP_FILE_NAME_EXTENSION;
import static org.roda.core.common.DownloadUtils.ZIP_MEDIA_TYPE;
import static org.roda.core.common.DownloadUtils.ZIP_PATH_DELIMITER;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.JwtUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.ReturnWithExceptionsWrapper;
import org.roda.core.common.dips.DIPUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.common.SecureString;
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
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.utils.URNUtils;
import org.roda.core.data.utils.XMLUtils;
import org.roda.core.data.v2.ConsumesOutputStream;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeyStatus;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationAIPEntry;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.ShallowFiles;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.LinkingIdentifier;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.ip.metadata.TechnicalMetadata;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.events.EventsManager;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.iterables.LogEntryFileSystemIterable;
import org.roda.core.model.iterables.LogEntryStorageIterable;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.model.utils.ResourceListUtils;
import org.roda.core.model.utils.ResourceParseUtils;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.ingest.PermissionUtils;
import org.roda.core.protocols.Protocol;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryConsumesOutputStream;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.EmptyClosableIterable;
import org.roda.core.storage.Entity;
import org.roda.core.storage.ExternalFileManifestContentPayload;
import org.roda.core.storage.JsonContentPayload;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.storage.utils.RODAInstanceUtils;
import org.roda.core.util.HTTPUtility;
import org.roda.core.util.IdUtils;
import org.roda.core.util.RESTClientUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that "relates" Model & Storage
 *
 *
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class DefaultModelService implements ModelService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelService.class);

  private static final DateTimeFormatter LOG_NAME_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final boolean FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA = false;
  private final StorageService storage;
  private final EventsManager eventsManager;
  private final NodeType nodeType;
  private String instanceId = "";
  private Object logFileLock = new Object();

  private long entryLogLineNumber = -1;

  // Observer
  private final List<ModelObserver> observers;

  public DefaultModelService(StorageService storage, EventsManager eventsManager, NodeType nodeType,
    String instanceId) {
    this.storage = storage;
    this.eventsManager = eventsManager;
    this.nodeType = nodeType;
    this.instanceId = instanceId;
    this.observers = new ArrayList<>();

    if (RodaCoreFactory.checkIfWriteIsAllowed(nodeType)) {
      ensureAllContainersExist();
      ensureAllDirectoriesExist();
    }
  }

  private void ensureAllContainersExist() {
    try {
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_AIP);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_PRESERVATION);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_ACTIONLOG);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_JOB);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_JOB_REPORT);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_RISK);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_ACCESS_KEYS);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_RISK_INCIDENCE);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DIP);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_REPRESENTATION_INFORMATION);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DISPOSAL_SCHEDULE);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DISPOSAL_HOLD);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DISPOSAL_CONFIRMATION);
      createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_DISPOSAL_RULE);
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

  private void ensureAllDirectoriesExist() {
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

  @Override
  public StorageService getStorage() {
    return storage;
  }

  /********************
   * AIP related
   ********************/

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

  private AIP updateAIPMetadata(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return updateAIPMetadata(aip, updatedBy, ModelUtils.getAIPStoragePath(aip.getId()));
  }

  private AIP updateAIPMetadata(AIP aip, String updatedBy, StoragePath storagePath)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    aip.setUpdatedOn(new Date());
    if (updatedBy != null) {
      aip.setUpdatedBy(updatedBy);
    }
    String json = JsonUtils.getJsonFromObject(aip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_AIP_METADATA_FILENAME);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(json), asReference, createIfNotExists,
      false, null);
    return aip;
  }

  @Override
  public CloseableIterable<OptionalWithCause<AIP>> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    final CloseableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getAIPContainerPath(), false);
    return ResourceParseUtils.convert(getStorage(), resourcesIterable, AIP.class);
  }

  @Override
  public AIP retrieveAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
  }

  /**
   * Create a new AIP
   *
   * @param aipId
   *          Suggested ID for the AIP, if <code>null</code> then an ID will be
   *          automatically generated. If ID cannot be allowed because it already
   *          exists or is not valid, another ID will be provided.
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
  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    String createdBy) throws RequestNotValidException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, ValidationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    // XXX possible optimization would be to allow move between storage
    ModelService sourceModelService = new DefaultModelService(sourceStorage, eventsManager, nodeType, instanceId);
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

      // Instance Id Management
      aip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

      if (notify) {
        notifyAipCreated(aip).failOnError();
      }
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, List<String> ingestSIPIds,
    String ingestJobId, boolean notify, String createdBy, boolean isGhost, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIPState state = AIPState.ACTIVE;
    Directory directory;
    if (aipId != null) {
      directory = storage.createDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId));
    } else {
      directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    }
    String id = directory.getStoragePath().getName();

    User user = this.retrieveUser(createdBy);
    Permissions inheritedPermissions = new Permissions();

    if (parentId != null) {
      inheritedPermissions = this.retrieveAIP(parentId).getPermissions();
    }

    Permissions finalPermissions = PermissionUtils.calculatePermissions(user, Optional.of(inheritedPermissions),
      Optional.of(permissions));

    AIP aip = new AIP(id, parentId, type, state, finalPermissions, createdBy);

    aip.setGhost(isGhost);
    aip.setIngestSIPIds(ingestSIPIds);
    aip.setIngestJobId(ingestJobId);

    // Instance Id Management
    aip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip).failOnError();
    }

    return aip;
  }

  @Override
  public AIP createAIP(String parentId, String type, Permissions permissions, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    AIPState state = AIPState.ACTIVE;
    boolean notify = true;
    return createAIP(state, parentId, type, permissions, notify, createdBy, aipId);
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String createdBy,
    String aipId) throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    boolean notify = true;
    return createAIP(state, parentId, type, permissions, notify, createdBy, aipId);
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, boolean notify,
    String createdBy, String aipId) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    Directory directory;
    if (aipId != null) {
      directory = storage.createDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId));
    } else {
      directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    }

    String id = directory.getStoragePath().getName();

    User user = this.retrieveUser(createdBy);
    Permissions inheritedPermissions = new Permissions();

    if (parentId != null) {
      inheritedPermissions = this.retrieveAIP(parentId).getPermissions();
    }

    Permissions finalPermissions = PermissionUtils.calculatePermissions(user, Optional.of(inheritedPermissions),
      Optional.of(permissions));

    AIP aip = new AIP(id, parentId, type, state, finalPermissions, createdBy);
    // Instance Id Management
    aip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip).failOnError();
    }

    return aip;
  }

  @Override
  public AIP createAIP(AIPState state, String parentId, String type, Permissions permissions, String ingestSIPUUID,
    List<String> ingestSIPIds, String ingestJobId, boolean notify, String createdBy, String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    Directory directory;
    if (aipId != null) {
      directory = storage.createDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP, aipId));
    } else {
      directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));
    }

    String id = directory.getStoragePath().getName();

    User user = this.retrieveUser(createdBy);
    Permissions inheritedPermissions = new Permissions();
    if (parentId != null) {
      inheritedPermissions = this.retrieveAIP(parentId).getPermissions();
    }

    Permissions finalPermissions = PermissionUtils.calculatePermissions(user, Optional.of(inheritedPermissions),
      Optional.of(permissions));

    AIP aip = new AIP(id, parentId, type, state, finalPermissions, createdBy).setIngestSIPIds(ingestSIPIds)
      .setIngestJobId(ingestJobId).setIngestSIPUUID(ingestSIPUUID);
    // Instance Id Management
    aip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    createAIPMetadata(aip);

    if (notify) {
      notifyAipCreated(aip).failOnError();
    }

    return aip;
  }

  @Override
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String createdBy)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    return createAIP(aipId, sourceStorage, sourcePath, true, createdBy);
  }

  @Override
  public AIP notifyAipCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipCreated(aip).failOnError();
    return aip;
  }

  @Override
  public AIP notifyAipUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    notifyAipUpdated(aip).failOnError();
    return aip;
  }

  private Permissions addParentPermissions(Permissions permissions, String parentId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    if (parentId != null) {
      AIP parentAIP = this.retrieveAIP(parentId);
      Set<String> parentGroupNames = parentAIP.getPermissions().getGroupnames();
      Set<String> parentUsernames = parentAIP.getPermissions().getUsernames();
      Set<String> groupnames = permissions.getGroupnames();
      Set<String> usernames = permissions.getUsernames();

      for (String user : parentUsernames) {
        if (!usernames.contains(user)) {
          permissions.setUserPermissions(user, parentAIP.getPermissions().getUserPermissions(user));
        }
      }

      for (String group : parentGroupNames) {
        if (!groupnames.contains(group)) {
          permissions.setGroupPermissions(group, parentAIP.getPermissions().getGroupPermissions(group));
        }
      }
    }

    return permissions;
  }

  @Override
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new DefaultModelService(sourceStorage, eventsManager, nodeType, instanceId);
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
      notifyAipUpdated(aip).failOnError();
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  @Override
  public AIP destroyAIP(AIP aip, String updatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    aip.setState(AIPState.DESTROYED);
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipDestroyed(updatedAIP).failOnError();
    return aip;
  }

  @Override
  public AIP updateAIP(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipUpdated(updatedAIP).failOnError();
    return aip;
  }

  @Override
  public AIP updateAIPState(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipStateUpdated(updatedAIP).failOnError();
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    return aip;
  }

  @Override
  public AIP updateAIPInstanceId(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    aip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);

    notifyAipInstanceIdUpdated(updatedAIP).failOnError();
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    return aip;
  }

  @Override
  public AIP moveAIP(String aipId, String parentId, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (aipId.equals(parentId)) {
      throw new RequestNotValidException("Cannot set itself as its parent: " + aipId);
    }

    // TODO ADD RESTRICTIONS
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    String oldParentId = aip.getParentId();
    aip.setParentId(parentId);
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);

    notifyAipMoved(updatedAIP, oldParentId, parentId).failOnError();
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();

    return aip;
  }

  @Override
  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath aipPath = ModelUtils.getAIPStoragePath(aipId);
    storage.deleteResource(aipPath);
    notifyAipDeleted(aipId).failOnError();
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

  @Override
  public void changeAIPType(String aipId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP aip = retrieveAIP(aipId);
    aip.setType(type);
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipUpdated(updatedAIP).failOnError();
  }

  /********************************
   * Descriptive Metadata related
   ********************************/

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    return retrieveDescriptiveMetadataBinary(aipId, null, descriptiveMetadataId);
  }

  @Override
  public Binary retrieveDescriptiveMetadataBinary(String aipId, String representationId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    return storage.getBinary(binaryPath);
  }

  @Override
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrieveDescriptiveMetadata(aipId, null, descriptiveMetadataId);
  }

  @Override
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

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy,
    boolean notify) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    return createDescriptiveMetadata(aipId, null, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy, notify);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, String descriptiveMetadataVersion, String createdBy)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    return createDescriptiveMetadata(aipId, null, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy, true);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return createDescriptiveMetadata(aipId, representationId, descriptiveMetadataId, payload, descriptiveMetadataType,
      descriptiveMetadataVersion, createdBy, true);
  }

  @Override
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload payload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, String createdBy, boolean notify) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    boolean asReference = false;

    storage.createBinary(binaryPath, payload, asReference);
    DescriptiveMetadata descriptiveMetadata = new DescriptiveMetadata(descriptiveMetadataId, aipId, representationId,
      descriptiveMetadataType, descriptiveMetadataVersion);

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.addDescriptiveMetadata(descriptiveMetadata);
    AIP updatedAIP = updateAIPMetadata(aip, createdBy);
    notifyAipUpdated(updatedAIP).failOnError();

    if (notify) {
      notifyDescriptiveMetadataCreated(descriptiveMetadata).failOnError();
    }

    return descriptiveMetadata;
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType, String descriptiveMetadataVersion,
    Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return updateDescriptiveMetadata(aipId, null, descriptiveMetadataId, descriptiveMetadataPayload,
      descriptiveMetadataType, descriptiveMetadataVersion, properties, updatedBy);
  }

  @Override
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String representationId,
    String descriptiveMetadataId, ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType,
    String descriptiveMetadataVersion, Map<String, String> properties, String updatedBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadata ret;

    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    // Update
    storage.updateBinaryContent(binaryPath, descriptiveMetadataPayload, asReference, createIfNotExists, true,
      properties);

    // set descriptive metadata type
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    ret = updateDescriptiveMetadata(aip, representationId, descriptiveMetadataId, descriptiveMetadataType,
      descriptiveMetadataVersion);

    AIP updateAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipUpdated(updateAIP).failOnError();
    notifyDescriptiveMetadataUpdated(ret).failOnError();

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

  @Override
  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId, String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    deleteDescriptiveMetadata(aipId, null, descriptiveMetadataId, deletedBy);
  }

  @Override
  public void deleteDescriptiveMetadata(String aipId, String representationId, String descriptiveMetadataId,
    String deletedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);

    storage.deleteResource(binaryPath);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    deleteDescriptiveMetadata(aip, representationId, descriptiveMetadataId);

    AIP updateAIP = updateAIPMetadata(aip, deletedBy);
    notifyAipUpdated(updateAIP).failOnError();
    notifyDescriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataId).failOnError();
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

  @Override
  public CloseableIterable<BinaryVersion> listDescriptiveMetadataVersions(String aipId, String representationId,
    String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);
    return storage.listBinaryVersions(binaryPath);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String descriptiveMetadataId, String versionId,
    Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return revertDescriptiveMetadataVersion(aipId, null, descriptiveMetadataId, versionId, properties);
  }

  @Override
  public BinaryVersion revertDescriptiveMetadataVersion(String aipId, String representationId,
    String descriptiveMetadataId, String versionId, Map<String, String> properties)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataStoragePath(aipId, representationId,
      descriptiveMetadataId);

    Binary updatedBinary = storage.revertBinaryVersion(binaryPath, versionId, properties);
    BinaryVersion beforeRevertVersion = storage.getBinaryVersion(binaryPath, updatedBinary.getPreviousVersionId());

    notifyDescriptiveMetadataUpdated(retrieveDescriptiveMetadata(aipId, representationId, descriptiveMetadataId))
      .failOnError();

    return beforeRevertVersion;
  }

  @Override
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

  @Override
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

  @Override
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

  /**************************
   * Representation related
   **************************/

  @Override
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

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy, List<String> representationState) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    Representation representation = new Representation(representationId, aipId, original, type);
    representation.setCreatedBy(createdBy);
    representation.setUpdatedBy(createdBy);
    representation.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    representation.setRepresentationStates(representationState);

    StoragePath directoryPath = ModelUtils.getRepresentationStoragePath(aipId, representationId);
    storage.createDirectory(directoryPath);

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.getRepresentations().add(representation);
    AIP updatedAIP = updateAIPMetadata(aip, createdBy);

    if (notify) {
      notifyRepresentationCreated(representation).failOnError();
      notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    }

    return representation;
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    boolean notify, String createdBy) throws RequestNotValidException, GenericException, NotFoundException,
    AuthorizationDeniedException, AlreadyExistsException {
    return createRepresentation(aipId, representationId, original, type, notify, createdBy, Collections.emptyList());
  }

  @Override
  public Representation createRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, boolean justData, String createdBy)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    Representation representation;

    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
    representation.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    aip.getRepresentations().add(representation);
    AIP updatedAIP = updateAIPMetadata(aip, createdBy);

    notifyRepresentationCreated(representation).failOnError();
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    return representation;
  }

  @Override
  public Representation updateRepresentationInfo(Representation representation) throws GenericException {
    notifyRepresentationUpdated(representation).failOnError();
    return representation;
  }

  @Override
  public void changeRepresentationType(String aipId, String representationId, String type, String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP aip = retrieveAIP(aipId);
    Iterator<Representation> it = aip.getRepresentations().iterator();

    while (it.hasNext()) {
      Representation representation = it.next();
      if (representation.getId().equals(representationId)) {
        representation.setType(type);
        representation.setUpdatedOn(new Date());
        representation.setUpdatedBy(updatedBy);
        notifyRepresentationUpdated(representation).failOnError();
        break;
      }
    }

    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();
  }

  @Override
  public void changeRepresentationShallowFileFlag(String aipId, String representationId, boolean hasShallowFiles,
    String updatedBy, boolean notify)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    AIP aip = retrieveAIP(aipId);
    Iterator<Representation> it = aip.getRepresentations().iterator();

    while (it.hasNext()) {
      Representation representation = it.next();
      if (representation.getId().equals(representationId)) {
        representation.setHasShallowFiles(hasShallowFiles);
        representation.setUpdatedOn(new Date());
        representation.setUpdatedBy(updatedBy);
        if (notify) {
          notifyRepresentationUpdated(representation).failOnError();
        }
        break;
      }
    }

    aip.setHasShallowFiles(hasShallowFiles);
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);

    if (notify) {
      notifyAipUpdated(updatedAIP).failOnError();
    }
  }

  @Override
  public void changeRepresentationStates(String aipId, String representationId, List<String> newStates,
    String updatedBy)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
      notifyRepresentationUpdated(representation.get()).failOnError();

      AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
      notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    }
  }

  private void changeRepresentationUpdateOn(String aipId, String representationId, String updatedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
      representation.get().setUpdatedOn(new Date());
      representation.get().setUpdatedBy(updatedBy);
      AIP updatedAIP = updateAIPMetadata(aip, updatedBy);

      if (notify) {
        notifyRepresentationUpdatedOnChanged(representation.get()).failOnError();
        notifyAipUpdatedOnChanged(updatedAIP).failOnError();
      }
    }
  }

  @Override
  public Representation updateRepresentation(String aipId, String representationId, boolean original, String type,
    StorageService sourceStorage, StoragePath sourcePath, String updatedBy) throws RequestNotValidException,
    NotFoundException, GenericException, AuthorizationDeniedException, ValidationException {
    Representation representation;

    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
    notifyRepresentationUpdated(representation).failOnError();
    return representation;
  }

  @Override
  public void deleteRepresentation(String aipId, String representationId, String username)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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

    AIP updatedAIP = updateAIPMetadata(aip, username);
    notifyAipUpdatedOnChanged(updatedAIP).failOnError();

    notifyRepresentationDeleted(aipId, representationId).failOnError();
  }

  @Override
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

  @Override
  public CloseableIterable<OptionalWithCause<File>> listExternalFilesUnder(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(file.getAipId(), file.getRepresentationId(), file.getPath(),
      file.getId());

    CloseableIterable<OptionalWithCause<File>> ret;
    try {
      final CloseableIterable<Resource> iterable = storage.listResourcesUnderFile(filePath, true);
      ret = ResourceParseUtils.convert(getStorage(), iterable, File.class);
    } catch (NotFoundException e) {
      // check if AIP exists
      storage.getDirectory(ModelUtils.getAIPStoragePath(file.getAipId()));
      // if no exception was sent by above method, return empty list
      ret = new EmptyClosableIterable<>();
    }

    return ret;
  }

  /*****************
   * File related
   *****************/

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(File f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return listFilesUnder(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId(), recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<File>> listFilesUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(filePath, recursive);
    return ResourceParseUtils.convert(getStorage(), iterable, File.class);
  }

  @Override
  public Long getExternalFilesTotalSize(File file)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    Long sizeInBytes = 0L;
    StoragePath storagePath = ModelUtils.getFileStoragePath(file);
    try (CloseableIterable<Resource> resources = getStorage().listResourcesUnderFile(storagePath, false)) {
      for (Resource resource : resources) {
        if (resource instanceof DefaultBinary) {
          ContentPayload content = ((DefaultBinary) resource).getContent();
          if (content instanceof JsonContentPayload) {
            ShallowFile shallowFile = JsonUtils.getObjectFromJson(content.createInputStream(), ShallowFile.class);
            sizeInBytes += shallowFile.getSize();
          }
        }
      }
    }
    return sizeInBytes;
  }

  @Override
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

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return createFile(aipId, representationId, directoryPath, fileId, contentPayload, createdBy, true);
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    boolean asReference = false;
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    if (contentPayload instanceof ExternalFileManifestContentPayload) {
      asReference = true;
    }
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    File file = ResourceParseUtils.convertResourceToFile(createdBinary);
    file.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    changeRepresentationUpdateOn(aipId, representationId, createdBy, notify);

    if (notify) {
      notifyFileCreated(file).failOnError();
    }

    return file;
  }

  @Override
  public File createFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String dirName, String createdBy, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    final Directory createdDirectory = storage.createDirectory(DefaultStoragePath.parse(filePath, dirName));
    File file = ResourceParseUtils.convertResourceToFile(createdDirectory);
    file.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    changeRepresentationUpdateOn(aipId, representationId, createdBy, notify);

    if (notify) {
      notifyFileCreated(file).failOnError();
    }

    return file;
  }

  @Override
  public File updateFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload, boolean createIfNotExists, String updatedBy, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    boolean asReference = false;
    if (contentPayload instanceof ExternalFileManifestContentPayload) {
      asReference = true;
    }

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    storage.updateBinaryContent(filePath, contentPayload, asReference, createIfNotExists, false, null);
    Binary binaryUpdated = storage.getBinary(filePath);
    File file = ResourceParseUtils.convertResourceToFile(binaryUpdated);

    changeRepresentationUpdateOn(aipId, representationId, updatedBy, notify);

    if (notify) {
      notifyFileUpdated(file).failOnError();
    }

    return file;
  }

  @Override
  public File updateFile(File file, ContentPayload contentPayload, boolean createIfNotExists, String updatedBy,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return updateFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), contentPayload,
      createIfNotExists, updatedBy, notify);
  }

  @Override
  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    storage.deleteResource(filePath);

    changeRepresentationUpdateOn(aipId, representationId, deletedBy, notify);

    if (notify) {
      notifyFileDeleted(aipId, representationId, directoryPath, fileId).failOnError();
    }
  }

  @Override
  public void deleteFile(File file, String deletedBy, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (file.isReference()) {
      StoragePath storagePath = ModelUtils.getFileStoragePath(file.getAipId(), file.getRepresentationId(),
        file.getPath(), RodaConstants.RODA_MANIFEST_EXTERNAL_FILES);
      Binary binary = getStorage().getBinary(storagePath);
      ContentPayload content = binary.getContent();
      if (content instanceof ExternalFileManifestContentPayload) {
        ShallowFiles shallowFiles = ((ExternalFileManifestContentPayload) content).getShallowFiles();
        shallowFiles.removeObject(IdUtils.getFileId(file));

        ContentPayload newContentPayload = new ExternalFileManifestContentPayload(shallowFiles);
        getStorage().updateBinaryContent(storagePath, newContentPayload, false, false, false, null);
        if (notify) {
          notifyFileDeleted(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId()).failOnError();
        }
      }
    } else {
      deleteFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId(), deletedBy, notify);
    }
  }

  @Override
  public File renameFolder(File folder, String newName, boolean reindexResources) throws AlreadyExistsException,
    GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath folderStoragePath = ModelUtils.getFileStoragePath(folder);

    if (storage.hasDirectory(folderStoragePath)) {
      List<String> newFolderPathList = new ArrayList<>();
      newFolderPathList.add(folderStoragePath.getContainerName());
      newFolderPathList.addAll(folderStoragePath.getDirectoryPath());
      newFolderPathList.add(newName);
      StoragePath newFileStoragePath = DefaultStoragePath.parse(newFolderPathList);
      storage.move(storage, folderStoragePath, newFileStoragePath);

      if (reindexResources) {
        notifyAipUpdated(folder.getAipId());
      }

      return retrieveFile(folder.getAipId(), folder.getRepresentationId(), folder.getPath(), newName);
    } else {
      throw new NotFoundException("Folder was moved or does not exist");
    }
  }

  @Override
  public File moveFile(File file, String newAipId, String newRepresentationId, List<String> newDirectoryPath,
    String newId, boolean reindexResources) throws AlreadyExistsException, GenericException, NotFoundException,
    RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath fileStoragePath = ModelUtils.getFileStoragePath(file);
    if (!storage.exists(fileStoragePath)) {
      Path filePath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), fileStoragePath);
      ShallowFile shallowFile = FSUtils.isResourcePresentOnManifestFile(filePath);
      if (shallowFile != null) {
        // TODO: Move shallow content
        throw new GenericException("Cannot move shallow content: " + shallowFile.getLocation());
      }
      throw new NotFoundException("File/folder '" + fileStoragePath.toString() + "' were moved or do not exist");
    }

    File newFile = new File(newId, newAipId, newRepresentationId, newDirectoryPath, file.isDirectory());
    StoragePath newFileStoragePath = ModelUtils.getFileStoragePath(newFile);

    storage.move(storage, fileStoragePath, newFileStoragePath);

    if (reindexResources) {
      notifyRepresentationUpdated(retrieveRepresentation(newAipId, newRepresentationId)).failOnError();
      if (!newAipId.equals(file.getAipId()) || !newRepresentationId.equals(file.getRepresentationId())) {
        notifyRepresentationUpdated(retrieveRepresentation(file.getAipId(), file.getRepresentationId())).failOnError();
      }
    }

    return newFile;
  }

  /***********************
   * Preservation related
   **********************
   * @return
   */

  @Override
  public PreservationMetadata createRepositoryEvent(PreservationEventType eventType, String eventDescription,
    PluginState outcomeState, String outcomeText, String outcomeDetail, String agentName, boolean notify,
    String eventId) {
    return createRepositoryEvent(eventType, eventDescription, null, null, outcomeState, outcomeText, outcomeDetail,
      agentName, notify, eventId);
  }

  @Override
  public PreservationMetadata createRepositoryEvent(PreservationEventType eventType, String eventDescription,
    List<LinkingIdentifier> sources, List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText,
    String outcomeDetail, String agentName, boolean notify, String eventId) {
    return createEvent(null, null, null, null, eventType, eventDescription, sources, targets, outcomeState, outcomeText,
      outcomeDetail, agentName, notify, eventId);
  }

  @Override
  public PreservationMetadata createUpdateAIPEvent(String aipId, String representationId, List<String> filePath,
    String fileId, PreservationEventType eventType, String eventDescription, PluginState outcomeState,
    String outcomeText, String outcomeDetail, String agentName, boolean notify, String eventId) {
    return createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, null, null, outcomeState,
      outcomeText, outcomeDetail, agentName, notify, eventId);
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, boolean notify, String eventId) {
    return createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources, targets,
      outcomeState, outcomeText, outcomeDetail, agentName, null, notify, eventId);
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeText, String outcomeDetail,
    String agentName, String agentRole, boolean notify, String eventId) {
    try {
      StringBuilder builder = new StringBuilder(outcomeText);
      if (StringUtils.isNotBlank(outcomeDetail) && !outcomeState.equals(PluginState.SUCCESS)) {
        builder.append("\n").append("The following reason has been reported by the user: ").append(agentName)
          .append("\n").append(outcomeDetail);
      }
      LinkingIdentifier linkingIdentifier = new LinkingIdentifier();
      linkingIdentifier.setValue(IdUtils.getUserAgentId(agentName, RODAInstanceUtils.getLocalInstanceIdentifier()));
      if (agentRole != null) {
        linkingIdentifier.getRoles().add(agentRole);
      }
      return createEvent(aipId, representationId, filePath, fileId, eventType, eventDescription, sources, targets,
        outcomeState, builder.toString(), "", Collections.singletonList(linkingIdentifier), agentName, notify, eventId);
    } catch (ValidationException | AlreadyExistsException | GenericException | NotFoundException
      | RequestNotValidException | AuthorizationDeniedException e1) {
      LOGGER.error("Could not create an event for: {}", eventDescription, e1);
      return null;
    }
  }

  @Override
  public PreservationMetadata createEvent(String aipId, String representationId, List<String> filePath, String fileId,
    PreservationEventType eventType, String eventDescription, List<LinkingIdentifier> sources,
    List<LinkingIdentifier> targets, PluginState outcomeState, String outcomeDetail, String outcomeExtension,
    List<LinkingIdentifier> agentIds, String username, boolean notify, String eventId)
    throws GenericException, ValidationException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (eventId == null) {
      eventId = IdUtils.createPreservationMetadataId(PreservationMetadataType.EVENT,
        RODAInstanceUtils.getLocalInstanceIdentifier());
    }
    ContentPayload premisEvent = PremisV3Utils.createPremisEventBinary(eventId, new Date(), eventType.toString(),
      eventDescription, sources, targets, outcomeState.toString(), outcomeDetail, outcomeExtension, agentIds);

    if (eventType.equals(PreservationEventType.DELETION)) {
      if (aipId != null && representationId == null) {
        return createPreservationMetadata(PreservationMetadataType.EVENT, eventId, null, null, null, null, premisEvent,
          username, notify);
      } else if (representationId != null && fileId == null) {
        return createPreservationMetadata(PreservationMetadataType.EVENT, eventId, aipId, null, null, null, premisEvent,
          username, notify);
      } else {
        return createPreservationMetadata(PreservationMetadataType.EVENT, eventId, aipId, representationId, null, null,
          premisEvent, username, notify);
      }
    } else {
      return createPreservationMetadata(PreservationMetadataType.EVENT, eventId, aipId, representationId, filePath,
        fileId, premisEvent, username, notify);
    }
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String id, PreservationMetadataType type) {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setType(type);
    return pm;
  }

  @Override
  public PreservationMetadata retrievePreservationMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, PreservationMetadataType type) {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(IdUtils.getPreservationId(type, aipId, representationId, fileDirectoryPath, null,
      RODAInstanceUtils.getLocalInstanceIdentifier()));
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setType(type);
    return pm;
  }

  @Override
  public Binary retrievePreservationRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String urn = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null, null,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    StoragePath path = ModelUtils.getPreservationMetadataStoragePath(urn, PreservationMetadataType.REPRESENTATION,
      aipId, representationId);
    return storage.getBinary(path);
  }

  @Override
  public boolean preservationRepresentationExists(String aipId, String representationId)
    throws RequestNotValidException {
    String urn = IdUtils.getPreservationId(PreservationMetadataType.REPRESENTATION, aipId, representationId, null, null,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    StoragePath path = ModelUtils.getPreservationMetadataStoragePath(urn, PreservationMetadataType.REPRESENTATION,
      aipId, representationId);
    return storage.exists(path);
  }

  @Override
  public Binary retrievePreservationFile(File file)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrievePreservationFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
  }

  @Override
  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = retrieveAIP(aipId);
    String identifier = IdUtils.getPreservationFileId(fileId, aip.getInstanceId());
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(identifier, PreservationMetadataType.FILE,
      aipId, representationId, fileDirectoryPath, fileId);
    return storage.getBinary(filePath);
  }

  @Override
  public boolean preservationFileExists(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, AuthorizationDeniedException {

    try {
      AIP aip = retrieveAIP(aipId);
      String identifier = IdUtils.getPreservationFileId(fileId, aip.getInstanceId());
      StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(identifier, PreservationMetadataType.FILE,
        aipId, representationId, fileDirectoryPath, fileId);
      return storage.exists(filePath);
    } catch (NotFoundException e) {
      return false;
    }
  }

  @Override
  public Binary retrieveRepositoryPreservationEvent(String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String fileName = fileId + RodaConstants.PREMIS_SUFFIX;
    StoragePath storagePath = ModelUtils.getPreservationRepositoryEventStoragePath();

    return storage.getBinary(DefaultStoragePath.parse(storagePath, fileName));
  }

  @Override
  public Binary retrievePreservationEvent(String aipId, String representationId, List<String> filePath, String fileId,
    String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationID,
      PreservationMetadataType.EVENT, aipId, representationId, filePath, fileId);
    return storage.getBinary(storagePath);
  }

  @Override
  public Binary retrievePreservationAgent(String preservationID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(preservationID,
      PreservationMetadataType.AGENT);
    return storage.getBinary(storagePath);
  }

  @Override
  public void createTechnicalMetadata(String aipId, String representationId, String metadataType, String fileId,
    ContentPayload payload, String createdBy, boolean notify) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    String urn = URNUtils.createRodaTechnicalMetadataURN(fileId, RODAInstanceUtils.getLocalInstanceIdentifier(),
      metadataType.toLowerCase());

    StoragePath binaryPath = ModelUtils.getTechnicalMetadataStoragePath(aipId, representationId,
      Collections.singletonList(metadataType), urn);
    storage.createBinary(binaryPath, payload, false);
    TechnicalMetadata techMd = new TechnicalMetadata(metadataType, aipId, representationId, metadataType);

    AIP updatedAIP = null;
    if (aipId != null) {
      AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
      aip.addTechnicalMetadata(techMd);
      updatedAIP = updateAIPMetadata(aip, createdBy);
    }

    if (notify && updatedAIP != null) {
      notifyAipUpdatedOnChanged(updatedAIP).failOnError();
    }
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    String identifier = fileId;
    if (!PreservationMetadataType.FILE.equals(type)) {
      identifier = IdUtils.getFileId(aipId, representationId, fileDirectoryPath, fileId);
    }

    String urn = URNUtils.createRodaPreservationURN(type, identifier, RODAInstanceUtils.getLocalInstanceIdentifier());
    return createPreservationMetadata(type, urn, aipId, representationId, fileDirectoryPath, fileId, payload, username,
      notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    List<String> fileDirectoryPath, String fileId, ContentPayload payload, String username, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    String id = IdUtils.getPreservationId(type, aipId, null, fileDirectoryPath, fileId,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    return createPreservationMetadata(type, id, aipId, null, fileDirectoryPath, fileId, payload, username, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    String representationId, ContentPayload payload, String username, boolean notify) throws GenericException,
    NotFoundException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    String id = IdUtils.getPreservationId(type, aipId, representationId, null, null,
      RODAInstanceUtils.getLocalInstanceIdentifier());
    return createPreservationMetadata(type, id, aipId, representationId, null, null, payload, username, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify) throws GenericException, NotFoundException, RequestNotValidException,
    AuthorizationDeniedException, AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return createPreservationMetadata(type, id, null, null, null, null, payload, null, notify);
  }

  @Override
  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String createdBy,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
    AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setType(type);
    pm.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.createBinary(binaryPath, payload, false);

    AIP updatedAIP = null;
    if (aipId != null) {
      AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
      updatedAIP = updateAIPMetadata(aip, createdBy);
    }

    if (notify) {
      notifyPreservationMetadataCreated(pm).failOnError();
      if (updatedAIP != null) {
        notifyAipUpdatedOnChanged(updatedAIP).failOnError();
      }
    }

    return pm;
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(PreservationMetadataType type, String id,
    ContentPayload payload, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return updatePreservationMetadata(id, type, null, null, null, null, payload, null, notify);
  }

  @Override
  public PreservationMetadata updatePreservationMetadata(String id, PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload, String updatedBy,
    boolean notify) throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    PreservationMetadata pm = new PreservationMetadata();
    pm.setId(id);
    pm.setType(type);
    pm.setAipId(aipId);
    pm.setRepresentationId(representationId);
    pm.setFileDirectoryPath(fileDirectoryPath);
    pm.setFileId(fileId);
    pm.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.updateBinaryContent(binaryPath, payload, false, true, false, null);

    AIP updatedAIP = null;
    if (aipId != null) {
      AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
      updatedAIP = updateAIPMetadata(aip, updatedBy);
    }

    if (notify) {
      notifyPreservationMetadataUpdated(pm).failOnError();
      if (updatedAIP != null) {
        notifyAipUpdatedOnChanged(updatedAIP).failOnError();
      }
    }

    return pm;
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadata pm, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.deleteResource(binaryPath);

    if (notify) {
      notifyPreservationMetadataDeleted(pm).failOnError();
    }
  }

  @Override
  public void deletePreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id, List<String> filePath, boolean notify)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);
    pm.setFileDirectoryPath(filePath);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.deleteResource(binaryPath);

    if (notify) {
      notifyPreservationMetadataDeleted(pm).failOnError();
    }
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  /***************************
   * Other metadata related
   ***************************/

  @Override
  public Binary retrieveOtherMetadataBinary(OtherMetadata om)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrieveOtherMetadataBinary(om.getAipId(), om.getRepresentationId(), om.getFileDirectoryPath(),
      om.getFileId(), om.getFileSuffix(), om.getType());
  }

  @Override
  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    binary = storage.getBinary(binaryPath);
    return binary;
  }

  @Override
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

  @Override
  public OtherMetadata createOrUpdateOtherMetadata(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId, String fileSuffix, String type, ContentPayload payload,
    String username, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    boolean asReference = false;
    boolean createIfNotExists = true;

    if (storage.exists(binaryPath)) {
      storage.updateBinaryContent(binaryPath, payload, asReference, createIfNotExists, false, null);
    } else {
      try {
        storage.createBinary(binaryPath, payload, asReference);
      } catch (AlreadyExistsException e) {
        // This should not happen, as it has already been checked if the file exists or
        // not
        throw new GenericException("file already exists, but was not found before", e);
      }
    }

    String id = IdUtils.getOtherMetadataId(aipId, representationId, fileDirectoryPath, fileId);
    OtherMetadata om = new OtherMetadata(id, type, aipId, representationId, fileDirectoryPath, fileId, fileSuffix);

    if (notify) {
      notifyOtherMetadataCreated(om).failOnError();
    }

    if (representationId != null) {
      changeRepresentationUpdateOn(aipId, representationId, username, notify);
    } else {
      AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
      AIP updatedAIP = updateAIPMetadata(aip, username);
      if (notify) {
        notifyAipUpdatedOnChanged(updatedAIP);
      }
    }

    return om;
  }

  @Override
  public void deleteOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath, String fileId,
    String fileSuffix, String type, String username)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getOtherMetadataStoragePath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    storage.deleteResource(binaryPath);

    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    AIP updatedAIP = updateAIPMetadata(aip, username);
    notifyAipUpdatedOnChanged(updatedAIP);
  }

  @Override
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

  @Override
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

  @Override
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

  /*********************
   * Log entry related
   *********************/

  @Override
  public List<LogEntry> importLogEntries(InputStream inputStream, String filename) throws AuthorizationDeniedException,
    GenericException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath logPath = ModelUtils.getLogStoragePath(filename);
    if (storage.exists(logPath)) {
      throw new AlreadyExistsException("Binary already exists: " + logPath);
    }

    Path tempDir = null;
    try {
      List<LogEntry> importedLogs = new ArrayList<>();
      tempDir = Files.createTempDirectory(new Date().getTime() + "");
      Path path = tempDir.resolve(filename);
      IOUtils.copyLarge(inputStream, Files.newOutputStream(path));

      for (OptionalWithCause<LogEntry> optionalLogEntry : new LogEntryFileSystemIterable(tempDir)) {
        // index
        if (optionalLogEntry.isPresent()) {
          importedLogs.add(optionalLogEntry.get());
          notifyLogEntryCreated(optionalLogEntry.get()).failOnError();
        }
      }

      // store
      storage.createBinary(logPath, new FSPathContentPayload(path), false);

      // return
      return importedLogs;
    } catch (IOException e) {
      throw new GenericException(e);
    } finally {
      FSUtils.deletePathQuietly(tempDir);
    }
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    synchronized (logFileLock) {
      String date = sdf.format(new Date());
      String id = date + "-";
      if (!"".equals(instanceId)) {
        id = id + instanceId;
      }
      Path logFile = logDirectory.resolve(id + ".log");

      // verify if file exists and if not, if older files exist (in that case,
      // move them to storage)
      if (!FSUtils.exists(logFile)) {
        entryLogLineNumber = 1;
        if (writeIsAllowed) {
          findOldLogsAndMoveThemToStorage(logDirectory, logFile);
        } else {
          findOldLogsAndSendThemToMaster(logDirectory, logFile);
        }

        try {
          Files.createFile(logFile);
        } catch (FileAlreadyExistsException e) {
          // do nothing (just caused due to concurrency)
        } catch (IOException e) {
          throw new GenericException("Error creating file to write log into", e);
        }
      } else if (entryLogLineNumber == -1) {
        // recalculate entryLogLineNumber as file exists but no value is set
        // memory
        entryLogLineNumber = JsonUtils.calculateNumberOfLines(logFile) + 1;
      }

      // write to log file
      logEntry.setId(id + "-" + entryLogLineNumber);
      logEntry.setInstanceId(instanceId);
      logEntry.setLineNumber(entryLogLineNumber);
      JsonUtils.appendObjectToFile(logEntry, logFile);
      entryLogLineNumber++;

      // emit event
      boolean slaveWriteInSolr = RodaCoreFactory.getProperty(RodaConstants.CORE_ACTION_LOGS_REPLICA_WRITE_IN_SOLR,
        false);
      if (notify && (writeIsAllowed || slaveWriteInSolr)) {
        notifyLogEntryCreated(logEntry).failOnError();
      }
    }
  }

  @Override
  public void addLogEntry(LogEntry logEntry, Path logDirectory)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    addLogEntry(logEntry, logDirectory, true);
  }

  @Override
  public synchronized void findOldLogsAndSendThemToMaster(Path logDirectory, Path currentLogFile) {

    String username = RodaCoreFactory.getProperty(RodaConstants.CORE_ACTION_LOGS_PRIMARY_USER, "");
    String url = RodaCoreFactory.getProperty(RodaConstants.CORE_ACTION_LOGS_PRIMARY_URL, "");
    String resource = RodaCoreFactory.getProperty(RodaConstants.CORE_ACTION_LOGS_PRIMARY_RESOURCE, "");

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDirectory);
      SecureString password = new SecureString(
        RodaCoreFactory.getProperty(RodaConstants.CORE_ACTION_LOGS_PRIMARY_PASS, "").toCharArray())) {
      for (Path path : directoryStream) {
        if (!path.equals(currentLogFile)) {
          int httpExitCode = RESTClientUtility.sendPostRequestWithFileHttp5(url, resource, username, password, path);
          if (httpExitCode == RodaConstants.HTTP_RESPONSE_CODE_CREATED) {
            LOGGER.info("The action log file ({}) was moved to Master successfully!", path);
            Files.delete(path);
          } else {
            LOGGER.error("The action log file ({}) was not moved to Master due to http response error: {}", path,
              httpExitCode);
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error listing directory for log files", e);
    } catch (RODAException e) {
      LOGGER.error("Error sending slave logs to master", e);
    }
  }

  @Override
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

  /************************
   * Users/Groups related
   ************************/

  @Override
  public User retrieveAuthenticatedUser(String name, String password)
    throws GenericException, AuthenticationDeniedException {
    return UserUtility.getLdapUtility().getAuthenticatedUser(name, password);
  }

  @Override
  public User retrieveUserByEmail(String email) throws GenericException {
    return UserUtility.getLdapUtility().getUserWithEmail(email);
  }

  @Override
  public User registerUser(User user, SecureString password, boolean notify)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    User registeredUser = UserUtility.getLdapUtility().registerUser(user, password);
    if (notify) {
      notifyUserCreated(registeredUser).failOnError();
    }

    return registeredUser;
  }

  @Override
  public User createUser(User user, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException, AuthorizationDeniedException {
    return createUser(user, null, notify);
  }

  @Override
  public User createUser(User user, SecureString password, boolean notify)
    throws EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    return createUser(user, password, notify, false);
  }

  /**
   * @param isHandlingEvent
   *          this should only be set to true if invoked from EventsManager
   *          related methods
   */
  @Override
  public User createUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException,
    NotFoundException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    User createdUser = UserUtility.getLdapUtility().addUser(user);
    if (password != null) {
      UserUtility.getLdapUtility().setUserPassword(createdUser.getId(), password);
      password = null;
    }

    if (notify && writeIsAllowed) {
      notifyUserCreated(createdUser).failOnError();
    }

    if (!isHandlingEvent) {
      eventsManager.notifyUserCreated(this, createdUser);
    }

    return createdUser;
  }

  @Override
  public User updateUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return updateUser(user, password, notify, false);
  }

  /**
   * @param isHandlingEvent
   *          this should only be set to true if invoked from EventsManager
   *          related methods
   */
  @Override
  public User updateUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    try {
      if (password != null) {
        UserUtility.getLdapUtility().setUserPassword(user.getId(), password);
      }

      User updatedUser = UserUtility.getLdapUtility().modifyUser(user, true);
      if (notify && writeIsAllowed) {
        notifyUserUpdated(updatedUser).failOnError();
      }

      if (!isHandlingEvent) {
        // FIXME 20180813 hsilva: user is not the previous state of the user
        eventsManager.notifyUserUpdated(this, user, updatedUser);
      }

      return updatedUser;
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return deActivateUser(id, activate, notify, false);
  }

  @Override
  public User deActivateUser(String id, boolean activate, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);
    try {
      User user = UserUtility.getLdapUtility().getUser(id);

      if (user.isActive() != activate) {
        user.setActive(activate);
        User updatedUser = UserUtility.getLdapUtility().modifyUser(user, false);
        if (notify && writeIsAllowed) {
          notifyUserUpdated(updatedUser).failOnError();
        }

        if (!isHandlingEvent) {
          // FIXME 20180813 hsilva: user is not the previous state of the user
          eventsManager.notifyUserUpdated(this, user, updatedUser);
        }

        return updatedUser;
      } else {
        return user;
      }
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    return updateMyUser(user, password, notify, false);
  }

  @Override
  public User updateMyUser(User user, SecureString password, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    try {
      User updatedUser = UserUtility.getLdapUtility().modifySelfUser(user, password);
      if (notify && writeIsAllowed) {
        notifyUserUpdated(updatedUser).failOnError();
      }

      if (!isHandlingEvent) {
        // FIXME 20180813 hsilva: user is not the previous state of the user
        eventsManager.notifyUserUpdated(this, user, updatedUser);
      }

      return updatedUser;
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }

  }

  @Override
  public void deleteUser(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    deleteUser(id, notify, false);
  }

  @Override
  public void deleteUser(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    try {
      UserUtility.getLdapUtility().removeUser(id);
      if (notify && writeIsAllowed) {
        notifyUserDeleted(id).failOnError();
      }

      if (!isHandlingEvent) {
        eventsManager.notifyUserDeleted(this, id);
      }
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  @Override
  public List<User> listUsers() throws GenericException {
    return UserUtility.getLdapUtility().getUsers();
  }

  @Override
  public User retrieveUser(String name) throws GenericException {
    return UserUtility.getLdapUtility().getUser(name);
  }

  @Override
  public String retrieveExtraLdap(String name) throws GenericException {
    return UserUtility.getLdapUtility().getExtraLDAP(name);
  }

  @Override
  public Group retrieveGroup(String name) throws GenericException, NotFoundException {
    return UserUtility.getLdapUtility().getGroup(name);
  }

  @Override
  public Group createGroup(Group group, boolean notify)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return createGroup(group, notify, false);
  }

  @Override
  public Group createGroup(Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    if (!writeIsAllowed && !isHandlingEvent) {
      RodaCoreFactory.throwExceptionIfWriteIsNotAllowed();
    }

    Group createdGroup = UserUtility.getLdapUtility().addGroup(group);
    if (notify && writeIsAllowed) {
      notifyGroupCreated(createdGroup).failOnError();
    }

    if (!isHandlingEvent) {
      eventsManager.notifyGroupCreated(this, createdGroup);
    }

    return createdGroup;
  }

  @Override
  public Group updateGroup(final Group group, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    return updateGroup(group, notify, false);
  }

  @Override
  public Group updateGroup(final Group group, boolean notify, boolean isHandlingEvent)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    if (!writeIsAllowed && !isHandlingEvent) {
      RodaCoreFactory.throwExceptionIfWriteIsNotAllowed();
    }

    try {
      Group updatedGroup = UserUtility.getLdapUtility().modifyGroup(group);
      if (notify && writeIsAllowed) {
        notifyGroupUpdated(updatedGroup).failOnError();
      }

      if (!isHandlingEvent) {
        // FIXME 20180813 hsilva: group is not the previous state of the group
        eventsManager.notifyGroupUpdated(this, group, updatedGroup);
      }

      return updatedGroup;
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }

  }

  @Override
  public void deleteGroup(String id, boolean notify) throws GenericException, AuthorizationDeniedException {
    deleteGroup(id, notify, false);
  }

  @Override
  public void deleteGroup(String id, boolean notify, boolean isHandlingEvent)
    throws GenericException, AuthorizationDeniedException {
    boolean writeIsAllowed = RodaCoreFactory.checkIfWriteIsAllowed(nodeType);

    if (!writeIsAllowed && !isHandlingEvent) {
      RodaCoreFactory.throwExceptionIfWriteIsNotAllowed();
    }

    try {
      UserUtility.getLdapUtility().removeGroup(id);
      if (notify && writeIsAllowed) {
        notifyGroupDeleted(id).failOnError();
      }

      if (!isHandlingEvent) {
        eventsManager.notifyGroupDeleted(this, id);
      }
    } catch (IllegalOperationException e) {
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
  }

  @Override
  public List<Group> listGroups() throws GenericException {
    return UserUtility.getLdapUtility().getGroups();
  }

  @Override
  public User confirmUserEmail(String username, String email, String emailConfirmationToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, GenericException {
    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().confirmUserEmail(username, email, emailConfirmationToken);
    }

    if (user != null && notify) {
      notifyUserUpdated(user).failOnError();
    }

    return user;
  }

  @Override
  public User requestPasswordReset(String username, String email, boolean useModel, boolean notify)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().requestPasswordReset(username, email);
    }

    if (user != null && notify) {
      notifyUserUpdated(user).failOnError();
    }

    return user;
  }

  @Override
  public User resetUserPassword(String username, SecureString password, String resetPasswordToken, boolean useModel,
    boolean notify) throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    User user = null;
    if (useModel) {
      user = UserUtility.getLdapUtility().resetUserPassword(username, password, resetPasswordToken);
    }

    if (user != null && notify) {
      notifyUserUpdated(user).failOnError();
    }

    return user;
  }

  /*****************
   * Jobs related
   *****************/

  @Override
  public void createJob(Job job)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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

  @Override
  public void createOrUpdateJob(Job job)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (job.getInstanceId() == null) {
      job.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    }
    // create or update job in storage
    String jobAsJson = JsonUtils.getJsonFromObject(job);
    StoragePath jobPath = ModelUtils.getJobStoragePath(job.getId());
    storage.updateBinaryContent(jobPath, new StringContentPayload(jobAsJson), false, true, false, null);
    // index it
    notifyJobCreatedOrUpdated(job, false).failOnError();
  }

  @Override
  public Job retrieveJob(String jobId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath jobPath = ModelUtils.getJobStoragePath(jobId);
    Binary binary = storage.getBinary(jobPath);
    Job ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, Job.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading job: " + jobId, e);
    }

    return ret;
  }

  public CloseableIterable<OptionalWithCause<Report>> listJobReports(String jobId)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    final CloseableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getJobReportsStoragePath(jobId), false);
    return ResourceParseUtils.convert(getStorage(), resourcesIterable, Report.class);
  }

  @Override
  public void deleteJob(String jobId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath jobPath = ModelUtils.getJobStoragePath(jobId);

    // remove it from storage
    storage.deleteResource(jobPath);

    // remove it from index
    notifyJobDeleted(jobId).failOnError();
  }

  @Override
  public Report retrieveJobReport(String jobId, String jobReportId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobId, jobReportId);
    Binary binary = storage.getBinary(jobReportPath);
    Report ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, Report.class);
    } catch (IOException e) {
      throw new GenericException("Error reading job report", e);
    }

    return ret;
  }

  @Override
  public Report retrieveJobReport(String jobId, String sourceObjectId, String outcomeObjectId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    String jobReportId = IdUtils.getJobReportId(jobId, sourceObjectId, outcomeObjectId);
    return retrieveJobReport(jobId, jobReportId);
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, Job cachedJob)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    jobReport.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    // create job report in storage
    try {
      // if job report changed id, set it and remove old report
      String newId = IdUtils.getJobReportId(jobReport.getJobId(), jobReport.getSourceObjectId(),
        jobReport.getOutcomeObjectId());
      if (!newId.equals(jobReport.getId())) {
        String oldId = jobReport.getId();
        jobReport.setId(newId);
        storage.deleteResource(ModelUtils.getJobReportStoragePath(jobReport.getJobId(), oldId));
        notifyJobReportDeleted(oldId);
      }

      String jobReportAsJson = JsonUtils.getJsonFromObject(jobReport);
      StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobReport.getJobId(), jobReport.getId());
      storage.updateBinaryContent(jobReportPath, new StringContentPayload(jobReportAsJson), false, true, false, null);

      // index it
      notifyJobReportCreatedOrUpdated(jobReport, cachedJob).failOnError();
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job report in storage", e);
    }
  }

  @Override
  public void createOrUpdateJobReport(Report jobReport, IndexedJob indexJob)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    jobReport.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    // create job report in storage
    try {
      // if job report changed id, set it and remove old report
      String newId = IdUtils.getJobReportId(jobReport.getJobId(), jobReport.getSourceObjectId(),
        jobReport.getOutcomeObjectId());
      if (!newId.equals(jobReport.getId())) {
        String oldId = jobReport.getId();
        jobReport.setId(newId);
        storage.deleteResource(ModelUtils.getJobReportStoragePath(jobReport.getJobId(), oldId));
        notifyJobReportDeleted(oldId);
      }

      String jobReportAsJson = JsonUtils.getJsonFromObject(jobReport);
      StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobReport.getJobId(), jobReport.getId());
      storage.updateBinaryContent(jobReportPath, new StringContentPayload(jobReportAsJson), false, true, false, null);

      // index it
      notifyJobReportCreatedOrUpdated(jobReport, indexJob).failOnError();
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error creating/updating job report in storage", e);
    }
  }

  @Override
  public void deleteJobReport(String jobId, String jobReportId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath jobReportPath = ModelUtils.getJobReportStoragePath(jobId, jobReportId);

    // remove it from storage
    storage.deleteResource(jobReportPath);

    // remove it from index
    notifyJobReportDeleted(jobReportId).failOnError();
  }

  @Override
  public void updateAIPPermissions(String aipId, Permissions permissions, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP aip = retrieveAIP(aipId);
    aip.setPermissions(permissions);
    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipPermissionsUpdated(updatedAIP).failOnError();
  }

  @Override
  public void updateAIPPermissions(AIP aip, String updatedBy)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    AIP updatedAIP = updateAIPMetadata(aip, updatedBy);
    notifyAipPermissionsUpdated(updatedAIP).failOnError();
  }

  @Override
  public void updateDIPPermissions(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    dip.setLastModified(new Date());
    updateDIPMetadata(dip);
    notifyDipPermissionsUpdated(dip).failOnError();
  }

  @Override
  public void deleteTransferredResource(TransferredResource transferredResource)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    FSUtils.deletePathQuietly(Paths.get(FilenameUtils.normalize(transferredResource.getFullPath())));
    notifyTransferredResourceDeleted(transferredResource.getUUID()).failOnError();
  }

  @Override
  public Job updateJobInstanceId(Job job)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    job.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    createOrUpdateJob(job);

    try (CloseableIterable<Resource> listResourcesUnderDirectory = storage
      .listResourcesUnderDirectory(ModelUtils.getJobReportsStoragePath(job.getId()), true)) {

      if (listResourcesUnderDirectory != null) {
        for (Resource resource : listResourcesUnderDirectory) {
          if (!resource.isDirectory()) {
            try (
              InputStream inputStream = storage.getBinary(resource.getStoragePath()).getContent().createInputStream()) {
              Report jobReport = JsonUtils.getObjectFromJson(inputStream, Report.class);
              jobReport.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
              createOrUpdateJobReport(jobReport, job);
            } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
              | IOException e) {
              LOGGER.error("Error getting report json from binary", e);
            }
          }
        }
      }
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException
      | IOException e) {
      LOGGER.error("Error updating instance identifier on job reports", e);
    }

    return job;
  }

  /*****************
   * Risk related
   *****************/

  @Override
  public Risk createRisk(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      if (risk.getId() == null) {
        risk.setId(IdUtils.createUUID());
      }

      risk.setCreatedOn(new Date());
      risk.setUpdatedOn(new Date());
      risk.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

      String riskAsJson = JsonUtils.getJsonFromObject(risk);
      StoragePath riskPath = ModelUtils.getRiskStoragePath(risk.getId());
      storage.createBinary(riskPath, new StringContentPayload(riskAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating risk in storage", e);
    }

    notifyRiskCreatedOrUpdated(risk, 0, commit).failOnError();
    return risk;
  }

  @Override
  public Risk updateRiskInstanceId(Risk risk, boolean commit) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

    risk.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    Risk updatedRisk = updateRisk(risk, properties, commit, 0);

    return updatedRisk;
  }

  @Override
  public Risk updateRisk(Risk risk, Map<String, String> properties, boolean commit, int incidences)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      risk.setUpdatedOn(new Date());
      String riskAsJson = JsonUtils.getJsonFromObject(risk);
      StoragePath riskPath = ModelUtils.getRiskStoragePath(risk.getId());

      storage.updateBinaryContent(riskPath, new StringContentPayload(riskAsJson), false, true, false, null);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating risk in storage", e);
    }

    notifyRiskCreatedOrUpdated(risk, incidences, commit).failOnError();
    return risk;
  }

  @Override
  public void deleteRisk(String riskId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath riskPath = ModelUtils.getRiskStoragePath(riskId);
    storage.deleteResource(riskPath);
    notifyRiskDeleted(riskId, commit).failOnError();
  }

  @Override
  public Risk retrieveRisk(String riskId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath riskPath = ModelUtils.getRiskStoragePath(riskId);
    Binary binary = storage.getBinary(riskPath);
    Risk ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, Risk.class);
    } catch (IOException e) {
      throw new GenericException("Error reading risk", e);
    }

    return ret;
  }

  @Override
  public BinaryVersion retrieveVersion(String riskId, String versionId)
    throws RequestNotValidException, GenericException, NotFoundException {
    StoragePath binaryPath = ModelUtils.getRiskStoragePath(riskId);
    return storage.getBinaryVersion(binaryPath, versionId);
  }

  @Override
  public BinaryVersion revertRiskVersion(String riskId, String versionId, Map<String, String> properties,
    boolean commit, int incidences)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath binaryPath = ModelUtils.getRiskStoragePath(riskId);

    Binary revertedBinary = storage.revertBinaryVersion(binaryPath, versionId, properties);
    BinaryVersion beforeRevertVersion = storage.getBinaryVersion(binaryPath, revertedBinary.getPreviousVersionId());

    notifyRiskCreatedOrUpdated(retrieveRisk(riskId), incidences, commit).failOnError();
    return beforeRevertVersion;
  }

  @Override
  public RiskIncidence createRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws AlreadyExistsException, NotFoundException, AuthorizationDeniedException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      riskIncidence.setId(IdUtils.createUUID());
      riskIncidence.setDetectedOn(new Date());
      riskIncidence.setUpdatedOn(new Date());
      riskIncidence.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

      String riskIncidenceAsJson = JsonUtils.getJsonFromObject(riskIncidence);
      StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidence.getId());
      storage.createBinary(riskIncidencePath, new StringContentPayload(riskIncidenceAsJson), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating risk incidence in storage", e);
    }

    notifyRiskIncidenceCreatedOrUpdated(riskIncidence, commit).failOnError();
    return riskIncidence;
  }

  @Override
  public RiskIncidence updateRiskIncidenceInstanceId(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    riskIncidence.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    RiskIncidence updatedRiskIncidence = updateRiskIncidence(riskIncidence, commit);

    return updatedRiskIncidence;
  }

  @Override
  public RiskIncidence updateRiskIncidence(RiskIncidence riskIncidence, boolean commit)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      riskIncidence.setRiskId(riskIncidence.getRiskId());
      riskIncidence.setUpdatedOn(new Date());
      String riskIncidenceAsJson = JsonUtils.getJsonFromObject(riskIncidence);
      StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidence.getId());
      storage.updateBinaryContent(riskIncidencePath, new StringContentPayload(riskIncidenceAsJson), false, true, false,
        null);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating risk incidence in storage", e);
    }

    notifyRiskIncidenceCreatedOrUpdated(riskIncidence, commit).failOnError();
    return riskIncidence;
  }

  @Override
  public void deleteRiskIncidence(String riskIncidenceId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(riskIncidenceId);
    storage.deleteResource(riskIncidencePath);
    notifyRiskIncidenceDeleted(riskIncidenceId, commit).failOnError();
  }

  @Override
  public RiskIncidence retrieveRiskIncidence(String incidenceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath riskIncidencePath = ModelUtils.getRiskIncidenceStoragePath(incidenceId);
    Binary binary = storage.getBinary(riskIncidencePath);
    RiskIncidence ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, RiskIncidence.class);
    } catch (IOException e) {
      throw new GenericException("Error reading risk incidence", e);
    }

    return ret;
  }

  /************************
   * Notification related
   ************************/

  @Override
  public Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    notification.setId(IdUtils.createUUID());
    notification.setAcknowledgeToken(IdUtils.createUUID());
    notification.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

    if (processor != null) {
      notification = processor.processNotification(this, notification);
    }

    try {
      String notificationAsJson = JsonUtils.getJsonFromObject(notification);
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notification.getId());
      storage.createBinary(notificationPath, new StringContentPayload(notificationAsJson), false);
      notifyNotificationCreatedOrUpdated(notification).failOnError();
    } catch (NotFoundException | RequestNotValidException | AlreadyExistsException e) {
      LOGGER.error("Error creating notification in storage", e);
      throw new GenericException(e);
    }

    return notification;
  }

  @Override
  public Notification updateNotificationInstanceId(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    notification.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    Notification updatedNotification = updateNotification(notification);
    return updatedNotification;
  }

  @Override
  public Notification updateNotification(Notification notification)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      String notificationAsJson = JsonUtils.getJsonFromObject(notification);
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notification.getId());
      storage.updateBinaryContent(notificationPath, new StringContentPayload(notificationAsJson), false, true, false,
        null);
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error updating notification in storage", e);
      throw new GenericException(e);
    }

    notifyNotificationCreatedOrUpdated(notification).failOnError();
    return notification;
  }

  @Override
  public void deleteNotification(String notificationId)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      StoragePath notificationPath = ModelUtils.getNotificationStoragePath(notificationId);
      storage.deleteResource(notificationPath);
      notifyNotificationDeleted(notificationId).failOnError();
    } catch (RequestNotValidException e) {
      LOGGER.error("Error deleting notification", e);
      throw new GenericException(e);
    }
  }

  @Override
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

  @Override
  public Notification acknowledgeNotification(String notificationId, String token)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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

  /*********************************************
   * DIP related
   *********************************************/

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(DIPFile f, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return listDIPFilesUnder(f.getDipId(), f.getPath(), f.getId(), recursive);
  }

  @Override
  public CloseableIterable<OptionalWithCause<DIPFile>> listDIPFilesUnder(String dipId, List<String> directoryPath,
    String fileId, boolean recursive)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(filePath, recursive);
    return ResourceParseUtils.convert(getStorage(), iterable, DIPFile.class);
  }

  @Override
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
    storage.createBinary(metadataStoragePath, new StringContentPayload(json), false);
  }

  @Override
  public void updateDIPInstanceId(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    dip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    dip.setLastModified(new Date());
    updateDIPMetadata(dip);
    notifyDipInstanceIdUpdated(dip).failOnError();
  }

  private void updateDIPMetadata(DIP dip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    updateDIPMetadata(dip, ModelUtils.getDIPStoragePath(dip.getId()));
  }

  private void updateDIPMetadata(DIP dip, StoragePath storagePath)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    String json = JsonUtils.getJsonFromObject(dip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath,
      RodaConstants.STORAGE_DIP_METADATA_FILENAME);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(json), asReference, createIfNotExists,
      false, null);
  }

  @Override
  public DIP createDIP(DIP dip, boolean notify) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
      dip.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
      createDIPMetadata(dip, directory.getStoragePath());

      if (notify) {
        notifyDIPCreated(dip, false).failOnError();
      }

      return dip;
    } catch (NotFoundException | RequestNotValidException | AlreadyExistsException e) {
      LOGGER.error("Error creating DIP in storage", e);
      throw new GenericException(e);
    }
  }

  @Override
  public DIP updateDIP(DIP dip) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      dip.setLastModified(new Date());
      updateDIPMetadata(dip, DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_DIP, dip.getId()));
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error updating DIP in storage", e);
      throw new GenericException(e);
    }

    notifyDIPUpdated(dip, false).failOnError();
    return dip;
  }

  @Override
  public void deleteDIP(String dipId) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

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
      notifyDIPDeleted(dipId, false).failOnError();
    } catch (RequestNotValidException e) {
      LOGGER.error("Error deleting DIP", e);
      throw new GenericException(e);
    }
  }

  @Override
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

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, long size,
    ContentPayload contentPayload, boolean notify) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    boolean asReference = false;

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    DIPFile file = ResourceParseUtils.convertResourceToDIPFile(createdBinary);
    file.setUUID(IdUtils.getDIPFileId(file));
    file.setSize(size);

    if (notify) {
      notifyDIPFileCreated(file).failOnError();
    }

    return file;
  }

  @Override
  public DIPFile createDIPFile(String dipId, List<String> directoryPath, String fileId, String dirName, boolean notify)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Directory createdDirectory = storage.createDirectory(DefaultStoragePath.parse(filePath, dirName));
    DIPFile file = ResourceParseUtils.convertResourceToDIPFile(createdDirectory);

    if (notify) {
      notifyDIPFileCreated(file).failOnError();
    }

    return file;
  }

  @Override
  public DIPFile updateDIPFile(String dipId, List<String> directoryPath, String oldFileId, String fileId, long size,
    ContentPayload contentPayload, boolean createIfNotExists, boolean notify) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    DIPFile file;
    boolean asReference = false;

    StoragePath oldFilePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, oldFileId);
    storage.deleteResource(oldFilePath);

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    final Binary binary = storage.createBinary(filePath, contentPayload, asReference);
    file = ResourceParseUtils.convertResourceToDIPFile(binary);

    if (notify) {
      notifyDIPFileUpdated(file).failOnError();
    }

    return file;
  }

  @Override
  public void deleteDIPFile(String dipId, List<String> directoryPath, String fileId, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath filePath = ModelUtils.getDIPFileStoragePath(dipId, directoryPath, fileId);
    storage.deleteResource(filePath);
    if (notify) {
      notifyDIPFileDeleted(dipId, directoryPath, fileId).failOnError();
    }
  }

  @Override
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

  /*********************************************************
   * OTHER DIRECTORIES (submission, documentation, schemas)
   *********************************************************/

  private Directory getSubmissionDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSubmissionStoragePath(aipId));
  }

  @Override
  public void createSubmission(StorageService submissionStorage, StoragePath submissionStoragePath, String aipId)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    storage.copy(submissionStorage, submissionStoragePath, DefaultStoragePath
      .parse(ModelUtils.getSubmissionStoragePath(aipId), DateTimeFormatter.ISO_INSTANT.format(Instant.now())));
  }

  @Override
  public void createSubmission(Path submissionPath, String aipId) throws AlreadyExistsException, GenericException,
    RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath submissionStoragePath = DefaultStoragePath.parse(ModelUtils.getSubmissionStoragePath(aipId),
      DateTimeFormatter.ISO_INSTANT.format(Instant.now()), submissionPath.getFileName().toString());
    storage.createBinary(submissionStoragePath, new FSPathContentPayload(submissionPath), false);
  }

  private Directory getDocumentationDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getDocumentationStoragePath(aipId));
  }

  private Directory getDocumentationDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getDocumentationStoragePath(aipId, representationId));
  }

  @Override
  public File createDocumentation(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    boolean asReference = false;
    StoragePath filePath = ModelUtils.getDocumentationStoragePath(aipId, representationId, directoryPath, fileId);
    final Binary createdBinary = storage.createBinary(filePath, contentPayload, asReference);
    return ResourceParseUtils.convertResourceToFile(createdBinary);
  }

  @Override
  public Long countDocumentationFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Directory documentationDirectory;
    if (representationId == null) {
      documentationDirectory = getDocumentationDirectory(aipId);
    } else {
      documentationDirectory = getDocumentationDirectory(aipId, representationId);
    }
    return getStorage().countResourcesUnderDirectory(documentationDirectory.getStoragePath(), true);
  }

  @Override
  public Long countSubmissionFiles(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Directory submissionDirectory = getSubmissionDirectory(aipId);
    return getStorage().countResourcesUnderDirectory(submissionDirectory.getStoragePath(), true);
  }

  @Override
  public Long countSchemaFiles(String aipId, String representationId)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Directory schemaDirectory;
    if (representationId == null) {
      schemaDirectory = getSchemasDirectory(aipId);
    } else {
      schemaDirectory = getSchemasDirectory(aipId, representationId);
    }
    return getStorage().countResourcesUnderDirectory(schemaDirectory.getStoragePath(), true);
  }

  private Directory getSchemasDirectory(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSchemasStoragePath(aipId));
  }

  private Directory getSchemasDirectory(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storage.getDirectory(ModelUtils.getSchemasStoragePath(aipId, representationId));
  }

  @Override
  public void createSchema(String aipId, String representationId, List<String> directoryPath, String fileId,
    ContentPayload contentPayload) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    boolean asReference = false;
    StoragePath filePath = ModelUtils.getSchemaStoragePath(aipId, representationId, directoryPath, fileId);
    storage.createBinary(filePath, contentPayload, asReference);
  }

  @Override
  public boolean checkIfSchemaExists(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException {
    StoragePath schemaStoragePath = ModelUtils.getSchemaStoragePath(aipId, representationId, directoryPath, fileId);
    return storage.exists(schemaStoragePath);
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

  @Override
  public <T extends IsRODAObject> Optional<LiteRODAObject> retrieveLiteFromObject(T object) {
    return LiteRODAObjectFactory.get(object);
  }

  @Override
  public <T extends IsModelObject> OptionalWithCause<T> retrieveObjectFromLite(LiteRODAObject liteRODAObject) {
    return LiteRODAObjectFactory.get(this, liteRODAObject);
  }

  @Override
  public TransferredResource retrieveTransferredResource(String fullPath) {
    TransferredResourcesScanner transferredResourcesScanner = RodaCoreFactory.getTransferredResourcesScanner();
    return transferredResourcesScanner.instantiateTransferredResource(Paths.get(FilenameUtils.normalize(fullPath)),
      transferredResourcesScanner.getBasePath());
  }

  @Override
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

  @Override
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
      /*
       * } else if (DisposalConfirmation.class.equals(objectClass)) { ret =
       * ResourceParseUtils.convertLite(getStorage(),
       * ResourceListUtils.listDisposalConfirmationResources(storage), objectClass);
       */
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

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries() {
    return listLogEntries(0);
  }

  @Override
  public CloseableIterable<OptionalWithCause<LogEntry>> listLogEntries(int daysToIndex) {
    CloseableIterable<OptionalWithCause<LogEntry>> inStorage = null;
    CloseableIterable<OptionalWithCause<LogEntry>> notStorage = null;

    try {
      final CloseableIterable<Resource> actionLogs = getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), false);

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

  @Override
  public CloseableIterable<Resource> listLogFilesInStorage() {
    CloseableIterable<Resource> inStorage = null;

    try {
      inStorage = getStorage()
        .listResourcesUnderContainer(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_ACTIONLOG), false);
    } catch (NotFoundException | GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.error("Error getting action log from storage", e);
    }

    return inStorage;
  }

  private boolean isToIndex(String fileName, int daysToIndex) {
    boolean isToIndex = false;
    String dateFromFileName = fileName.replaceFirst("([0-9]{4}-[0-9]{2}-[0-9]{2}).*", "$1");

    try {
      TemporalAccessor dt = LOG_NAME_DATE_FORMAT.parse(dateFromFileName);

      if (LocalDate.from(dt).plus(daysToIndex + 1, ChronoUnit.DAYS).isAfter(LocalDate.now())) {
        isToIndex = true;
      }
    } catch (IllegalArgumentException | UnsupportedOperationException e) {
      LOGGER.error("Could not parse log file name", e);
    }

    return isToIndex;
  }

  @Override
  public boolean hasObjects(Class<? extends IsRODAObject> objectClass) {
    try {
      if (LogEntry.class.equals(objectClass) || RODAMember.class.equals(objectClass)
        || TransferredResource.class.equals(objectClass) || IndexedPreservationAgent.class.equals(objectClass)
        || IndexedPreservationEvent.class.equals(objectClass) || IndexedAIP.class.equals(objectClass)
        || RepresentationInformation.class.equals(objectClass) || RiskIncidence.class.equals(objectClass)) {
        return true;
      } else {
        StoragePath storagePath = ModelUtils.getContainerPath(objectClass);
        try {
          return getStorage().countResourcesUnderContainer(storagePath, false).intValue() > 0;
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

  @Override
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

  /*******************************************
   * Representation information related
   *******************************************/

  @Override
  public RepresentationInformation createRepresentationInformation(RepresentationInformation ri, String createdBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      ri.setId(IdUtils.createUUID());

      Date creationDate = new Date();
      ri.setCreatedBy(createdBy);
      ri.setCreatedOn(creationDate);
      ri.setUpdatedBy(createdBy);
      ri.setUpdatedOn(creationDate);
      ri.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());

      String riAsXML = XMLUtils.getXMLFromObject(ri);
      StoragePath representationInformationPath = ModelUtils.getRepresentationInformationStoragePath(ri.getId());
      storage.createBinary(representationInformationPath, new StringContentPayload(riAsXML), false);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException
      | AlreadyExistsException e) {
      LOGGER.error("Error creating representation information in storage", e);
    }

    notifyRepresentationInformationCreatedOrUpdated(ri, commit).failOnError();
    return ri;
  }

  @Override
  public RepresentationInformation updateRepresentationInformation(RepresentationInformation ri, String updatedBy,
    boolean commit) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      ri.setUpdatedBy(updatedBy);
      ri.setUpdatedOn(new Date());

      String riAsXML = XMLUtils.getXMLFromObject(ri);
      StoragePath representationInformationPath = ModelUtils.getRepresentationInformationStoragePath(ri.getId());
      storage.updateBinaryContent(representationInformationPath, new StringContentPayload(riAsXML), false, true, false,
        null);
    } catch (GenericException | RequestNotValidException | AuthorizationDeniedException | NotFoundException e) {
      LOGGER.error("Error updating format in storage", e);
    }

    notifyRepresentationInformationCreatedOrUpdated(ri, commit).failOnError();
    return ri;
  }

  @Override
  public RepresentationInformation updateRepresentationInformationInstanceId(RepresentationInformation ri,
    String updatedBy, boolean notify)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    ri.setInstanceId(RODAInstanceUtils.getLocalInstanceIdentifier());
    updateRepresentationInformation(ri, updatedBy, notify);
    return ri;
  }

  @Override
  public void deleteRepresentationInformation(String representationInformationId, boolean commit)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath representationInformationPath = ModelUtils
      .getRepresentationInformationStoragePath(representationInformationId);
    storage.deleteResource(representationInformationPath);
    notifyRepresentationInformationDeleted(representationInformationId, commit).failOnError();
  }

  @Override
  public RepresentationInformation retrieveRepresentationInformation(String representationInformationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath representationInformationPath = ModelUtils
      .getRepresentationInformationStoragePath(representationInformationId);
    Binary binary = storage.getBinary(representationInformationPath);
    RepresentationInformation ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = XMLUtils.getObjectFromXML(inputStream, RepresentationInformation.class);
    } catch (IOException e) {
      throw new GenericException("Error reading representation information", e);
    }

    return ret;
  }

  /************************************
   * Disposal hold related
   ************************************/

  @Override
  public DisposalHold retrieveDisposalHold(String disposalHoldId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath disposalHoldPath = ModelUtils.getDisposalHoldStoragePath(disposalHoldId);
    Binary binary = storage.getBinary(disposalHoldPath);
    DisposalHold ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, DisposalHold.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading disposal hold: " + disposalHoldId, e);
    }

    Long count = SolrUtils.count(RodaCoreFactory.getSolr(), IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, ret.getId())));
    ret.setAipCounter(count);

    return ret;
  }

  @Override
  public DisposalHold createDisposalHold(DisposalHold disposalHold, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    DisposalHold newDisposalHold = new DisposalHold(IdUtils.createUUID(), disposalHold.getTitle(),
      disposalHold.getDescription(), disposalHold.getMandate(), disposalHold.getScopeNotes());
    newDisposalHold.setCreatedOn(new Date());
    newDisposalHold.setCreatedBy(createdBy);
    newDisposalHold.setUpdatedOn(new Date());
    newDisposalHold.setUpdatedBy(createdBy);

    String disposalHoldAsJson = JsonUtils.getJsonFromObject(newDisposalHold);
    StoragePath disposalHoldPath = ModelUtils.getDisposalHoldStoragePath(newDisposalHold.getId());
    storage.createBinary(disposalHoldPath, new StringContentPayload(disposalHoldAsJson), false);

    return newDisposalHold;
  }

  @Override
  public DisposalHold updateDisposalHoldFirstUseDate(DisposalHold disposalHold, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    return updateDisposalHold(disposalHold, updatedBy, true, null);
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, String details)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, IllegalOperationException,
    GenericException {
    return updateDisposalHold(disposalHold, updatedBy, false, details);
  }

  @Override
  public DisposalHold updateDisposalHold(DisposalHold disposalHold, String updatedBy, boolean updateFirstUseDate,
    String details) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    // Check if disposal hold is ACTIVE to update
    DisposalHold currentDisposalHold = retrieveDisposalHold(disposalHold.getId());
    if (DisposalHoldState.LIFTED.equals(currentDisposalHold.getState())) {
      throw new IllegalOperationException("Error updating disposal hold: " + currentDisposalHold.getId()
        + ". Reason: Disposal hold is lifted therefore can not change its content");
    }

    currentDisposalHold.setUpdatedOn(new Date());
    currentDisposalHold.setUpdatedBy(updatedBy);
    currentDisposalHold.setTitle(disposalHold.getTitle());
    currentDisposalHold.setDescription(disposalHold.getDescription());
    currentDisposalHold.setMandate(disposalHold.getMandate());
    currentDisposalHold.setScopeNotes(disposalHold.getScopeNotes());
    currentDisposalHold.setState(disposalHold.getState());
    currentDisposalHold.setLiftedOn(disposalHold.getLiftedOn());
    currentDisposalHold.setLiftedBy(disposalHold.getLiftedBy());

    if (updateFirstUseDate) {
      currentDisposalHold.setFirstTimeUsed(disposalHold.getFirstTimeUsed());
    }

    String disposalHoldAsJson = JsonUtils.getJsonFromObject(currentDisposalHold);
    StoragePath disposalHoldPath = ModelUtils.getDisposalHoldStoragePath(currentDisposalHold.getId());

    storage.updateBinaryContent(disposalHoldPath, new StringContentPayload(disposalHoldAsJson), false, true, false,
      null);

    createRepositoryEvent(PreservationEventType.UPDATE, "Update disposal hold", PluginState.SUCCESS, "", details, "",
      true, null);

    return currentDisposalHold;
  }

  @Override
  public void deleteDisposalHold(String disposalHoldId) throws RequestNotValidException, NotFoundException,
    GenericException, AuthorizationDeniedException, IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    DisposalHold disposalHold = retrieveDisposalHold(disposalHoldId);

    if (disposalHold.getFirstTimeUsed() == null) {
      StoragePath disposalHoldPath = ModelUtils.getDisposalHoldStoragePath(disposalHold.getId());
      storage.deleteResource(disposalHoldPath);
    } else {
      throw new IllegalOperationException("Error deleting disposal hold: " + disposalHold.getId()
        + ". Reason: One or more AIPs where associated under this disposal hold");
    }
  }

  @Override
  public DisposalHolds listDisposalHolds()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    StoragePath disposalHoldContainerPath = ModelUtils.getDisposalHoldContainerPath();
    DisposalHolds disposalHolds = new DisposalHolds();

    try (CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(disposalHoldContainerPath, false)) {
      for (Resource resource : iterable) {
        DisposalHold hold = ResourceParseUtils.convertResourceToObject(resource, DisposalHold.class);
        disposalHolds.addObject(hold);
      }
    } catch (NotFoundException e) {
      return new DisposalHolds();
    }

    return disposalHolds;
  }

  @Override
  public DisposalAIPMetadata createDisposalHoldAssociation(String aipId, String disposalHoldId, Date associatedOn,
    String associatedBy)
    throws AuthorizationDeniedException, GenericException, NotFoundException, RequestNotValidException {

    final DisposalHold disposalHold = retrieveDisposalHold(disposalHoldId);
    if (disposalHold.getLiftedOn() != null) {
      throw new NotFoundException("Could not associate an AIP with a disposal hold that is lifted: " + disposalHoldId);
    }

    // update AIP metadata
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    DisposalAIPMetadata disposal = aip.getDisposal();
    if (disposal == null) {
      disposal = new DisposalAIPMetadata();
      aip.setDisposal(disposal);
    }

    DisposalHoldAIPMetadata disposalHoldAIPMetadata = new DisposalHoldAIPMetadata();
    disposalHoldAIPMetadata.setId(disposalHoldId);
    disposalHoldAIPMetadata.setAssociatedBy(associatedBy);
    disposalHoldAIPMetadata.setAssociatedOn(associatedOn);

    disposal.addDisposalHold(disposalHoldAIPMetadata);

    AIP updatedAIP = updateAIPMetadata(aip, associatedBy);
    notifyAipUpdated(updatedAIP.getId());

    return disposal;
  }

  @Override
  public List<DisposalHold> retrieveDirectActiveDisposalHolds(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    List<DisposalHold> disposalHoldList = new ArrayList<>();

    for (DisposalHoldAIPMetadata hold : aip.getHolds()) {
      DisposalHold disposalHold = retrieveDisposalHold(hold.getId());
      if (disposalHold != null && (disposalHold.getState() == DisposalHoldState.ACTIVE)) {
        disposalHoldList.add(disposalHold);
      }
    }

    return disposalHoldList;
  }

  @Override
  public boolean onDisposalHold(String aipId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);

    if (aip.getHolds() != null) {
      for (DisposalHoldAIPMetadata hold : aip.getHolds()) {
        DisposalHold disposalHold = retrieveDisposalHold(hold.getId());
        if (disposalHold != null && disposalHold.getState() == DisposalHoldState.ACTIVE) {
          return true;
        }
      }
    }

    if (aip.getTransitiveHolds() != null) {
      for (DisposalTransitiveHoldAIPMetadata transitiveHold : aip.getTransitiveHolds()) {
        DisposalHold transitiveDisposalHold = retrieveDisposalHold(transitiveHold.getId());
        if (transitiveDisposalHold != null && transitiveDisposalHold.getState() == DisposalHoldState.ACTIVE) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean isAIPOnDirectHold(String aipId, String holdId)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    AIP aip = ResourceParseUtils.getAIPMetadata(getStorage(), aipId);
    DisposalHold disposalHold = retrieveDisposalHold(holdId);

    if (disposalHold.getState() == DisposalHoldState.ACTIVE) {
      return aip.findHold(holdId) != null;
    }

    return false;
  }

  /************************************
   * Disposal schedule related
   ************************************/

  @Override
  public DisposalSchedule createDisposalSchedule(DisposalSchedule disposalSchedule, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (disposalSchedule.getId() == null) {
      disposalSchedule.setId(IdUtils.createUUID());
    }

    disposalSchedule.setCreatedBy(createdBy);
    disposalSchedule.setCreatedOn(new Date());
    disposalSchedule.setUpdatedBy(createdBy);
    disposalSchedule.setUpdatedOn(new Date());

    String disposalScheduleAsJson = JsonUtils.getJsonFromObject(disposalSchedule);
    StoragePath disposalSchedulePath = ModelUtils.getDisposalScheduleStoragePath(disposalSchedule.getId());
    storage.createBinary(disposalSchedulePath, new StringContentPayload(disposalScheduleAsJson), false);

    return disposalSchedule;
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(DisposalSchedule disposalSchedule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    // Check if disposal schedule is ACTIVE to update
    DisposalSchedule currentDisposalSchedule = retrieveDisposalSchedule(disposalSchedule.getId());
    if (DisposalScheduleState.INACTIVE.equals(currentDisposalSchedule.getState())) {
      throw new IllegalOperationException("Error updating disposal schedule: " + currentDisposalSchedule.getId()
        + ". Reason: Disposal schedule is inactive therefore can not change its content");
    }

    currentDisposalSchedule.setTitle(disposalSchedule.getTitle());
    currentDisposalSchedule.setDescription(disposalSchedule.getDescription());
    currentDisposalSchedule.setMandate(disposalSchedule.getMandate());
    currentDisposalSchedule.setScopeNotes(disposalSchedule.getScopeNotes());
    currentDisposalSchedule.setUpdatedOn(new Date());
    currentDisposalSchedule.setUpdatedBy(updatedBy);

    try {
      DisposalRules disposalRules = listDisposalRules();
      Optional<DisposalRule> first = disposalRules.getObjects().stream()
        .filter(p -> p.getDisposalScheduleId().equals(disposalSchedule.getId())).findFirst();

      if (first.isEmpty() && (currentDisposalSchedule.getApiCounter() <= 0
        || !DisposalScheduleState.INACTIVE.equals(currentDisposalSchedule.getState()))) {
        currentDisposalSchedule.setState(disposalSchedule.getState());
      }
    } catch (IOException e) {
      throw new GenericException("Failed to obtain disposal rules", e);
    }

    if (currentDisposalSchedule.getFirstTimeUsed() == null) {
      currentDisposalSchedule.setFirstTimeUsed(disposalSchedule.getFirstTimeUsed());
    }

    String disposalScheduleAsJson = JsonUtils.getJsonFromObject(currentDisposalSchedule);
    StoragePath disposalSchedulePath = ModelUtils.getDisposalScheduleStoragePath(currentDisposalSchedule.getId());
    storage.updateBinaryContent(disposalSchedulePath, new StringContentPayload(disposalScheduleAsJson), false, false,
      false, null);

    return currentDisposalSchedule;
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String disposalScheduleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    if (disposalScheduleId == null) {
      throw new GenericException("Error retrieving disposal schedule identifier must not be null");
    }
    StoragePath disposalSchedulePath = ModelUtils.getDisposalScheduleStoragePath(disposalScheduleId);
    Binary binary = storage.getBinary(disposalSchedulePath);
    DisposalSchedule ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, DisposalSchedule.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading disposal schedule: " + disposalScheduleId, e);
    }

    Long count = SolrUtils.count(RodaCoreFactory.getSolr(), IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID, ret.getId())));
    ret.setApiCounter(count);

    return ret;
  }

  @Override
  public DisposalSchedules listDisposalSchedules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    StoragePath disposalScheduleContainerPath = ModelUtils.getDisposalScheduleContainerPath();
    DisposalSchedules disposalSchedules = new DisposalSchedules();

    try (CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(disposalScheduleContainerPath,
      false)) {
      for (Resource resource : iterable) {
        DisposalSchedule schedule = ResourceParseUtils.convertResourceToObject(resource, DisposalSchedule.class);
        disposalSchedules.addObject(schedule);
      }

    } catch (NotFoundException e) {
      LOGGER.error("Could not find any disposal schedules to list: {}", e.getMessage(), e);
      return disposalSchedules;
    }

    return disposalSchedules;
  }

  @Override
  public void deleteDisposalSchedule(String disposalScheduleId) throws NotFoundException, GenericException,
    AuthorizationDeniedException, RequestNotValidException, IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath disposalSchedulePath = ModelUtils.getDisposalScheduleStoragePath(disposalScheduleId);

    // check if the disposal schedule was used to destroy an AIP
    // if so, block the action and keep the disposal schedule
    if (retrieveDisposalSchedule(disposalScheduleId).getFirstTimeUsed() != null) {
      throw new IllegalOperationException("Error deleting disposal schedule: " + disposalScheduleId
        + ". Reason: One or more AIPs where destroyed under this disposal schedule");
    }

    // check if the disposal schedule is being used in a disposal rule
    // if so, block the action and keep the disposal schedule
    try {
      DisposalRules disposalRules = listDisposalRules();
      Optional<DisposalRule> first = disposalRules.getObjects().stream()
        .filter(p -> p.getDisposalScheduleId().equals(disposalScheduleId)).findFirst();
      if (first.isPresent()) {
        throw new IllegalOperationException("Error deleting disposal schedule: " + disposalScheduleId
          + ". Reason: This schedule is being used in a disposal rule");
      }
    } catch (IOException e) {
      throw new GenericException("Failed to obtain the disposal rules", e);
    }

    storage.deleteResource(disposalSchedulePath);
  }

  /**********************************
   * Disposal confirmation related
   **********************************/
  @Override
  public DisposalConfirmation retrieveDisposalConfirmation(String disposalConfirmationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(
      ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId),
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME);
    Binary binary = storage.getBinary(metadataStoragePath);
    DisposalConfirmation ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, DisposalConfirmation.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading disposal confirmation: " + disposalConfirmationId, e);
    }

    return ret;
  }

  @Override
  public void addDisposalHoldEntry(String disposalConfirmationId, DisposalHold disposalHold)
    throws GenericException, RequestNotValidException {
    StoragePath confirmationStoragePath = ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId);
    Path confirmationPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationStoragePath);

    Path file = FSUtils.createFile(confirmationPath,
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_HOLDS_FILENAME, true, true);

    JsonUtils.appendObjectToFile(disposalHold, file);
  }

  @Override
  public void addDisposalHoldTransitiveEntry(String disposalConfirmationId, DisposalHold transitiveDisposalHold)
    throws RequestNotValidException, GenericException {
    StoragePath confirmationStoragePath = ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId);
    Path confirmationPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationStoragePath);

    Path file = FSUtils.createFile(confirmationPath,
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_TRANSITIVE_HOLDS_FILENAME, true, true);

    JsonUtils.appendObjectToFile(transitiveDisposalHold, file);
  }

  @Override
  public void addDisposalScheduleEntry(String disposalConfirmationId, DisposalSchedule disposalSchedule)
    throws RequestNotValidException, GenericException {
    StoragePath confirmationStoragePath = ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId);
    Path confirmationPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationStoragePath);

    Path file = FSUtils.createFile(confirmationPath,
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_SCHEDULES_FILENAME, true, true);

    JsonUtils.appendObjectToFile(disposalSchedule, file);
  }

  @Override
  public void addAIPEntry(String disposalConfirmationId, DisposalConfirmationAIPEntry entry)
    throws RequestNotValidException, GenericException {
    StoragePath confirmationStoragePath = ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId);
    Path confirmationPath = FSUtils.getEntityPath(RodaCoreFactory.getStoragePath(), confirmationStoragePath);

    Path file = FSUtils.createFile(confirmationPath,
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME, true, true);

    JsonUtils.appendObjectToFile(entry, file);
  }

  @Override
  public DisposalConfirmation updateDisposalConfirmation(DisposalConfirmation disposalConfirmation)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    String disposalConfirmationAsJson = JsonUtils.getJsonFromObject(disposalConfirmation);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(
      ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmation.getId()),
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME);
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(disposalConfirmationAsJson), false, false,
      false, null);

    notifyDisposalConfirmationCreatedOrUpdated(disposalConfirmation).failOnError();
    return disposalConfirmation;
  }

  @Override
  public DisposalConfirmation createDisposalConfirmation(DisposalConfirmation disposalConfirmation, String createdBy)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (disposalConfirmation.getId() == null) {
      disposalConfirmation.setId(IdUtils.createUUID());
    }

    disposalConfirmation.setCreatedBy(createdBy);
    disposalConfirmation.setCreatedOn(new Date());

    String disposalConfirmationAsJson = JsonUtils.getJsonFromObject(disposalConfirmation);

    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(
      ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmation.getId()),
      RodaConstants.STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME);

    storage.createBinary(metadataStoragePath, new StringContentPayload(disposalConfirmationAsJson), false);
    notifyDisposalConfirmationCreatedOrUpdated(disposalConfirmation).failOnError();

    return disposalConfirmation;
  }

  @Override
  public void deleteDisposalConfirmation(String disposalConfirmationId) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath disposalSchedulePath = ModelUtils.getDisposalConfirmationStoragePath(disposalConfirmationId);
    DisposalConfirmation metadata = retrieveDisposalConfirmation(disposalConfirmationId);

    // check if the disposal confirmation is pending
    // if so the disposal confirmation can be deleted from the system
    if (DisposalConfirmationState.PENDING.equals(metadata.getState())) {
      storage.deleteResource(disposalSchedulePath);
      notifyDisposalConfirmationDeleted(disposalConfirmationId, false).failOnError();
    } else {
      throw new IllegalOperationException("Error deleting disposal confirmation: " + disposalConfirmationId
        + ". Reason: This confirmation state is " + metadata.getState().toString() + " and cannot be deleted");
    }
  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DisposalHoldsAIPMetadata disposalHoldsAIPMetadata = new DisposalHoldsAIPMetadata();
    disposalHoldsAIPMetadata.setObjects(ResourceParseUtils.getAIPMetadata(getStorage(), aipId).getHolds());
    return disposalHoldsAIPMetadata;
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveDisposalHolds(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DisposalTransitiveHoldsAIPMetadata list = new DisposalTransitiveHoldsAIPMetadata();
    list.setObjects(ResourceParseUtils.getAIPMetadata(getStorage(), aipId).getTransitiveHolds());
    return list;
  }

  /************************************
   * Disposal rule related
   ************************************/

  @Override
  public DisposalRule createDisposalRule(DisposalRule disposalRule, String createdBy) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (disposalRule.getId() == null) {
      disposalRule.setId(IdUtils.createUUID());
    }

    disposalRule.setCreatedBy(createdBy);
    disposalRule.setCreatedOn(new Date());
    disposalRule.setUpdatedBy(createdBy);
    disposalRule.setUpdatedOn(new Date());

    String disposalRuleAsJson = JsonUtils.getJsonFromObject(disposalRule);
    StoragePath disposalRulePath = ModelUtils.getDisposalRuleStoragePath(disposalRule.getId());
    storage.createBinary(disposalRulePath, new StringContentPayload(disposalRuleAsJson), false);

    return disposalRule;
  }

  @Override
  public DisposalRule updateDisposalRule(DisposalRule disposalRule, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    disposalRule.setUpdatedOn(new Date());
    disposalRule.setUpdatedBy(updatedBy);

    String disposalRuleAsJson = JsonUtils.getJsonFromObject(disposalRule);
    StoragePath disposalRulePath = ModelUtils.getDisposalRuleStoragePath(disposalRule.getId());
    storage.updateBinaryContent(disposalRulePath, new StringContentPayload(disposalRuleAsJson), false, false, false,
      null);

    return disposalRule;
  }

  @Override
  public void deleteDisposalRule(String disposalRuleId, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath disposalRulePath = ModelUtils.getDisposalRuleStoragePath(disposalRuleId);
    storage.deleteResource(disposalRulePath);

    DisposalRules disposalRules = listDisposalRules();
    int index = 0;
    for (DisposalRule rule : disposalRules.getObjects()) {
      rule.setOrder(index++);
      updateDisposalRule(rule, updatedBy);
    }
  }

  @Override
  public DisposalRule retrieveDisposalRule(String disposalRuleId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath disposalRulePath = ModelUtils.getDisposalRuleStoragePath(disposalRuleId);
    Binary binary = storage.getBinary(disposalRulePath);
    DisposalRule ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, DisposalRule.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading disposal rule: " + disposalRuleId, e);
    }

    return ret;
  }

  @Override
  public DisposalRules listDisposalRules()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    StoragePath disposalRuleContainerPath = ModelUtils.getDisposalRuleContainerPath();
    DisposalRules disposalRules = new DisposalRules();

    try (CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(disposalRuleContainerPath, false)) {
      for (Resource resource : iterable) {
        DisposalRule rule = ResourceParseUtils.convertResourceToObject(resource, DisposalRule.class);
        disposalRules.addObject(rule);
      }
      Collections.sort(disposalRules.getObjects());
    } catch (NotFoundException e) {
      LOGGER.error("Could not find any disposal rules to list: {}", e.getMessage(), e);
      return disposalRules;
    }

    return disposalRules;
  }

  /************************************
   * Disposal bin related
   ************************************/

  /************************************
   * Distributed instances system related
   ************************************/
  @Override
  public DistributedInstance createDistributedInstance(DistributedInstance distributedInstance, String createdBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, IllegalOperationException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (distributedInstance.getId() == null) {
      distributedInstance.setId(IdUtils.createUUID());
    }

    // Create a new User
    String username = RodaConstants.DISTRIBUTED_INSTANCE_USER_PREFIX + distributedInstance.getName();

    Set<String> roles = new HashSet<>();
    roles.add(RodaConstants.REPOSITORY_PERMISSIONS_DISTRIBUTED_INSTANCES_MANAGE);
    roles.add(RodaConstants.REPOSITORY_PERMISSIONS_LOCAL_INSTANCES_MANAGE);
    roles.add(RodaConstants.REPOSITORY_PERMISSIONS_JOB_READ);

    User user = new User(username);
    user.setAllRoles(roles);
    user.setDirectRoles(roles);

    User accessTokenUser = createUser(user, true);

    // Create a new Group
    RODAInstanceUtils.createDistributedGroup(user);

    // Create a new Access token
    AccessKey accessKey = new AccessKey();
    accessKey.setName(RodaConstants.DISTRIBUTED_INSTANCE_ACCESS_KEY_PREFIX + distributedInstance.getName()
      + RodaConstants.DISTRIBUTED_INSTANCE_ACCESS_KEY_SUFFIX);
    accessKey.setUserName(accessTokenUser.getName());
    createAccessKey(accessKey, createdBy);

    // Associate to access token
    distributedInstance.setAccessKeyId(accessKey.getId());
    distributedInstance.setUsername(username);

    distributedInstance.setCreatedOn(new Date());
    distributedInstance.setCreatedBy(createdBy);
    distributedInstance.setUpdatedOn(new Date());
    distributedInstance.setUpdatedBy(createdBy);

    String distributedInstanceAsJson = JsonUtils.getJsonFromObject(distributedInstance);
    StoragePath distributedInstancePath = ModelUtils.getDistributedInstanceStoragePath(distributedInstance.getId());
    storage.createBinary(distributedInstancePath, new StringContentPayload(distributedInstanceAsJson), false);

    return distributedInstance;
  }

  @Override
  public DistributedInstances listDistributedInstances()
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, IOException {
    StoragePath distributedInstancesContainerPath = ModelUtils.getDistributedInstancesContainerPath();
    DistributedInstances distributedInstances = new DistributedInstances();

    try {
      CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(distributedInstancesContainerPath,
        false);
      for (Resource resource : iterable) {
        DistributedInstance distributedInstance = ResourceParseUtils.convertResourceToObject(resource,
          DistributedInstance.class);
        distributedInstances.addObject(distributedInstance);
      }

    } catch (NotFoundException e) {
      LOGGER.debug("Could not find any distributed instance to list: {}", e.getMessage());
      return distributedInstances;
    }

    return distributedInstances;
  }

  @Override
  public DistributedInstance retrieveDistributedInstance(String distributedInstanceId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath distributedInstancePath = ModelUtils.getDistributedInstanceStoragePath(distributedInstanceId);
    Binary binary = null;
    try {
      binary = storage.getBinary(distributedInstancePath);
    } catch (NotFoundException e) {
      throw new GenericException("Could not find the distributed instance " + distributedInstanceId, e);
    }
    DistributedInstance ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, DistributedInstance.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading distributed instance: " + distributedInstanceId, e);
    }

    return ret;
  }

  @Override
  public void deleteDistributedInstance(String distributedInstanceId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath distributedInstancePath = ModelUtils.getDistributedInstanceStoragePath(distributedInstanceId);
    storage.deleteResource(distributedInstancePath);
  }

  @Override
  public DistributedInstance updateDistributedInstance(DistributedInstance distributedInstance, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    distributedInstance.setUpdatedOn(new Date());
    distributedInstance.setUpdatedBy(updatedBy);

    String distributedInstanceAsJson = JsonUtils.getJsonFromObject(distributedInstance);
    StoragePath distributedInstancePath = ModelUtils.getDistributedInstanceStoragePath(distributedInstance.getId());

    if (distributedInstancePath != null) {
      storage.updateBinaryContent(distributedInstancePath, new StringContentPayload(distributedInstanceAsJson), false,
        false, false, null);
    }
    return distributedInstance;
  }

  /************************************
   * Access Token related
   ************************************/
  @Override
  public AccessKey createAccessKey(AccessKey accessKey, String createdBy) throws GenericException,
    AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (accessKey.getId() == null) {
      accessKey.setId(IdUtils.createUUID());
    }

    if (accessKey.getExpirationDate() == null) {
      Date today = new Date();
      Date expirationDate = new Date(today.getTime() + RodaCoreFactory.getAccessKeyValidity());
      accessKey.setExpirationDate(expirationDate);
    }

    String token = JwtUtils.generateToken(accessKey.getUserName(), accessKey.getExpirationDate());

    accessKey.setKey(token);

    accessKey.setCreatedOn(new Date());
    accessKey.setCreatedBy(createdBy);
    accessKey.setUpdatedOn(new Date());
    accessKey.setUpdatedBy(createdBy);

    String accessKeyAsJson = JsonUtils.getJsonFromObject(accessKey);
    StoragePath accessKeyPath = ModelUtils.getAccessKeysStoragePath(accessKey.getId());
    storage.createBinary(accessKeyPath, new StringContentPayload(accessKeyAsJson), false);

    return accessKey;
  }

  @Override
  public AccessKeys listAccessKeys() throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    StoragePath accessKeysContainerPath = ModelUtils.getAccessKeysContainerPath();
    AccessKeys accessKeys = new AccessKeys();

    try {
      CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(accessKeysContainerPath, false);
      for (Resource resource : iterable) {
        AccessKey accessKey = ResourceParseUtils.convertResourceToObject(resource, AccessKey.class);
        accessKeys.addObject(accessKey);
      }

    } catch (NotFoundException | IOException e) {
      LOGGER.error("Could not find any access token to list: {}", e.getMessage(), e);
      return accessKeys;
    }

    return accessKeys;
  }

  @Override
  public AccessKey retrieveAccessKey(String accessKeyId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath accessKeysStoragePath = ModelUtils.getAccessKeysStoragePath(accessKeyId);
    Binary binary = storage.getBinary(accessKeysStoragePath);
    AccessKey ret;

    try (InputStream inputStream = binary.getContent().createInputStream()) {
      ret = JsonUtils.getObjectFromJson(inputStream, AccessKey.class);
    } catch (IOException | GenericException e) {
      throw new GenericException("Error reading access key: " + accessKeyId, e);
    }

    return ret;
  }

  @Override
  public AccessKey updateAccessKey(AccessKey accessKey, String updatedBy)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    accessKey.setUpdatedOn(new Date());
    accessKey.setUpdatedBy(updatedBy);

    String accessKeyAsJson = JsonUtils.getJsonFromObject(accessKey);
    StoragePath accessKeysStoragePath = ModelUtils.getAccessKeysStoragePath(accessKey.getId());
    storage.updateBinaryContent(accessKeysStoragePath, new StringContentPayload(accessKeyAsJson), false, false, false,
      null);

    return accessKey;
  }

  @Override
  public void deleteAccessKey(String accessKeyId)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    StoragePath accessKeysStoragePath = ModelUtils.getAccessKeysStoragePath(accessKeyId);
    storage.deleteResource(accessKeysStoragePath);
  }

  @Override
  public void updateAccessKeyLastUsageDate(AccessKey accessKey)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    if (accessKey.getStatus().equals(AccessKeyStatus.CREATED)) {
      accessKey.setStatus(AccessKeyStatus.ACTIVE);
    }
    String accessKeyAsJson = JsonUtils.getJsonFromObject(accessKey);
    StoragePath accessKeysStoragePath = ModelUtils.getAccessKeysStoragePath(accessKey.getId());
    storage.updateBinaryContent(accessKeysStoragePath, new StringContentPayload(accessKeyAsJson), false, false, false,
      null);
  }

  @Override
  public AccessKeys listAccessKeysByUser(String userId)
    throws RequestNotValidException, AuthorizationDeniedException, GenericException {
    StoragePath accessKeysContainerPath = ModelUtils.getAccessKeysContainerPath();
    AccessKeys accessKeys = new AccessKeys();

    try {
      CloseableIterable<Resource> iterable = storage.listResourcesUnderDirectory(accessKeysContainerPath, false);
      for (Resource resource : iterable) {
        AccessKey accessKey = ResourceParseUtils.convertResourceToObject(resource, AccessKey.class);
        if (accessKey.getUserName().equals(userId)) {
          accessKeys.addObject(accessKey);
        }
      }

    } catch (NotFoundException | IOException e) {
      LOGGER.error("Could not find any access token to list: {}", e.getMessage(), e);
      return accessKeys;
    }

    return accessKeys;
  }

  @Override
  public void deactivateUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    AccessKeys accessKeys = listAccessKeysByUser(userId);

    for (AccessKey accessKey : accessKeys.getObjects()) {
      accessKey.setStatus(AccessKeyStatus.INACTIVE);
      updateAccessKey(accessKey, updatedBy);
    }
  }

  @Override
  public void deleteUserAccessKeys(String userId, String updatedBy)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    AccessKeys accessKeys = listAccessKeysByUser(userId);

    for (AccessKey accessKey : accessKeys.getObjects()) {
      deleteAccessKey(accessKey.getId());
    }
  }

  @Override
  public Date retrieveFileCreationDate(File file) throws RequestNotValidException, GenericException {
    return storage.getCreationDate(ModelUtils.getFileStoragePath(file));
  }

  @Override
  public Date retrievePreservationMetadataCreationDate(PreservationMetadata pm)
    throws RequestNotValidException, GenericException {
    return storage.getCreationDate(ModelUtils.getPreservationMetadataStoragePath(pm));
  }

  // Observable methods
  @Override
  public void addModelObserver(ModelObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeModelObserver(ModelObserver observer) {
    observers.remove(observer);
  }

  private ReturnWithExceptionsWrapper notifyObserversSafely(Function<ModelObserver, ReturnWithExceptions<?, ?>> func) {
    ReturnWithExceptionsWrapper wrapper = new ReturnWithExceptionsWrapper();
    for (ModelObserver observer : observers) {
      try {
        wrapper.addToList(func.apply(observer));
      } catch (Exception e) {
        LOGGER.error("Error invoking method in observer {}", observer.getClass().getSimpleName(), e);
        // do nothing, just want to sandbox observer method invocation
      }
    }
    return wrapper;
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipCreated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipCreated(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipUpdated(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipUpdatedOnChanged(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipUpdatedOn(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipDestroyed(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipDestroyed(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipMoved(AIP aip, String oldParentId, String newParentId) {
    return notifyObserversSafely(observer -> observer.aipMoved(aip, oldParentId, newParentId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipStateUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipStateUpdated(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipInstanceIdUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipInstanceIdUpdated(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipDeleted(String aipId) {
    return notifyObserversSafely(observer -> observer.aipDeleted(aipId, true));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataCreated(DescriptiveMetadata descriptiveMetadata) {
    return notifyObserversSafely(observer -> observer.descriptiveMetadataCreated(descriptiveMetadata));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataUpdated(DescriptiveMetadata descriptiveMetadata) {
    return notifyObserversSafely(observer -> observer.descriptiveMetadataUpdated(descriptiveMetadata));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDescriptiveMetadataDeleted(String aipId, String representationId,
    String descriptiveMetadataBinaryId) {
    return notifyObserversSafely(
      observer -> observer.descriptiveMetadataDeleted(aipId, representationId, descriptiveMetadataBinaryId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationCreated(Representation representation) {
    return notifyObserversSafely(observer -> observer.representationCreated(representation));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationUpdated(Representation representation) {
    return notifyObserversSafely(observer -> observer.representationUpdated(representation));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationDeleted(String aipId, String representationId) {
    return notifyObserversSafely(observer -> observer.representationDeleted(aipId, representationId, true));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationUpdatedOnChanged(Representation representation) {
    return notifyObserversSafely(observer -> observer.representationUpdatedOn(representation));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileCreated(File file) {
    return notifyObserversSafely(observer -> observer.fileCreated(file));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileUpdated(File file) {
    return notifyObserversSafely(observer -> observer.fileUpdated(file));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyFileDeleted(String aipId, String representationId,
    List<String> fileDirectoryPath, String fileId) {
    return notifyObserversSafely(
      observer -> observer.fileDeleted(aipId, representationId, fileDirectoryPath, fileId, true));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyLogEntryCreated(LogEntry entry) {
    return notifyObserversSafely(observer -> observer.logEntryCreated(entry));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserCreated(User user) {
    return notifyObserversSafely(observer -> observer.userCreated(user));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserUpdated(User user) {
    return notifyObserversSafely(observer -> observer.userUpdated(user));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyUserDeleted(String userID) {
    return notifyObserversSafely(observer -> observer.userDeleted(userID));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupCreated(Group group) {
    return notifyObserversSafely(observer -> observer.groupCreated(group));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupUpdated(Group group) {
    return notifyObserversSafely(observer -> observer.groupUpdated(group));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyGroupDeleted(String groupID) {
    return notifyObserversSafely(observer -> observer.groupDeleted(groupID));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataCreated(
    PreservationMetadata preservationMetadataBinary) {
    return notifyObserversSafely(observer -> observer.preservationMetadataCreated(preservationMetadataBinary));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataUpdated(
    PreservationMetadata preservationMetadataBinary) {
    return notifyObserversSafely(observer -> observer.preservationMetadataUpdated(preservationMetadataBinary));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyPreservationMetadataDeleted(PreservationMetadata pm) {
    return notifyObserversSafely(observer -> observer.preservationMetadataDeleted(pm));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyOtherMetadataCreated(OtherMetadata otherMetadataBinary) {
    return notifyObserversSafely(observer -> observer.otherMetadataCreated(otherMetadataBinary));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobCreatedOrUpdated(Job job, boolean reindexJobReports) {
    return notifyObserversSafely(observer -> observer.jobCreatedOrUpdated(job, reindexJobReports));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobDeleted(String jobId) {
    return notifyObserversSafely(observer -> observer.jobDeleted(jobId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, Job cachedJob) {
    return notifyObserversSafely(observer -> observer.jobReportCreatedOrUpdated(jobReport, cachedJob));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportCreatedOrUpdated(Report jobReport, IndexedJob indexedJob) {
    return notifyObserversSafely(observer -> observer.jobReportCreatedOrUpdated(jobReport, indexedJob));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyJobReportDeleted(String jobReportId) {
    return notifyObserversSafely(observer -> observer.jobReportDeleted(jobReportId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyAipPermissionsUpdated(AIP aip) {
    return notifyObserversSafely(observer -> observer.aipPermissionsUpdated(aip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDipPermissionsUpdated(DIP dip) {
    return notifyObserversSafely(observer -> observer.dipPermissionsUpdated(dip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDipInstanceIdUpdated(DIP dip) {
    return notifyObserversSafely(observer -> observer.dipInstanceIdUpdated(dip));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyTransferredResourceDeleted(String transferredResourceID) {
    return notifyObserversSafely(observer -> observer.transferredResourceDeleted(transferredResourceID));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskCreatedOrUpdated(Risk risk, int incidences, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskCreatedOrUpdated(risk, incidences, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskDeleted(String riskId, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskDeleted(riskId, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskIncidenceCreatedOrUpdated(RiskIncidence riskIncidence, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskIncidenceCreatedOrUpdated(riskIncidence, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRiskIncidenceDeleted(String riskIncidenceId, boolean commit) {
    return notifyObserversSafely(observer -> observer.riskIncidenceDeleted(riskIncidenceId, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationInformationCreatedOrUpdated(RepresentationInformation ri,
    boolean commit) {
    return notifyObserversSafely(observer -> observer.representationInformationCreatedOrUpdated(ri, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyRepresentationInformationDeleted(String representationInformationId,
    boolean commit) {
    return notifyObserversSafely(
      observer -> observer.representationInformationDeleted(representationInformationId, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyNotificationCreatedOrUpdated(Notification notification) {
    return notifyObserversSafely(observer -> observer.notificationCreatedOrUpdated(notification));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyNotificationDeleted(String notificationId) {
    return notifyObserversSafely(observer -> observer.notificationDeleted(notificationId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPCreated(DIP dip, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipCreated(dip, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPUpdated(DIP dip, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipUpdated(dip, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPDeleted(String dipId, boolean commit) {
    return notifyObserversSafely(observer -> observer.dipDeleted(dipId, commit));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileCreated(DIPFile file) {
    return notifyObserversSafely(observer -> observer.dipFileCreated(file));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileUpdated(DIPFile file) {
    return notifyObserversSafely(observer -> observer.dipFileUpdated(file));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDIPFileDeleted(String dipId, List<String> path, String fileId) {
    return notifyObserversSafely(observer -> observer.dipFileDeleted(dipId, path, fileId));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDisposalConfirmationCreatedOrUpdated(DisposalConfirmation confirmation) {
    return notifyObserversSafely(observer -> observer.disposalConfirmationCreateOrUpdate(confirmation));
  }

  @Override
  public ReturnWithExceptionsWrapper notifyDisposalConfirmationDeleted(String disposalConfirmationId, boolean commit) {
    return notifyObserversSafely(observer -> observer.disposalConfirmationDeleted(disposalConfirmationId, commit));
  }

  /************************************
   * Storage Utils
   ************************************/
  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, IsRODAObject object, String... pathPartials)
    throws GenericException, RequestNotValidException {
    return resolveTemporaryResourceShallow(jobId, getStorage(), object, pathPartials);
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, IsRODAObject object,
    String... pathPartials) throws GenericException, RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials);
    FileStorageService temporaryStorage;
    Path tempPath = RodaCoreFactory.getFileShallowTmpDirectoryPath().resolve(jobId)
      .resolve(String.valueOf(storagePath.hashCode()));
    try {
      if (Files.exists(tempPath)) {
        temporaryStorage = new FileStorageService(tempPath, false, null, false);
      } else {
        Files.createDirectories(tempPath);
        temporaryStorage = new FileStorageService(tempPath, false, null, false);

        temporaryStorage.copy(storage, storagePath, storagePath);
        List<StoragePath> externalFiles = temporaryStorage.getShallowFiles(storagePath);
        for (StoragePath externalFile : externalFiles) {
          final CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderFile(externalFile, true);
          for (Resource resource : resources) {
            if (resource instanceof DefaultBinary) {
              ContentPayload content = ((DefaultBinary) resource).getContent();
              if (content instanceof JsonContentPayload) {
                ShallowFile shallowFile = JsonUtils.getObjectFromJson(content.createInputStream(), ShallowFile.class);
                Protocol pm = RodaCoreFactory.getProtocol(shallowFile.getLocation());
                pm.downloadResource(temporaryStorage.getDirectAccess(externalFile).getPath().getParent());
              }

            }
          }
          temporaryStorage.deleteResource(externalFile);
        }
      }
    } catch (IOException | AlreadyExistsException | AuthorizationDeniedException e) {
      throw new GenericException("Cannot create temporary directory " + tempPath);
    } catch (RequestNotValidException | NotFoundException e) {
      throw new GenericException("Cannot found resources at " + storagePath.toString());
    }
    return temporaryStorage;
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    return resolveTemporaryResourceShallow(jobId, getStorage(), lite, pathPartials);
  }

  @Override
  public StorageService resolveTemporaryResourceShallow(String jobId, StorageService storage, LiteRODAObject lite,
    String... pathPartials) throws GenericException, RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    FileStorageService temporaryStorage;
    Path tempPath = RodaCoreFactory.getFileShallowTmpDirectoryPath().resolve(jobId)
      .resolve(String.valueOf(storagePath.hashCode()));
    try {
      if (Files.exists(tempPath)) {
        temporaryStorage = new FileStorageService(tempPath, false, null, false);
      } else {
        Files.createDirectories(tempPath);
        temporaryStorage = new FileStorageService(tempPath, false, null, false);

        temporaryStorage.copy(storage, storagePath, storagePath);
        List<StoragePath> externalFiles = temporaryStorage.getShallowFiles(storagePath);
        for (StoragePath externalFile : externalFiles) {
          final CloseableIterable<Resource> resources = temporaryStorage.listResourcesUnderFile(externalFile, true);
          for (Resource resource : resources) {
            if (resource instanceof DefaultBinary) {
              ContentPayload content = ((DefaultBinary) resource).getContent();
              if (content instanceof JsonContentPayload) {
                ShallowFile shallowFile = JsonUtils.getObjectFromJson(content.createInputStream(), ShallowFile.class);
                Protocol pm = RodaCoreFactory.getProtocol(shallowFile.getLocation());
                pm.downloadResource(temporaryStorage.getDirectAccess(externalFile).getPath().getParent());
              }

            }
          }
          temporaryStorage.deleteResource(externalFile);
        }
      }
    } catch (IOException | AlreadyExistsException | AuthorizationDeniedException e) {
      throw new GenericException("Cannot create temporary directory " + tempPath);
    } catch (RequestNotValidException | NotFoundException e) {
      throw new GenericException("Cannot found resources at " + storagePath.toString());
    }
    return temporaryStorage;
  }

  @Override
  public Binary getBinary(IsRODAObject object, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    return getStorage().getBinary(DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials));
  }

  @Override
  public Binary getBinary(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    return getStorage().getBinary(DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials));
  }

  @Override
  public BinaryVersion getBinaryVersion(IsRODAObject object, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object),
      pathPartials.toArray(new String[0]));
    return getStorage().getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion getBinaryVersion(LiteRODAObject lite, String version, List<String> pathPartials)
    throws RequestNotValidException, NotFoundException, GenericException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite),
      pathPartials.toArray(new String[0]));
    return getStorage().getBinaryVersion(storagePath, version);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(IsRODAObject object)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    StoragePath storagePath = ModelUtils.getStoragePath(object);
    return getStorage().listBinaryVersions(storagePath);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(LiteRODAObject lite)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    StoragePath storagePath = ModelUtils.getStoragePath(lite);
    return getStorage().listBinaryVersions(storagePath);
  }

  @Override
  public void deleteBinaryVersion(IsRODAObject object, String version)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    getStorage().deleteBinaryVersion(ModelUtils.getStoragePath(object), version);
  }

  @Override
  public void deleteBinaryVersion(LiteRODAObject lite, String version)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    getStorage().deleteBinaryVersion(ModelUtils.getStoragePath(lite), version);
  }

  @Override
  public Binary updateBinaryContent(IsRODAObject object, ContentPayload payload, boolean asReference,
    boolean createIfNotExists, boolean snapshotCurrentVersion, Map<String, String> properties)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    StoragePath storagePath = ModelUtils.getStoragePath(object);
    return getStorage().updateBinaryContent(storagePath, payload, asReference, createIfNotExists,
      snapshotCurrentVersion, properties);
  }

  @Override
  public Binary updateBinaryContent(LiteRODAObject lite, ContentPayload payload, boolean asReference,
    boolean createIfNotExists, boolean snapshotCurrentVersion, Map<String, String> properties)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    StoragePath storagePath = ModelUtils.getStoragePath(lite);
    return getStorage().updateBinaryContent(storagePath, payload, asReference, createIfNotExists,
      snapshotCurrentVersion, properties);
  }

  @Override
  public Directory createDirectory(IsRODAObject object, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials);
    return getStorage().createDirectory(storagePath);
  }

  @Override
  public Directory createDirectory(LiteRODAObject lite, String... pathPartials)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException, RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    return getStorage().createDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(IsRODAObject object, String... pathPartials) throws RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials);
    return getStorage().hasDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    return getStorage().hasDirectory(storagePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject obj, StorageService storage, String... pathPartials)
    throws RequestNotValidException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(obj), pathPartials);
    return storage.getDirectAccess(storagePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject lite, StorageService storage, String... pathPartials)
    throws RequestNotValidException, GenericException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    return storage.getDirectAccess(storagePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(IsRODAObject obj, String... pathPartials)
    throws RequestNotValidException {
    return getDirectAccess(obj, getStorage(), pathPartials);
  }

  @Override
  public DirectResourceAccess getDirectAccess(LiteRODAObject liteObj, String... pathPartials)
    throws RequestNotValidException, GenericException {
    return getDirectAccess(liteObj, getStorage(), pathPartials);
  }

  // TODO: Review these import methods to see if IndexService really needs to be
  // in Model
  @Override
  public int importAll(IndexService index, final FileStorageService fromStorage, final boolean importJobs)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException {
    int total = 0;

    for (Container container : fromStorage.listContainers()) {
      // Let local instance handle job creation
      if (!RodaConstants.STORAGE_CONTAINER_JOB.equals(container.getStoragePath().getName()) || importJobs) {
        for (Resource resource : fromStorage.listResourcesUnderContainer(container.getStoragePath(), false)) {
          StoragePath storagePath = resource.getStoragePath();
          if (RodaConstants.STORAGE_CONTAINER_PRESERVATION.equals(container.getStoragePath().getName())
            || RodaConstants.STORAGE_CONTAINER_JOB_REPORT.equals(container.getStoragePath().getName())) {
            CloseableIterable<Resource> resources = fromStorage.listResourcesUnderDirectory(resource.getStoragePath(),
              true);
            for (Resource pmResource : resources) {
              StoragePath pmStoragePath = pmResource.getStoragePath();
              importResource(index, fromStorage, pmResource, pmStoragePath);
            }
          } else {
            importResource(index, fromStorage, resource, storagePath);
          }
          total++;
        }
      }
    }
    return total;
  }

  private void importResource(IndexService index, FileStorageService tmpStorage, Resource resource,
    StoragePath storagePath) throws NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, RequestNotValidException {

    // if the resource already exists, remove it before moving the updated resource
    if (getStorage().exists(storagePath)) {
      getStorage().deleteResource(storagePath);
    }
    getStorage().copy(tmpStorage, storagePath, storagePath);
    reindexResource(index, resource);
  }

  private void reindexResource(IndexService index, Resource resource)
    throws AuthorizationDeniedException, GenericException, NotFoundException {
    String containerName = resource.getStoragePath().getContainerName();
    Class<IsRODAObject> objectClass = ModelUtils.giveRespectiveModelClassFromContainerName(containerName);

    if (Report.class.equals(objectClass) || PreservationMetadata.class.equals(objectClass)) {
      if (resource.isDirectory()) {
        return;
      }
    }

    OptionalWithCause<LiteRODAObject> liteRODAObject = ResourceParseUtils.convertResourceToLite(resource, objectClass);
    if (liteRODAObject.isPresent()) {
      OptionalWithCause<IsModelObject> rodaObject = retrieveObjectFromLite(liteRODAObject.get());
      if (rodaObject.isPresent()) {
        if (PreservationMetadata.class.equals(objectClass)) {
          notifyPreservationMetadataCreated((PreservationMetadata) rodaObject.get()).failOnError();
        } else if (Report.class.equals(objectClass)) {
          String jobId = ModelUtils.getJobAndReportIds(resource.getStoragePath()).get(0);
          IndexedJob job = index.retrieve(IndexedJob.class, jobId, Arrays.asList());
          notifyJobReportCreatedOrUpdated((Report) rodaObject.get(), job);
        } else {
          clearSpecificIndexes(index, objectClass, rodaObject.get());
          index.reindex(rodaObject.get());
        }
      }
    }
  }

  private static void clearSpecificIndexes(IndexService index, Class<IsRODAObject> objectClass,
    IsModelObject rodaObject) throws AuthorizationDeniedException {
    if (AIP.class.equals(objectClass)) {
      List<String> ids = Arrays.asList(rodaObject.getId());
      index.delete(IndexedRepresentation.class,
        new Filter(new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, ids)));
      index.delete(IndexedFile.class, new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_AIP_ID, ids)));
      index.delete(IndexedPreservationEvent.class,
        new Filter(new OneOfManyFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, ids)));
    }
  }

  @Override
  public void exportAll(StorageService toStorage) {
    // TODO
  }

  @Override
  public void importObject(IsRODAObject object, StorageService fromStorage) {
    // TODO
  }

  @Override
  public void exportObject(IsRODAObject object, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException {
    StoragePath objectPath = ModelUtils.getStoragePath(object);
    toStorage.copy(getStorage(), objectPath, DefaultStoragePath.parse(toPathPartials));
  }

  @Override
  public void exportObject(LiteRODAObject lite, StorageService toStorage, String... toPathPartials)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    StoragePath litePath = ModelUtils.getStoragePath(lite);
    toStorage.copy(getStorage(), litePath, DefaultStoragePath.parse(toPathPartials));
  }

  @Override
  public void exportToPath(IsRODAObject object, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    StoragePath sourceObjectPath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), fromPathPartials);
    getStorage().copy(getStorage(), sourceObjectPath, toPath, "", replaceExisting);
  }

  public void exportToPath(LiteRODAObject lite, Path toPath, boolean replaceExisting, String... fromPathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException, GenericException {
    StoragePath sourceObjectPath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), fromPathPartials);
    getStorage().copy(getStorage(), sourceObjectPath, toPath, "", replaceExisting);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String... objectPathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return exportObjectToStream(object, null, false, objectPathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String... objectPathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return exportObjectToStream(lite, null, false, objectPathPartials);
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {
    ConsumesOutputStream stream;

    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials);
    final Class<? extends Entity> entity = getStorage().getEntity(storagePath);

    if (entity.equals(Binary.class) || entity.equals(DefaultBinary.class)) {
      // Send the one file
      stream = new BinaryConsumesOutputStream(getBinary(object, pathPartials));
    } else {
      // Send zip with directory contents
      stream = exportObjectToZip(object, name, addTopDirectory, pathPartials);
    }
    return stream;
  }

  @Override
  public ConsumesOutputStream exportObjectToStream(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    ConsumesOutputStream stream;

    final DirectResourceAccess directAccess = getDirectAccess(lite, pathPartials);

    if (directAccess.isDirectory()) {
      // Send zip with directory contents
      stream = exportObjectToZip(lite, name, addTopDirectory, pathPartials);
    } else {
      // Send the one file
      stream = new BinaryConsumesOutputStream(getBinary(lite, pathPartials));
    }
    return stream;
  }

  private ConsumesOutputStream exportObjectToZip(IsRODAObject object, String name, boolean addTopDirectory,
    String... pathPartials) throws RequestNotValidException {
    final StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(object), pathPartials);
    final String fileName = name == null ? storagePath.getName() : name;

    return new ConsumesOutputStream() {
      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(out);
          ZipOutputStream zos = new ZipOutputStream(bos);
          CloseableIterable<Resource> resources = getStorage().listResourcesUnderDirectory(storagePath, true);) {
          int basePathSize = storagePath.asList().size();

          for (Resource r : resources) {
            List<String> pathAsList = r.getStoragePath().asList();
            List<String> relativePathAsList = pathAsList.subList(basePathSize, pathAsList.size());
            String entryPath = relativePathAsList.stream().collect(Collectors.joining(ZIP_PATH_DELIMITER));
            String entryDirectoryPath;
            if (addTopDirectory) {
              entryDirectoryPath = storagePath.getName() + ZIP_PATH_DELIMITER + entryPath;
            } else {
              entryDirectoryPath = entryPath;
            }
            if (r.isDirectory()) {
              // adding a directory
              entryDirectoryPath += ZIP_PATH_DELIMITER;
              zos.putNextEntry(new ZipEntry(entryDirectoryPath));
              zos.closeEntry();
            } else {
              // adding a file
              ZipEntry entry = new ZipEntry(entryDirectoryPath);
              zos.putNextEntry(entry);
              Binary binary = getStorage().getBinary(r.getStoragePath());
              try (InputStream inputStream = binary.getContent().createInputStream()) {
                IOUtils.copy(inputStream, zos);
              }
              zos.closeEntry();
            }
          }
        } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
          throw new IOException(e);
        }
      }

      @Override
      public String getFileName() {
        return fileName + ZIP_FILE_NAME_EXTENSION;
      }

      @Override
      public String getMediaType() {
        return ZIP_MEDIA_TYPE;
      }

      @Override
      public Date getLastModified() {
        return null;
      }

      @Override
      public long getSize() {
        return -1;
      }
    };
  }

  public ConsumesOutputStream exportObjectToZip(LiteRODAObject lite, String name, boolean addTopDirectory,
    String... pathPartials) throws RequestNotValidException, GenericException {
    final StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    final String fileName = name == null ? storagePath.getName() : name;

    return new ConsumesOutputStream() {
      @Override
      public void consumeOutputStream(OutputStream out) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(out);
          ZipOutputStream zos = new ZipOutputStream(bos);
          CloseableIterable<Resource> resources = getStorage().listResourcesUnderDirectory(storagePath, true);) {
          int basePathSize = storagePath.asList().size();

          for (Resource r : resources) {
            List<String> pathAsList = r.getStoragePath().asList();
            List<String> relativePathAsList = pathAsList.subList(basePathSize, pathAsList.size());
            String entryPath = relativePathAsList.stream().collect(Collectors.joining(ZIP_PATH_DELIMITER));
            String entryDirectoryPath;
            if (addTopDirectory) {
              entryDirectoryPath = storagePath.getName() + ZIP_PATH_DELIMITER + entryPath;
            } else {
              entryDirectoryPath = entryPath;
            }
            if (r.isDirectory()) {
              // adding a directory
              entryDirectoryPath += ZIP_PATH_DELIMITER;
              zos.putNextEntry(new ZipEntry(entryDirectoryPath));
              zos.closeEntry();
            } else {
              // adding a file
              ZipEntry entry = new ZipEntry(entryDirectoryPath);
              zos.putNextEntry(entry);
              Binary binary = getStorage().getBinary(r.getStoragePath());
              try (InputStream inputStream = binary.getContent().createInputStream()) {
                IOUtils.copy(inputStream, zos);
              }
              zos.closeEntry();
            }
          }
        } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
          throw new IOException(e);
        }
      }

      @Override
      public String getFileName() {
        return fileName + ZIP_FILE_NAME_EXTENSION;
      }

      @Override
      public String getMediaType() {
        return ZIP_MEDIA_TYPE;
      }

      @Override
      public Date getLastModified() {
        return null;
      }

      @Override
      public long getSize() {
        return -1;
      }
    };
  }

  @Override
  public void moveObject(LiteRODAObject fromObject, LiteRODAObject toObject) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    StoragePath fromPath = ModelUtils.getStoragePath(fromObject);
    StoragePath toPath = ModelUtils.getStoragePath(toObject);
    StorageService storageService = getStorage();
    storageService.move(storageService, fromPath, toPath);
  }

  @Override
  public String getObjectPathAsString(IsRODAObject object, boolean skipContainer) throws RequestNotValidException {
    StoragePath objectPath = ModelUtils.getStoragePath(object);
    return getStorage().getStoragePathAsString(objectPath, skipContainer);
  }

  @Override
  public String getObjectPathAsString(LiteRODAObject lite, boolean skipContainer)
    throws RequestNotValidException, GenericException {
    StoragePath objectPath = ModelUtils.getStoragePath(lite);
    return getStorage().getStoragePathAsString(objectPath, skipContainer);
  }

  @Override
  public boolean existsInStorage(LiteRODAObject lite, String... pathPartials)
    throws RequestNotValidException, GenericException {
    StoragePath storagePath = DefaultStoragePath.parse(ModelUtils.getStoragePath(lite), pathPartials);
    return getStorage().exists(storagePath);
  }
}
