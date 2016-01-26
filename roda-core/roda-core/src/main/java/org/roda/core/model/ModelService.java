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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.ValidationUtils;
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
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.RODAObjectPermissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationFilePreservationObject;
import org.roda.core.data.v2.ip.RepresentationPreservationObject;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentMetadata;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.ip.metadata.OtherMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
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
import org.roda.core.storage.EmptyClosableIterable;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSPathContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import jersey.repackaged.com.google.common.collect.Sets;
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
      storage.createContainer(DefaultStoragePath.parse(containerName), new HashMap<String, Set<String>>());
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
      storage.createDirectory(directoryPath, new HashMap<String, Set<String>>());
    } catch (AlreadyExistsException e) {
      // do nothing
    }

  }

  public StorageService getStorage() {
    return storage;
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
              return convertResourceToAIP(next);
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
    return convertResourceToAIP(directory);
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
    if (isAIPvalid(sourceModelService, sourceDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA)) {

      storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPpath(aipId));
      Directory newDirectory = storage.getDirectory(ModelUtils.getAIPpath(aipId));

      aip = convertResourceToAIP(newDirectory);
      if (notify) {
        notifyAipCreated(aip);
      }
    } else {
      throw new GenericException("Error while creating AIP, reason: AIP is not valid");
    }

    return aip;
  }

  public AIP createAIP(Map<String, Set<String>> metadata) throws RequestNotValidException, NotFoundException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return createAIP(metadata, true);
  }

  public AIP createAIP(Map<String, Set<String>> metadata, boolean notify) throws RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
    return createAIP(metadata, true, notify);
  }

  public AIP createAIP(Map<String, Set<String>> metadata, boolean active, boolean notify)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {

    // set basic AIP information
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_ACTIVE, active);
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_DATE_CREATED, new Date());
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_DATE_MODIFIED, new Date());

    // set default permissions
    // TODO setPermissions(metadata, defaultPermissions);

    AIP aip;
    Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP),
      metadata);
    aip = convertResourceToAIP(directory);
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
    AlreadyExistsException {
    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;

    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    if (isAIPvalid(sourceModelService, sourceDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA)) {
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);

      // FIXME is this the best way?
      storage.deleteResource(aipPath);

      storage.copy(sourceStorage, sourcePath, aipPath);
      Directory directoryUpdated = storage.getDirectory(aipPath);

      aip = convertResourceToAIP(directoryUpdated);
      notifyAipUpdated(aip);
    } else {
      throw new GenericException("Error while updating AIP");
    }

    return aip;
  }

  public AIP updateAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    AIP aip;
    StoragePath aipPath = ModelUtils.getAIPpath(aipId);
    Directory aipDirectory = storage.getDirectory(aipPath);
    if (isAIPvalid(this, aipDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA)) {
      aip = convertResourceToAIP(aipDirectory);
      notifyAipUpdated(aip);
    } else {
      throw new GenericException("Error while updating AIP");
    }

    return aip;
  }

  public void deleteAIP(String aipId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath aipPath = ModelUtils.getAIPpath(aipId);
    storage.deleteResource(aipPath);
    notifyAipDeleted(aipId);
  }

  public ClosableIterable<DescriptiveMetadata> listDescriptiveMetadataBinaries(String aipId)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<DescriptiveMetadata> it;

    try {
      final ClosableIterable<Resource> resourcesIterable = storage
        .listResourcesUnderDirectory(ModelUtils.getDescriptiveMetadataPath(aipId));
      final Iterator<Resource> resourcesIterator = resourcesIterable.iterator();

      it = new ClosableIterable<DescriptiveMetadata>() {

        @Override
        public Iterator<DescriptiveMetadata> iterator() {
          return new Iterator<DescriptiveMetadata>() {

            @Override
            public boolean hasNext() {
              if (resourcesIterator == null) {
                return false;
              }
              return resourcesIterator.hasNext();
            }

            @Override
            public DescriptiveMetadata next() {
              try {
                return convertResourceToDescriptiveMetadata(resourcesIterator.next());
              } catch (GenericException | NoSuchElementException e) {
                LOGGER.error("Error while listing descriptive metadata binaries", e);
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

    } catch (NotFoundException e) {
      it = new EmptyClosableIterable<DescriptiveMetadata>();
    }

    return it;
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

  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    Binary binary = retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
    DescriptiveMetadata descriptiveMetadataBinary = convertResourceToDescriptiveMetadata(binary);

    return descriptiveMetadataBinary;
  }

  // FIXME descriptiveMetadataType shouldn't be a parameter but instead be
  // already present in the Binary metadata
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType) throws RequestNotValidException, GenericException, AlreadyExistsException,
      AuthorizationDeniedException, NotFoundException {
    DescriptiveMetadata descriptiveMetadataBinary = null;

    // StoragePath binaryPath = binary.getStoragePath();
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;
    Map<String, Set<String>> binaryMetadata = binary.getMetadata();
    binaryMetadata.put(RodaConstants.STORAGE_META_TYPE, Sets.newHashSet(descriptiveMetadataType));

    storage.createBinary(binaryPath, binaryMetadata, binary.getContent(), asReference);
    descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType,
      binaryPath);
    notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);

    return descriptiveMetadataBinary;
  }

  // FIXME descriptiveMetadataType shouldn't be a parameter but instead be
  // already present in the Binary metadata (and therefore to be changed
  // appropriated method should be called)
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    DescriptiveMetadata descriptiveMetadataBinary = null;

    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
    boolean asReference = false;
    boolean createIfNotExists = false;

    storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);
    Map<String, Set<String>> binaryMetadata = binary.getMetadata();
    binaryMetadata.put(RodaConstants.STORAGE_META_TYPE, Sets.newHashSet(descriptiveMetadataType));
    storage.updateMetadata(binaryPath, binaryMetadata, true);

    descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType,
      binaryPath);
    notifyDescriptiveMetadataUpdated(descriptiveMetadataBinary);

    return descriptiveMetadataBinary;
  }

  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);

    storage.deleteResource(binaryPath);
    notifyDescriptiveMetadataDeleted(aipId, descriptiveMetadataId);

  }

  public ClosableIterable<Representation> listRepresentations(String aipId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<Representation> it = null;

    ClosableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId));
    final Iterator<Resource> resourcesIterator = resourcesIterable.iterator();

    it = new ClosableIterable<Representation>() {

      @Override
      public Iterator<Representation> iterator() {
        return new Iterator<Representation>() {

          @Override
          public boolean hasNext() {
            if (resourcesIterator == null) {
              return true;
            }
            return resourcesIterator.hasNext();
          }

          @Override
          public Representation next() {
            try {
              return convertResourceToRepresentation(resourcesIterator.next());
            } catch (NotFoundException | NoSuchElementException | GenericException | AuthorizationDeniedException
              | RequestNotValidException e) {
              LOGGER.error("Error while listing representations", e);
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

    return it;
  }

  public Representation retrieveRepresentation(String aipId, String representationId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Representation representation;
    Directory directory = storage.getDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
    representation = convertResourceToRepresentation(directory);

    return representation;
  }

  // TODO support asReference
  public Representation createRepresentation(String aipId, String representationId, StorageService sourceStorage,
    StoragePath sourcePath) throws RequestNotValidException, GenericException, NotFoundException,
      AuthorizationDeniedException, AlreadyExistsException {
    Representation representation;

    StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId, representationId);

    // verify structure of source representation
    Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
    if (isRepresentationValid(sourceDirectory)) {
      storage.copy(sourceStorage, sourcePath, directoryPath);
      Directory directory = storage.getDirectory(directoryPath);

      representation = convertResourceToRepresentation(directory);
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

    // get representation metadata (from source representation)
    Map<String, Set<String>> representationMetadata = sourceDirectory.getMetadata();

    // obtain information (from metadata) to build representation object
    boolean active = ModelUtils.getBoolean(representationMetadata, RodaConstants.STORAGE_META_ACTIVE);
    Date dateCreated = ModelUtils.getDate(representationMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
    Date dateModified = new Date();
    Long sizeInBytes = ModelUtils.getLong(representationMetadata, RodaConstants.STORAGE_META_SIZE_IN_BYTES);
    representationMetadata.put(RodaConstants.STORAGE_META_DATE_MODIFIED,
      Sets.newHashSet(RodaUtils.dateToString(dateModified)));
    String type = ModelUtils.getString(representationMetadata, RodaConstants.STORAGE_META_TYPE);
    Set<RepresentationState> statuses = ModelUtils.getStatuses(representationMetadata);

    // update representation metadata (essentially date.modified)
    updateRepresentationMetadata(aipId, representationId, representationMetadata);

    // build return object
    Representation representation = new Representation(representationId, aipId, active, dateCreated, dateModified,
      statuses, type, sizeInBytes, fileIDsToUpdate);
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

  private void updateRepresentationMetadata(String aipId, String representationId,
    Map<String, Set<String>> representationMetadata)
      throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException {
    storage.updateMetadata(ModelUtils.getRepresentationPath(aipId, representationId), representationMetadata, true);

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
          fileIDsToUpdate.add(fileUpdated.getStoragePath().getName());
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
    notifyRepresentationDeleted(aipId, representationId);

  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public Iterable<File> listFiles(String aipId, String representationId)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    Iterable<File> it = null;

    final Iterator<Resource> iterator = storage.listResourcesUnderDirectory(ModelUtils.getRepresentationsPath(aipId))
      .iterator();

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
            } catch (GenericException | NoSuchElementException e) {
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

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File retrieveFile(String aipId, String representationId, String fileId)
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

    final Binary createdBinary = storage.createBinary(filePath, binary.getMetadata(), binary.getContent(), asReference);
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
    storage.updateMetadata(filePath, binary.getMetadata(), true);
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
      Iterator<Resource> preservationIterator = storage
        .listResourcesUnderDirectory(ModelUtils.getPreservationPath(aipId, resource.getStoragePath().getName()))
        .iterator();
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

  public RepresentationPreservationObject getRepresentationPreservationObject(String aipId, String representationId,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationPreservationObject obj = null;

    StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
    Binary b = storage.getBinary(sp);
    obj = convertResourceToRepresentationPreservationObject(aipId, representationId, fileId, b);

    return obj;
  }

  public EventPreservationObject getEventPreservationObject(String aipId, String representationId,
    String preservationObjectID)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    EventPreservationObject obj = null;

    StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, preservationObjectID);
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
        Map<String, Set<String>> directoryMetadata = resource.getMetadata();

        // retrieve needed information to instantiate Representation
        Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
        // Date dateCreated = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_CREATED);
        // Date dateModified = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_MODIFIED);

        if (active == null) {
          // when not stated, considering active=false
          active = false;
        }
        PremisAgentHelper pah = PremisAgentHelper.newInstance(resource.getContent().createInputStream());
        AgentPreservationObject apo = new AgentPreservationObject();
        apo.setAgentName((pah.getAgent().getAgentNameList() != null && pah.getAgent().getAgentNameList().size() > 0)
          ? pah.getAgent().getAgentNameList().get(0) : "");
        apo.setAgentType(pah.getAgent().getAgentType());
        apo.setFileID(agentID);
        apo
          .setId((pah.getAgent().getAgentIdentifierList() != null && pah.getAgent().getAgentIdentifierList().size() > 0)
            ? pah.getAgent().getAgentIdentifierList().get(0).getAgentIdentifierValue() : "");
        apo.setType(""); // TODO: ??????????
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

  private boolean isAIPvalid(ModelService model, Directory directory, boolean failIfNoDescriptiveMetadataSchema)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    boolean valid = true;
    // validate metadata (against schemas)
    valid = ValidationUtils.isAIPDescriptiveMetadataValid(model, directory.getStoragePath().getName(),
      failIfNoDescriptiveMetadataSchema);

    // FIXME validate others aspects

    return valid;
  }

  private boolean isRepresentationValid(Directory directory) {
    // FIXME implement this
    return true;
  }

  private AIP convertResourceToAIP(Resource resource)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    AIP aip;
    if (resource instanceof DefaultDirectory) {
      StoragePath storagePath = resource.getStoragePath();
      Map<String, Set<String>> metadata = resource.getMetadata();

      // obtain basic AIP information
      String parentId = ModelUtils.getString(metadata, RodaConstants.STORAGE_META_PARENT_ID);
      Boolean active = ModelUtils.getBoolean(metadata, RodaConstants.STORAGE_META_ACTIVE);
      Date dateCreated = ModelUtils.getDate(metadata, RodaConstants.STORAGE_META_DATE_CREATED);
      Date dateModified = ModelUtils.getDate(metadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

      // obtain permissions
      RODAObjectPermissions permissions = getPermissions(metadata);

      if (active == null) {
        // when not stated, consider active=false
        active = false;
      }

      // obtain descriptive metadata information
      List<String> descriptiveMetadataBinaryIds = ModelUtils.getChildIds(storage,
        ModelUtils.getDescriptiveMetadataPath(storagePath.getName()), false);

      // obtain representations information
      List<String> representationIds = ModelUtils.getChildIds(storage,
        ModelUtils.getRepresentationsPath(storagePath.getName()), false);

      // obtain preservation information
      final Map<String, List<String>> preservationRepresentationObjects = new HashMap<String, List<String>>();
      final Map<String, List<String>> preservationFileObjects = new HashMap<String, List<String>>();
      final Map<String, List<String>> preservationEvents = new HashMap<String, List<String>>();
      retrieveAIPPreservationInformation(storagePath, representationIds, preservationRepresentationObjects,
        preservationFileObjects, preservationEvents);

      aip = new AIP(storagePath.getName(), parentId, active, permissions, descriptiveMetadataBinaryIds,
        representationIds, preservationRepresentationObjects, preservationEvents, preservationFileObjects);

    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Directory into an AIP (" + resource + ")");

    }
    return aip;
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

  private RODAObjectPermissions getPermissions(Map<String, Set<String>> metadata) {
    RODAObjectPermissions permissions = new RODAObjectPermissions();

    // grant permissions
    Set<String> set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_GRANT_USERS);
    permissions.setGrantUsers(set);
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_GRANT_GROUPS);
    permissions.setGrantGroups(set);

    // read permissions
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_READ_USERS);
    permissions.setReadUsers(set);
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_READ_GROUPS);
    permissions.setReadGroups(set);

    // insert permissions
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_INSERT_USERS);
    permissions.setInsertUsers(set);
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_INSERT_GROUPS);
    permissions.setInsertGroups(set);

    // modify permissions
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_MODIFY_USERS);
    permissions.setModifyUsers(set);
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_MODIFY_GROUPS);
    permissions.setModifyGroups(set);

    // remove permissions
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_REMOVE_USERS);
    permissions.setRemoveUsers(set);
    set = metadata.get(RodaConstants.STORAGE_META_PERMISSION_REMOVE_GROUPS);
    permissions.setRemoveGroups(set);

    return permissions;
  }

  private void retrieveAIPPreservationInformation(StoragePath storagePath, List<String> representationIds,
    final Map<String, List<String>> preservationRepresentationObjects,
    final Map<String, List<String>> preservationFileObjects, final Map<String, List<String>> preservationEvents)
      throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    for (String representationID : representationIds) {
      StoragePath representationPreservationPath = ModelUtils.getPreservationPath(storagePath.getName(),
        representationID);

      // obtain list of preservation related files
      List<String> preservationFileIds = ModelUtils.getChildIds(storage, representationPreservationPath, false);

      final List<String> preservationRepresentationObjectFileIds = new ArrayList<String>();
      final List<String> preservationFileObjectFileIds = new ArrayList<String>();
      final List<String> preservationEventFileIds = new ArrayList<String>();

      for (String preservationFileId : preservationFileIds) {
        StoragePath binaryPath = ModelUtils.getPreservationFilePath(storagePath.getName(), representationID,
          preservationFileId);
        Binary preservationBinary = storage.getBinary(binaryPath);

        lc.xmlns.premisV2.Representation representation = ModelUtils
          .getPreservationRepresentationObject(preservationBinary);
        if (representation != null) {
          preservationRepresentationObjectFileIds.add(preservationFileId);
        } else {
          EventComplexType event = ModelUtils.getPreservationEvent(preservationBinary);
          if (event != null) {
            preservationEventFileIds.add(preservationFileId);
          } else {
            lc.xmlns.premisV2.File file = ModelUtils.getPreservationFileObject(preservationBinary);
            if (file != null) {
              preservationFileObjectFileIds.add(preservationFileId);
            } else {
              LOGGER.warn(
                "The binary {} is neither a PreservationRepresentationObject or PreservationEvent or PreservationFileObject...Moving on...",
                binaryPath.asString());
            }
          }
        }
      }
      preservationRepresentationObjects.put(representationID, preservationRepresentationObjectFileIds);
      preservationFileObjects.put(representationID, preservationFileObjectFileIds);
      preservationEvents.put(representationID, preservationEventFileIds);

    }
  }

  private DescriptiveMetadata convertResourceToDescriptiveMetadata(Resource resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      // retrieve needed information to instantiate DescriptiveMetadata
      String type = ModelUtils.getString(resource.getMetadata(), RodaConstants.STORAGE_META_TYPE);

      return new DescriptiveMetadata(resource.getStoragePath().getName(),
        ModelUtils.getAIPidFromStoragePath(resource.getStoragePath()), type, resource.getStoragePath());
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a descriptive metadata binary");
    }
  }

  private PreservationMetadata convertResourceToPreservationMetadata(Resource resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      String type = ModelUtils.getPreservationType((DefaultBinary) resource);
      return new PreservationMetadata(resource.getStoragePath().getName(),
        ModelUtils.getAIPidFromStoragePath(resource.getStoragePath()),
        ModelUtils.getRepresentationIdFromStoragePath(resource.getStoragePath()), resource.getStoragePath(), type);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a preservation metadata binary");
    }
  }

  private Representation convertResourceToRepresentation(Resource resource)
    throws GenericException, NotFoundException, AuthorizationDeniedException, RequestNotValidException {
    if (resource instanceof DefaultDirectory) {
      StoragePath directoryPath = resource.getStoragePath();
      Map<String, Set<String>> directoryMetadata = resource.getMetadata();

      // retrieve needed information to instantiate Representation
      Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
      Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
      Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);
      Set<RepresentationState> statuses = ModelUtils.getStatuses(directoryMetadata);
      String type = ModelUtils.getString(directoryMetadata, RodaConstants.STORAGE_META_TYPE);
      List<String> fileIds = ModelUtils.getChildIds(storage, resource.getStoragePath(), true);
      Long sizeInBytes = ModelUtils.getLong(directoryMetadata, RodaConstants.STORAGE_META_SIZE_IN_BYTES);

      if (sizeInBytes == null) {
        sizeInBytes = 0L;
      }

      if (active == null) {
        // when not stated, considering active=false
        active = false;
      }

      return new Representation(directoryPath.getName(), ModelUtils.getAIPidFromStoragePath(directoryPath), active,
        dateCreated, dateModified, statuses, type, sizeInBytes, fileIds);

    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Directory into a representation");
    }
  }

  private File convertResourceToRepresentationFile(Resource resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      StoragePath binaryPath = resource.getStoragePath();
      Map<String, Set<String>> metadata = resource.getMetadata();

      // retrieve needed information to instantiate File
      Boolean entryPoint = ModelUtils.getBoolean(metadata, RodaConstants.STORAGE_META_ENTRYPOINT);

      if (entryPoint == null) {
        entryPoint = false;
      }

      return new File(binaryPath.getName(), ModelUtils.getAIPidFromStoragePath(binaryPath),
        ModelUtils.getRepresentationIdFromStoragePath(binaryPath), entryPoint, binaryPath, binaryPath.getName(),
        ((DefaultBinary) resource).getSizeInBytes(), true);
    } else {
      throw new GenericException(
        "Error while trying to convert something that it isn't a Binary into a representation file");
    }
  }

  public RepresentationPreservationObject retrieveRepresentationPreservationObject(String aipId,
    String representationId, String fileId)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationPreservationObject representationPreservationObject = null;

    StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
    Binary binary = storage.getBinary(filePath);
    representationPreservationObject = convertResourceToRepresentationPreservationObject(aipId, representationId,
      fileId, binary);
    representationPreservationObject.setId(fileId);

    return representationPreservationObject;
  }

  public RepresentationFilePreservationObject retrieveRepresentationFileObject(String aipId, String representationId,
    String fileId) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    RepresentationFilePreservationObject representationPreservationObject = null;

    StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId + ".premis.xml");
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
        Map<String, Set<String>> directoryMetadata = resource.getMetadata();

        // retrieve needed information to instantiate Representation
        Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
        // Date dateCreated = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_CREATED);
        // Date dateModified = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_MODIFIED);

        if (active == null) {
          // when not stated, considering active=false
          active = false;
        }

        // FIXME check if inputstream gets closed
        PremisFileObjectHelper pfoh = PremisFileObjectHelper.newInstance(resource.getContent().createInputStream());

        RepresentationFilePreservationObject rfpo = new RepresentationFilePreservationObject();
        rfpo.setAipID(aipId);
        rfpo.setRepresentationID(representationId);
        rfpo.setFileID(fileId);
        rfpo.setCompositionLevel(pfoh.getRepresentationFilePreservationObject().getCompositionLevel());
        rfpo.setContentLocationType(pfoh.getRepresentationFilePreservationObject().getContentLocationType());
        rfpo.setContentLocationValue(pfoh.getRepresentationFilePreservationObject().getContentLocationValue());
        rfpo.setCreatedDate(pfoh.getRepresentationFilePreservationObject().getCreatedDate());
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
        rfpo.setId(pfoh.getRepresentationFilePreservationObject().getId());
        rfpo.setLabel(pfoh.getRepresentationFilePreservationObject().getLabel());
        rfpo.setLastModifiedDate(pfoh.getRepresentationFilePreservationObject().getLastModifiedDate());
        rfpo.setMimetype(pfoh.getRepresentationFilePreservationObject().getMimetype());
        rfpo.setModel(pfoh.getRepresentationFilePreservationObject().getModel());
        rfpo.setObjectCharacteristicsExtension(
          pfoh.getRepresentationFilePreservationObject().getObjectCharacteristicsExtension());
        rfpo.setOriginalName(pfoh.getRepresentationFilePreservationObject().getOriginalName());
        rfpo.setPreservationLevel(pfoh.getRepresentationFilePreservationObject().getPreservationLevel());
        rfpo.setPronomId(pfoh.getRepresentationFilePreservationObject().getPronomId());
        rfpo.setRepresentationObjectId(pfoh.getRepresentationFilePreservationObject().getRepresentationObjectId());
        rfpo.setSize(pfoh.getRepresentationFilePreservationObject().getSize());
        rfpo.setState(pfoh.getRepresentationFilePreservationObject().getState());
        rfpo.setType(pfoh.getRepresentationFilePreservationObject().getType());
        rfpo.setRepresentationID(representationId);
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
        Map<String, Set<String>> directoryMetadata = resource.getMetadata();

        // retrieve needed information to instantiate Representation
        Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
        // Date dateCreated = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_CREATED);
        // Date dateModified = ModelUtils.getDate(directoryMetadata,
        // RodaConstants.STORAGE_META_DATE_MODIFIED);

        if (active == null) {
          // when not stated, considering active=false
          active = false;
        }

        // FIXME check if inputstream gets closed
        PremisRepresentationObjectHelper proh = PremisRepresentationObjectHelper
          .newInstance(resource.getContent().createInputStream());
        RepresentationPreservationObject rpo = new RepresentationPreservationObject();
        rpo.setAipID(aipId);
        rpo.setRepresentationID(representationId);
        rpo.setFileID(fileId);
        rpo.setCreatedDate(proh.getRepresentationPreservationObject().getCreatedDate());
        rpo.setDerivationEventID(proh.getRepresentationPreservationObject().getDerivationEventID());
        rpo.setDerivedFromRepresentationObjectID(
          proh.getRepresentationPreservationObject().getDerivedFromRepresentationObjectID());
        rpo.setId(proh.getRepresentationPreservationObject().getId());
        rpo.setLabel(proh.getRepresentationPreservationObject().getLabel());
        rpo.setLastModifiedDate(proh.getRepresentationPreservationObject().getLastModifiedDate());
        rpo.setModel(proh.getRepresentationPreservationObject().getModel());
        rpo.setPartFiles(proh.getRepresentationPreservationObject().getPartFiles());
        rpo.setPreservationEventIDs(proh.getRepresentationPreservationObject().getPreservationEventIDs());
        rpo.setPreservationLevel(proh.getRepresentationPreservationObject().getPreservationLevel());
        rpo.setRepresentationObjectID(proh.getRepresentationPreservationObject().getRepresentationObjectID());
        rpo.setRootFile(proh.getRepresentationPreservationObject().getRootFile());
        rpo.setState(proh.getRepresentationPreservationObject().getState());
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

  public EventPreservationObject retrieveEventPreservationObject(String aipId, String representationId, String fileId)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    EventPreservationObject eventPreservationObject = null;

    StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
    Binary binary = storage.getBinary(filePath);
    eventPreservationObject = convertResourceToEventPreservationObject(aipId, representationId, fileId, binary);
    eventPreservationObject.setId(fileId);

    return eventPreservationObject;
  }

  // FIXME verify/refactor this method
  private EventPreservationObject convertResourceToEventPreservationObject(String aipId, String representationId,
    String fileId, Binary resource) throws GenericException {
    if (resource instanceof DefaultBinary) {
      try {
        Map<String, Set<String>> directoryMetadata = resource.getMetadata();
        // retrieve needed information to instantiate Representation
        Boolean active = ModelUtils.getBoolean(directoryMetadata, RodaConstants.STORAGE_META_ACTIVE);
        Date dateCreated = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_CREATED);
        Date dateModified = ModelUtils.getDate(directoryMetadata, RodaConstants.STORAGE_META_DATE_MODIFIED);

        if (active == null) {
          active = false;
        }

        PremisEventHelper peh = PremisEventHelper.newInstance(resource.getContent().createInputStream());

        // TODO premisevent to EventPreservationObject
        EventPreservationObject epo = new EventPreservationObject();
        epo.setAipID(aipId);
        epo.setRepresentationID(representationId);
        epo.setFileID(fileId);
        epo.setLastModifiedDate(dateModified);
        epo.setCreatedDate(dateCreated);
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
        epo.setId(fileId);
        epo.setLabel(""); // TODO: ???
        epo.setModel(""); // TODO: ???
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
        epo.setState(""); // TODO: ???
        epo.setTargetID(""); // TODO: ???
        epo.setType(peh.getEvent().getEventType());
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
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    ClosableIterable<PreservationMetadata> it;

    final ClosableIterable<Resource> resourcesIterable = storage
      .listResourcesUnderDirectory(ModelUtils.getPreservationPath(aipId, representationID));
    final Iterator<Resource> resourcesIterator = resourcesIterable.iterator();

    it = new ClosableIterable<PreservationMetadata>() {

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
              return convertResourceToPreservationMetadata(resourcesIterator.next());
            } catch (GenericException | NoSuchElementException e) {
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

    return it;
  }

  // TODO verify
  public PreservationMetadata createPreservationMetadata(String aipId, String representationID,
    String preservationMetadataId, Binary binary)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    PreservationMetadata preservationMetadataBinary;

    StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationID, preservationMetadataId);
    boolean asReference = false;
    Binary updatedBinary = storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, true);
    preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, representationID, binaryPath,
      ModelUtils.getPreservationType(updatedBinary));
    notifyPreservationMetadataCreated(preservationMetadataBinary);

    return preservationMetadataBinary;
  }

  // TODO verify
  public PreservationMetadata createPreservationMetadata(String aipId, String preservationMetadataId, Binary binary)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    PreservationMetadata preservationMetadataBinary;
    StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, preservationMetadataId);
    boolean asReference = false;
    Binary updatedBinary = storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, true);
    preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, null, binaryPath,
      ModelUtils.getPreservationType(updatedBinary));
    notifyPreservationMetadataCreated(preservationMetadataBinary);
    return preservationMetadataBinary;
  }

  public AgentMetadata createAgentMetadata(String agentID, StorageService sourceStorage, StoragePath sourcePath,
    boolean notify) throws RequestNotValidException, AlreadyExistsException, GenericException, NotFoundException,
      AuthorizationDeniedException {
    AgentMetadata agentMetadata;

    // TODO validate agent
    storage.copy(sourceStorage, sourcePath, ModelUtils.getPreservationAgentPath(agentID));
    agentMetadata = new AgentMetadata(agentID, ModelUtils.getPreservationAgentPath(agentID));
    if (notify) {
      notifyAgentMetadataCreated(agentMetadata);
    }

    return agentMetadata;
  }

  // TODO verify
  public AgentMetadata createAgentMetadata(String agentID, Binary binary) throws RequestNotValidException,
    GenericException, AlreadyExistsException, AuthorizationDeniedException, NotFoundException {
    AgentMetadata agentMetadataBinary;

    StoragePath binaryPath = ModelUtils.getPreservationAgentPath(agentID);
    boolean asReference = false;
    Map<String, Set<String>> binaryMetadata = binary.getMetadata();
    storage.createBinary(binaryPath, binaryMetadata, binary.getContent(), asReference);

    agentMetadataBinary = new AgentMetadata(agentID, binaryPath);
    notifyAgentMetadataCreated(agentMetadataBinary);

    return agentMetadataBinary;
  }

  // TODO verify
  public AgentMetadata updateAgentMetadata(String agentID, Binary binary)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    AgentMetadata agentMetadataBinary;

    storage.updateBinaryContent(binary.getStoragePath(), binary.getContent(), binary.isReference(), true);
    storage.updateMetadata(binary.getStoragePath(), binary.getMetadata(), true);
    agentMetadataBinary = new AgentMetadata(agentID, binary.getStoragePath());
    notifyAgentMetadataUpdated(agentMetadataBinary);

    return agentMetadataBinary;
  }

  public void deleteAgentMetadata(String agentID)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    storage.deleteResource(ModelUtils.getPreservationAgentPath(agentID));
    notifyAgentMetadataDeleted(agentID);

  }

  // TODO verify
  public PreservationMetadata updatePreservationMetadata(String aipId, String representationId,
    String preservationMetadataId, Binary binary, boolean payloadOnly)
      throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    PreservationMetadata preservationMetadataBinary;
    String type = ModelUtils.getPreservationType(binary);
    StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationId, preservationMetadataId);
    if (payloadOnly) {
      storage.updateBinaryContent(binaryPath, binary.getContent(), false, true);
      preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, representationId, binaryPath,
        type);
    } else {

      boolean asReference = false;
      boolean createIfNotExists = false;
      storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);
      Map<String, Set<String>> binaryMetadata = binary.getMetadata();
      storage.updateMetadata(binaryPath, binaryMetadata, true);
      preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, representationId, binaryPath,
        type);
    }
    notifyPreservationMetadataUpdated(preservationMetadataBinary);

    return preservationMetadataBinary;
  }

  // TODO verify
  public void deletePreservationMetadata(String aipId, String representationId, String preservationMetadataId)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationId, preservationMetadataId);
    storage.deleteResource(binaryPath);
    notifyPreservationMetadataDeleted(aipId, representationId, preservationMetadataId);
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
            storage.createBinary(logPath, new HashMap<String, Set<String>>(), new FSPathContentPayload(path), false);
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

  public OtherMetadata retrieveOtherMetadata(String aipId, String representationId, String fileId, String type)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipId, representationId, fileId, type);
    return new OtherMetadata(type + "_" + aipId + "_" + representationId + "_" + fileId, aipId, type, binaryPath);
  }

  public OtherMetadata createOtherMetadata(String aipID, String representationId, String fileName, String type,
    Binary binary) throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    OtherMetadata otherMetadataBinary = null;
    try {
      StoragePath otherMetadataPath = ModelUtils.getToolRepresentationMetadataDirectory(aipID, representationId, type);
      storage.getDirectory(otherMetadataPath);
      LOGGER.debug("Tool directory already exists...");
    } catch (NotFoundException e) {
      LOGGER.debug("Tool directory doesn't exist... Creating...");
      try {
        StoragePath otherMetadataPath = ModelUtils.getOtherMetadataDirectory(aipID);
        storage.createDirectory(otherMetadataPath, new HashMap<String, Set<String>>());
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }

      try {
        StoragePath otherMetadataPath = ModelUtils.getToolMetadataDirectory(aipID, type);
        storage.createDirectory(otherMetadataPath, new HashMap<String, Set<String>>());
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }
      try {
        StoragePath otherMetadataPath = ModelUtils.getToolRepresentationMetadataDirectory(aipID, representationId,
          type);
        storage.createDirectory(otherMetadataPath, new HashMap<String, Set<String>>());
      } catch (AlreadyExistsException e1) {
        // nothing to do
      }
    }

    StoragePath binaryPath = ModelUtils.getToolMetadataPath(aipID, representationId, fileName, type);
    boolean asReference = false;
    boolean createIfNotExists = true;
    storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);
    otherMetadataBinary = new OtherMetadata(type + "_" + aipID + "_" + representationId + "_" + fileName, aipID, type,
      binaryPath);
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
          file.getRepresentationId(), file.getId() + ".premis.xml");
        rfpo = PremisUtils.updateFile(rfpo, file);
        Path premisFile = Files.createTempFile("file", ".premis.xml");
        PremisFileObjectHelper helper = new PremisFileObjectHelper(rfpo);
        helper.saveToFile(premisFile.toFile());
        Binary b = (Binary) FSUtils.convertPathToResource(premisFile.getParent(), premisFile);
        storage.updateBinaryContent(
          ModelUtils.getPreservationFilePath(file.getAipId(), file.getRepresentationId(), file.getId() + ".premis.xml"),
          b.getContent(), false, true);
      } catch (IOException | PremisMetadataException | GenericException | RequestNotValidException | NotFoundException
        | AuthorizationDeniedException e) {
        LOGGER.warn("Error updating file format in storage for file {}/{}/{} ", file.getAipId(),
          file.getRepresentationId(), file.getId());
      }
    }
    // TODO is any notify needed?
  }
}
