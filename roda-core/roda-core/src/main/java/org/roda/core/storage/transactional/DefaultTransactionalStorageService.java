package org.roda.core.storage.transactional;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DefaultTransactionalStorageService implements TransactionalStorageService {

  private StorageService stagingStorageService;
  private StorageService mainStorageService;

  public DefaultTransactionalStorageService(StorageService mainStorageService, StorageService stagingStorageService)
    throws GenericException {
    this.stagingStorageService = stagingStorageService;
    this.mainStorageService = mainStorageService;
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    return stagingStorageService.exists(storagePath);
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return stagingStorageService.listContainers();
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException {
    return stagingStorageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return stagingStorageService.getContainer(storagePath);
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    stagingStorageService.deleteContainer(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return stagingStorageService.listResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return stagingStorageService.countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    return stagingStorageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    return stagingStorageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return stagingStorageService.getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    return stagingStorageService.hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return stagingStorageService.listResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return stagingStorageService.listResourcesUnderFile(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return stagingStorageService.countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    return stagingStorageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    return stagingStorageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return stagingStorageService.getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    return stagingStorageService.hasBinary(storagePath);
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    return stagingStorageService.updateBinaryContent(storagePath, payload, asReference, createIfNotExists);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    stagingStorageService.deleteResource(storagePath);
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    return stagingStorageService.getEntity(storagePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    stagingStorageService.copy(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, Path toPath, String resource)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    stagingStorageService.copy(fromService, fromStoragePath, toPath, resource);
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    stagingStorageService.move(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(StoragePath storagePath) {
    return stagingStorageService.getDirectAccess(storagePath);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return stagingStorageService.listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    return stagingStorageService.getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return stagingStorageService.createBinaryVersion(storagePath, properties);
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    stagingStorageService.revertBinaryVersion(storagePath, version);
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    stagingStorageService.deleteBinaryVersion(storagePath, version);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    return stagingStorageService.getStoragePathAsString(storagePath, skipStoragePathContainer, anotherStoragePath,
      skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    return stagingStorageService.getStoragePathAsString(storagePath, skipContainer);
  }

  @Override
  public List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException {
    return stagingStorageService.getShallowFiles(storagePath);
  }

  @Override
  public void commit() {
      try {
          stagingStorageService.listContainers().forEach(container -> {
            try {
              StorageServiceUtils.moveBetweenStorageServices(stagingStorageService, container.getStoragePath(),
                mainStorageService, container.getStoragePath(), Container.class);
            } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
              | AuthorizationDeniedException e) {
              e.printStackTrace();
            }
          });
      } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
          throw new RuntimeException(e);
      }
  }

  @Override
  public void rollback() {

  }

}
