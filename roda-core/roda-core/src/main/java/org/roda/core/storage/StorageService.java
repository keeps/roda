/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;

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
   * Test if file/folder exists.
   *
   */
  boolean exists(StoragePath storagePath);

  /**
   * List all existing containers.
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  CloseableIterable<Container> listContainers()
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException;

  /**
   * Creates a new container with the specified name.
   *
   * @param storagePath
   *          storage path with a unique name for the new container.
   *
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws AuthorizationDeniedException
   * @throws RequestNotValidException
   *
   */
  Container createContainer(StoragePath storagePath)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException, RequestNotValidException;

  /**
   * Get an existing container.
   *
   * @param name
   *          storage path that identifies the container.
   *
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   */
  Container getContainer(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException;

  /**
   * Delete an existing container.
   *
   * @param storagePath
   *          storage path that identifies the container.
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   */
  void deleteContainer(StoragePath storagePath)
    throws NotFoundException, GenericException, AuthorizationDeniedException;

  /**
   * List all resources under this container.
   *
   * @param storagePath
   *          storage path that identifies the container.
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  /**
   * Count all resources under this container.
   *
   * @param storagePath
   *          storage path that identifies the container.
   *
   * @throws AuthorizationDeniedException
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   */
  Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException;

  /**
   * Creates a new directory with the specified name.
   *
   * @param storagePath
   *          storage path that identifies the directory
   *
   * @throws AlreadyExistsException
   * @throws GenericException
   * @throws AuthorizationDeniedException
   *
   */
  Directory createDirectory(StoragePath storagePath)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException;

  /**
   * Creates a new directory with a random name.
   *
   * @param parentStoragePath
   *          storage path that identifies parent of the directory
   *
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AlreadyExistsException
   * @throws AuthorizationDeniedException
   *
   */
  Directory createRandomDirectory(StoragePath parentStoragePath) throws RequestNotValidException, GenericException,
    NotFoundException, AlreadyExistsException, AuthorizationDeniedException;

  /**
   * Get an existing directory.
   *
   * @param storagePath
   *          storage path that identifies the directory
   *
   * @throws RequestNotValidException
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   */
  Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  /**
   * Tests if directory exists.
   *
   * @param storagePath
   *          storage path that identifies the directory
   *
   */
  boolean hasDirectory(StoragePath storagePath);

  /**
   * List all resources, container or binaries, under this directory.
   *
   * @param storagePath
   *          storage path that identifies the directory
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  CloseableIterable<Resource> listResourcesUnderFile(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  /**
   * Count all resources, container or binaries, under this directory.
   *
   * @param storagePath
   *          storage path that identifies the directory
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   * @throws RequestNotValidException
   */
  Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException;

  /**
   * Create a binary resource with a defined content.
   *
   * @param storagePath
   *          storage path that identifies the binary
   * @param payload
   *          the content payload
   * @param asReference
   *          create the binary as a reference to the real content, which is
   *          managed externally. If false, content should be copied into the
   *          storage service.
   *
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   */
  Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference) throws GenericException,
    AlreadyExistsException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  /**
   * Create a binary resource with a defined content with a generated id.
   *
   * @param parentStoragePath
   *          storage path that identifies the parent of the binary
   * @param payload
   *          the content payload
   * @param asReference
   *          create the binary as a reference to the real content, which is
   *          managed externally. If false, content should be copied into the
   *          storage service.
   *
   * @throws GenericException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   */
  Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  /**
   * Get an existing binary resource.
   *
   * @param storagePath
   *          storage path that identifies the binary
   *
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   *
   */
  Binary getBinary(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException;

  /**
   * Tests if binary exists.
   *
   * @param storagePath
   *          storage path that identifies the binary
   *
   */
  boolean hasBinary(StoragePath storagePath);

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
   * @throws GenericException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, NotFoundException, RequestNotValidException, AuthorizationDeniedException;

  /**
   * Delete an existing resource, being it a container or a binary. If it is a
   * container, recursively delete all resources under it.
   *
   * @param storagePath
   *          storage path that identifies the resource
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   */
  void deleteResource(StoragePath storagePath) throws NotFoundException, GenericException, AuthorizationDeniedException;

  /**
   * Get entity class
   *
   * @param storagePath
   *          storage path that identifies the resource
   *
   * @throws GenericException
   * @throws NotFoundException
   * @throws AuthorizationDeniedException
   * @throws RequestNotValidException
   */
  Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException;

  /**
   * Copy resources from another (or the same) storage service.
   *
   * @param fromService
   * @param fromContainer
   * @param toStoragePath
   *
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException;

  void copy(StorageService fromService, StoragePath fromStoragePath, Path toPath, String resource, boolean replaceExisting)
    throws AlreadyExistsException, GenericException, AuthorizationDeniedException;

  /**
   * Move resources from another (or the same) storage service.
   *
   * @param fromService
   * @param fromStoragePath
   * @param toStoragePath
   *
   * @throws GenericException
   * @throws AlreadyExistsException
   * @throws NotFoundException
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   */
  void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws AlreadyExistsException, GenericException, RequestNotValidException, NotFoundException,
    AuthorizationDeniedException;

  DirectResourceAccess getDirectAccess(StoragePath storagePath);

  // TODO: gbarros, implement this
  default DirectResourceAccess getDirectAccess(StoragePath storagePath, Boolean isReadOnly) {
    return getDirectAccess(storagePath);
  }

  CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException;

  BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException;

  BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, NotFoundException, GenericException, AuthorizationDeniedException;

  void revertBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, RequestNotValidException, GenericException, AuthorizationDeniedException;

  void deleteBinaryVersion(StoragePath storagePath, String version)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException;

  String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer);

  String getStoragePathAsString(StoragePath storagePath, boolean skipContainer);

  List<StoragePath> getShallowFiles(StoragePath storagePath) throws NotFoundException, GenericException;

  Date getCreationDate(StoragePath storagePath) throws GenericException;

  void importBinaryVersion(StorageService fromService, StoragePath storagePath, String version)
          throws AlreadyExistsException, GenericException, RequestNotValidException, AuthorizationDeniedException;
}
