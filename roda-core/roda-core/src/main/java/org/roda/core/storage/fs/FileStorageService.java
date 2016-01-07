/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.ActionForbiddenException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultContainer;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.StorageServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that persists binary files and their containers in the File System.
 *
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class FileStorageService implements StorageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

  private final Path basePath;

  public FileStorageService(Path basePath) throws StorageServiceException {
    this.basePath = basePath;
    if (!Files.exists(basePath)) {
      try {
        Files.createDirectories(basePath);
      } catch (IOException e) {
        throw new StorageServiceException("Could not created base path " + basePath,
          StorageServiceException.INTERNAL_SERVER_ERROR);

      }
    } else if (!Files.isDirectory(basePath)) {
      throw new StorageServiceException("Base path is not a directory " + basePath,
        StorageServiceException.INTERNAL_SERVER_ERROR);
    } else if (!Files.isReadable(basePath)) {
      throw new StorageServiceException("Cannot read from base path " + basePath,
        StorageServiceException.INTERNAL_SERVER_ERROR);
    } else if (!Files.isWritable(basePath)) {
      throw new StorageServiceException("Cannot write from base path " + basePath,
        StorageServiceException.INTERNAL_SERVER_ERROR);
    } else {
      // do nothing
    }
  }

  @Override
  public ClosableIterable<Container> listContainers() throws GenericException {
    return FSUtils.listContainers(basePath);
  }

  @Override
  public Container createContainer(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws GenericException, AlreadyExistsException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;
    try {
      directory = Files.createDirectory(containerPath);
      FSYamlMetadataUtils.createPropertiesDirectory(directory);
      FSYamlMetadataUtils.writeMetadata(directory, metadata, true);
      return new DefaultContainer(storagePath, metadata);
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
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException {
    if (!storagePath.isFromAContainer()) {
      throw new RequestNotValidException("Storage path is not from a container");
    }

    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Container container;
    if (Files.exists(containerPath)) {
      Map<String, Set<String>> metadata = FSYamlMetadataUtils.readMetadata(containerPath);
      container = new DefaultContainer(storagePath, metadata);
    } else {
      throw new NotFoundException("Container not found: " + storagePath);
    }
    return container;
  }

  @Override
  public void deleteContainer(StoragePath storagePath) throws NotFoundException, GenericException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    FSUtils.deletePath(containerPath);
  }

  @Override
  public ClosableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath)
    throws NotFoundException, GenericException {
    Path path = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.listPath(basePath, path);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath) throws NotFoundException, GenericException {
    Path path = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.countPath(basePath, path);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws AlreadyExistsException, GenericException {
    Path dirPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;
    try {
      directory = Files.createDirectory(dirPath);
      FSYamlMetadataUtils.createPropertiesDirectory(directory);
      FSYamlMetadataUtils.writeMetadata(directory, metadata, true);
      return new DefaultDirectory(storagePath, metadata);
    } catch (FileAlreadyExistsException e) {
      // cleanup
      try {
        FSUtils.deletePath(directory);
      } catch (NotFoundException | GenericException e1) {
        LOGGER.warn("Error while cleaning up", e1);
      }

      throw new AlreadyExistsException("Could not create directory at " + dirPath, e);
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
  public Directory createRandomDirectory(StoragePath parentStoragePath, Map<String, Set<String>> metadata)
    throws RequestNotValidException, GenericException, NotFoundException, AlreadyExistsException {
    Path parentDirPath = FSUtils.getEntityPath(basePath, parentStoragePath);
    Path directory = null;

    try {
      directory = FSUtils.createRandomDirectory(parentDirPath);
      FSYamlMetadataUtils.createPropertiesDirectory(directory);
      FSYamlMetadataUtils.writeMetadata(directory, metadata, true);
      return new DefaultDirectory(DefaultStoragePath.parse(directory.toString()), metadata);
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
  public ClosableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath)
    throws NotFoundException, GenericException {
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.listPath(basePath, directoryPath);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath) throws NotFoundException, GenericException {
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.countPath(basePath, directoryPath);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws GenericException, AlreadyExistsException {
    if (asReference) {
      // TODO create binary as a reference
      throw new GenericException("Method not yet implemented");
    } else {
      Path binPath = FSUtils.getEntityPath(basePath, storagePath);
      if (Files.exists(binPath)) {
        throw new AlreadyExistsException("Binary already exists: " + binPath);
      } else {

        try {
          // ensuring parent exists
          Path parent = binPath.getParent();
          if (!Files.exists(parent)) {
            Files.createDirectories(parent);
          }

          // writing file
          payload.writeToPath(binPath);
          Map<String, String> contentDigest = FSUtils.generateContentDigest(binPath);
          FSYamlMetadataUtils.addContentDigestToMetadata(metadata, contentDigest);
          FSYamlMetadataUtils.writeMetadata(binPath, metadata, true);
          return new DefaultBinary(storagePath, metadata, new FSPathContentPayload(binPath), Files.size(binPath), false,
            contentDigest);
        } catch (IOException e) {
          throw new GenericException("Could not create binary", e);
        }
      }
    }
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, Map<String, Set<String>> metadata,
    ContentPayload payload, boolean asReference) throws GenericException, RequestNotValidException {
    if (asReference) {
      // TODO create binary as a reference
      throw new GenericException("Method not yet implemented");
    } else {
      Path parent = FSUtils.getEntityPath(basePath, parentStoragePath);
      try {
        // ensure parent exists
        if (!Files.exists(parent)) {
          Files.createDirectories(parent);
        }

        // create file
        Path binPath = FSUtils.createRandomFile(parent);

        // writing file
        payload.writeToPath(binPath);
        Map<String, String> contentDigest = FSUtils.generateContentDigest(binPath);
        FSYamlMetadataUtils.addContentDigestToMetadata(metadata, contentDigest);
        FSYamlMetadataUtils.writeMetadata(binPath, metadata, true);
        StoragePath storagePath = DefaultStoragePath.parse(binPath.toString());
        return new DefaultBinary(storagePath, metadata, new FSPathContentPayload(binPath), Files.size(binPath), false,
          contentDigest);
      } catch (IOException e) {
        throw new GenericException("Could not create binary", e);
      }
    }
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists) throws GenericException, NotFoundException, RequestNotValidException {
    if (asReference) {
      // TODO update binary as a reference
      throw new GenericException("Method not yet implemented");
    } else {

      Path binaryPath = FSUtils.getEntityPath(basePath, storagePath);
      if (!Files.exists(binaryPath) && !createIfNotExists) {
        throw new NotFoundException("Binary does not exist: " + binaryPath);
      } else {
        try {
          payload.writeToPath(binaryPath);
        } catch (IOException e) {
          throw new GenericException("Could not update binary content", e);
        }
      }
      Resource resource = FSUtils.convertPathToResource(basePath, binaryPath);
      if (resource instanceof Binary) {
        DefaultBinary binary = (DefaultBinary) resource;
        // calculate content digest
        Map<String, String> contentDigest = FSUtils.generateContentDigest(binaryPath);
        // merge with metadata & add to resource
        FSYamlMetadataUtils.addContentDigestToMetadata(binary.getMetadata(), contentDigest);
        binary.setContentDigest(contentDigest);
        // update metadata
        FSYamlMetadataUtils.writeMetadata(binaryPath, binary.getMetadata(), true);

        return binary;
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
    FSUtils.deletePath(resourcePath);
  }

  @Override
  public Map<String, Set<String>> getMetadata(StoragePath storagePath) throws GenericException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    return FSYamlMetadataUtils.readMetadata(resourcePath);
  }

  @Override
  public Map<String, Set<String>> updateMetadata(StoragePath storagePath, Map<String, Set<String>> metadata,
    boolean replaceAll) throws GenericException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    return FSYamlMetadataUtils.writeMetadata(resourcePath, metadata, replaceAll);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    ActionForbiddenException {
    if (fromService instanceof FileStorageService) {
      Path sourcePath = ((FileStorageService) fromService).basePath.resolve(fromStoragePath.asString());
      Path targetPath = basePath.resolve(toStoragePath.asString());
      FSUtils.copy(sourcePath, targetPath, false);

    } else {
      Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);
      StorageServiceUtils.copyBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    ActionForbiddenException {
    if (fromService instanceof FileStorageService) {
      Path sourcePath = ((FileStorageService) fromService).basePath.resolve(fromStoragePath.asString());
      Path targetPath = basePath.resolve(toStoragePath.asString());
      FSUtils.move(sourcePath, targetPath, false);
    } else {
      Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);
      StorageServiceUtils.moveBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath) throws GenericException {
    Path entity = FSUtils.getEntityPath(basePath, storagePath);
    if (Files.exists(entity)) {
      if (Files.isDirectory(entity)) {
        if (storagePath.isFromAContainer()) {
          return DefaultContainer.class;
        } else {
          return DefaultDirectory.class;
        }
      } else {
        return DefaultBinary.class;
      }
    } else {
      throw new GenericException(
        "There isn't a Container or Directory or Binary representated by " + storagePath.asString());
    }
  }

}
