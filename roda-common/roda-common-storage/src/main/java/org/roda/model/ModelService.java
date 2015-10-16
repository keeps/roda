package org.roda.model;

import java.io.IOException;
import java.io.StringWriter;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.roda.common.LdapUtilityException;
import org.roda.common.RodaUtils;
import org.roda.common.UserUtility;
import org.roda.common.ValidationUtils;
import org.roda.core.common.EmailAlreadyExistsException;
import org.roda.core.common.GroupAlreadyExistsException;
import org.roda.core.common.IllegalOperationException;
import org.roda.core.common.NoSuchGroupException;
import org.roda.core.common.NoSuchUserException;
import org.roda.core.common.RodaConstants;
import org.roda.core.common.UserAlreadyExistsException;
import org.roda.core.data.v2.AgentPreservationObject;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.LogEntry;
import org.roda.core.data.v2.RODAObjectPermissions;
import org.roda.core.data.v2.Representation;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.RepresentationPreservationObject;
import org.roda.core.data.v2.RepresentationState;
import org.roda.core.data.v2.SIPReport;
import org.roda.core.data.v2.User;
import org.roda.core.metadata.v2.premis.PremisAgentHelper;
import org.roda.core.metadata.v2.premis.PremisEventHelper;
import org.roda.core.metadata.v2.premis.PremisFileObjectHelper;
import org.roda.core.metadata.v2.premis.PremisMetadataException;
import org.roda.core.metadata.v2.premis.PremisRepresentationObjectHelper;
import org.roda.model.utils.ModelUtils;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Directory;
import org.roda.storage.EmptyClosableIterable;
import org.roda.storage.Resource;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.XMLContentPayload;
import org.roda.storage.fs.FSPathContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import jersey.repackaged.com.google.common.collect.Sets;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.EventOutcomeDetailComplexType;
import lc.xmlns.premisV2.EventOutcomeInformationComplexType;
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
    createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_AIP);
    createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_PRESERVATION);
    createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_ACTIONLOG);
    createContainerIfNotExists(RodaConstants.STORAGE_CONTAINER_SIP_REPORT);
  }

  private void createContainerIfNotExists(String containerName) {
    try {
      storage.createContainer(DefaultStoragePath.parse(containerName), new HashMap<String, Set<String>>());
    } catch (StorageServiceException e) {
      if (e.getCode() != StorageServiceException.ALREADY_EXISTS) {
        LOGGER.error("Error initializing container: " + containerName, e);
      }
    }
  }

  private void ensureAllDiretoriesExist() {
    try {
      createDirectoryIfNotExists(
        DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_PRESERVATION, RodaConstants.STORAGE_DIRECTORY_AGENTS));
    } catch (StorageServiceException e) {
      LOGGER.error("Error initializing directories", e);
    }
  }

  private void createDirectoryIfNotExists(StoragePath directoryPath) {
    try {
      storage.createDirectory(directoryPath, new HashMap<String, Set<String>>());
    } catch (StorageServiceException e) {
      if (e.getCode() != StorageServiceException.ALREADY_EXISTS) {
        LOGGER.error("Error initializing directory: " + directoryPath.asString(), e);
      }
    }
  }

  public StorageService getStorage() {
    return storage;
  }

  public ClosableIterable<AIP> listAIPs() throws ModelServiceException {
    ClosableIterable<AIP> aipsIterable;

    try {
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
              } catch (ModelServiceException | NoSuchElementException e) {
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
    } catch (StorageServiceException e) {

      throw new ModelServiceException("Error while obtaining AIP list from storage", e.getCode(), e);
    }

    return aipsIterable;
  }

  public AIP retrieveAIP(String aipId) throws ModelServiceException {
    AIP aip;
    try {
      Directory directory = storage.getDirectory(ModelUtils.getAIPpath(aipId));
      aip = convertResourceToAIP(directory);
    } catch (StorageServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        throw new ModelServiceException("AIP not found: " + aipId, ModelServiceException.NOT_FOUND, e);
      } else if (e.getCode() == StorageServiceException.FORBIDDEN) {
        throw new ModelServiceException("You do not have permission to access AIP: " + aipId,
          ModelServiceException.FORBIDDEN, e);
      } else {
        throw new ModelServiceException("Unexpected error while retrieving AIP",
          ModelServiceException.INTERNAL_SERVER_ERROR, e);
      }

    }
    return aip;
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
   * @throws ModelServiceException
   */
  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, boolean notify,
    Path configBasePath) throws ModelServiceException {
    // TODO verify structure of source AIP and copy it to the storage
    // XXX possible optimization would be to allow move between storage
    // TODO support asReference
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;
    try {
      Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
      if (isAIPvalid(sourceModelService, sourceDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA, configBasePath)) {

        storage.copy(sourceStorage, sourcePath, ModelUtils.getAIPpath(aipId));
        Directory newDirectory = storage.getDirectory(ModelUtils.getAIPpath(aipId));

        aip = convertResourceToAIP(newDirectory);
        if (notify) {
          notifyAipCreated(aip);
        }
      } else {
        throw new ModelServiceException("Error while creating AIP, reason: AIP is not valid",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating AIP in storage", e.getCode(), e);
    }

    return aip;
  }

  public AIP createAIP(Map<String, Set<String>> metadata) throws ModelServiceException {
    return createAIP(metadata, true);
  }

  public AIP createAIP(Map<String, Set<String>> metadata, boolean notify) throws ModelServiceException {

    // set basic AIP information
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_ACTIVE, Boolean.TRUE);
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_DATE_CREATED, new Date());
    ModelUtils.setAs(metadata, RodaConstants.STORAGE_META_DATE_MODIFIED, new Date());

    // set default permissions
    // TODO setPermissions(metadata, defaultPermissions);

    AIP aip;
    try {
      Directory directory = storage.createRandomDirectory(DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_AIP),
        metadata);
      aip = convertResourceToAIP(directory);
      if (notify) {
        notifyAipCreated(aip);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating AIP in storage", e.getCode(), e);
    }
    return aip;
  }

  public AIP createAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, Path configBasePath)
    throws ModelServiceException {
    return createAIP(aipId, sourceStorage, sourcePath, true, configBasePath);
  }

  // TODO support asReference
  public AIP updateAIP(String aipId, StorageService sourceStorage, StoragePath sourcePath, Path configBasePath)
    throws ModelServiceException {
    // TODO verify structure of source AIP and update it in the storage
    ModelService sourceModelService = new ModelService(sourceStorage);
    AIP aip;
    try {
      Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
      if (isAIPvalid(sourceModelService, sourceDirectory, FAIL_IF_NO_DESCRIPTIVE_METADATA_SCHEMA, configBasePath)) {
        StoragePath aipPath = ModelUtils.getAIPpath(aipId);

        // FIXME is this the best way?
        storage.deleteResource(aipPath);

        storage.copy(sourceStorage, sourcePath, aipPath);
        Directory directoryUpdated = storage.getDirectory(aipPath);

        aip = convertResourceToAIP(directoryUpdated);
        notifyAipUpdated(aip);
      } else {
        throw new ModelServiceException("Error while updating AIP", ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating AIP in storage", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return aip;
  }

  public void deleteAIP(String aipId) throws ModelServiceException {
    try {
      StoragePath aipPath = ModelUtils.getAIPpath(aipId);

      storage.deleteResource(aipPath);
      notifyAipDeleted(aipId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error deleting AIP from storage, reason: " + e.getMessage(), e.getCode());
    }
  }

  public ClosableIterable<DescriptiveMetadata> listDescriptiveMetadataBinaries(String aipId)
    throws ModelServiceException {
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
              } catch (ModelServiceException | NoSuchElementException e) {
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

    } catch (StorageServiceException e) {
      if (e.getCode() == StorageServiceException.NOT_FOUND) {
        it = new EmptyClosableIterable<DescriptiveMetadata>();
      } else {
        throw new ModelServiceException("Error while obtaining descriptive metadata binary list from storage",
          ModelServiceException.INTERNAL_SERVER_ERROR, e);
      }
    }

    return it;
  }

  public Binary retrieveDescriptiveMetadataBinary(String aipId, String descriptiveMetadataId)
    throws ModelServiceException {
    Binary binary;

    try {
      StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
      binary = storage.getBinary(binaryPath);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining descriptive metadata binary from storage", e.getCode(), e);
    }

    return binary;
  }

  public DescriptiveMetadata retrieveDescriptiveMetadata(String aipId, String descriptiveMetadataId)
    throws ModelServiceException {

    Binary binary = retrieveDescriptiveMetadataBinary(aipId, descriptiveMetadataId);
    DescriptiveMetadata descriptiveMetadataBinary = convertResourceToDescriptiveMetadata(binary);

    return descriptiveMetadataBinary;
  }

  // FIXME descriptiveMetadataType shouldn't be a parameter but instead be
  // already present in the Binary metadata
  public DescriptiveMetadata createDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType) throws ModelServiceException {
    DescriptiveMetadata descriptiveMetadataBinary = null;
    try {
      // StoragePath binaryPath = binary.getStoragePath();
      StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);
      boolean asReference = false;
      Map<String, Set<String>> binaryMetadata = binary.getMetadata();
      binaryMetadata.put(RodaConstants.STORAGE_META_TYPE, Sets.newHashSet(descriptiveMetadataType));

      storage.createBinary(binaryPath, binaryMetadata, binary.getContent(), asReference);
      descriptiveMetadataBinary = new DescriptiveMetadata(descriptiveMetadataId, aipId, descriptiveMetadataType,
        binaryPath);
      notifyDescriptiveMetadataCreated(descriptiveMetadataBinary);

    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating descriptive metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return descriptiveMetadataBinary;
  }

  // FIXME descriptiveMetadataType shouldn't be a parameter but instead be
  // already present in the Binary metadata (and therefore to be changed
  // appropriated method should be called)
  public DescriptiveMetadata updateDescriptiveMetadata(String aipId, String descriptiveMetadataId, Binary binary,
    String descriptiveMetadataType) throws ModelServiceException {
    DescriptiveMetadata descriptiveMetadataBinary = null;
    try {
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
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error updating descriptive metadata binary",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return descriptiveMetadataBinary;
  }

  public void deleteDescriptiveMetadata(String aipId, String descriptiveMetadataId) throws ModelServiceException {
    try {
      StoragePath binaryPath = ModelUtils.getDescriptiveMetadataPath(aipId, descriptiveMetadataId);

      storage.deleteResource(binaryPath);
      notifyDescriptiveMetadataDeleted(aipId, descriptiveMetadataId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error deleting descriptive metadata binary from storage, reason: " + e.getMessage(), e.getCode());
    }

  }

  public ClosableIterable<Representation> listRepresentations(String aipId) throws ModelServiceException {
    ClosableIterable<Representation> it = null;

    try {
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
              } catch (ModelServiceException | NoSuchElementException e) {
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

    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error while obtaining Representation list from storage, reason: " + e.getMessage(), e.getCode());
    }

    return it;
  }

  public Representation retrieveRepresentation(String aipId, String representationId) throws ModelServiceException {
    Representation representation;

    try {
      Directory directory = storage.getDirectory(ModelUtils.getRepresentationPath(aipId, representationId));
      representation = convertResourceToRepresentation(directory);

    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining Representation from storage, reason: " + e.getMessage(),
        e.getCode());
    }

    return representation;
  }

  // TODO support asReference
  public Representation createRepresentation(String aipId, String representationId, StorageService sourceStorage,
    StoragePath sourcePath) throws ModelServiceException {
    Representation representation;

    try {
      StoragePath directoryPath = ModelUtils.getRepresentationPath(aipId, representationId);

      // verify structure of source representation
      Directory sourceDirectory = sourceStorage.getDirectory(sourcePath);
      if (isRepresentationValid(sourceDirectory)) {
        storage.copy(sourceStorage, sourcePath, directoryPath);
        Directory directory = storage.getDirectory(directoryPath);

        representation = convertResourceToRepresentation(directory);
        notifyRepresentationCreated(representation);
      } else {
        throw new ModelServiceException("Error while creating representation, reason: representation is not valid",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while creating representation in storage, reason: " + e.getMessage(),
        e.getCode());
    }

    return representation;
  }

  public Representation updateRepresentation(String aipId, String representationId, StorageService sourceStorage,
    StoragePath sourcePath) throws ModelServiceException {

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
    throws ModelServiceException {
    Directory sourceDirectory;
    try {
      sourceDirectory = sourceStorage.getDirectory(sourcePath);
      if (!isRepresentationValid(sourceDirectory)) {
        throw new ModelServiceException("Error while updating AIP, reason: representation is not valid",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while updating representation in storage, reason: " + e.getMessage(),
        e.getCode());
    }
    return sourceDirectory;
  }

  private void updateRepresentationMetadata(String aipId, String representationId,
    Map<String, Set<String>> representationMetadata) throws ModelServiceException {
    try {
      storage.updateMetadata(ModelUtils.getRepresentationPath(aipId, representationId), representationMetadata, true);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while updating representation metadata",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  private void deleteUnneededFilesFromRepresentation(String aipId, String representationId,
    final List<String> fileIDsToUpdate) throws ModelServiceException {
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

    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while delete removed representation files",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
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
    StoragePath sourcePath) throws ModelServiceException {
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

          fileIDsToUpdate.add(fileUpdated.getStoragePath().getName());
        } else {
          // FIXME log error and continue???
        }
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while updating representation files",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
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

  public void deleteRepresentation(String aipId, String representationId) throws ModelServiceException {
    try {
      StoragePath representationPath = ModelUtils.getRepresentationPath(aipId, representationId);

      storage.deleteResource(representationPath);
      notifyRepresentationDeleted(aipId, representationId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error deleting representation from storage, reason: " + e.getMessage(),
        e.getCode());
    }
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public Iterable<File> listFiles(String aipId, String representationId) throws ModelServiceException {
    Iterable<File> it = null;

    try {
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
              } catch (ModelServiceException | NoSuchElementException e) {
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

    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error while obtaining representation files from storage, reason: " + e.getMessage(), e.getCode());
    }

    return it;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File retrieveFile(String aipId, String representationId, String fileId) throws ModelServiceException {
    File file;

    try {
      StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

      Binary binary = storage.getBinary(filePath);
      file = convertResourceToRepresentationFile(binary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error while obtaining representation file from storage, reason: " + e.getMessage(), e.getCode());
    }

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File createFile(String aipId, String representationId, String fileId, Binary binary)
    throws ModelServiceException {
    File file;
    // FIXME how to set this?
    boolean asReference = false;

    try {
      StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

      final Binary createdBinary = storage.createBinary(filePath, binary.getMetadata(), binary.getContent(),
        asReference);
      file = convertResourceToRepresentationFile(createdBinary);
      notifyFileCreated(file);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating representation file in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public File updateFile(String aipId, String representationId, String fileId, Binary binary, boolean createIfNotExists,
    boolean notify) throws ModelServiceException {
    File file = null;
    // FIXME how to set this?
    boolean asReference = false;

    try {
      StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

      storage.updateBinaryContent(filePath, binary.getContent(), asReference, createIfNotExists);
      storage.updateMetadata(filePath, binary.getMetadata(), true);
      Binary binaryUpdated = storage.getBinary(filePath);
      file = convertResourceToRepresentationFile(binaryUpdated);
      if (notify) {
        notifyFileUpdated(file);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while updating representation file",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return file;
  }

  // FIXME under a certain representation may exist files but also folders.
  // how to handle that in this method?
  public void deleteFile(String aipId, String representationId, String fileId) throws ModelServiceException {
    try {
      StoragePath filePath = ModelUtils.getRepresentationFilePath(aipId, representationId, fileId);

      storage.deleteResource(filePath);
      notifyFileDeleted(aipId, representationId, fileId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error deleting representation file from storage, reason: " + e.getMessage(),
        e.getCode());
    }
  }

  // FIXME turn this into ClosableIterable
  // TODO to improve...
  public Iterable<RepresentationPreservationObject> getAipPreservationObjects(String aipId)
    throws ModelServiceException {
    Iterable<RepresentationPreservationObject> it = null;
    final List<RepresentationPreservationObject> rpos = new ArrayList<RepresentationPreservationObject>();
    try {
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
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining AIP preservation objects, reason: " + e.getMessage(),
        e.getCode());
    }
    return it;
  }

  public RepresentationPreservationObject getRepresentationPreservationObject(String aipId, String representationId,
    String fileId) throws ModelServiceException {
    RepresentationPreservationObject obj = null;
    try {
      StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
      Binary b = storage.getBinary(sp);
      obj = convertResourceToRepresentationPreservationObject(aipId, representationId, fileId, b);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while getting representation preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return obj;
  }

  public EventPreservationObject getEventPreservationObject(String aipId, String representationId,
    String preservationObjectID) throws ModelServiceException {
    EventPreservationObject obj = null;
    try {
      StoragePath sp = ModelUtils.getPreservationFilePath(aipId, representationId, preservationObjectID);
      Binary b = storage.getBinary(sp);
      obj = convertResourceToEventPreservationObject(aipId, representationId, preservationObjectID, b);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while getting event preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return obj;
  }

  public AgentPreservationObject getAgentPreservationObject(String agentID) throws ModelServiceException {
    AgentPreservationObject apo = null;
    try {
      StoragePath sp = ModelUtils.getPreservationAgentPath(agentID);
      Binary b = storage.getBinary(sp);
      apo = convertResourceToAgentPreservationObject(agentID, b);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while getting agent preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return apo;
  }

  private AgentPreservationObject convertResourceToAgentPreservationObject(String agentID, Binary resource)
    throws ModelServiceException {
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
      } catch (PremisMetadataException e) {
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      throw new ModelServiceException(
        "Error while trying to convert a binary into a representation preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isAIPvalid(ModelService model, Directory directory, boolean failIfNoDescriptiveMetadataSchema,
    Path configBasePath) {
    boolean valid = true;

    try {
      // validate metadata (against schemas)
      valid = ValidationUtils.isAIPDescriptiveMetadataValid(model, directory.getStoragePath().getName(),
        failIfNoDescriptiveMetadataSchema, configBasePath);

      // FIXME validate others aspects

    } catch (ModelServiceException e) {
      LOGGER.error("Error validating AIP", e);
      valid = false;
    }

    return valid;
  }

  private boolean isRepresentationValid(Directory directory) {
    // FIXME implement this
    return true;
  }

  private AIP convertResourceToAIP(Resource resource) throws ModelServiceException {
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

      try {
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

        aip = new AIP(storagePath.getName(), parentId, active, dateCreated, dateModified, permissions,
          descriptiveMetadataBinaryIds, representationIds, preservationRepresentationObjects, preservationEvents,
          preservationFileObjects);
      } catch (StorageServiceException e) {
        throw new ModelServiceException("Error while obtaining information to instantiate an AIP", e.getCode(), e);
      }
    } else {
      throw new ModelServiceException(
        "Error while trying to convert something that it isn't a Directory into an AIP (" + resource + ")",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
    return aip;
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
      throws ModelServiceException {
    for (String representationID : representationIds) {
      try {
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
      } catch (StorageServiceException e) {
        LOGGER.error("Error while obtaining preservation related binaries", e);
        throw new ModelServiceException("Error while obtaining preservation related binaries", e.getCode(), e);
      }
    }
  }

  private DescriptiveMetadata convertResourceToDescriptiveMetadata(Resource resource) throws ModelServiceException {
    if (resource instanceof DefaultBinary) {
      // retrieve needed information to instantiate DescriptiveMetadata
      String type = ModelUtils.getString(resource.getMetadata(), RodaConstants.STORAGE_META_TYPE);

      return new DescriptiveMetadata(resource.getStoragePath().getName(),
        ModelUtils.getAIPidFromStoragePath(resource.getStoragePath()), type, resource.getStoragePath());
    } else {
      throw new ModelServiceException(
        "Error while trying to convert something that it isn't a Binary into a descriptive metadata binary",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  private PreservationMetadata convertResourceToPreservationMetadata(Resource resource) throws ModelServiceException {
    if (resource instanceof DefaultBinary) {
      String type = ModelUtils.getPreservationType((DefaultBinary) resource);
      return new PreservationMetadata(resource.getStoragePath().getName(),
        ModelUtils.getAIPidFromStoragePath(resource.getStoragePath()),
        ModelUtils.getRepresentationIdFromStoragePath(resource.getStoragePath()), resource.getStoragePath(), type);
    } else {
      throw new ModelServiceException(
        "Error while trying to convert something that it isn't a Binary into a preservation metadata binary",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  private Representation convertResourceToRepresentation(Resource resource) throws ModelServiceException {
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

      if (active == null) {
        // when not stated, considering active=false
        active = false;
      }

      return new Representation(directoryPath.getName(), ModelUtils.getAIPidFromStoragePath(directoryPath), active,
        dateCreated, dateModified, statuses, type, sizeInBytes, fileIds);

    } else {
      throw new ModelServiceException(
        "Error while trying to convert something that it isn't a Directory into a representation",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  private File convertResourceToRepresentationFile(Resource resource) throws ModelServiceException {
    if (resource instanceof DefaultBinary) {
      StoragePath binaryPath = resource.getStoragePath();
      Map<String, Set<String>> metadata = resource.getMetadata();

      // retrieve needed information to instantiate File
      Boolean entryPoint = ModelUtils.getBoolean(metadata, RodaConstants.STORAGE_META_ENTRYPOINT);
      FileFormat fileFormat = ModelUtils.getFileFormat(metadata);

      if (entryPoint == null) {
        // if entry point not defined, considering false
        entryPoint = false;
      }

      return new File(binaryPath.getName(), ModelUtils.getAIPidFromStoragePath(binaryPath),
        ModelUtils.getRepresentationIdFromStoragePath(binaryPath), entryPoint, fileFormat, binaryPath);
    } else {
      throw new ModelServiceException(
        "Error while trying to convert something that it isn't a Binary into a representation file",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  public RepresentationPreservationObject retrieveRepresentationPreservationObject(String aipId,
    String representationId, String fileId) throws ModelServiceException {
    RepresentationPreservationObject representationPreservationObject = null;
    try {
      StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
      Binary binary = storage.getBinary(filePath);
      representationPreservationObject = convertResourceToRepresentationPreservationObject(aipId, representationId,
        fileId, binary);
      representationPreservationObject.setId(fileId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining Representation from storage, reason: " + e.getMessage(),
        e.getCode());
    }
    return representationPreservationObject;
  }

  public RepresentationFilePreservationObject retrieveRepresentationFileObject(String aipId, String representationId,
    String fileId) throws ModelServiceException {
    RepresentationFilePreservationObject representationPreservationObject = null;
    try {
      StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
      Binary binary = storage.getBinary(filePath);
      representationPreservationObject = convertResourceToRepresentationFilePreservationObject(aipId, representationId,
        fileId, binary);
      representationPreservationObject.setId(fileId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error while obtaining Representation File from storage, reason: " + e.getMessage(), e.getCode());
    }
    return representationPreservationObject;
  }

  // FIXME verify/refactor this method
  private RepresentationFilePreservationObject convertResourceToRepresentationFilePreservationObject(String aipId,
    String representationId, String fileId, Binary resource) throws ModelServiceException {
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

        PremisFileObjectHelper pfoh = PremisFileObjectHelper.newInstance(resource.getContent().createInputStream());

        RepresentationFilePreservationObject rfpo = new RepresentationFilePreservationObject();
        rfpo.setAipId(aipId);
        rfpo.setRepresentationId(representationId);
        rfpo.setFileId(fileId);
        rfpo.setCompositionLevel(pfoh.getRepresentationFilePreservationObject().getCompositionLevel());
        rfpo.setContentLocationType(pfoh.getRepresentationFilePreservationObject().getContentLocationType());
        rfpo.setContentLocationValue(pfoh.getRepresentationFilePreservationObject().getContentLocationValue());
        rfpo.setCreatedDate(pfoh.getRepresentationFilePreservationObject().getCreatedDate());
        rfpo.setCreatingApplicationName(pfoh.getRepresentationFilePreservationObject().getCreatingApplicationName());
        rfpo.setCreatingApplicationVersion(
          pfoh.getRepresentationFilePreservationObject().getCreatingApplicationVersion());
        rfpo.setDateCreatedByApplication(pfoh.getRepresentationFilePreservationObject().getDateCreatedByApplication());
        rfpo.setFileId(fileId);
        rfpo.setFixities(pfoh.getRepresentationFilePreservationObject().getFixities());
        rfpo.setFormatDesignationName(pfoh.getRepresentationFilePreservationObject().getFormatDesignationName());
        rfpo.setFormatDesignationVersion(pfoh.getRepresentationFilePreservationObject().getFormatDesignationVersion());
        rfpo.setFormatRegistryKey(pfoh.getRepresentationFilePreservationObject().getFormatRegistryKey());
        rfpo.setFormatRegistryName(pfoh.getRepresentationFilePreservationObject().getFormatRegistryName());
        rfpo.setFormatRegistryRole(pfoh.getRepresentationFilePreservationObject().getFormatRegistryRole());
        rfpo.setHash(pfoh.getRepresentationFilePreservationObject().getHash());
        rfpo.setID(pfoh.getRepresentationFilePreservationObject().getID());
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
        rfpo.setRepresentationId(representationId);
        return rfpo;

      } catch (PremisMetadataException e) {
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      throw new ModelServiceException(
        "Error while trying to convert a binary into a representation preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  // FIXME verify/refactor this method
  private RepresentationPreservationObject convertResourceToRepresentationPreservationObject(String aipId,
    String representationId, String fileId, Binary resource) throws ModelServiceException {
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

        PremisRepresentationObjectHelper proh = PremisRepresentationObjectHelper
          .newInstance(resource.getContent().createInputStream());
        RepresentationPreservationObject rpo = new RepresentationPreservationObject();
        rpo.setAipId(aipId);
        rpo.setRepresentationId(representationId);
        rpo.setFileId(fileId);
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
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        throw new ModelServiceException(
          "Error while trying to convert a binary into a representation preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      throw new ModelServiceException(
        "Error while trying to convert a binary into a representation preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  public EventPreservationObject retrieveEventPreservationObject(String aipId, String representationId, String fileId)
    throws ModelServiceException {
    EventPreservationObject eventPreservationObject = null;
    try {
      StoragePath filePath = ModelUtils.getPreservationFilePath(aipId, representationId, fileId);
      Binary binary = storage.getBinary(filePath);
      eventPreservationObject = convertResourceToEventPreservationObject(aipId, representationId, fileId, binary);
      eventPreservationObject.setId(fileId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining Representation from storage, reason: " + e.getMessage(),
        e.getCode());
    }
    return eventPreservationObject;
  }

  // FIXME verify/refactor this method
  private EventPreservationObject convertResourceToEventPreservationObject(String aipId, String representationId,
    String fileId, Binary resource) throws ModelServiceException {
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
        epo.setAipId(aipId);
        epo.setRepresentationId(representationId);
        epo.setFileId(fileId);
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
        epo.setFileId(fileId);
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
            epo.setOutcomeDetailExtension(eodc.getEventOutcomeDetailExtension().toString());
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
        throw new ModelServiceException("Error while trying to convert a binary into a event preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      } catch (IOException e) {
        throw new ModelServiceException("Error while trying to convert a binary into a event preservation object",
          ModelServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      throw new ModelServiceException("Error while trying to convert a binary into a event preservation object",
        ModelServiceException.INTERNAL_SERVER_ERROR);
    }
  }

  // FIXME this should be synchronized (at least access to logFile)
  public void addLogEntry(LogEntry logEntry, Path logDirectory, boolean notify) throws ModelServiceException {
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
        throw new ModelServiceException("Error creating file to write log into",
          ModelServiceException.INTERNAL_SERVER_ERROR, e);
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
    throws ModelServiceException {
    ClosableIterable<PreservationMetadata> it;

    try {
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
              } catch (ModelServiceException | NoSuchElementException e) {
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

    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error while obtaining descriptive metadata binary list from storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return it;
  }

  // TODO verify
  public PreservationMetadata createPreservationMetadata(String aipId, String representationID,
    String preservationMetadataId, Binary binary) throws ModelServiceException {
    PreservationMetadata preservationMetadataBinary;
    try {
      StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationID, preservationMetadataId);
      boolean asReference = false;
      Binary updatedBinary = storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, true);
      preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, representationID, binaryPath,
        ModelUtils.getPreservationType(updatedBinary));
      notifyPreservationMetadataCreated(preservationMetadataBinary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating preservation metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return preservationMetadataBinary;
  }

  public AgentMetadata createAgentMetadata(String agentID, StorageService sourceStorage, StoragePath sourcePath,
    boolean notify) throws ModelServiceException {
    AgentMetadata agentMetadata;
    try {
      // TODO validate agent
      storage.copy(sourceStorage, sourcePath, ModelUtils.getPreservationAgentPath(agentID));
      agentMetadata = new AgentMetadata(agentID, ModelUtils.getPreservationAgentPath(agentID));
      if (notify) {
        notifyAgentMetadataCreated(agentMetadata);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating AgentMetadata in storage", e.getCode(), e);
    }

    return agentMetadata;
  }

  // TODO verify
  public AgentMetadata createAgentMetadata(String agentID, Binary binary) throws ModelServiceException {
    AgentMetadata agentMetadataBinary;
    try {
      StoragePath binaryPath = ModelUtils.getPreservationAgentPath(agentID);
      boolean asReference = false;
      Map<String, Set<String>> binaryMetadata = binary.getMetadata();
      storage.createBinary(binaryPath, binaryMetadata, binary.getContent(), asReference);

      agentMetadataBinary = new AgentMetadata(agentID, binaryPath);
      notifyAgentMetadataCreated(agentMetadataBinary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating agent metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return agentMetadataBinary;
  }

  // TODO verify
  public AgentMetadata updateAgentMetadata(String agentID, Binary binary) throws ModelServiceException {
    AgentMetadata agentMetadataBinary;
    try {
      storage.updateBinaryContent(binary.getStoragePath(), binary.getContent(), binary.isReference(), true);
      storage.updateMetadata(binary.getStoragePath(), binary.getMetadata(), true);
      agentMetadataBinary = new AgentMetadata(agentID, binary.getStoragePath());
      notifyAgentMetadataUpdated(agentMetadataBinary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating agent metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    return agentMetadataBinary;
  }

  public void deleteAgentMetadata(String agentID) throws ModelServiceException {
    try {
      storage.deleteResource(ModelUtils.getPreservationAgentPath(agentID));
      notifyAgentMetadataDeleted(agentID);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating agent metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  // TODO verify
  public PreservationMetadata updatePreservationMetadata(String aipId, String representationId,
    String preservationMetadataId, Binary binary) throws ModelServiceException {
    PreservationMetadata preservationMetadataBinary;
    try {
      String type = ModelUtils.getPreservationType(binary);
      StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationId, preservationMetadataId);
      boolean asReference = false;
      boolean createIfNotExists = false;
      storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);

      Map<String, Set<String>> binaryMetadata = binary.getMetadata();
      storage.updateMetadata(binaryPath, binaryMetadata, true);
      preservationMetadataBinary = new PreservationMetadata(preservationMetadataId, aipId, representationId, binaryPath,
        type);
      notifyPreservationMetadataUpdated(preservationMetadataBinary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error updating preservation metadata binary in the storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return preservationMetadataBinary;
  }

  // TODO verify
  public void deletePreservationMetadata(String aipId, String representationId, String preservationMetadataId)
    throws ModelServiceException {
    try {
      StoragePath binaryPath = ModelUtils.getPreservationFilePath(aipId, representationId, preservationMetadataId);
      storage.deleteResource(binaryPath);
      notifyPreservationMetadataDeleted(aipId, representationId, preservationMetadataId);
    } catch (StorageServiceException e) {
      throw new ModelServiceException(
        "Error deleting descriptive metadata binary from storage, reason: " + e.getMessage(), e.getCode());
    }

  }

  public void addLogEntry(LogEntry logEntry, Path logDirectory) throws ModelServiceException {
    addLogEntry(logEntry, logDirectory, true);
  }

  // FIXME this should be synchronized (at least access to logFile)
  public synchronized void findOldLogsAndMoveThemToStorage(Path logDirectory, Path currentLogFile) {
    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDirectory);

      for (Path path : directoryStream) {
        if (!path.equals(currentLogFile)) {
          try {
            StoragePath logPath = ModelUtils.getLogPath(path.getFileName().toString());
            storage.createBinary(logPath, new HashMap<String, Set<String>>(), new FSPathContentPayload(path), false);
            Files.delete(path);
          } catch (StorageServiceException e) {
            LOGGER.error("Error creating binary for old log file", e);
          } catch (IOException e) {
            LOGGER.error("Error deleting old log file", e);
          }
        }
      }
      directoryStream.close();

    } catch (IOException e) {
      LOGGER.error("Error listing directory for log files", e);
    }
  }

  // FIXME refactor this method
  public void addSipReport(SIPReport sipReport, boolean notify) throws ModelServiceException {

    StoragePath sipStatePath;
    try {
      sipStatePath = DefaultStoragePath.parse(RodaConstants.STORAGE_CONTAINER_SIP_REPORT, sipReport.getId());
      sipReport.setFileID(sipStatePath.getName());

      StringWriter sw = new StringWriter();
      JAXBContext jc = JAXBContext.newInstance(SIPReport.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.marshal(sipReport, sw);
      storage.updateBinaryContent(sipStatePath, new XMLContentPayload(sw.toString()), false, true);
      if (notify) {
        notifySipStateCreated(sipReport);
      }
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error adding SIP State to storage", e.getCode(), e);
    } catch (JAXBException e) {
      throw new ModelServiceException("Error adding SIP State to storage", ModelServiceException.INTERNAL_SERVER_ERROR,
        e);
    }

  }

  public void addSipReport(SIPReport sipReport) throws ModelServiceException {
    addSipReport(sipReport, true);
  }

  public void addUser(User user, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().addUser(user);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error adding user to LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (UserAlreadyExistsException e) {
      success = false;
      throw new ModelServiceException("User already exists", ModelServiceException.ALREADY_EXISTS, e);
    } catch (EmailAlreadyExistsException e) {
      success = false;
      throw new ModelServiceException("Email already exists", ModelServiceException.ALREADY_EXISTS, e);
    }
    if (success && notify) {
      notifyUserCreated(user);
    }
  }

  public void updateUser(User user, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().modifyUser(user);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error updating user to LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (EmailAlreadyExistsException e) {
      success = false;
      throw new ModelServiceException("User already exists", ModelServiceException.ALREADY_EXISTS, e);
    } catch (NoSuchUserException e) {
      success = false;
      throw new ModelServiceException("User doesn't exist", ModelServiceException.NOT_FOUND, e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new ModelServiceException("Illegal operation", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    if (success && notify) {
      notifyUserUpdated(user);
    }
  }

  public void deleteUser(String id, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().removeUser(id);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error deleting user from LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new ModelServiceException("Illegal operation", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    if (success && notify) {
      notifyUserDeleted(id);
    }
  }

  public void addGroup(Group group, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().addGroup(group);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error adding group to LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (GroupAlreadyExistsException e) {
      success = false;
      throw new ModelServiceException("Group already exists", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    if (success && notify) {
      notifyGroupCreated(group);
    }
  }

  public void updateGroup(Group group, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().modifyGroup(group);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error updating group to LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (NoSuchGroupException e) {
      success = false;
      throw new ModelServiceException("Group doesn't exist", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new ModelServiceException("Illegal operation", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    if (success && notify) {
      notifyGroupUpdated(group);
    }
  }

  public void deleteGroup(String id, boolean useModel, boolean notify) throws ModelServiceException {
    boolean success = true;
    try {
      if (useModel) {
        UserUtility.getLdapUtility().removeGroup(id);
      }
    } catch (LdapUtilityException e) {
      success = false;
      throw new ModelServiceException("Error updating group to LDAP", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    } catch (IllegalOperationException e) {
      success = false;
      throw new ModelServiceException("Illegal operation", ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }
    if (success && notify) {
      notifyGroupDeleted(id);
    }
  }

  public OtherMetadata createOtherMetadata(String aipID, String fileName, String type, Binary binary)
    throws ModelServiceException {
    OtherMetadata otherMetadataBinary = null;
    try {
      StoragePath binaryPath = ModelUtils.getOtherMetadataPath(aipID, fileName, type);
      boolean asReference = false;
      boolean createIfNotExists = true;
      Map<String, Set<String>> binaryMetadata = binary.getMetadata();
      storage.updateBinaryContent(binaryPath, binary.getContent(), asReference, createIfNotExists);
      storage.updateMetadata(binaryPath, binaryMetadata, true);
      otherMetadataBinary = new OtherMetadata(aipID+"_"+type+"_"+fileName,aipID,type,binaryPath);
      notifyOtherMetadataCreated(otherMetadataBinary);
    } catch (StorageServiceException e) {
      throw new ModelServiceException("Error creating other metadata binary in storage",
        ModelServiceException.INTERNAL_SERVER_ERROR, e);
    }

    return otherMetadataBinary;

  }
}
