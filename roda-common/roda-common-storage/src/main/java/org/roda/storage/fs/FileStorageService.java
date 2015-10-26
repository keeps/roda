/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.storage.fs;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.Container;
import org.roda.storage.ContentPayload;
import org.roda.storage.DefaultBinary;
import org.roda.storage.DefaultContainer;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.Directory;
import org.roda.storage.Entity;
import org.roda.storage.Resource;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StorageServiceUtils;

/**
 * Class that persists binary files and their containers in the File System.
 *
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class FileStorageService implements StorageService {

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
  public ClosableIterable<Container> listContainers() throws StorageServiceException {
    return FSUtils.listContainers(basePath);
  }

  @Override
  public Container createContainer(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;
    try {
      directory = Files.createDirectory(containerPath);
      FSYamlMetadataUtils.createPropertiesDirectory(directory);
      FSYamlMetadataUtils.writeMetadata(directory, metadata, true);
      return new DefaultContainer(storagePath, metadata);
    } catch (FileAlreadyExistsException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new StorageServiceException("Could not create container at " + containerPath,
        StorageServiceException.ALREADY_EXISTS, e);
    } catch (IOException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new StorageServiceException("Could not create container at " + containerPath,
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  @Override
  public Container getContainer(StoragePath storagePath) throws StorageServiceException {
    if (!storagePath.isFromAContainer()) {
      throw new StorageServiceException("Storage path is not from a container", StorageServiceException.BAD_REQUEST);
    }

    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    Container container;
    if (Files.exists(containerPath)) {
      Map<String, Set<String>> metadata = FSYamlMetadataUtils.readMetadata(containerPath);
      container = new DefaultContainer(storagePath, metadata);
    } else {
      throw new StorageServiceException("Container not found: " + storagePath, StorageServiceException.NOT_FOUND);
    }
    return container;
  }

  @Override
  public void deleteContainer(StoragePath storagePath) throws StorageServiceException {
    Path containerPath = FSUtils.getEntityPath(basePath, storagePath);
    FSUtils.deletePath(containerPath);
  }

  @Override
  public ClosableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath)
    throws StorageServiceException {
    Path path = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.listPath(basePath, path);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {
    Path dirPath = FSUtils.getEntityPath(basePath, storagePath);
    Path directory = null;
    try {
      directory = Files.createDirectory(dirPath);
      FSYamlMetadataUtils.createPropertiesDirectory(directory);
      FSYamlMetadataUtils.writeMetadata(directory, metadata, true);
      return new DefaultDirectory(storagePath, metadata);
    } catch (FileAlreadyExistsException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new StorageServiceException("Could not create directory at " + dirPath,
        StorageServiceException.ALREADY_EXISTS, e);
    } catch (IOException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new StorageServiceException("Could not create directory at " + dirPath,
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {
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

      throw new StorageServiceException("Could not create random directory under " + parentDirPath,
        StorageServiceException.ALREADY_EXISTS, e);
    } catch (IOException e) {
      // cleanup
      FSUtils.deletePath(directory);

      throw new StorageServiceException("Could not create random directory under " + parentDirPath,
        StorageServiceException.INTERNAL_SERVER_ERROR, e);
    }

  }

  @Override
  public Directory getDirectory(StoragePath storagePath) throws StorageServiceException {
    if (storagePath.isFromAContainer()) {
      throw new StorageServiceException("Invalid storage path for a directory: " + storagePath,
        StorageServiceException.BAD_REQUEST);
    }
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    Resource resource = FSUtils.convertPathToResource(basePath, directoryPath);
    if (resource instanceof Directory) {
      return (Directory) resource;
    } else {
      throw new StorageServiceException("Looking for a directory but found something else: " + storagePath,
        StorageServiceException.BAD_REQUEST);
    }

  }

  @Override
  public ClosableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath)
    throws StorageServiceException {
    Path directoryPath = FSUtils.getEntityPath(basePath, storagePath);
    return FSUtils.listPath(basePath, directoryPath);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws StorageServiceException {
    if (asReference) {
      // TODO create binary as a reference
      throw new StorageServiceException("Method not yet implemented", StorageServiceException.NOT_IMPLEMENTED);
    } else {
      Path binPath = FSUtils.getEntityPath(basePath, storagePath);
      if (Files.exists(binPath)) {
        throw new StorageServiceException("Binary already exists: " + binPath, StorageServiceException.ALREADY_EXISTS);
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
          throw new StorageServiceException("Could not create binary", StorageServiceException.INTERNAL_SERVER_ERROR,
            e);
        }
      }
    }
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, Map<String, Set<String>> metadata,
    ContentPayload payload, boolean asReference) throws StorageServiceException {
    if (asReference) {
      // TODO create binary as a reference
      throw new StorageServiceException("Method not yet implemented", StorageServiceException.NOT_IMPLEMENTED);
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
        throw new StorageServiceException("Could not create binary", StorageServiceException.INTERNAL_SERVER_ERROR, e);
      }
    }
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists) throws StorageServiceException {
    if (asReference) {
      // TODO update binary as a reference
      throw new StorageServiceException("Method not yet implemented", StorageServiceException.INTERNAL_SERVER_ERROR);
    } else {

      Path binaryPath = FSUtils.getEntityPath(basePath, storagePath);
      if (!Files.exists(binaryPath) && !createIfNotExists) {
        throw new StorageServiceException("Binary does not exist: " + binaryPath, StorageServiceException.NOT_FOUND);
      } else {
        try {
          payload.writeToPath(binaryPath);
        } catch (IOException e) {
          throw new StorageServiceException("Could not update binary content",
            StorageServiceException.INTERNAL_SERVER_ERROR, e);
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
        throw new StorageServiceException("Looking for a binary but found something else",
          StorageServiceException.INTERNAL_SERVER_ERROR);
      }
    }
  }

  @Override
  public Binary getBinary(StoragePath storagePath) throws StorageServiceException {
    Path binaryPath = FSUtils.getEntityPath(basePath, storagePath);
    Resource resource = FSUtils.convertPathToResource(basePath, binaryPath);
    if (resource instanceof Binary) {
      return (Binary) resource;
    } else {
      throw new StorageServiceException("Looking for a binary but found something else",
        StorageServiceException.BAD_REQUEST);
    }
  }

  @Override
  public void deleteResource(StoragePath storagePath) throws StorageServiceException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    FSUtils.deletePath(resourcePath);
  }

  @Override
  public Map<String, Set<String>> getMetadata(StoragePath storagePath) throws StorageServiceException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    return FSYamlMetadataUtils.readMetadata(resourcePath);
  }

  @Override
  public Map<String, Set<String>> updateMetadata(StoragePath storagePath, Map<String, Set<String>> metadata,
    boolean replaceAll) throws StorageServiceException {
    Path resourcePath = FSUtils.getEntityPath(basePath, storagePath);
    return FSYamlMetadataUtils.writeMetadata(resourcePath, metadata, replaceAll);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws StorageServiceException {
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
    throws StorageServiceException {
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
  public Class<? extends Entity> getEntity(StoragePath storagePath) throws StorageServiceException {
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
      throw new StorageServiceException(
        "There isn't a Container or Directory or Binary representated by " + storagePath.asString(),
        StorageServiceException.INTERNAL_SERVER_ERROR);
    }
  }

}
