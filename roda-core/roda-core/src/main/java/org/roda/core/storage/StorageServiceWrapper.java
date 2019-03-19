/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;

public class StorageServiceWrapper implements StorageService {
  private StorageService storageService;
  private NodeType nodeType;

  public StorageServiceWrapper(StorageService storageService, NodeType nodeType) {
    super();
    this.storageService = storageService;
    this.nodeType = nodeType;
  }

  public StorageService getWrappedStorageService() {
    return storageService;
  }

  @Override
  public boolean exists(StoragePath storagePath) {
    return storageService.exists(storagePath);
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    return storageService.listContainers();
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createContainer(storagePath);
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return storageService.getContainer(storagePath);
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.deleteContainer(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return storageService.listResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return storageService.countResourcesUnderContainer(storagePath, recursive);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createDirectory(storagePath);
  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException,
    GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createRandomDirectory(parentStoragePath);
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    return storageService.getDirectory(storagePath);
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    return storageService.hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return storageService.listResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    return storageService.countResourcesUnderDirectory(storagePath, recursive);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createBinary(storagePath, payload, asReference);
  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createRandomBinary(parentStoragePath, payload, asReference);
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return storageService.getBinary(storagePath);
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    return storageService.hasBinary(storagePath);
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.updateBinaryContent(storagePath, payload, asReference, createIfNotExists);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.deleteResource(storagePath);
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    return storageService.getEntity(storagePath);
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.copy(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.move(fromService, fromStoragePath, toStoragePath);
  }

  @Override
  public DirectResourceAccess getDirectAccess(StoragePath storagePath) {
    return storageService.getDirectAccess(storagePath);
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    return storageService.listBinaryVersions(storagePath);
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    return storageService.getBinaryVersion(storagePath, version);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    return storageService.createBinaryVersion(storagePath, properties);
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.revertBinaryVersion(storagePath, version);
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    storageService.deleteBinaryVersion(storagePath, version);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    return storageService.getStoragePathAsString(storagePath, skipStoragePathContainer, anotherStoragePath,
      skipAnotherStoragePathContainer);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    return storageService.getStoragePathAsString(storagePath, skipContainer);
  }

}
