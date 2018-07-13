/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultBinaryVersion;
import org.roda.core.storage.DefaultContainer;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.EmptyClosableIterable;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that persists binary files and their containers in the File System.
 * 
 * <p>
 * 20160718 hsilva: it has been decided that all Filesystem Storage Service
 * delete methods would not effectively delete files/folders but instead move
 * them to a 'trash' folder with the same folder structure
 * </p>
 *
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class FileStorageService implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

  public static final String HISTORY_SUFFIX = "-history";
  private static final String HISTORY_DATA_FOLDER = "data";
  private static final String HISTORY_METADATA_FOLDER = "metadata";

  private final Path rodaDataPath;
  private final Path basePath;
  private final Path historyPath;
  private final Path historyDataPath;
  private final Path historyMetadataPath;
  private final Path trashPath;

  public FileStorageService(Path basePath, boolean createTrash, String trashDirName, boolean createHistory)
    throws GenericException {
    this.basePath = basePath;
    rodaDataPath = this.basePath.getParent();
    historyPath = rodaDataPath.resolve(basePath.getFileName() + HISTORY_SUFFIX);
    historyDataPath = historyPath.resolve(HISTORY_DATA_FOLDER);
    historyMetadataPath = historyPath.resolve(HISTORY_METADATA_FOLDER);
    trashPath = rodaDataPath.resolve(trashDirName == null ? RodaConstants.TRASH_CONTAINER : trashDirName);

    initialize(basePath);
    if (createHistory) {
      initialize(historyPath);
      initialize(historyDataPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP));
      initialize(historyMetadataPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP));
    }
    if (createTrash) {
      initialize(trashPath);
    }

  }

  public FileStorageService(Path basePath, String trashDirName) throws GenericException {
    this(basePath, true, trashDirName, true);
  }

  public FileStorageService(Path basePath) throws GenericException {
    this(basePath, null);
  }

  private void initialize(Path path) throws GenericException {
    if (!FSUtils.exists(path)) {
      try {
        Files.createDirectories(path);
      } catch (IOException e) {
        throw new GenericException("Could not create path " + path, e);

      }
    } else if (!FSUtils.isDirectory(path)) {
      throw new GenericException("Path is not a directory " + path);
    } else if (!Files.isReadable(path)) {
      throw new GenericException("Cannot read from path " + path);
    } else if (!Files.isWritable(path)) {
      throw new GenericException("Cannot write to path " + path);
    } else {
      // do nothing
    }
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    return FSUtils.exists(FSUtils.getEntityPath(basePath, storagePath));
  }

  @Override
  public CloseableIterable<Container> listContainers() throws GenericException {
    return FSUtils.listContainers(basePath);
  }

  @Override
  public Container createContainer(StoragePath storagePath) throws GenericException, AlreadyExistsException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;
    try {
      directory = Files.createDirectory(containerPath);
      return new DefaultContainer(storagePath);
    } catch (FileAlreadyExistsException e) {
      // cleanup
      try {
        FSUtils.deletePath(directory);
      } catch (NotFoundException e1) {
        LOGGER.warn("Error while trying to clean up", e1);
      }

      throw new AlreadyExistsException("Could not create container at " + containerPath, e);
    } catch (IOException e) {
      // cleanup
      try {
        FSUtils.deletePath(directory);
      } catch (NotFoundException e1) {
        LOGGER.warn("Error while trying to clean up", e1);
      }

      throw new GenericException("Could not create container at " + containerPath, e);
    }
  }

  @Override
  public Container getContainer(StoragePath storagePath) throws RequestNotValidException, NotFoundException {
    if (!storagePath.isFromAContainer()) {
      throw new RequestNotValidException("Storage path is not from a container");
    }

    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Container container;
    if (FSUtils.exists(containerPath)) {
      container = new DefaultContainer(storagePath);
    } else {
      throw new NotFoundException("Container not found: " + storagePath);
    }
    return container;
  }

  @Override
  public void deleteContainer(StoragePath storagePath) throws NotFoundException, GenericException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    trash(containerPath);

    // cleanup history
    deleteAllBinaryVersionsUnder(storagePath);

  }

  private void trash(Path fromPath) throws GenericException, NotFoundException {
    if (trashPath == null) {
      LOGGER.warn("Skipping trash '{}' because no trash folder is defined!", fromPath);
      return;
    }
    try {
      Path toPath = trashPath.resolve(rodaDataPath.relativize(fromPath));
      LOGGER.debug("Moving to trash: {} to {}", fromPath, toPath);
      FSUtils.move(fromPath, toPath, true);
    } catch (AlreadyExistsException e) {
      String unique = IdUtils.createUUID();
      Path uniqueToPath = trashPath.resolve(unique).resolve(rodaDataPath.relativize(fromPath));
      try {
        LOGGER.debug("Re-trying to move to trash: {} to {}", fromPath, uniqueToPath);
        FSUtils.move(fromPath, uniqueToPath, true);
      } catch (AlreadyExistsException e1) {
        LOGGER.error("Error moving to trash: {} to {}", fromPath, uniqueToPath, e1);
        throw new GenericException("Unexpected exception while moving to trash", e1);
      }
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException {
    Path path = FSUtils.getEntityPath(basePath, storagePath);
    if (recursive) {
      return FSUtils.recursivelyListPath(basePath, path);
    } else {
      return FSUtils.listPath(basePath, path);
    }
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException {
    Path path = FSUtils.getEntityPath(basePath, storagePath);
    if (recursive) {
      return FSUtils.recursivelyCountPath(path);
    } else {
      return FSUtils.countPath(path);
    }
  }

  @Override
  public Directory createDirectory(StoragePath storagePath) throws AlreadyExistsException, GenericException {
    Path dirPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;

    if (FSUtils.exists(dirPath)) {
      throw new AlreadyExistsException("Could not create directory at " + dirPath);
    }

    try {
      directory = Files.createDirectories(dirPath);
      return new DefaultDirectory(storagePath);
    } catch (IOException e) {
      // cleanup
      try {
        FSUtils.deletePath(directory);
      } catch (NotFoundException | GenericException e1) {
        LOGGER.warn("Error while cleaning up", e1);
      }

      throw new GenericException("Could not create directory at " + dirPath, e);
    }
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath)
    throws RequestNotValidException, GenericException, NotFoundException, AlreadyExistsException {
    Path parentDirPath = FSUtils.getEntityPath(basePath, parentStoragePath);
    Path directory = null;

    try {
      directory = FSUtils.createRandomDirectory(parentDirPath);

      return new DefaultDirectory(FSUtils.getStoragePath(basePath, directory));
    } catch (FileAlreadyExistsException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new AlreadyExistsException("Could not create random directory under " + parentDirPath, e);
    } catch (IOException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new GenericException("Could not create random directory under " + parentDirPath, e);
    }

  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException {
    if (storagePath.isFromAContainer()) {
      throw new RequestNotValidException("Invalid storage path for a directory: " + storagePath);
    }

    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    Resource resource = FSUtils.convertPathToResource(basePath, directoryPath);

    if (resource instanceof Directory) {
      return (Directory) resource;
    } else {
      throw new RequestNotValidException("Looking for a directory but found something else: " + storagePath);
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException {
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    if (recursive) {
      return FSUtils.recursivelyListPath(basePath, directoryPath);
    } else {
      return FSUtils.listPath(basePath, directoryPath);
    }
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException {
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    if (recursive) {
      return FSUtils.recursivelyCountPath(directoryPath);
    } else {
      return FSUtils.countPath(directoryPath);
    }
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException {
    if (asReference) {
      throw new GenericException("Method not yet implemented");
    } else {
      Path binPath = FSUtils.getEntityPath(basePath, storagePath);
      if (FSUtils.exists(binPath)) {
        throw new AlreadyExistsException("Binary already exists: " + binPath);
      } else {
        try {
          // ensuring parent exists
          Path parent = binPath.getParent();
          if (!FSUtils.exists(parent)) {
            Files.createDirectories(parent);
          }

          // writing file
          payload.writeToPath(binPath);
          ContentPayload newPayload = new FSPathContentPayload(binPath);
          Long sizeInBytes = Files.size(binPath);
          boolean isReference = false;
          Map<String, String> contentDigest = null;

          return new DefaultBinary(storagePath, newPayload, sizeInBytes, isReference, contentDigest);
        } catch (FileAlreadyExistsException e) {
          throw new AlreadyExistsException("Binary already exists: " + binPath);
        } catch (IOException e) {
          throw new GenericException("Could not create binary", e);
        }
      }
    }
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException {
    if (asReference) {
      throw new GenericException("Method not yet implemented");
    } else {
      Path parent = FSUtils.getEntityPath(basePath, parentStoragePath);
      try {
        // ensure parent exists
        if (!FSUtils.exists(parent)) {
          Files.createDirectories(parent);
        }

        // create file
        Path binPath = FSUtils.createRandomFile(parent);

        // writing file
        payload.writeToPath(binPath);
        StoragePath storagePath = FSUtils.getStoragePath(basePath, binPath);
        ContentPayload newPayload = new FSPathContentPayload(binPath);
        Long sizeInBytes = Files.size(binPath);
        boolean isReference = false;
        Map<String, String> contentDigest = null;

        return new DefaultBinary(storagePath, newPayload, sizeInBytes, isReference, contentDigest);
      } catch (IOException e) {
        throw new GenericException("Could not create binary", e);
      }
    }
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists) throws GenericException, NotFoundException, RequestNotValidException {
    if (asReference) {
      throw new GenericException("Method not yet implemented");
    } else {

      Path binaryPath = FSUtils.getEntityPath(basePath, storagePath);
      boolean fileExists = FSUtils.exists(binaryPath);

      if (!fileExists && !createIfNotExists) {
        throw new NotFoundException("Binary does not exist: " + binaryPath);
      } else if (fileExists && !FSUtils.isFile(binaryPath)) {
        throw new GenericException("Looking for a binary but found something else");
      } else {
        try {
          payload.writeToPath(binaryPath);
        } catch (IOException e) {
          throw new GenericException("Could not update binary content", e);
        }
      }

      Resource resource = FSUtils.convertPathToResource(basePath, binaryPath);
      if (resource instanceof Binary) {
        return (DefaultBinary) resource;
      } else {
        throw new GenericException("Looking for a binary but found something else");
      }
    }
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException {
    Path binaryPath = FSUtils.getEntityPath(basePath, storagePath);
    Resource resource = FSUtils.convertPathToResource(basePath, binaryPath);
    if (resource instanceof Binary) {
      return (Binary) resource;
    } else {
      throw new RequestNotValidException("Looking for a binary but found something else");
    }
  }

  @Override
  public void deleteResource(StoragePath storagePath) throws NotFoundException, GenericException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    trash(resourcePath);

    // cleanup history
    deleteAllBinaryVersionsUnder(storagePath);
  }

  public Path resolve(StoragePath storagePath) {
    return FSUtils.getEntityPath(basePath, storagePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    if (fromService instanceof FileStorageService) {
      Path sourcePath = ((FileStorageService) fromService).resolve(fromStoragePath);
      Path targetPath = FSUtils.getEntityPath(basePath, toStoragePath);
      FSUtils.copy(sourcePath, targetPath, false);
    } else {
      Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);
      StorageServiceUtils.copyBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    if (fromService instanceof FileStorageService) {
      Path sourcePath = ((FileStorageService) fromService).resolve(fromStoragePath);
      Path targetPath = FSUtils.getEntityPath(basePath, toStoragePath);
      FSUtils.move(sourcePath, targetPath, false);
    } else {
      Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);
      StorageServiceUtils.moveBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath) throws NotFoundException {
    Path entity = FSUtils.getEntityPath(basePath, storagePath);
    if (FSUtils.exists(entity)) {
      if (FSUtils.isDirectory(entity)) {
        if (storagePath.isFromAContainer()) {
          return DefaultContainer.class;
        } else {
          return DefaultDirectory.class;
        }
      } else {
        return DefaultBinary.class;
      }
    } else {
      throw new NotFoundException("Entity was not found: " + storagePath);
    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(final StoragePath storagePath) {
    return new DirectResourceAccess() {

      @Override
      public Path getPath() {
        // TODO disable write access to resource
        // for UNIX programs using user with read-only permissions
        // for Java programs using SecurityManager and Policy
        return FSUtils.getEntityPath(basePath, storagePath);
      }

      @Override
      public void close() {
        // nothing to do
      }
    };
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, NotFoundException {
    if (historyDataPath == null) {
      LOGGER.warn("Skipping list binary versions because no history folder is defined, so returning empty list!");
      return new EmptyClosableIterable<>();
    }

    Path fauxPath = FSUtils.getEntityPath(historyDataPath, storagePath);
    Path parent = fauxPath.getParent();
    final String baseName = fauxPath.getFileName().toString();

    CloseableIterable<BinaryVersion> iterable;

    if (!FSUtils.exists(parent)) {
      return new EmptyClosableIterable<>();
    }

    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent,
        new DirectoryStream.Filter<Path>() {

          @Override
          public boolean accept(Path entry) {
            return entry.getFileName().toString().startsWith(baseName);
          }
        });

      final Iterator<Path> pathIterator = directoryStream.iterator();
      iterable = new CloseableIterable<BinaryVersion>() {

        @Override
        public Iterator<BinaryVersion> iterator() {
          return new Iterator<BinaryVersion>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public BinaryVersion next() {
              Path next = pathIterator.next();
              BinaryVersion ret;
              try {
                ret = FSUtils.convertPathToBinaryVersion(historyDataPath, historyMetadataPath, next);
              } catch (GenericException | NotFoundException | RequestNotValidException e) {
                LOGGER.error("Error while list path " + basePath + " while parsing resource " + next, e);
                ret = null;
              }

              return ret;
            }

          };
        }

        @Override
        public void close() throws IOException {
          directoryStream.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not find versions of " + storagePath, e);
    } catch (IOException e) {
      throw new GenericException("Error finding version of " + storagePath, e);
    }

    return iterable;
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    if (historyDataPath == null) {
      throw new GenericException("Skipping get binary version because no history folder is defined!");
    }
    Path binVersionPath = FSUtils.getEntityPath(historyDataPath, storagePath, version);
    return FSUtils.convertPathToBinaryVersion(historyDataPath, historyMetadataPath, binVersionPath);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException {
    if (historyDataPath == null) {
      throw new GenericException("Skipping create binary version because no history folder is defined!");
    }

    Path binPath = FSUtils.getEntityPath(basePath, storagePath);

    String id = IdUtils.createUUID();
    Path dataPath = FSUtils.getEntityPath(historyDataPath, storagePath, id);
    Path metadataPath = FSUtils.getBinaryHistoryMetadataPath(historyDataPath, historyMetadataPath, dataPath);

    if (!FSUtils.exists(binPath)) {
      throw new NotFoundException("Binary does not exist: " + binPath);
    }

    if (!FSUtils.isFile(binPath)) {
      throw new RequestNotValidException("Not a regular file: " + binPath);
    }

    if (FSUtils.exists(dataPath)) {
      throw new GenericException("Binary version id collided: " + dataPath);
    }

    try {
      // ensuring parent exists
      Path parent = dataPath.getParent();
      if (!FSUtils.exists(parent)) {
        Files.createDirectories(parent);
      }

      // writing file
      Files.copy(binPath, dataPath);

      // Creating metadata
      DefaultBinaryVersion b = new DefaultBinaryVersion();
      b.setId(id);
      b.setProperties(properties);
      b.setCreatedDate(new Date());
      Files.createDirectories(metadataPath.getParent());
      JsonUtils.writeObjectToFile(b, metadataPath);

      return FSUtils.convertPathToBinaryVersion(historyDataPath, historyMetadataPath, dataPath);
    } catch (IOException e) {
      throw new GenericException("Could not create binary", e);
    }

  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException {
    if (historyDataPath == null) {
      LOGGER.warn("Skipping revert binary version because no history folder is defined!");
      return;
    }

    Path binPath = FSUtils.getEntityPath(basePath, storagePath);
    Path binVersionPath = FSUtils.getEntityPath(historyDataPath, storagePath, version);

    if (!FSUtils.exists(binPath)) {
      throw new NotFoundException("Binary does not exist: " + binPath);
    }

    if (!FSUtils.isFile(binPath)) {
      throw new RequestNotValidException("Not a regular file: " + binPath);
    }

    if (!FSUtils.exists(binVersionPath)) {
      throw new NotFoundException("Binary version does not exist: " + binVersionPath);
    }

    try {
      // writing file
      Files.copy(binVersionPath, binPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new GenericException("Could not create binary", e);
    }

  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException {
    if (historyDataPath == null) {
      LOGGER.warn("Skipping delete binary version because no history folder is defined!");
      return;
    }

    Path dataPath = FSUtils.getEntityPath(historyDataPath, storagePath, version);
    Path metadataPath = FSUtils.getBinaryHistoryMetadataPath(historyDataPath, historyMetadataPath, dataPath);

    trash(dataPath);
    trash(metadataPath);

    // cleanup created parents
    FSUtils.deleteEmptyAncestorsQuietly(dataPath, historyDataPath);
    FSUtils.deleteEmptyAncestorsQuietly(metadataPath, historyMetadataPath);
  }

  private void deleteAllBinaryVersionsUnder(StoragePath storagePath) {
    if (historyDataPath == null) {
      LOGGER.warn("Skipping delete all binary versions because no history folder is defined!");
      return;
    }

    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);

    Path relativePath = basePath.relativize(resourcePath);
    Path resourceHistoryDataPath = historyDataPath.resolve(relativePath);

    if (FSUtils.isDirectory(resourceHistoryDataPath)) {
      try {
        Path resourceHistoryMetadataPath = historyMetadataPath
          .resolve(historyDataPath.relativize(resourceHistoryDataPath));

        trash(resourceHistoryDataPath);
        trash(resourceHistoryMetadataPath);

        FSUtils.deleteEmptyAncestorsQuietly(resourceHistoryDataPath, historyDataPath);
        FSUtils.deleteEmptyAncestorsQuietly(resourceHistoryMetadataPath, historyMetadataPath);
      } catch (GenericException | NotFoundException e) {
        LOGGER.warn("Could not delete history under " + resourceHistoryDataPath, e);
      }
    } else {
      Path parent = resourceHistoryDataPath.getParent();
      final String baseName = resourceHistoryDataPath.getFileName().toString();

      if (FSUtils.exists(parent)) {
        DirectoryStream<Path> directoryStream = null;
        try {
          directoryStream = Files.newDirectoryStream(parent, new DirectoryStream.Filter<Path>() {

            @Override
            public boolean accept(Path entry) {
              return entry.getFileName().toString().startsWith(baseName);
            }
          });

          for (Path p : directoryStream) {
            trash(p);

            Path pMetadata = FSUtils.getBinaryHistoryMetadataPath(historyDataPath, historyMetadataPath, p);
            trash(pMetadata);

            FSUtils.deleteEmptyAncestorsQuietly(p, historyDataPath);
            FSUtils.deleteEmptyAncestorsQuietly(pMetadata, historyMetadataPath);
          }
        } catch (IOException | GenericException | NotFoundException e) {
          LOGGER.warn("Could not delete history under " + resourceHistoryDataPath, e);
        } finally {
          IOUtils.closeQuietly(directoryStream);
        }
      }
    }
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    try {
      this.getDirectory(storagePath);
      return true;
    } catch (NotFoundException | RequestNotValidException | GenericException e) {
      return false;
    }
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    try {
      this.getBinary(storagePath);
      return true;
    } catch (NotFoundException | RequestNotValidException | GenericException e) {
      return false;
    }
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    return FSUtils.getStoragePathAsString(storagePath, skipStoragePathContainer, anotherStoragePath,
      skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    return FSUtils.getStoragePathAsString(storagePath, skipContainer);
  }
}
