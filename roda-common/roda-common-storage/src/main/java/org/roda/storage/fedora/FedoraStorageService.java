package org.roda.storage.fedora;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.fcrepo.client.AlreadyExistsException;
import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.roda.storage.Binary;
import org.roda.storage.ClosableIterable;
import org.roda.storage.Container;
import org.roda.storage.ContentPayload;
import org.roda.storage.DefaultContainer;
import org.roda.storage.DefaultDirectory;
import org.roda.storage.Directory;
import org.roda.storage.Entity;
import org.roda.storage.Resource;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceUtils;
import org.roda.storage.fedora.utils.FedoraConversionUtils;
import org.roda.storage.fedora.utils.FedoraUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that persists binary files and their containers in Fedora.
 *
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public class FedoraStorageService implements StorageService {
	public static final String RODA_PREFIX = "roda";
	public static final String RODA_NAMESPACE = "http://www.roda-project.org/roda#";

	public static final String FEDORA_CONTAINER = "fedora:Container";
	public static final String FEDORA_BINARY = "fedora:Binary";
	public static final String FEDORA_RESOURCE_METADATA = "fcr:metadata";

	private static final Logger LOGGER = LoggerFactory.getLogger(FedoraStorageService.class);

	private String fedoraURL;
	private String fedoraUsername;
	private String fedoraPassword;
	private FedoraRepository fedoraRepository;

	/**
	 * Public constructor (for using without user credentials)
	 *
	 * @param fedoraURL
	 *            Fedora base URL
	 * */
	public FedoraStorageService(String fedoraURL) {
		this.fedoraURL = fedoraURL;
		this.fedoraUsername = null;
		this.fedoraPassword = null;
		this.fedoraRepository = new FedoraRepositoryImpl(fedoraURL);
	}

	/**
	 * Public constructor
	 *
	 * @param fedoraURL
	 *            Fedora base URL
	 * @param username
	 *            Fedora username
	 * @param password
	 *            Fedora password
	 * */
	public FedoraStorageService(String fedoraURL, String username,
			String password) {
		this.fedoraURL = fedoraURL;
		this.fedoraUsername = username;
		this.fedoraPassword = password;
		this.fedoraRepository = new FedoraRepositoryImpl(fedoraURL, username,
				password);
	}

	public String getFedoraURL() {
		return fedoraURL;
	}

	public String getFedoraUsername() {
		return fedoraUsername;
	}

	public String getFedoraPassword() {
		return fedoraPassword;
	}

	public FedoraRepository getFedoraRepository() {
		return fedoraRepository;
	}

	@Override
	public ClosableIterable<Container> listContainers() throws StorageActionException {
		return new IterableContainer(fedoraRepository);
	}

	@Override
	public Container createContainer(StoragePath storagePath,
			Map<String, Set<String>> metadata) throws StorageActionException {

		try {
			FedoraObject container = fedoraRepository.createObject(FedoraUtils
					.createFedoraPath(storagePath));
			addMetadataToResource(container, metadata);
			return new DefaultContainer(storagePath, metadata);
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (AlreadyExistsException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.ALREADY_EXISTS, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}

	}

	@Override
	public Container getContainer(StoragePath storagePath)
			throws StorageActionException {

		if (!storagePath.isFromAContainer()) {
			throw new StorageActionException(
					"The storage path provided isn't from a container: "
							+ storagePath, StorageActionException.BAD_REQUEST);
		}

		try {
			return FedoraConversionUtils
					.fedoraObjectToContainer(fedoraRepository
							.getObject(FedoraUtils
									.createFedoraPath(storagePath)));
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}

	}

	@Override
	public void deleteContainer(StoragePath storagePath)
			throws StorageActionException {
		try {
			fedoraRepository.getObject(
					FedoraUtils.createFedoraPath(storagePath)).forceDelete();
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (AlreadyExistsException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.ALREADY_EXISTS, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}
	}

	@Override
	public ClosableIterable<Resource> listResourcesUnderContainer(
			StoragePath storagePath) throws StorageActionException {
		return new IterableResource(fedoraRepository, storagePath);
	}

	@Override
	public Directory createDirectory(StoragePath storagePath,
			Map<String, Set<String>> metadata) throws StorageActionException {
		try {
			FedoraObject directory = fedoraRepository.createObject(FedoraUtils
					.createFedoraPath(storagePath));

			addMetadataToResource(directory, metadata);
			return new DefaultDirectory(storagePath, metadata);
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (AlreadyExistsException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.ALREADY_EXISTS, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}

	}

	@Override
	public Directory getDirectory(StoragePath storagePath)
			throws StorageActionException {
		if (storagePath.isFromAContainer()) {
			throw new StorageActionException(
					"Invalid storage path for a directory: " + storagePath,
					StorageActionException.BAD_REQUEST);
		}
		try {
			FedoraObject object = fedoraRepository.getObject(FedoraUtils
					.createFedoraPath(storagePath));
			return FedoraConversionUtils.fedoraObjectToDirectory(
					fedoraRepository.getRepositoryUrl(), object);
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}
	}

	@Override
	public ClosableIterable<Resource> listResourcesUnderDirectory(
			StoragePath storagePath) throws StorageActionException {
		return new IterableResource(fedoraRepository, storagePath);
	}

	@Override
	public Binary createBinary(StoragePath storagePath,
			Map<String, Set<String>> metadata, ContentPayload payload,
			boolean asReference) throws StorageActionException {
		if (asReference) {
			// TODO method to create binary as reference.
			throw new StorageActionException(
					"Creating binary as reference not yet supported",
					StorageActionException.NOT_IMPLEMENTED);
		} else {
			try {
				FedoraDatastream binary = fedoraRepository.createDatastream(
						FedoraUtils.createFedoraPath(storagePath),
						FedoraConversionUtils
								.contentPayloadToFedoraContent(payload));

				addMetadataToResource(binary, metadata);

				return FedoraConversionUtils.fedoraDatastreamToBinary(binary);
			} catch (ForbiddenException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.FORBIDDEN, e);
			} catch (AlreadyExistsException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.ALREADY_EXISTS, e);
			} catch (NotFoundException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.NOT_FOUND, e);
			} catch (FedoraException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.BAD_REQUEST, e);
			}
		}

	}

	@Override
	public Binary updateBinaryContent(StoragePath storagePath,
			ContentPayload payload, boolean asReference,
			boolean createIfNotExists) throws StorageActionException {
		if (asReference) {
			// TODO method to update binary as reference.
			throw new StorageActionException(
					"Updating binary as reference not yet supported",
					StorageActionException.INTERNAL_SERVER_ERROR);
		} else {
			try {
				FedoraDatastream datastream = fedoraRepository
						.getDatastream(FedoraUtils
								.createFedoraPath(storagePath));

				datastream.updateContent(FedoraConversionUtils
						.contentPayloadToFedoraContent(payload));

				return FedoraConversionUtils
						.fedoraDatastreamToBinary(datastream);
			} catch (ForbiddenException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.FORBIDDEN, e);
			} catch (AlreadyExistsException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.ALREADY_EXISTS, e);
			} catch (NotFoundException e) {
				if (createIfNotExists) {
					return createBinary(storagePath,
							new HashMap<String, Set<String>>(), payload,
							asReference);
				} else {
					throw new StorageActionException(e.getMessage(),
							StorageActionException.NOT_FOUND, e);
				}
			} catch (FedoraException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.BAD_REQUEST, e);
			}

		}
	}

	@Override
	public Binary getBinary(StoragePath storagePath)
			throws StorageActionException {
		try {
			FedoraDatastream ds = fedoraRepository.getDatastream(FedoraUtils
					.createFedoraPath(storagePath));

			if (!isDatastream(ds)) {
				throw new StorageActionException(
						"The resource obtained as being a datastream isn't really a datastream",
						StorageActionException.BAD_REQUEST);
			}

			return FedoraConversionUtils.fedoraDatastreamToBinary(ds);
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}
	}

	private boolean isDatastream(FedoraDatastream ds) throws FedoraException {
		Collection<String> mixins = ds.getMixins();
		return !mixins.contains(FEDORA_CONTAINER);
	}

	@Override
	public void deleteResource(StoragePath storagePath)
			throws StorageActionException {
		String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
		try {
			if (fedoraRepository.exists(fedoraPath)) {
				boolean deleted = false;
				try {
					FedoraDatastream fds = fedoraRepository
							.getDatastream(fedoraPath);
					if (fds != null) {
						fds.forceDelete();
						deleted = true;
					}
				} catch (FedoraException e) {
					// FIXME add proper error handling
				}
				if (!deleted) {
					try {
						FedoraObject object = fedoraRepository
								.getObject(fedoraPath);
						if (object != null) {
							object.forceDelete();
						}
					} catch (FedoraException e) {
						// FIXME add proper error handling
					}
				}
			} else {
				throw new StorageActionException(
						"The resource identified by the path \"" + storagePath
								+ "\" was not found",
						StorageActionException.NOT_FOUND);
			}
		} catch (StorageActionException e) {
			throw e;
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST);
		}

	}

	@Override
	public Map<String, Set<String>> getMetadata(StoragePath storagePath)
			throws StorageActionException {
		String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
		boolean exist = false;
		try {
			exist = fedoraRepository.exists(fedoraPath);
		} catch (ForbiddenException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.FORBIDDEN, e);
		} catch (FedoraException e) {
			throw new StorageActionException(e.getMessage(),
					StorageActionException.BAD_REQUEST, e);
		}
		if (!exist) {
			throw new StorageActionException(
					"The resource identified by the path \"" + storagePath
							+ "\" was not found",
					StorageActionException.NOT_FOUND);
		} else {
			try {
				FedoraObject fo = fedoraRepository.getObject(fedoraPath);
				return FedoraConversionUtils.tripleIteratorToMap(fo
						.getProperties());
			} catch (FedoraException fe) {
				try {
					FedoraDatastream fds = fedoraRepository
							.getDatastream(fedoraPath);
					return FedoraConversionUtils.tripleIteratorToMap(fds
							.getProperties());
				} catch (ForbiddenException e) {
					throw new StorageActionException(e.getMessage(),
							StorageActionException.FORBIDDEN);
				} catch (BadRequestException e) {
					throw new StorageActionException(e.getMessage(),
							StorageActionException.BAD_REQUEST);
				} catch (NotFoundException e) {
					throw new StorageActionException(e.getMessage(),
							StorageActionException.NOT_FOUND);
				} catch (FedoraException e) {
					throw new StorageActionException(e.getMessage(),
							StorageActionException.BAD_REQUEST);
				}
			}
		}

	}

	@Override
	public Map<String, Set<String>> updateMetadata(StoragePath storagePath,
			Map<String, Set<String>> metadata, boolean replaceAll)
			throws StorageActionException {

		if (metadata != null) {
			String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
			boolean exist = false;
			try {
				exist = fedoraRepository.exists(fedoraPath);
			} catch (ForbiddenException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.FORBIDDEN, e);
			} catch (FedoraException e) {
				throw new StorageActionException(e.getMessage(),
						StorageActionException.BAD_REQUEST, e);
			}
			if (!exist) {
				throw new StorageActionException(
						"The resource identified by the path \"" + storagePath
								+ "\" was not found",
						StorageActionException.NOT_FOUND);
			} else {
				try {
					FedoraObject fo = fedoraRepository.getObject(fedoraPath);

					Map<String, Set<String>> old = FedoraConversionUtils
							.tripleIteratorToMap(fo.getProperties());

					return updateMetadata(fo, old, metadata, replaceAll);
				} catch (FedoraException fe) {
					try {
						FedoraDatastream fds = fedoraRepository
								.getDatastream(fedoraPath);
						Map<String, Set<String>> old = FedoraConversionUtils
								.tripleIteratorToMap(fds.getProperties());
						return updateMetadata(fds, old, metadata, replaceAll);
					} catch (ForbiddenException e) {
						throw new StorageActionException(e.getMessage(),
								StorageActionException.FORBIDDEN);
					} catch (BadRequestException e) {
						throw new StorageActionException(e.getMessage(),
								StorageActionException.BAD_REQUEST);
					} catch (NotFoundException e) {
						throw new StorageActionException(e.getMessage(),
								StorageActionException.NOT_FOUND);
					} catch (FedoraException e) {
						throw new StorageActionException(e.getMessage(),
								StorageActionException.BAD_REQUEST);
					}
				}
			}
		} else {
			throw new StorageActionException("Cannot update metadata to null",
					StorageActionException.BAD_REQUEST);
		}

	}

	private Map<String, Set<String>> updateMetadata(FedoraResource resource,
			Map<String, Set<String>> oldMetadata,
			Map<String, Set<String>> newMetadata, boolean replaceAll)
			throws FedoraException {

		String sparqlUpdate;
		if (replaceAll) {
			sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(newMetadata,
					oldMetadata);
		} else {
			Map<String, Set<String>> metadataToDelete = new HashMap<String, Set<String>>();
			for (Entry<String, Set<String>> entry : newMetadata.entrySet()) {
				if (oldMetadata.containsKey(entry.getKey())) {
					metadataToDelete.put(entry.getKey(), entry.getValue());
				}
			}
			sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(newMetadata,
					metadataToDelete);
		}

		if (sparqlUpdate != null) {
			resource.updateProperties(sparqlUpdate);
		}

		return FedoraConversionUtils.tripleIteratorToMap(resource
				.getProperties());
	}

	private void addMetadataToResource(FedoraResource resource,
			Map<String, Set<String>> metadata) throws FedoraException {
		if (metadata != null) {
			final String sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(
					metadata, null);
			if (sparqlUpdate != null) {
				LOGGER.debug("Updating properties of resource: "
						+ resource.getName() + "\n" + sparqlUpdate);
				resource.updateProperties(sparqlUpdate);
			}
		}
	}

	@Override
	public void copy(StorageService fromService, StoragePath fromStoragePath,
			StoragePath toStoragePath) throws StorageActionException {

		Class<? extends Entity> rootEntity = fromService
				.getEntity(fromStoragePath);

		if (fromService instanceof FedoraStorageService
				&& ((FedoraStorageService) fromService).getFedoraURL()
						.equalsIgnoreCase(getFedoraURL())) {
			copyInsideFedora(fromStoragePath, toStoragePath, rootEntity);
		} else {
			StorageServiceUtils.copyBetweenStorageServices(fromService,
					fromStoragePath, this, toStoragePath, rootEntity);
		}

	}

	private void copyInsideFedora(StoragePath fromStoragePath,
			StoragePath toStoragePath, Class<? extends Entity> rootEntity)
			throws StorageActionException {
		try {
			if (rootEntity.equals(Container.class)
					|| rootEntity.equals(Directory.class)) {
				FedoraObject object = fedoraRepository.getObject(FedoraUtils
						.createFedoraPath(fromStoragePath));

				object.copy(FedoraUtils.createFedoraPath(toStoragePath));
			} else {
				FedoraDatastream datastream = fedoraRepository
						.getDatastream(FedoraUtils
								.createFedoraPath(fromStoragePath));

				datastream.copy(FedoraUtils.createFedoraPath(toStoragePath));
			}

		} catch (ForbiddenException e) {
			throw new StorageActionException(
					"Error while copying from one storage path to another",
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(
					"Error while copying from one storage path to another",
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(
					"Error while copying from one storage path to another",
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(
					"Error while copying from one storage path to another",
					StorageActionException.BAD_REQUEST, e);
		}
	}

	@Override
	public void move(StorageService fromService, StoragePath fromStoragePath,
			StoragePath toStoragePath) throws StorageActionException {

		Class<? extends Entity> rootEntity = fromService
				.getEntity(fromStoragePath);

		if (fromService instanceof FedoraStorageService
				&& ((FedoraStorageService) fromService).getFedoraURL()
						.equalsIgnoreCase(getFedoraURL())) {
			moveInsideFedora(fromStoragePath, toStoragePath, rootEntity);
		} else {
			StorageServiceUtils.moveBetweenStorageServices(fromService,
					fromStoragePath, this, toStoragePath, rootEntity);
		}
	}

	private void moveInsideFedora(StoragePath fromStoragePath,
			StoragePath toStoragePath, Class<? extends Entity> rootEntity)
			throws StorageActionException {
		try {
			if (rootEntity.equals(Container.class)
					|| rootEntity.equals(Directory.class)) {
				FedoraObject object = fedoraRepository.getObject(FedoraUtils
						.createFedoraPath(fromStoragePath));

				object.forceMove(FedoraUtils.createFedoraPath(toStoragePath));
			} else {
				FedoraDatastream datastream = fedoraRepository
						.getDatastream(FedoraUtils
								.createFedoraPath(fromStoragePath));

				datastream.forceMove(FedoraUtils
						.createFedoraPath(toStoragePath));
			}

		} catch (ForbiddenException e) {
			throw new StorageActionException(
					"Error while moving from one storage path to another",
					StorageActionException.FORBIDDEN, e);
		} catch (BadRequestException e) {
			throw new StorageActionException(
					"Error while moving from one storage path to another",
					StorageActionException.BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new StorageActionException(
					"Error while moving from one storage path to another",
					StorageActionException.NOT_FOUND, e);
		} catch (FedoraException e) {
			throw new StorageActionException(
					"Error while moving from one storage path to another",
					StorageActionException.BAD_REQUEST, e);
		}
	}

	@Override
	public Class<? extends Entity> getEntity(StoragePath storagePath)
			throws StorageActionException {
		if (storagePath.isFromAContainer()) {
			if (getContainer(storagePath) != null) {
				return Container.class;
			} else {
				throw new StorageActionException(
						"There is no Container in the storage represented by \""
								+ storagePath.asString() + "\"",
						StorageActionException.INTERNAL_SERVER_ERROR);
			}
		} else {
			// it's a directory or binary. but first let's see if that entity
			// exists in the storage
			try {
				FedoraObject object = fedoraRepository.getObject(FedoraUtils
						.createFedoraPath(storagePath)
						+ "/"
						+ FEDORA_RESOURCE_METADATA);

				if (object.getMixins().contains(FEDORA_CONTAINER)) {
					return Directory.class;
				} else {
					// it exists, it's not a directory, so it can only be a
					// binary
					return Binary.class;
				}
			} catch (FedoraException e) {
				throw new StorageActionException(
						"There is no Directory or Binary in the storage represented by \""
								+ storagePath.asString() + "\"",
						StorageActionException.INTERNAL_SERVER_ERROR, e);
			}

		}
	}

}
