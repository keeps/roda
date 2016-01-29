/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.validation.ParseError;
import org.roda.core.common.validation.ValidationException;
import org.roda.core.common.validation.ValidationReport;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.GroupAlreadyExistsException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPPermissions;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.ip.metadata.Metadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationLinkingAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.ip.metadata.RepresentationFilePreservationObject;
import org.roda.core.data.v2.ip.metadata.RepresentationPreservationObject;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.EventOutcomeDetailComplexType;
import lc.xmlns.premisV2.EventOutcomeInformationComplexType;
import lc.xmlns.premisV2.ExtensionComplexType;
import lc.xmlns.premisV2.LinkingObjectIdentifierComplexType;

/**
 * Class that "relates" Model & Storage
 * 
 * XXX assumptions:
 * 
 * 1) when creating or updating stuff, metadata will be already set and
 * therefore to instantiate {@link DescriptiveMetadata}, {@link File} and
 * {@link Representation} one just need to read those values from
 * object.getMetadata()
 * 
 * 2) ATM, files beneath a certain representation can be represented as a flat
 * list and therefore no folders are supported. to support folders, we need to
 * re-think how to represent files in a representation (ATM those are
 * represented by a list of strings<=>name) and change all methods that deal
 * with representation
 * 
 * FIXME questions:
 * 
 * 1) how to undo things created/changed upon exceptions??? if using fedora
 * perhaps with transactions
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ModelService extends ModelObservable {

  private static final String AIP_METADATA_FILENAME = "aip.json";

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

  private void createAIPMetadata(AIP aip) throws RequestNotValidException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, NotFoundException {
    createAIPMetadata(aip, ModelUtils.getAIPpath(aip.getId()));
  }

  private void createAIPMetadata(AIP aip, StoragePath storagePath) throws RequestNotValidException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    String json = ModelUtils.getJsonFromObject(aip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath, AIP_METADATA_FILENAME);
    boolean asReference = false;
    storage.createBinary(metadataStoragePath, new StringContentPayload(json), asReference);
  }

  private void updateAIPMetadata(AIP aip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    updateAIPMetadata(aip, ModelUtils.getAIPpath(aip.getId()));
  }

  private void updateAIPMetadata(AIP aip, StoragePath storagePath)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    String json = ModelUtils.getJsonFromObject(aip);
    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath, AIP_METADATA_FILENAME);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(metadataStoragePath, new StringContentPayload(json), asReference, createIfNotExists);
  }

  private AIP getAIPMetadata(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getAIPMetadata(aipId, ModelUtils.getAIPpath(aipId));
  }

  private AIP getAIPMetadata(StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return getAIPMetadata(storagePath.getName(), storagePath);
  }

  private AIP getAIPMetadata(String aipId, StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    DefaultStoragePath metadataStoragePath = DefaultStoragePath.parse(storagePath, AIP_METADATA_FILENAME);
    Binary binary = storage.getBinary(metadataStoragePath);

    String json;
    AIP aip;
    try {
      json = IOUtils.toString(binary.getContent().createInputStream());
      aip = ModelUtils.getObjectFromJson(json, AIP.class);
    } catch (IOException e) {
      throw new GenericException("Could not parse AIP metadata", e);
    }

    // Setting information that does not come in JSON
    aip.setId(aipId);

    return aip;
  }

  public ClosableIterable<AIP> listAIPs()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    ClosableIterable<AIP> aipsIterable;

    final ClosableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getAIPcontainerPath());
    Iterator<Resource> resourcesIterator = resourcesIterable.iterator();

    aipsIterable = new ClosableIterable<AIP>() {

      @Override
      public Iterator<AIP> iterator() {
        return new Iterator<AIP>() {

          @Override
          public boolean hasNext() {
            if (resourcesIterator == null) {
              return false;
            }
            return resourcesIterator.hasNext();
          }

          @Override
          public AIP next() {
            try {
              Resource next = resourcesIterator.next();
              return getAIPMetadata(next.getStoragePath());
            } catch (NoSuchElementException | NotFoundException | GenericException | RequestNotValidException
              | AuthorizationDeniedException e) {
              LOGGER.error("Error while listing AIPs", e);
              return null;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

      @Override
      public void close() throws IOException {
        resourcesIterable.close();
      }
    };

    return aipsIterable;
  }

  public AIP retrieveAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Directory directory = storage.getDirectory(ModelUtils.getAIPpath(aipId));
    return getAIPMetadata(directory.getStoragePath());
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
   */
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    // TODO verify structure of source AIP and copy it to the storage
    // XXX possible optimization would be to allow move between storage
    // TODO support asReference
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport<String> validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {

      storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPpath(aipId));
      Directory newDirectory = storage.getDirectory(ModelUtils.getAIPpath(aipId));

      aip = getAIPMetadata(newDirectory.getStoragePath());
      if (notify) {
        notifyAipCreated(aip);
      }
    } else {
      throw new GenericException("Error while creating AIP, reason: AIP is not valid");
    }

    return aip;
  }

  public AIP createAIP(String parentId) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    boolean active = true;
    AIPPermissions permissions = new AIPPermissions();
    boolean notify = true;
    return createAIP(active, parentId, permissions, notify);
  }

  public AIP createAIP(boolean active, String parentId, AIPPermissions permissions) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    boolean notify = true;
    return createAIP(active, parentId, permissions, notify);
  }

  public AIP createAIP(boolean active, String parentId, AIPPermissions permissions, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {

    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP));

    String id = directory.getStoragePath().getName();
    Metadata metadata = new Metadata();
    List<Representation> representations = new ArrayList<>();

    AIP aip = new AIP(id, parentId, active, permissions, metadata, representations);

    createAIPMetadata(aip, directory.getStoragePath());

    if (notify) {
      notifyAipCreated(aip);
    }

    return aip;
  }

  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    return createAIP(aipId, sourceStorage, sourcePath, true);
  }

  // TODO support asReference
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport<String> validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);

      // FIXME is this the best way?
      storage.deleteResource(aipPath);

      storage.copy(sourceStorage, sourcePath, aipPath);
      Directory directoryUpdated = storage.getDirectory(aipPath);

      aip = getAIPMetadata(directoryUpdated.getStoragePath());
      notifyAipUpdated(aip);
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  public AIP updateAIP(AIP aip) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, ValidationException {
    StoragePath aipPath = ModelUtils.getAIPpath(aip.getId());
    Directory aipDirectory = storage.getDirectory(aipPath);
    ValidationReport<String> validationReport = isAIPvalid(this, aipDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {
      updateAIPMetadata(aip, aipPath);
      notifyAipUpdated(aip);
    } else {
      throw new ValidationException(validationReport);
    }

    return aip;
  }

  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath aipPath = ModelUtils.getAIPpath(aipId);
    storage.deleteResource(aipPath);
    notifyAipDeleted(aipId);
  }

  // TODO check if this method has become superfluous
  public List<DescriptiveMetadata> listDescriptiveMetadata(String aipId)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    ClosableIterable<DescriptiveMetadata> it;

    return getAIPMetadata(aipId).getMetadata().getDescriptiveMetadata();
  }

  public Long countDescriptiveMetadataBinaries(String aipId)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    try {
      return storage.countResourcesUnderDirectory(ModelUtils.getDescriptiveMetadataPath(aipId));
    } catch (NotFoundException e) {
      try {
        storage.getDirectory(ModelUtils.getAIPpath(aipId));
        // AIP is there but metadata directory is not
        return 0L;
      } catch (NotFoundException e1) {
        // AIP is not there, sending exception
        throw new NotFoundException("Could not find AIP: " + aipId, e1);
      }
    }
  }

  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    binary = storage.getBinary(binaryPath);

    return binary;
  }

  // TODO check if this method has become superfluous
  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    AIP aip = getAIPMetadata(aipId);

    DescriptiveMetadata ret = null;
    for (DescriptiveMetadata descriptiveMetadata : aip.getMetadata().getDescriptiveMetadata()) {
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

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType) throws RequestNotValidException, GenericException, AlreadyExistsException,
      AuthorizationDeniedException, NotFoundException {
    DescriptiveMetadata descriptiveMetadataBinary = null;

    // StoragePath binaryPath = binary.getStoragePath();
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;

    storage.createBinary(binaryPath, binary.getContent(), asReference);
    descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType);

    AIP aip = getAIPMetadata(aipId);
    aip.getMetadata().getDescriptiveMetadata().add(descriptiveMetadataBinary);
    updateAIPMetadata(aip);

    notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);

    return descriptiveMetadataBinary;
  }

  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType) throws RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException, ValidationException {
    DescriptiveMetadata ret = null;

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);

    // set descriptive metadata type
    AIP aip = getAIPMetadata(aipId);
    List<DescriptiveMetadata> descriptiveMetadata = aip.getMetadata().getDescriptiveMetadata();
    Optional<DescriptiveMetadata> odm = descriptiveMetadata.stream()
      .filter(dm -> dm.getId().equals(descriptiveMetadataId)).findFirst();
    if (odm.isPresent()) {
      ret = odm.get();
      ret.setType(descriptiveMetadataType);
    } else {
      ret = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType);
      descriptiveMetadata.add(ret);
    }
    updateAIP(aip);

    notifyDescriptiveMetadataUpdated(ret);

    return ret;
  }

  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);

    storage.deleteResource(binaryPath);

    // update AIP metadata
    AIP aip = getAIPMetadata(aipId);
    for (Iterator<DescriptiveMetadata> it = aip.getMetadata().getDescriptiveMetadata().iterator(); it.hasNext();) {
      DescriptiveMetadata descriptiveMetadata = it.next();
      if (descriptiveMetadata.getId().equals(descriptiveMetadataId)) {
        it.remove();
        break;
      }
    }
    updateAIPMetadata(aip);

    notifyDescriptiveMetadataDeleted(aipId, descriptiveMetadataId);

  }

  // TODO check if this method as not become superfluous
  public List<Representation> listRepresentations(String aipId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return getAIPMetadata(aipId).getRepresentations();

  }

  // TODO check if this method is now superfluous
  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    AIP aip = getAIPMetadata(aipId);

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

  // TODO support asReference
  public Representation createRepresentation(String aipId, String representationId, boolean original,
    StorageService sourceStorage, StoragePath sourcePath) throws RequestNotValidException, GenericException,
      NotFoundException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    Representation representation;

    StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId, representationId);

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    if (isRepresentationValid(sourceDirectory)) {
      storage.copy(sourceStorage, sourcePath, directoryPath);

      representation = new Representation(representationId, aipId, original);

      // update AIP metadata
      AIP aip = getAIPMetadata(aipId);
      aip.getRepresentations().add(representation);
      updateAIP(aip);

      notifyRepresentationCreated(representation);
    } else {
      throw new GenericException("Error while creating representation, reason: representation is not valid");
    }

    return representation;
  }

  public Representation updateRepresentation(String aipId, String representationId, StorageService sourceStorage,
    StoragePath sourcePath)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    // verify structure of source representation
    Directory sourceDirectory = verifySourceRepresentation(sourceStorage, sourcePath);

    // update each representation file (from source representation)
    final List<String> fileIDsToUpdate = updateRepresentationFiles(aipId, representationId, sourceStorage, sourcePath);

    // delete files that were removed on representation update
    deleteUnneededFilesFromRepresentation(aipId, representationId, fileIDsToUpdate);

    // TODO how to known if representation is original?
    boolean original = true;

    // build return object
    Representation representation = new Representation(representationId, aipId, original);

    notifyRepresentationUpdated(representation);
    return representation;
  }

  private Directory verifySourceRepresentation(StorageService sourceStorage, StoragePath sourcePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    Directory sourceDirectory;

    sourceDirectory = sourceStorage.getDirectory(sourcePath);
    if (!isRepresentationValid(sourceDirectory)) {
      throw new GenericException("Error while updating AIP, reason: representation is not valid");
    }

    return sourceDirectory;
  }

  private void deleteUnneededFilesFromRepresentation(String aipId, String representationId,
    final List<String> fileIDsToUpdate)
      throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<Resource> filesToRemoveIterable = null;
    try {
      filesToRemoveIterable = storage
        .listResourcesUnderDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
      for (Resource fileToRemove : filesToRemoveIterable) {
        StoragePath fileToRemovePath = fileToRemove.getStoragePath();
        if (!fileIDsToUpdate.contains(fileToRemovePath.getName())) {
          storage.deleteResource(fileToRemovePath);
        }
      }

    } finally {
      try {
        if (filesToRemoveIterable != null) {
          filesToRemoveIterable.close();
        }
      } catch (IOException e) {
        LOGGER.error("Error while while freeing up resources", e);
      }
    }
  }

  private List<String> updateRepresentationFiles(String aipId, String representationId, StorageService sourceStorage,
    StoragePath sourcePath)
      throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    final List<String> fileIDsToUpdate = new ArrayList<String>();
    ClosableIterable<Resource> filesIterable = null;
    try {
      filesIterable = sourceStorage.listResourcesUnderDirectory(sourcePath);
      for (Resource file : filesIterable) {
        if (file instanceof DefaultBinary) {
          boolean createIfNotExists = true;
          boolean notify = false;
          File fileUpdated = updateFile(aipId, representationId, file.getStoragePath().getName(), (Binary) file,
            createIfNotExists, notify);
          notifyFileUpdated(fileUpdated);
          fileIDsToUpdate.add(fileUpdated.getId());
        } else {
          // FIXME log error and continue???
        }
      }
    } finally {
      try {
        if (filesIterable != null) {
          filesIterable.close();
        }
      } catch (IOException e) {
        LOGGER.error("Error while while freeing up resources", e);
      }
    }
    return fileIDsToUpdate;
  }

  public void deleteRepresentation(String aipId, String representationId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath representationPath = ModelUtils.getRepresentationPath(aipId, representationId);

    storage.deleteResource(representationPath);

    // update AIP metadata
    AIP aip = getAIPMetadata(aipId);
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

  public Iterable<File> listFilesDirectlyUnder(String aipId, String representationId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Iterable<File> it = null;

    final Iterator<Resource> iterator = storage
      .listResourcesUnderDirectory(ModelUtils.getRepresentationPath(aipId, representationId)).iterator();

    it = new Iterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public File next() {
            try {
              return convertResourceToRepresentationFile(iterator.next());
            } catch (GenericException | NoSuchElementException | NotFoundException | AuthorizationDeniedException
              | RequestNotValidException e) {
              LOGGER.error("Error while listing representation files", e);
              return null;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };

    return it;
  }

  public Iterable<File> listFilesDirectlyUnder(File f)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    // TODO get a better method for getting the path
    List<String> path = new ArrayList<>();
    if (f.getPath() != null) {
      path.addAll(f.getPath());
    }
    path.add(f.getId());

    return listFilesDirectlyUnder(f.getAipId(), f.getRepresentationId(), path.toArray(new String[] {}));
  }

  public Iterable<File> listFilesDirectlyUnder(String aipId, String representationId, String... fileId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Iterable<File> it = null;

    StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

    final Iterator<Resource> iterator = storage.listResourcesUnderDirectory(filePath).iterator();

    it = new Iterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          @Override
          public boolean hasNext() {
            if (iterator == null) {
              return true;
            }
            return iterator.hasNext();
          }

          @Override
          public File next() {
            try {
              return convertResourceToRepresentationFile(iterator.next());
            } catch (GenericException | NoSuchElementException | NotFoundException | AuthorizationDeniedException
              | RequestNotValidException e) {
              LOGGER.error("Error while listing representation files", e);
              return null;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };

    return it;
  }

  public Iterable<File> listAllFiles(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final Iterator<File> directlyUnder = listFilesDirectlyUnder(file).iterator();
    // final Iterator<String> fileIdIterator = fileIds.iterator();

    Iterable<File> ret = new Iterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          Stack<Iterable<File>> itStack = new Stack<Iterable<File>>();

          @Override
          public boolean hasNext() {
            boolean hasNext;

            if (itStack.isEmpty()) {
              hasNext = directlyUnder.hasNext();
            } else {
              hasNext = false;
              // find a non-empty iterator or empty stack
              do {
                if (!itStack.peek().iterator().hasNext()) {
                  itStack.pop();
                } else {
                  hasNext = true;
                }
              } while (!hasNext && !itStack.isEmpty());

            }
            return hasNext;
          }

          @Override
          public File next() {
            File file = null;
            if (itStack.isEmpty()) {
              file = directlyUnder.next();
            } else {
              file = itStack.peek().iterator().next();
            }

            try {
              if (file != null && file.isDirectory()) {
                Iterable<File> subIterable = listFilesDirectlyUnder(file);
                itStack.push(subIterable);
              }
            } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
              LOGGER.warn("Error while listing all files", e);
            }

            return file;
          }
        };
      }
    };

    return ret;

  }

  public Iterable<File> listAllFiles(String aipId, String representationId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final Iterator<File> filesDirectlyUnder = listFilesDirectlyUnder(aipId, representationId).iterator();

    Iterable<File> it = new Iterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          Iterator<File> it = null;

          @Override
          public boolean hasNext() {
            return it != null ? it.hasNext() : filesDirectlyUnder.hasNext();
          }

          @Override
          public File next() {
            File nextFile;
            if (it == null) {
              nextFile = filesDirectlyUnder.next();
            } else {
              nextFile = it.next();
            }

            if (nextFile.isDirectory()) {
              try {
                it = listAllFiles(nextFile).iterator();

              } catch (NotFoundException | GenericException | RequestNotValidException
                | AuthorizationDeniedException e) {
                LOGGER.error("Error listing files", e);
              }
            }
            return nextFile;
          }
        };
      }
    };

    return it;

  }

  public File retrieveFile(String aipId, String representationId, String... fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    File file;
    StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);
    Binary binary = storage.getBinary(filePath);
    file = convertResourceToRepresentationFile(binary);

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File createFile(String aipId, String representationId, String fileId, Binary binary)
    throws RequestNotValidException, GenericException, AlreadyExistsException, AuthorizationDeniedException,
    NotFoundException {
    File file;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

    final Binary createdBinary = storage.createBinary(filePath, binary.getContent(), asReference);
    file = convertResourceToRepresentationFile(createdBinary);
    notifyFileCreated(file);

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File updateFile(String aipId, String representationId, String fileId, Binary binary, boolean createIfNotExists,
    boolean notify) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    File file = null;
    // FIXME how to set this?
    boolean asReference = false;

    StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

    storage.updateBinaryContent(filePath, binary.getContent(), asReference, createIfNotExists);
    Binary binaryUpdated = storage.getBinary(filePath);
    file = convertResourceToRepresentationFile(binaryUpdated);
    if (notify) {
      notifyFileUpdated(file);
    }

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public void deleteFile(String aipId, String representationId, String fileId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);
    storage.deleteResource(filePath);
    notifyFileDeleted(aipId, representationId, fileId);

  }

  // FIXME turn this into ClosableIterable
  // TODO to improve...
  public Iterable<RepresentationPreservationObject> getAipPreservationObjects(String aipId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Iterable<RepresentationPreservationObject> it = null;
    final List<RepresentationPreservationObject> rpos = new ArrayList<RepresentationPreservationObject>();

    final Iterator<Resource> resourceIterator = storage
      .listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId)).iterator();
    while (resourceIterator.hasNext()) {
      Resource resource = resourceIterator.next();
      Iterator<Resource> preservationIterator = storage.listResourcesUnderDirectory(
        ModelUtils.getAIPRepresentationPreservationPath(aipId, resource.getStoragePath().getName())).iterator();
      while (preservationIterator.hasNext()) {
        Resource preservationObject = preservationIterator.next();
        Binary preservationBinary = storage.getBinary(preservationObject.getStoragePath());
        lc.xmlns.premisV2.Representation r = ModelUtils.getPreservationRepresentationObject(preservationBinary);
        if (r != null) {
          rpos.add(convertResourceToRepresentationPreservationObject(aipId, resource.getStoragePath().getName(),
            preservationObject.getStoragePath().getName(), preservationBinary));
        }

      }
    }
    it = new Iterable<RepresentationPreservationObject>() {
      @Override
      public Iterator<RepresentationPreservationObject> iterator() {
        return rpos.iterator();
      }
    };

    return it;
  }

  public RepresentationPreservationObject getRepresentationPreservationObject(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationPreservationObject obj = null;

    StoragePath sp = ModelUtils.getPreservationRepresentationPath(aipId, representationId);
    Binary b = storage.getBinary(sp);
    obj = convertResourceToRepresentationPreservationObject(aipId, representationId, b.getStoragePath().getName(), b);

    return obj;
  }

  public EventPreservationObject getEventPreservationObject(String aipId, String representationId, String fileID,
    String preservationObjectID)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    EventPreservationObject obj = null;

    StoragePath sp = ModelUtils.getPreservationFilePathRaw(aipId, representationId, preservationObjectID);
    Binary b = storage.getBinary(sp);
    obj = convertResourceToEventPreservationObject(aipId, representationId, preservationObjectID, b);

    return obj;
  }

  public AgentPreservationObject getAgentPreservationObject(String agentID)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AgentPreservationObject apo = null;

    StoragePath sp = ModelUtils.getPreservationAgentPath(agentID);
    Binary b = storage.getBinary(sp);
    apo = convertResourceToAgentPreservationObject(agentID, b);

    return apo;
  }

  private AgentPreservationObject convertResourceToAgentPreservationObject(String agentID, Binary resource)
    throws GenericException {
    if (resource instanceof DefaultBinary) {
      try {
        PremisAgentHelper pah = PremisAgentHelper.newInstance(resource.getContent().createInputStream());
        AgentPreservationObject apo = new AgentPreservationObject();
        apo.setAgentName((pah.getAgent().getAgentNameList() != null && pah.getAgent().getAgentNameList().size() > 0)
          ? pah.getAgent().getAgentNameList().get(0) : "");
        apo.setAgentType(pah.getAgent().getAgentType());
        apo
          .setId((pah.getAgent().getAgentIdentifierList() != null && pah.getAgent().getAgentIdentifierList().size() > 0)
            ? pah.getAgent().getAgentIdentifierList().get(0).getAgentIdentifierValue() : "");
        apo.setType(PreservationMetadataType.AGENT);
        return apo;
      } catch (PremisMetadataException | IOException e) {
        throw new GenericException("Error while trying to convert a binary into a representation preservation object",
          e);
      }
    } else {
      throw new GenericException(
        "Error while trying to convert a binary into a representation preservation object because resource is not a DefaultBinary");
    }
  }

  private ValidationReport<String> isAIPvalid(ModelService model, Directory directory,
    boolean failIfNoDescriptiveMetadataSchema)
      throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    ValidationReport<String> report = new ValidationReport<>();

    // validate metadata (against schemas)
    ValidationReport<ParseError> descriptiveMetadataValidationReport = ValidationUtils
      .isAIPDescriptiveMetadataValid(model, directory.getStoragePath().getName(), failIfNoDescriptiveMetadataSchema);

    report.setValid(descriptiveMetadataValidationReport.isValid());
    report.setIssues(
      descriptiveMetadataValidationReport.getIssues().stream().map(r -> r.toString()).collect(Collectors.toList()));

    // FIXME validate others aspects

    return report;
  }

  private boolean isRepresentationValid(Directory directory) {
    // FIXME implement this
    return true;
  }

  // TODO to improve...
  public boolean hasAgentPreservationObject(String agentID) {
    boolean hasAgent = false;
    try {
      StoragePath sp = ModelUtils.getPreservationAgentPath(agentID);
      Binary b = storage.getBinary(sp);
      if (b != null && b.getSizeInBytes() > 0)
        hasAgent = true;
    } catch (AuthorizationDeniedException | NotFoundException | RequestNotValidException | GenericException e) {
      hasAgent = false;
    }
    return hasAgent;
  }

  private List<PreservationMetadata> retrieveAIPPreservationInformation(String aipId, List<String> representationIds)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    List<PreservationMetadata> ret = new ArrayList<>();

    for (String representationID : representationIds) {
      StoragePath representationPreservationPath = ModelUtils.getAIPRepresentationPreservationPath(aipId,
        representationID);

      // obtain list of preservation related files
      List<String> preservationFileIds = ModelUtils.getChildIds(storage, representationPreservationPath, false);

      for (String preservationFileId : preservationFileIds) {
        // TODO review due to directories in representations
        StoragePath binaryPath = ModelUtils.getPath(representationPreservationPath, preservationFileId);
        Binary preservationBinary = storage.getBinary(binaryPath);

        lc.xmlns.premisV2.Representation representation = ModelUtils
          .getPreservationRepresentationObject(preservationBinary);
        if (representation != null) {
          ret.add(new PreservationMetadata(preservationFileId, aipId, representationID,
            PreservationMetadataType.OBJECT_REPRESENTATION));
        } else {
          EventComplexType event = ModelUtils.getPreservationEvent(preservationBinary);
          if (event != null) {
            ret.add(
              new PreservationMetadata(preservationFileId, aipId, representationID, PreservationMetadataType.EVENT));
          } else {
            lc.xmlns.premisV2.File file = ModelUtils.getPreservationFileObject(preservationBinary);
            if (file != null) {
              ret.add(new PreservationMetadata(preservationFileId, aipId, representationID,
                PreservationMetadataType.OBJECT_FILE));

            } else {
              LOGGER.warn(
                "The binary {} is neither a PreservationRepresentationObject or PreservationEvent or PreservationFileObject...Moving on...",
                binaryPath.asString());
            }
          }
        }
      }
    }

    return ret;
  }

  private DescriptiveMetadata convertResourceToDescriptiveMetadata(Resource resource) throws GenericException {
    if (resource instanceof DefaultBinary) {

      String id = resource.getStoragePath().getName();
      String aipId = ModelUtils.getAIPidFromStoragePath(resource.getStoragePath());
      // TODO find another way to retrieve descriptive metadata type
      // String type = ModelUtils.getString(resource.getMetadata(),
      // RodaConstants.STORAGE_META_TYPE);
      String type = null;

      return new DescriptiveMetadata(id, aipId, type);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a descriptive metadata binary: "
          + resource);
    }
  }

  private PreservationMetadata convertResourceToPreservationMetadata(Resource resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      String id = resource.getStoragePath().getName();
      String aipId = ModelUtils.getAIPidFromStoragePath(resource.getStoragePath());
      String representationId = ModelUtils.getRepresentationIdFromStoragePath(resource.getStoragePath());
      PreservationMetadataType type = ModelUtils.getPreservationType((DefaultBinary) resource);

      return new PreservationMetadata(id, aipId, representationId, type);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a preservation metadata binary");
    }
  }

  @Deprecated
  private Representation convertResourceToRepresentation(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    if (resource instanceof DefaultDirectory) {
      StoragePath directoryPath = resource.getStoragePath();

      String id = directoryPath.getName();
      String aipId = ModelUtils.getAIPidFromStoragePath(directoryPath);

      // TODO infer original
      boolean original = true;

      return new Representation(id, aipId, original);

    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Directory into a representation");
    }
  }

  private File convertResourceToRepresentationFile(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    File ret;

    StoragePath resourcePath = resource.getStoragePath();

    String id = resourcePath.getName();
    String aipId = ModelUtils.getAIPidFromStoragePath(resourcePath);
    LOGGER.debug("convertResourceToRepresentationFile: " + resourcePath.asString());
    LOGGER.debug("convertResourceToRepresentationFile2: " + resourcePath.getDirectoryPath());
    String representationId = ModelUtils.getRepresentationIdFromStoragePath(resourcePath);
    List<String> filePath = ModelUtils.getFilePathFromStoragePath(resourcePath);

    if (resource instanceof DefaultBinary) {
      boolean isDirectory = false;

      ret = new File(id, aipId, representationId, filePath, isDirectory);
    } else if (resource instanceof DefaultDirectory) {
      boolean isDirectory = true;

      ret = new File(resourcePath.getName(), aipId, representationId, filePath, isDirectory);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a representation file");
    }
    return ret;
  }

  public RepresentationPreservationObject retrieveRepresentationPreservationObject(String aipId,
    String representationId)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationPreservationObject representationPreservationObject = null;

    StoragePath filePath = ModelUtils.getPreservationRepresentationPath(aipId, representationId);
    Binary binary = storage.getBinary(filePath);
    representationPreservationObject = convertResourceToRepresentationPreservationObject(aipId, representationId,
      representationId, binary);
    representationPreservationObject.setId(representationId);

    return representationPreservationObject;
  }

  // FIXME support file path
  public RepresentationFilePreservationObject retrieveRepresentationFileObject(String aipId, String representationId,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationFilePreservationObject representationPreservationObject = null;

    StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
    Binary binary = storage.getBinary(filePath);
    representationPreservationObject = convertResourceToRepresentationFilePreservationObject(aipId, representationId,
      fileId, binary);
    representationPreservationObject.setId(fileId);

    return representationPreservationObject;
  }

  // FIXME verify/refactor this method
  private RepresentationFilePreservationObject convertResourceToRepresentationFilePreservationObject(String aipId,
    String representationId, String fileId, Binary resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      try {
        // retrieve needed information to instantiate Representation
        // FIXME check if inputstream gets closed
        PremisFileObjectHelper pfoh = PremisFileObjectHelper.newInstance(resource.getContent().createInputStream());

        RepresentationFilePreservationObject rfpo = new RepresentationFilePreservationObject();
        rfpo.setAipId(aipId);
        rfpo.setRepresentationId(representationId);
        rfpo.setFileId(fileId);
        rfpo.setId(pfoh.getRepresentationFilePreservationObject().getId());
        rfpo.setCompositionLevel(pfoh.getRepresentationFilePreservationObject().getCompositionLevel());
        rfpo.setContentLocationType(pfoh.getRepresentationFilePreservationObject().getContentLocationType());
        rfpo.setContentLocationValue(pfoh.getRepresentationFilePreservationObject().getContentLocationValue());
        rfpo.setCreatingApplicationName(pfoh.getRepresentationFilePreservationObject().getCreatingApplicationName());
        rfpo.setCreatingApplicationVersion(
          pfoh.getRepresentationFilePreservationObject().getCreatingApplicationVersion());
        rfpo.setDateCreatedByApplication(pfoh.getRepresentationFilePreservationObject().getDateCreatedByApplication());
        rfpo.setFixities(pfoh.getRepresentationFilePreservationObject().getFixities());
        rfpo.setFormatDesignationName(pfoh.getRepresentationFilePreservationObject().getFormatDesignationName());
        rfpo.setFormatDesignationVersion(pfoh.getRepresentationFilePreservationObject().getFormatDesignationVersion());
        rfpo.setFormatRegistryKey(pfoh.getRepresentationFilePreservationObject().getFormatRegistryKey());
        rfpo.setFormatRegistryName(pfoh.getRepresentationFilePreservationObject().getFormatRegistryName());
        rfpo.setFormatRegistryRole(pfoh.getRepresentationFilePreservationObject().getFormatRegistryRole());
        rfpo.setHash(pfoh.getRepresentationFilePreservationObject().getHash());
        rfpo.setMimetype(pfoh.getRepresentationFilePreservationObject().getMimetype());
        rfpo.setObjectCharacteristicsExtension(
          pfoh.getRepresentationFilePreservationObject().getObjectCharacteristicsExtension());
        rfpo.setOriginalName(pfoh.getRepresentationFilePreservationObject().getOriginalName());
        rfpo.setPreservationLevel(pfoh.getRepresentationFilePreservationObject().getPreservationLevel());
        rfpo.setPronomId(pfoh.getRepresentationFilePreservationObject().getPronomId());
        rfpo.setRepresentationObjectId(pfoh.getRepresentationFilePreservationObject().getRepresentationObjectId());
        rfpo.setSize(pfoh.getRepresentationFilePreservationObject().getSize());
        rfpo.setType(pfoh.getRepresentationFilePreservationObject().getType());
        rfpo.setRepresentationId(representationId);
        return rfpo;

      } catch (PremisMetadataException e) {
        throw new GenericException("Error while trying to convert a binary into a representation preservation object",
          e);
      } catch (IOException e) {
        throw new GenericException("Error while trying to convert a binary into a representation preservation object",
          e);
      }
    } else {
      throw new GenericException("Error while trying to convert a binary into a representation preservation object");
    }
  }

  // FIXME verify/refactor this method
  private RepresentationPreservationObject convertResourceToRepresentationPreservationObject(String aipId,
    String representationId, String fileId, Binary resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      try {

        // FIXME check if inputstream gets closed
        PremisRepresentationObjectHelper proh = PremisRepresentationObjectHelper
          .newInstance(resource.getContent().createInputStream());
        RepresentationPreservationObject rpo = new RepresentationPreservationObject();
        rpo.setAipId(aipId);
        rpo.setRepresentationId(representationId);
        rpo.setId(proh.getRepresentationPreservationObject().getId());

        rpo.setDerivationEventID(proh.getRepresentationPreservationObject().getDerivationEventID());
        rpo.setDerivedFromRepresentationObjectID(
          proh.getRepresentationPreservationObject().getDerivedFromRepresentationObjectID());

        rpo.setPartFiles(proh.getRepresentationPreservationObject().getPartFiles());
        rpo.setPreservationEventIDs(proh.getRepresentationPreservationObject().getPreservationEventIDs());
        rpo.setPreservationLevel(proh.getRepresentationPreservationObject().getPreservationLevel());
        rpo.setRepresentationObjectID(proh.getRepresentationPreservationObject().getRepresentationObjectID());
        rpo.setRootFile(proh.getRepresentationPreservationObject().getRootFile());
        rpo.setType(proh.getRepresentationPreservationObject().getType());

        return rpo;
      } catch (PremisMetadataException e) {
        throw new GenericException("Error while trying to convert a binary into a representation preservation object");
      } catch (IOException e) {
        throw new GenericException("Error while trying to convert a binary into a representation preservation object");
      }
    } else {
      throw new GenericException("Error while trying to convert a binary into a representation preservation object");
    }
  }

  public EventPreservationObject retrieveEventPreservationObject(String aipId, String representationId, String fileId,
    String preservationID)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    EventPreservationObject eventPreservationObject = null;

    StoragePath storagePath = ModelUtils.getPreservationMetadataStoragePath(aipId, representationId, preservationID,
      PreservationMetadataType.EVENT);
    Binary binary = storage.getBinary(storagePath);
    eventPreservationObject = convertResourceToEventPreservationObject(aipId, representationId, fileId, binary);
    eventPreservationObject.setId(fileId);

    return eventPreservationObject;
  }

  // FIXME verify/refactor this method
  private EventPreservationObject convertResourceToEventPreservationObject(String aipId, String representationId,
    String fileId, Binary resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      try {

        // retrieve needed information to instantiate Representation

        PremisEventHelper peh = PremisEventHelper.newInstance(resource.getContent().createInputStream());

        // TODO premisevent to EventPreservationObject
        EventPreservationObject epo = new EventPreservationObject();
        epo.setType(PreservationMetadataType.EVENT);
        epo.setId(fileId);
        epo.setAipId(aipId);
        epo.setRepresentationId(representationId);
        epo.setAgentID((peh.getEvent().getLinkingAgentIdentifierList() != null
          && peh.getEvent().getLinkingAgentIdentifierList().size() > 0)
            ? peh.getEvent().getLinkingAgentIdentifierList().get(0).getLinkingAgentIdentifierValue() : null);
        epo.setAgentRole((peh.getEvent().getLinkingAgentIdentifierList() != null
          && peh.getEvent().getLinkingAgentIdentifierList().size() > 0)
            ? peh.getEvent().getLinkingAgentIdentifierList().get(0).getRole() : null);
        epo.setDate(new Date()); // TODO: ????
        try {
          epo.setDatetime(DateParser.parse(peh.getEvent().getEventDateTime().toString()));
        } catch (InvalidDateException ide) {
          epo.setDatetime(new Date());
        }
        epo.setDescription(""); // TODO: ????
        epo.setEventDetail(peh.getEvent().getEventDetail());
        epo.setEventType(peh.getEvent().getEventType());
        epo.setName(""); // TODO: ???
        List<LinkingObjectIdentifierComplexType> linkingObjects = peh.getEvent().getLinkingObjectIdentifierList();
        if (linkingObjects != null && linkingObjects.size() > 0) {
          List<String> objectIds = new ArrayList<String>();
          for (LinkingObjectIdentifierComplexType loi : linkingObjects) {
            objectIds.add(loi.getTitle());
          }
          epo.setObjectIDs(objectIds.toArray(new String[objectIds.size()]));
        } else {
          epo.setObjectIDs(null);
        }
        epo.setOutcome(peh.getEvent().getEventOutcomeInformationList().get(0).getEventOutcome());
        if (peh.getEvent().getEventOutcomeInformationList() != null
          && peh.getEvent().getEventOutcomeInformationList().size() > 0) {
          EventOutcomeInformationComplexType eoict = peh.getEvent().getEventOutcomeInformationList().get(0);
          epo.setOutcome(eoict.getEventOutcome());
          if (eoict.getEventOutcomeDetailList() != null && eoict.getEventOutcomeDetailList().size() > 0) {
            EventOutcomeDetailComplexType eodc = eoict.getEventOutcomeDetailList().get(0);
            List<ExtensionComplexType> ects = eodc.getEventOutcomeDetailExtensionList();
            if (ects != null && ects.size() > 0) {
              String outcomeDetailExtension = "";
              for (ExtensionComplexType ect : ects) {
                outcomeDetailExtension += ect.xmlText();
              }
              epo.setOutcomeDetailExtension(outcomeDetailExtension);

            }

            epo.setOutcomeDetailNote(eodc.getEventOutcomeDetailNote());
          } else {
            epo.setOutcomeDetailExtension("");
            epo.setOutcomeDetailNote("");
          }
        } else {
          epo.setOutcome("");
          epo.setOutcomeDetailExtension("");
          epo.setOutcomeDetailNote("");
        }
        epo.setOutcomeDetails(""); // TODO: ???
        epo.setOutcomeResult(""); // TODO: ???
        epo.setTargetID(""); // TODO: ???
        epo.setEventType(peh.getEvent().getEventType());
        return epo;
      } catch (PremisMetadataException e) {
        throw new GenericException("Error while trying to convert a binary into a event preservation object");
      } catch (IOException e) {
        throw new GenericException("Error while trying to convert a binary into a event preservation object");
      }
    } else {
      throw new GenericException("Error while trying to convert a binary into a event preservation object");
    }
  }

  // FIXME this should be synchronized (at least access to logFile)
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String date = sdf.format(new Date()) + ".log";
    logFile = logDirectory.resolve(date);

    // verify if file exists and if not, if older files exist (in that case,
    // move them to storage)
    if (!Files.exists(logFile)) {
      findOldLogsAndMoveThemToStorage(logDirectory, logFile);
      try {
        Files.createFile(logFile);
      } catch (IOException e) {
        throw new GenericException("Error creating file to write log into", e);
      }
    }

    // write to log file
    ModelUtils.writeLogEntryToFile(logEntry, logFile);

    // emit event
    if (notify) {
      notifyLogEntryCreated(logEntry);
    }
  }

  // TODO verify
  public ClosableIterable<PreservationMetadata> listPreservationMetadataBinaries(String aipId, String representationID)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    ClosableIterable<PreservationMetadata> aipsIterable;

    final ClosableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderContainer(ModelUtils.getAIPRepresentationPreservationPath(aipId, representationID));
    Iterator<Resource> resourcesIterator = resourcesIterable.iterator();

    aipsIterable = new ClosableIterable<PreservationMetadata>() {

      @Override
      public Iterator<PreservationMetadata> iterator() {
        return new Iterator<PreservationMetadata>() {

          @Override
          public boolean hasNext() {
            if (resourcesIterator == null) {
              return false;
            }
            return resourcesIterator.hasNext();
          }

          @Override
          public PreservationMetadata next() {
            try {
              Resource next = resourcesIterator.next();
              return convertResourceToPreservationMetadata(next);
            } catch (NoSuchElementException | GenericException e) {
              LOGGER.error("Error while listing preservation metadata binaries", e);
              return null;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

      @Override
      public void close() throws IOException {
        resourcesIterable.close();
      }
    };

    return aipsIterable;
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
            StoragePath logPath = ModelUtils.getLogPath(path.getFileName().toString());
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

  public void addUser(User user, boolean useModel, boolean notify) throws AlreadyExistsException, GenericException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().addUser(user);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error adding user to LDAP", e);
    } catch (UserAlreadyExistsException e) {
      success = false;
      throw new AlreadyExistsException("User already exists", e);
    } catch (EmailAlreadyExistsException e) {
      success = false;
      throw new AlreadyExistsException("Email already exists", e);
    }
    if (success && notify) {
      notifyUserCreated(user);
    }
  }

  public void updateUser(User user, boolean useModel, boolean notify)
    throws AuthorizationDeniedException, NotFoundException, AlreadyExistsException, GenericException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().modifyUser(user);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error updating user to LDAP", e);
    } catch (EmailAlreadyExistsException e) {
      success = false;
      throw new AlreadyExistsException("User already exists", e);
    } catch (NotFoundException e) {
      success = false;
      throw new NotFoundException("User doesn't exist", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
    if (success && notify) {
      notifyUserUpdated(user);
    }
  }

  public void deleteUser(String id, boolean useModel, boolean notify)
    throws GenericException, AuthorizationDeniedException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().removeUser(id);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error deleting user from LDAP", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
    if (success && notify) {
      notifyUserDeleted(id);
    }
  }

  public void addGroup(Group group, boolean useModel, boolean notify) throws GenericException, AlreadyExistsException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().addGroup(group);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error adding group to LDAP", e);
    } catch (GroupAlreadyExistsException e) {
      success = false;
      throw new AlreadyExistsException("Group already exists", e);
    }
    if (success && notify) {
      notifyGroupCreated(group);
    }
  }

  public void updateGroup(Group group, boolean useModel, boolean notify)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().modifyGroup(group);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error updating group to LDAP", e);
    } catch (NotFoundException e) {
      success = false;
      throw new NotFoundException("Group doesn't exist", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
    if (success && notify) {
      notifyGroupUpdated(group);
    }
  }

  public void deleteGroup(String id, boolean useModel, boolean notify)
    throws GenericException, AuthorizationDeniedException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().removeGroup(id);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error updating group to LDAP", e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new AuthorizationDeniedException("Illegal operation", e);
    }
    if (success && notify) {
      notifyGroupDeleted(id);
    }
  }

  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, String fileId, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipId, representationId, fileId, type);
    binary = storage.getBinary(binaryPath);

    return binary;
  }

  public OtherMetadata createOtherMetadata(String aipId, String representationId, String fileName, String type,
    Binary binary) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    OtherMetadata otherMetadataBinary = null;
    try {
      StoragePath otherMetadataPath = ModelUtils.getToolRepresentationMetadataDirectory(aipId, representationId, type);
      storage.getDirectory(otherMetadataPath);
      LOGGER.debug("Tool directory already exists...");
    } catch (NotFoundException e) {
      LOGGER.debug("Tool directory doesn't exist... Creating...");
      try {
        StoragePath otherMetadataPath = ModelUtils.getOtherMetadataDirectory(aipId);
        storage.createDirectory(otherMetadataPath);
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }

      try {
        StoragePath otherMetadataPath = ModelUtils.getToolMetadataDirectory(aipId, type);
        storage.createDirectory(otherMetadataPath);
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }
      try {
        StoragePath otherMetadataPath = ModelUtils.getToolRepresentationMetadataDirectory(aipId, representationId,
          type);
        storage.createDirectory(otherMetadataPath);
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }
    }

    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipId, representationId, fileName, type);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);
    // TODO create a better id
    String id = type + "_" + aipId + "_" + representationId + "_" + fileName;
    otherMetadataBinary = new OtherMetadata(id, aipId, representationId, type);
    notifyOtherMetadataCreated(otherMetadataBinary);

    return otherMetadataBinary;
  }

  public void createJob(Job job) throws GenericException {
    // create job in storage
    ModelUtils.createOrUpdateJobInStorage(storage, job);

    // index it
    notifyJobCreated(job);
  }

  public void updateJob(Job job) throws GenericException {
    // TODO should we always write to storage or only when completion percentage
    // is 100???
    // update job in storage
    ModelUtils.createOrUpdateJobInStorage(storage, job);

    // index it
    notifyJobUpdated(job);
  }

  public void updateFile(File file) {
    // TODO

    notifyFileUpdated(file);
  }

  public void createJobReport(JobReport jobReport) throws GenericException {
    // create job report in storage
    ModelUtils.createOrUpdateJobReportInStorage(storage, jobReport);

    // index it
    notifyJobReportCreated(jobReport);
  }

  public void updateJobReport(JobReport jobReport) throws GenericException {
    // update job report in storage
    ModelUtils.createOrUpdateJobReportInStorage(storage, jobReport);

    // index it
    notifyJobReportUpdated(jobReport);
  }

  public void updateFileFormats(List<IndexedFile> updatedFiles) {
    for (IndexedFile file : updatedFiles) {
      // update PREMIS
      try {
        RepresentationFilePreservationObject rfpo = PremisUtils.getPremisFile(storage, file.getAipId(),
          file.getRepresentationId(), file.getId());
        rfpo = PremisUtils.updateFile(rfpo, file);
        Path premisFile = Files.createTempFile("file", ".premis.xml");
        PremisFileObjectHelper helper = new PremisFileObjectHelper(rfpo);
        helper.saveToFile(premisFile.toFile());
        Binary b = (Binary) FSUtils.convertPathToResource(premisFile.getParent(), premisFile);

        storage.updateBinaryContent(
          ModelUtils.getPreservationFilePath(file.getAipId(), file.getRepresentationId(), file.getId()), b.getContent(),
          false, true);
      } catch (IOException | PremisMetadataException | GenericException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.warn("Error updating file format in storage for file {}/{}/{} ", file.getAipId(),
          file.getRepresentationId(), file.getId());
      }
    }
    // TODO is any notify needed?
  }

  // TODO remove PREMIS type and file Id
  public void createPreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id, Binary binary)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.updateBinaryContent(binaryPath, binary.getContent(), false, true);
    List<PreservationLinkingAgent> agents = ModelUtils.extractAgentsFromPreservationBinary(binary, type);
    if (agents != null) {
      for (PreservationLinkingAgent pla : agents) {
        try {
          AgentPreservationObject agent = new AgentPreservationObject();
          agent.setAgentName(pla.getTitle() + "/" + pla.getVersion());
          agent.setAgentType(pla.getType());
          agent.setId(pla.getIdentifierValue());
          byte[] serializedPremisAgent = new PremisAgentHelper(agent).saveToByteArray();
          Path agentFile = Files.createTempFile("agent_preservation", ".xml");
          Files.copy(new ByteArrayInputStream(serializedPremisAgent), agentFile, StandardCopyOption.REPLACE_EXISTING);
          Binary agentResource = (Binary) FSUtils.convertPathToResource(agentFile.getParent(), agentFile);
          createPreservationMetadata(PreservationMetadataType.AGENT, null, null, agent.getId(), agentResource);
        } catch (PremisMetadataException | IOException pme) {
          LOGGER.error("Error creating agent: " + pme.getMessage(), pme);
        }
      }
    }
    notifyPreservationMetadataCreated(pm);
  }

  // TODO remove PREMIS type and file Id
  public void updatePreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id, Binary binary)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.updateBinaryContent(binaryPath, binary.getContent(), false, true);
    notifyPreservationMetadataUpdated(pm);
  }

  // TODO remove PREMIS type and file Id
  public void deletePreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);

    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.deleteResource(binaryPath);
    notifyPreservationMetadataDeleted(pm);
  }
}
