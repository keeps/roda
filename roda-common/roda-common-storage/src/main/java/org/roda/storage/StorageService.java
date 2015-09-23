package org.roda.storage;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Interface that abstract the persistence of binary files and their containers.
 * The interface is based on Fedora 4 and OpenStack Swift APIs.
 * </p>
 * 
 * It organizes the content into:
 * <ul>
 * <li>container: a top-level unique namespace for binaries and directories.</li>
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
   */
  public ClosableIterable<Container> listContainers() throws StorageServiceException;

  /**
   * Creates a new container with the specified name.
   * 
   * @param storagePath
   *          storage path with a unique name for the new container.
   * @param metadata
   *          optional metadata for this container
   * 
   * @throws StorageServiceException
   * 
   */
  public Container createContainer(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException;

  /**
   * Get an existing container.
   * 
   * @param name
   *          storage path that identifies the container.
   * @return
   * @throws StorageServiceException
   */
  public Container getContainer(StoragePath storagePath) throws StorageServiceException;

  /**
   * Delete an existing container.
   * 
   * @param storagePath
   *          storage path that identifies the container.
   * @return
   * @throws StorageServiceException
   */
  public void deleteContainer(StoragePath storagePath) throws StorageServiceException;

  /**
   * List all resources directly under this container.
   * 
   * @param storagePath
   *          storage path that identifies the container.
   * @return
   * 
   * @throws StorageServiceException
   */
  public ClosableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath) throws StorageServiceException;

  /**
   * Creates a new directory with the specified name.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * @param metadata
   *          optional metadata associated with this resource
   * 
   * @throws StorageServiceException
   * 
   */
  public Directory createDirectory(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException;
  
  
  /**
   * Creates a new directory with a random name.
   * 
   * @param parentStoragePath
   *          storage path that identifies parent of the directory
   * @param metadata
   *          optional metadata associated with this resource
   * 
   * @throws StorageServiceException
   * 
   */
  public Directory createRandomDirectory(StoragePath parentStoragePath, Map<String, Set<String>> metadata)
    throws StorageServiceException;

  /**
   * Get an existing directory.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * 
   * @return
   * @throws StorageServiceException
   */
  public Directory getDirectory(StoragePath storagePath) throws StorageServiceException;

  /**
   * List all resources, container or binaries, directly under this directory.
   * 
   * @param storagePath
   *          storage path that identifies the directory
   * @return
   * 
   * @throws StorageServiceException
   */
  public ClosableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath) throws StorageServiceException;

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
   * @throws StorageServiceException
   */
  public Binary createBinary(StoragePath storagePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws StorageServiceException;
  
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
   */
  public Binary createRandomBinary(StoragePath parentStoragePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws StorageServiceException;

  /**
   * Get an existing binary resource.
   * 
   * @param storagePath
   *          storage path that identifies the binary
   * @return
   * @throws StorageServiceException
   */
  public Binary getBinary(StoragePath storagePath) throws StorageServiceException;

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
   * @throws StorageServiceException
   */
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists) throws StorageServiceException;

  /**
   * Delete an existing resource, being it a container or a binary. If it is a
   * container, recursively delete all resources under it.
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @throws StorageServiceException
   */
  public void deleteResource(StoragePath storagePath) throws StorageServiceException;

  /**
   * Get metadata associated to a resource.
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * @return
   * @throws StorageActionExceptionResource
   */
  public Map<String, Set<String>> getMetadata(StoragePath storagePath) throws StorageServiceException;

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
   * @throws StorageServiceException
   */
  public Map<String, Set<String>> updateMetadata(StoragePath storagePath, Map<String, Set<String>> metadata,
    boolean replaceAll) throws StorageServiceException;

  /**
   * Get entity class
   * 
   * @param storagePath
   *          storage path that identifies the resource
   * */
  public Class<? extends Entity> getEntity(StoragePath storagePath) throws StorageServiceException;

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
   * @throws StorageServiceException
   */
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws StorageServiceException;

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
   * @throws StorageServiceException
   */
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws StorageServiceException;

}
