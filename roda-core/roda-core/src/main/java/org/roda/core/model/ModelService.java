/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.UserUtility;
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
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.Metadata;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobReport;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;
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
    return getAIPMetadata(aipId);
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
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, ValidationException {
    // XXX possible optimization would be to allow move between storage
    // TODO support asReference
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {

      storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPpath(aipId));
      Directory newDirectory = storage.getDirectory(ModelUtils.getAIPpath(aipId));

      aip = getAIPMetadata(newDirectory.getStoragePath());
      if (notify) {
        notifyAipCreated(aip);
      }
    } else {
      throw new ValidationException(validationReport);
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
    NotFoundException, ValidationException {
    return createAIP(aipId, sourceStorage, sourcePath, true);
  }

  public AIP notifyAIPCreated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = getAIPMetadata(aipId);
    notifyAipCreated(aip);
    return aip;
  }

  public AIP notifyAIPUpdated(String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    AIP aip = getAIPMetadata(aipId);
    notifyAipUpdated(aip);
    return aip;
  }

  // TODO support asReference
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException,
    AlreadyExistsException, ValidationException {
    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isAIPvalid(sourceModelService, sourceDirectory,
      FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA);
    if (validationReport.isValid()) {
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);

      // XXX possible optimization only creating new files, updating changed and
      // removing deleted ones.
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

  public AIP updateAIP(AIP aip)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    updateAIPMetadata(aip);
    notifyAipUpdated(aip);

    return aip;
  }

  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath aipPath = ModelUtils.getAIPpath(aipId);
    storage.deleteResource(aipPath);
    notifyAipDeleted(aipId);
  }

  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    binary = storage.getBinary(binaryPath);

    return binary;
  }

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

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType) throws RequestNotValidException, GenericException,
      AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    return createDescriptiveMetadata(aipId, descriptiveMetadataId, payload, descriptiveMetadataType, true);
  }

  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload payload, String descriptiveMetadataType, boolean notify) throws RequestNotValidException,
      GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    DescriptiveMetadata descriptiveMetadataBinary = null;

    // StoragePath binaryPath = binary.getStoragePath();
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;

    storage.createBinary(binaryPath, payload, asReference);
    descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType);

    AIP aip = getAIPMetadata(aipId);
    aip.getMetadata().getDescriptiveMetadata().add(descriptiveMetadataBinary);
    updateAIPMetadata(aip);

    if (notify) {
      notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);
    }

    return descriptiveMetadataBinary;
  }

  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId,
    ContentPayload descriptiveMetadataPayload, String descriptiveMetadataType) throws RequestNotValidException,
      GenericException, NotFoundException, AuthorizationDeniedException, ValidationException {
    DescriptiveMetadata ret = null;

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    storage.updateBinaryContent(binaryPath, descriptiveMetadataPayload, asReference, createIfNotExists);

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

  public Representation createRepresentation(String aipId, String representationId, boolean original, boolean notify)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException,
    AlreadyExistsException {
    Representation representation = new Representation(representationId, aipId, original);

    // update AIP metadata
    AIP aip = getAIPMetadata(aipId);
    aip.getRepresentations().add(representation);
    updateAIPMetadata(aip);

    if (notify) {
      notifyRepresentationCreated(representation);
    }

    return representation;

  }

  // TODO support asReference
  public Representation createRepresentation(String aipId, String representationId, boolean original,
    StorageService sourceStorage, StoragePath sourcePath) throws RequestNotValidException, GenericException,
      NotFoundException, AuthorizationDeniedException, AlreadyExistsException, ValidationException {
    Representation representation;

    StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId, representationId);

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isRepresentationValid(sourceDirectory);
    if (validationReport.isValid()) {
      storage.copy(sourceStorage, sourcePath, directoryPath);

      representation = new Representation(representationId, aipId, original);

      // update AIP metadata
      AIP aip = getAIPMetadata(aipId);
      aip.getRepresentations().add(representation);
      updateAIPMetadata(aip);

      notifyRepresentationCreated(representation);
    } else {
      throw new ValidationException(validationReport);
    }

    return representation;
  }

  public Representation updateRepresentation(String aipId, String representationId, boolean original,
    StorageService sourceStorage, StoragePath sourcePath) throws RequestNotValidException, NotFoundException,
      GenericException, AuthorizationDeniedException, ValidationException {
    Representation representation;

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    ValidationReport validationReport = isRepresentationValid(sourceDirectory);

    if (validationReport.isValid()) {
      // XXX possible optimization only creating new files, updating changed and
      // removing deleted

      StoragePath representationPath = ModelUtils.getRepresentationPath(aipId, representationId);
      storage.deleteResource(representationPath);
      try {
        storage.copy(sourceStorage, sourcePath, representationPath);
      } catch (AlreadyExistsException e) {
        throw new GenericException("Copying after delete gave an unexpected already exists exception", e);
      }

      // build return object
      representation = new Representation(representationId, aipId, original);

      notifyRepresentationUpdated(representation);
    } else {
      throw new ValidationException(validationReport);
    }

    return representation;
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

  public ClosableIterable<File> listFilesDirectlyUnder(String aipId, String representationId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<File> it = null;

    final ClosableIterable<Resource> iterable = storage
      .listResourcesUnderDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
    final Iterator<Resource> iterator = iterable.iterator();

    it = new ClosableIterable<File>() {

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

      @Override
      public void close() throws IOException {
        iterable.close();
      }
    };

    return it;
  }

  public ClosableIterable<File> listFilesDirectlyUnder(File f)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    return listFilesDirectlyUnder(f.getAipId(), f.getRepresentationId(), f.getPath(), f.getId());
  }

  public ClosableIterable<File> listFilesDirectlyUnder(String aipId, String representationId,
    List<String> directoryPath, String fileId)
      throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<File> it = null;

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);

    final ClosableIterable<Resource> iterable = storage.listResourcesUnderDirectory(filePath);
    final Iterator<Resource> iterator = iterable.iterator();

    it = new ClosableIterable<File>() {

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

      @Override
      public void close() throws IOException {
        iterable.close();
      }
    };

    return it;
  }

  public ClosableIterable<File> listAllFiles(File file)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final ClosableIterable<File> iterable = listFilesDirectlyUnder(file);
    final Iterator<File> directlyUnder = iterable.iterator();

    ClosableIterable<File> ret = new ClosableIterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          Stack<ClosableIterable<File>> itStack = new Stack<ClosableIterable<File>>();

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
                  try {
                    itStack.pop().close();
                  } catch (IOException e) {
                    LOGGER.warn("Error closing file iterable, possible file leak", e);
                  }
                } else {
                  hasNext = true;
                }
              } while (!hasNext && !itStack.isEmpty());

              if (itStack.isEmpty()) {
                hasNext = directlyUnder.hasNext();
              }
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
                ClosableIterable<File> subIterable = listFilesDirectlyUnder(file);
                itStack.push(subIterable);
              }
            } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
              LOGGER.warn("Error while listing all files", e);
            }

            return file;
          }
        };
      }

      @Override
      public void close() throws IOException {
        iterable.close();
      }
    };

    return ret;

  }

  public ClosableIterable<File> listAllFiles(String aipId, String representationId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {

    final ClosableIterable<File> iterable = listFilesDirectlyUnder(aipId, representationId);
    final Iterator<File> filesDirectlyUnder = iterable.iterator();

    ClosableIterable<File> it = new ClosableIterable<File>() {

      @Override
      public Iterator<File> iterator() {
        return new Iterator<File>() {

          ClosableIterable<File> iterable = null;
          Iterator<File> it = null;

          @Override
          public boolean hasNext() {

            if (it != null && !it.hasNext()) {
              IOUtils.closeQuietly(iterable);
              iterable = null;
              it = null;
            }

            return it != null ? it.hasNext() : filesDirectlyUnder.hasNext();
          }

          @Override
          public File next() {
            File nextFile;
            if (it == null) {
              nextFile = filesDirectlyUnder.next();

              if (nextFile.isDirectory()) {
                try {
                  iterable = listAllFiles(nextFile);
                  it = iterable.iterator();

                } catch (NotFoundException | GenericException | RequestNotValidException
                  | AuthorizationDeniedException e) {
                  LOGGER.error("Error listing files", e);
                }
              }
            } else {
              nextFile = it.next();
            }

            return nextFile;
          }
        };
      }

      @Override
      public void close() throws IOException {
        iterable.close();
      }
    };

    return it;

  }

  public File retrieveFile(String aipId, String representationId, List<String> directoryPath, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    File file;
    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    Binary binary = storage.getBinary(filePath);
    file = convertResourceToRepresentationFile(binary);

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
    file = convertResourceToRepresentationFile(createdBinary);

    if (notify) {
      notifyFileCreated(file);
    }

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
    file = convertResourceToRepresentationFile(binaryUpdated);
    if (notify) {
      notifyFileUpdated(file);
    }

    return file;
  }

  public void deleteFile(String aipId, String representationId, List<String> directoryPath, String fileId,
    boolean notify) throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getFileStoragePath(aipId, representationId, directoryPath, fileId);
    storage.deleteResource(filePath);
    if (notify) {
      notifyFileDeleted(aipId, representationId, fileId);
    }

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

  private ValidationReport isRepresentationValid(Directory directory) {
    return new ValidationReport();
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

  private File convertResourceToRepresentationFile(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    File ret;

    StoragePath resourcePath = resource.getStoragePath();

    String id = resourcePath.getName();
    String aipId = ModelUtils.getAIPidFromStoragePath(resourcePath);
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

  public Binary retrieveRepresentationPreservationObject(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath filePath = ModelUtils.getPreservationRepresentationPath(aipId, representationId);
    return storage.getBinary(filePath);

  }

  public Binary retrievePreservationFile(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(fileId, PreservationMetadataType.OBJECT_FILE,
      aipId, representationId, fileDirectoryPath, fileId);
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

  public void addUser(User user, boolean useModel, boolean notify) throws GenericException, EmailAlreadyExistsException,
    UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    addUser(user, null, useModel, notify);
  }

  public void addUser(User user, String password, boolean useModel, boolean notify) throws GenericException,
    EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().addUser(user);

        if (password != null) {
          UserUtility.getLdapUtility().setUserPassword(user.getId(), password);
        }
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new GenericException("Error adding user to LDAP", e);
    } catch (UserAlreadyExistsException e) {
      success = false;
      throw new UserAlreadyExistsException("User already exists", e);
    } catch (EmailAlreadyExistsException e) {
      success = false;
      throw new EmailAlreadyExistsException("Email already exists", e);
    }
    if (success && notify) {
      notifyUserCreated(user);
    }
  }

  public void modifyUser(User user, boolean useModel, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    modifyUser(user, null, useModel, notify);
  }

  public void modifyUser(User user, String password, boolean useModel, boolean notify)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    boolean success = true;
    try {
      if (useModel) {
        if (password != null) {
          UserUtility.getLdapUtility().setUserPassword(user.getId(), password);
        }

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

  public void removeUser(String id, boolean useModel, boolean notify)
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

  public void modifyGroup(Group group, boolean useModel, boolean notify)
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

  public void removeGroup(String id, boolean useModel, boolean notify)
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

  public Binary retrieveOtherMetadataBinary(OtherMetadata om)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    return retrieveOtherMetadataBinary(om.getAipId(), om.getRepresentationId(), om.getFileDirectoryPath(),
      om.getFileId(), om.getFileSuffix(), om.getType());
  }

  public Binary retrieveOtherMetadataBinary(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Binary binary;
    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    binary = storage.getBinary(binaryPath);

    return binary;
  }

  public OtherMetadata createOtherMetadata(String aipId, String representationId, List<String> fileDirectoryPath,
    String fileId, String fileSuffix, String type, ContentPayload payload)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    OtherMetadata otherMetadataBinary = null;
    try {
      StoragePath otherMetadataPath = ModelUtils.getToolRepresentationMetadataDirectory(aipId, representationId, type);
      storage.getDirectory(otherMetadataPath);
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

    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipId, representationId, fileDirectoryPath, fileId,
      fileSuffix, type);
    boolean asReference = false;
    boolean createIfNotExists = true;
    try{
      storage.createBinary(binaryPath, payload, asReference);
    }catch(AlreadyExistsException e){
      storage.updateBinaryContent(binaryPath, payload, asReference, createIfNotExists);
    }
    // TODO create a better id
    StringBuilder idBuilder = new StringBuilder();
    idBuilder.append(type).append("_");
    idBuilder.append(aipId).append("_");
    idBuilder.append(representationId).append("_");
    if (fileDirectoryPath != null) {
      for (String dirItem : fileDirectoryPath) {
        idBuilder.append(dirItem).append("_");
      }
    }

    idBuilder.append(fileId);

    otherMetadataBinary = new OtherMetadata(idBuilder.toString(), type, aipId, representationId, fileDirectoryPath,
      fileId, fileSuffix);
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

  public JobReport retrieveJobReport(String jobId, String aipId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath jobReportPath = ModelUtils.getJobReportPath(ModelUtils.getJobReportId(jobId, aipId));
    Binary binary = storage.getBinary(jobReportPath);
    JobReport ret;
    try {
      ret = ModelUtils.getObjectFromJson(binary.getContent().createInputStream(), JobReport.class);
    } catch (IOException e) {
      throw new GenericException("Error reading job report", e);
    }
    return ret;
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
    // TODO this method should go to PremisUtils
    // TODO update to preservation metadata should go through model

    for (IndexedFile file : updatedFiles) {
      // update PREMIS
      try {
        Binary b = retrievePreservationFile(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
        
        b = PremisUtils.updateFile(b, file);

        StoragePath filePath = ModelUtils.getPreservationMetadataStoragePath(file.getId(),
          PreservationMetadataType.OBJECT_FILE, file.getAipId(), file.getRepresentationId(), file.getPath(),
          file.getId());

        storage.updateBinaryContent(filePath, b.getContent(), false, true);
      } catch (IOException | GenericException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException | XmlException e) {
        LOGGER.warn("Error updating file format in storage for file {}/{}/{} ", file.getAipId(),
          file.getRepresentationId(), file.getId());
        LOGGER.warn(e.getMessage(),e);
      }
    }
    // TODO is any notify needed?
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
    String id = ModelUtils.generatePreservationMetadataId(type, aipId, representationId, fileDirectoryPath, fileId);
    return createPreservationMetadata(type, id, aipId, representationId, fileDirectoryPath, fileId, payload);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id, String aipId,
    String representationId, ContentPayload payload) throws GenericException, NotFoundException,
      RequestNotValidException, AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPreservationMetadata(type, id, aipId, representationId, null, null, payload);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id,
    ContentPayload payload) throws GenericException, NotFoundException, RequestNotValidException,
      AuthorizationDeniedException, ValidationException, AlreadyExistsException {
    return createPreservationMetadata(type, id, null, null, null, null, payload);
  }

  public PreservationMetadata createPreservationMetadata(PreservationMetadataType type, String id, String aipId,
    String representationId, List<String> fileDirectoryPath, String fileId, ContentPayload payload)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException,
      ValidationException, AlreadyExistsException {
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
    boolean validatePremis = true;
    List<IndexedPreservationAgent> agents = ModelUtils.extractAgentsFromPreservationBinary(payload, type,
      validatePremis);
    if (agents != null) {
      for (IndexedPreservationAgent pla : agents) {
        try{
          ContentPayload b = PremisUtils.createPremisAgentBinary(pla.getIdentifierValue(),
            pla.getTitle() + "/" + pla.getVersion(), pla.getType());
          createPreservationMetadata(PreservationMetadataType.AGENT, pla.getIdentifierValue(), b);
        }catch(AlreadyExistsException alreadyExists){
          LOGGER.warn("Agent already exists: "+pla.getIdentifierValue());
        }
      }
    }

    if (aipId != null) {
      // Update AIP metadata
      AIP aip = getAIPMetadata(aipId);
      aip.getMetadata().getPreservationMetadata().add(pm);
      updateAIPMetadata(aip);
    }

    notifyPreservationMetadataCreated(pm);
    return pm;
  }

  // TODO remove PREMIS type and file Id
  public void updatePreservationMetadata(PreservationMetadataType type, String aipId, String representationId,
    String id, ContentPayload payload)
      throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    PreservationMetadata pm = new PreservationMetadata();
    pm.setAipId(aipId);
    pm.setId(id);
    pm.setRepresentationId(representationId);
    pm.setType(type);
    StoragePath binaryPath = ModelUtils.getPreservationMetadataStoragePath(pm);
    storage.updateBinaryContent(binaryPath, payload, false, true);

    // set descriptive metadata type
    AIP aip = getAIPMetadata(aipId);
    List<PreservationMetadata> preservationMetadata = aip.getMetadata().getPreservationMetadata();
    Optional<PreservationMetadata> opm = preservationMetadata.stream().filter(m -> m.getId().equals(id)).findFirst();
    if (opm.isPresent()) {
      opm.get().setType(type);
    } else {
      preservationMetadata.add(pm);
    }
    updateAIP(aip);

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

    // update AIP metadata
    AIP aip = getAIPMetadata(aipId);
    for (Iterator<PreservationMetadata> it = aip.getMetadata().getPreservationMetadata().iterator(); it.hasNext();) {
      PreservationMetadata preservationMetadata = it.next();
      if (preservationMetadata.getId().equals(id)) {
        it.remove();
        break;
      }
    }

    notifyPreservationMetadataDeleted(pm);
  }

}
