/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;
import java.util.Set;

import org.roda.core.data.exceptions.ActionForbiddenException;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;

/**
 * <p>
 * Interface that abstract the persistence of binary files and their containers.
 * The interface is based on Fedora 4 and OpenStack Swift APIs.
 * </p>
 * 
 * It organizes the content into:
 * <ul>
 * <li>container: a top-level unique namespace for binaries and directories.
 * </li>
 * 
 * <li>directory: a nestable structure (blank node) that allows organization of
 * content.</li>
 * 
 * <li>binary: a resource that stores the binary content, such as documents,
 * images and so on.</li>
 * </ul>
 * 
 * @author Luis Faria <lfaria@keep.pt>
 */
public interface StorageService {

  /**
   * List all existing containers.
   * 
   * @return
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   */
  public ClosableIterable<Container> listContainers()
    throws GenericException, ActionForbiddenException, RequestNotValidException, NotFoundException;

  /**
   * Creates a new container with the specified name.
   * 
   * @param storagePath
   *          storage path with a unique name for the new container.
   * @param metadata
   *          optional metadata for this container
   * 
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws ActionForbiddenException
   * @throws RequestNotValidException
   * 
   */
  public Container createContainer(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws GenericException, AlreadyExistsException, ActionForbiddenException, RequestNotValidException;

  /**
   * Get an existing container.
   * 
   * @param name
   *          storage path that identifies the container.
   * @return
   * @throws StorageServiceException
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   */
  public Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, ActionForbiddenException;

  /**
   * Delete an existing container.
   * 
   * @param storagePath
   *          storage path that identifies the container.
   * @return
   * @throws GenericException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   */
  public void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, ActionForbiddenException;

  /**
   * List all resources directly under this container.
   * 
   * @param storagePath
   *          storage path that identifies the container.
   * @return
   * 
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   */
  public ClosableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, ActionForbiddenException, RequestNotValidException;

  /**
   * Count all resources directly under this container.
   * 
   * @param storagePath
   *          storage path that identifies the container.
   * @return
   * 
   * @throws StorageServiceException
   * @throws ActionForbiddenException
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   */
  public Long countResourcesUnderContainer(StoragePath storagePath)
    throws ActionForbiddenException, RequestNotValidException, NotFoundException, GenericException;

  /**
   * Creates a new directory with the specified name.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * @param metadata
   *          optional metadata associated with this resource
   * 
   * @throws AlreadyExistsException
   * @throws GenericException
   * @throws ActionForbiddenException
   * 
   */
  public Directory createDirectory(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws AlreadyExistsException, GenericException, ActionForbiddenException;

  /**
   * Creates a new directory with a random name.
   * 
   * @param parentStoragePath
   *          storage path that identifies parent of the directory
   * @param metadata
   *          optional metadata associated with this resource
   * 
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AlreadyExistsException
   * @throws ActionForbiddenException
   * 
   */
  public Directory createRandomDirectory(StoragePath parentStoragePath, Map<String, Set<String>> metadata)
    throws RequestNotValidException, GenericException, NotFoundException, AlreadyExistsException,
    ActionForbiddenException;

  /**
   * Get an existing directory.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * 
   * @return
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   */
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, ActionForbiddenException;

  /**
   * List all resources, container or binaries, directly under this directory.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * @return
   * 
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   */
  public ClosableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath)
    throws NotFoundException, GenericException, ActionForbiddenException, RequestNotValidException;

  /**
   * Count all resources, container or binaries, directly under this directory.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * @return
   * 
   * @throws GenericException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   * @throws RequestNotValidException
   */
  public Long countResourcesUnderDirectory(StoragePath storagePath)
    throws NotFoundException, GenericException, ActionForbiddenException, RequestNotValidException;

  /**
   * Create a binary resource with a defined content.
   * 
   * @param storagePath
   *          storage path that identifies the binary
   * @param metadata
   *          optional associated metadata
   * @param payload
   *          the content payload
   * @param asReference
   *          create the binary as a reference to the real content, which is
   *          managed externally. If false, content should be copied into the
   *          storage service.
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   * @throws NotFoundException
   */
  public Binary createBinary(StoragePath storagePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws GenericException, AlreadyExistsException, RequestNotValidException,
      ActionForbiddenException, NotFoundException;

  /**
   * Create a binary resource with a defined content with a generated id.
   * 
   * @param parentStoragePath
   *          storage path that identifies the parent of the binary
   * @param metadata
   *          optional associated metadata
   * @param payload
   *          the content payload
   * @param asReference
   *          create the binary as a reference to the real content, which is
   *          managed externally. If false, content should be copied into the
   *          storage service.
   * @throws StorageServiceException
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   * @throws NotFoundException
   */
  public Binary createRandomBinary(StoragePath parentStoragePath, Map<String, Set<String>> metadata,
    ContentPayload payload, boolean asReference)
      throws GenericException, RequestNotValidException, ActionForbiddenException, NotFoundException;

  /**
   * Get an existing binary resource.
   * 
   * @param storagePath
   *          storage path that identifies the binary
   * @return
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   * @throws StorageServiceException
   */
  public Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, ActionForbiddenException;

  /**
   * Replace existing binary content with given one, not changing any associated
   * metadata.
   * 
   * @param storagePath
   *          storage path that identifies the binary
   * @param payload
   *          the new content payload that would replace existing one.
   * @param asReference
   *          update the binary as a reference to the real content, which is
   *          managed externally. If false, content should be copied into the
   *          storage service.
   * @param createIfNotExists
   *          If <code>true</code> and binary does not exists then it will be
   *          create. If <code>false</code> and binary does not exist then a
   *          {@link StorageServiceException} will be thrown with code
   *          <code>StorageActionException.NOT_FOUND</code>.
   * 
   * @return
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   */
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
      throws GenericException, NotFoundException, RequestNotValidException, ActionForbiddenException;

  /**
   * Delete an existing resource, being it a container or a binary. If it is a
   * container, recursively delete all resources under it.
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @throws GenericException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   */
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, GenericException, ActionForbiddenException;

  /**
   * Get metadata associated to a resource.
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @return
   * @throws GenericException
   * @throws ActionForbiddenException
   * @throws RequestNotValidException
   * @throws NotFoundException
   */
  public Map<String, Set<String>> getMetadata(StoragePath storagePath)
    throws GenericException, ActionForbiddenException, RequestNotValidException, NotFoundException;

  /**
   * Update metadata associated with a container.
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @param replaceAll
   *          true indicates that existing metadata will be discarded and
   *          replaced by the new one; false indicates that existing metadata
   *          will be updated to the new values (by replacing the old values
   *          with the new ones)
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   * @throws NotFoundException
   */
  public Map<String, Set<String>> updateMetadata(StoragePath storagePath, Map<String, Set<String>> metadata,
    boolean replaceAll) throws GenericException, RequestNotValidException, ActionForbiddenException, NotFoundException;

  /**
   * Get entity class
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @throws GenericException
   * @throws NotFoundException
   * @throws ActionForbiddenException
   * @throws RequestNotValidException
   */
  public Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, ActionForbiddenException, NotFoundException;

  /**
   * TODO
   * 
   * @param fromService
   * @param fromContainer
   * @param fromPath
   * @param fromName
   * @param toContainer
   * @param toPath
   * @param toName
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   * @throws StorageServiceException
   */
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    ActionForbiddenException;

  /**
   * TODO
   * 
   * @param fromService
   * @param fromContainer
   * @param fromPath
   * @param fromName
   * @param toContainer
   * @param toPath
   * @param toName
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws ActionForbiddenException
   */
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    ActionForbiddenException;

}
