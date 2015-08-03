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
	public Iterable<Container> listContainers() throws StorageActionException;

	/**
	 * Creates a new container with the specified name.
	 * 
	 * @param storagePath
	 *            storage path with a unique name for the new container.
	 * @param metadata
	 *            optional metadata for this container
	 * 
	 * @throws StorageActionException
	 * 
	 */
	public Container createContainer(StoragePath storagePath,
			Map<String, Set<String>> metadata) throws StorageActionException;

	/**
	 * Get an existing container.
	 * 
	 * @param name
	 *            storage path that identifies the container.
	 * @return
	 * @throws StorageActionException
	 */
	public Container getContainer(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * Delete an existing container.
	 * 
	 * @param storagePath
	 *            storage path that identifies the container.
	 * @return
	 * @throws StorageActionException
	 */
	public void deleteContainer(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * List all resources directly under this container.
	 * 
	 * @param storagePath
	 *            storage path that identifies the container.
	 * @return
	 * 
	 * @throws StorageActionException
	 */
	public ClosableIterable<Resource> listResourcesUnderContainer(
			StoragePath storagePath) throws StorageActionException;

	/**
	 * Creates a new directory with the specified name.
	 * 
	 * @param storagePath
	 *            storage path that identifies the directory
	 * @param metadata
	 *            optional metadata associated with this resource
	 * 
	 * @throws StorageActionException
	 * 
	 */
	public Directory createDirectory(StoragePath storagePath,
			Map<String, Set<String>> metadata) throws StorageActionException;

	/**
	 * Get an existing directory.
	 * 
	 * @param storagePath
	 *            storage path that identifies the directory
	 * 
	 * @return
	 * @throws StorageActionException
	 */
	public Directory getDirectory(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * List all resources, container or binaries, directly under this directory.
	 * 
	 * @param storagePath
	 *            storage path that identifies the directory
	 * @return
	 * 
	 * @throws StorageActionException
	 */
	public ClosableIterable<Resource> listResourcesUnderDirectory(
			StoragePath storagePath) throws StorageActionException;

	/**
	 * Create a binary resource with a defined content.
	 * 
	 * @param storagePath
	 *            storage path that identifies the binary
	 * @param metadata
	 *            optional associated metadata
	 * @param payload
	 *            the content payload
	 * @param asReference
	 *            create the binary as a reference to the real content, which is
	 *            managed externally. If false, content should be copied into
	 *            the storage service.
	 * @throws StorageActionException
	 */
	public Binary createBinary(StoragePath storagePath,
			Map<String, Set<String>> metadata, ContentPayload payload,
			boolean asReference) throws StorageActionException;

	/**
	 * Get an existing binary resource.
	 * 
	 * @param storagePath
	 *            storage path that identifies the binary
	 * @return
	 * @throws StorageActionException
	 */
	public Binary getBinary(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * Replace existing binary content with given one, not changing any
	 * associated metadata.
	 * 
	 * @param storagePath
	 *            storage path that identifies the binary
	 * @param payload
	 *            the new content payload that would replace existing one.
	 * @param asReference
	 *            update the binary as a reference to the real content, which is
	 *            managed externally. If false, content should be copied into
	 *            the storage service.
	 * @param createIfNotExists
	 *            If <code>true</code> and binary does not exists then it will
	 *            be create. If <code>false</code> and binary does not exist
	 *            then a {@link StorageActionException} will be thrown with code
	 *            <code>StorageActionException.NOT_FOUND</code>.
	 * 
	 * @return
	 * @throws StorageActionException
	 */
	public Binary updateBinaryContent(StoragePath storagePath,
			ContentPayload payload, boolean asReference,
			boolean createIfNotExists) throws StorageActionException;

	/**
	 * Delete an existing resource, being it a container or a binary. If it is a
	 * container, recursively delete all resources under it.
	 * 
	 * @param storagePath
	 *            storage path that identifies the resource
	 * @throws StorageActionException
	 */
	public void deleteResource(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * Get metadata associated to a resource.
	 * 
	 * @param storagePath
	 *            storage path that identifies the resource
	 * @return
	 * @throws StorageActionExceptionResource
	 */
	public Map<String, Set<String>> getMetadata(StoragePath storagePath)
			throws StorageActionException;

	/**
	 * Update metadata associated with a container.
	 * 
	 * @param storagePath
	 *            storage path that identifies the resource
	 * @param replaceAll
	 *            true indicates that existing metadata will be discarded and
	 *            replaced by the new one; false indicates that existing
	 *            metadata will be updated to the new values (by replacing the
	 *            old values with the new ones)
	 * @throws StorageActionException
	 */
	public Map<String, Set<String>> updateMetadata(StoragePath storagePath,
			Map<String, Set<String>> metadata, boolean replaceAll)
			throws StorageActionException;

	/**
	 * Get entity class
	 * 
	 * @param storagePath
	 *            storage path that identifies the resource
	 * */
	public Class<? extends Entity> getEntity(StoragePath storagePath)
			throws StorageActionException;

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
	 * @throws StorageActionException
	 */
	public void copy(StorageService fromService, StoragePath fromStoragePath,
			StoragePath toStoragePath) throws StorageActionException;

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
	 * @throws StorageActionException
	 */
	public void move(StorageService fromService, StoragePath fromStoragePath,
			StoragePath toStoragePath) throws StorageActionException;

}
