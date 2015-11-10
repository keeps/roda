/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

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
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
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
import org.roda.core.storage.fedora.utils.FedoraConversionUtils;
import org.roda.core.storage.fedora.utils.FedoraUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that persists binary files and their containers in Fedora.
 *
 * @author Sébastien Leroux <sleroux@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
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
   *          Fedora base URL
   */
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
   *          Fedora base URL
   * @param username
   *          Fedora username
   * @param password
   *          Fedora password
   */
  public FedoraStorageService(String fedoraURL, String username, String password) {
    this.fedoraURL = fedoraURL;
    this.fedoraUsername = username;
    this.fedoraPassword = password;
    this.fedoraRepository = new FedoraRepositoryImpl(fedoraURL, username, password);
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
  public ClosableIterable<Container> listContainers() throws StorageServiceException {
    return new IterableContainer(fedoraRepository);
  }

  @Override
  public Container createContainer(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {

    try {
      FedoraObject container = fedoraRepository.createObject(FedoraUtils.createFedoraPath(storagePath));
      addMetadataToResource(container, metadata);
      return new DefaultContainer(storagePath, metadata);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (AlreadyExistsException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.ALREADY_EXISTS, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }

  }

  @Override
  public Container getContainer(StoragePath storagePath) throws StorageServiceException {

    if (!storagePath.isFromAContainer()) {
      throw new StorageServiceException("The storage path provided isn't from a container: " + storagePath,
        StorageServiceException.BAD_REQUEST);
    }

    try {
      return FedoraConversionUtils
        .fedoraObjectToContainer(fedoraRepository.getObject(FedoraUtils.createFedoraPath(storagePath)));
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }

  }

  @Override
  public void deleteContainer(StoragePath storagePath) throws StorageServiceException {
    try {
      fedoraRepository.getObject(FedoraUtils.createFedoraPath(storagePath)).forceDelete();
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (AlreadyExistsException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.ALREADY_EXISTS, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }
  }

  @Override
  public ClosableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath)
    throws StorageServiceException {
    return new IterableResource(fedoraRepository, storagePath);
  }

  @Override
  public Directory createDirectory(StoragePath storagePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {
    try {
      FedoraObject directory = fedoraRepository.createObject(FedoraUtils.createFedoraPath(storagePath));

      addMetadataToResource(directory, metadata);
      return new DefaultDirectory(storagePath, metadata);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (AlreadyExistsException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.ALREADY_EXISTS, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }

  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath, Map<String, Set<String>> metadata)
    throws StorageServiceException {

    FedoraObject directory;
    try {
      StoragePath storagePath = DefaultStoragePath.parse(parentStoragePath, UUID.randomUUID().toString());
      do {
        try {
          // XXX may want to change create object to native Fedora method that creates a random object
          directory = fedoraRepository.createObject(FedoraUtils.createFedoraPath(storagePath));
        } catch (AlreadyExistsException e) {
          directory = null;
          LOGGER.warn("Got a colision when creating random directory", e);
        }
      } while (directory == null);
      addMetadataToResource(directory, metadata);
      return new DefaultDirectory(storagePath, metadata);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }
  }

  @Override
  public Directory getDirectory(StoragePath storagePath) throws StorageServiceException {
    if (storagePath.isFromAContainer()) {
      throw new StorageServiceException("Invalid storage path for a directory: " + storagePath,
        StorageServiceException.BAD_REQUEST);
    }
    try {
      FedoraObject object = fedoraRepository.getObject(FedoraUtils.createFedoraPath(storagePath));
      return FedoraConversionUtils.fedoraObjectToDirectory(fedoraRepository.getRepositoryUrl(), object);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }
  }

  @Override
  public ClosableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath)
    throws StorageServiceException {
    return new IterableResource(fedoraRepository, storagePath);
  }

  @Override
  public Binary createBinary(StoragePath storagePath, Map<String, Set<String>> metadata, ContentPayload payload,
    boolean asReference) throws StorageServiceException {
    if (asReference) {
      // TODO method to create binary as reference.
      throw new StorageServiceException("Creating binary as reference not yet supported",
        StorageServiceException.NOT_IMPLEMENTED);
    } else {
      try {
        FedoraDatastream binary = fedoraRepository.createDatastream(FedoraUtils.createFedoraPath(storagePath),
          FedoraConversionUtils.contentPayloadToFedoraContent(payload));

        addMetadataToResource(binary, metadata);

        return FedoraConversionUtils.fedoraDatastreamToBinary(binary);
      } catch (ForbiddenException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
      } catch (AlreadyExistsException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.ALREADY_EXISTS, e);
      } catch (NotFoundException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
      } catch (FedoraException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
      }
    }

  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, Map<String, Set<String>> metadata,
    ContentPayload payload, boolean asReference) throws StorageServiceException {
    if (asReference) {
      // TODO method to create binary as reference.
      throw new StorageServiceException("Creating binary as reference not yet supported",
        StorageServiceException.NOT_IMPLEMENTED);
    } else {
      try {
        FedoraDatastream binary;
        StoragePath storagePath = DefaultStoragePath.parse(parentStoragePath, UUID.randomUUID().toString());
        do {
          try {
            // XXX may want to change create object to native Fedora method that creates a random datastream
            binary = fedoraRepository.createDatastream(FedoraUtils.createFedoraPath(storagePath),
              FedoraConversionUtils.contentPayloadToFedoraContent(payload));
          } catch (AlreadyExistsException e) {
            binary = null;
            LOGGER.warn("Got a colision when creating random bianry", e);
          }
        } while (binary == null);
        addMetadataToResource(binary, metadata);

        return FedoraConversionUtils.fedoraDatastreamToBinary(binary);
      } catch (ForbiddenException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
      } catch (NotFoundException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
      } catch (FedoraException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
      }
    }
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists) throws StorageServiceException {
    if (asReference) {
      // TODO method to update binary as reference.
      throw new StorageServiceException("Updating binary as reference not yet supported",
        StorageServiceException.INTERNAL_SERVER_ERROR);
    } else {
      try {
        FedoraDatastream datastream = fedoraRepository.getDatastream(FedoraUtils.createFedoraPath(storagePath));

        datastream.updateContent(FedoraConversionUtils.contentPayloadToFedoraContent(payload));

        return FedoraConversionUtils.fedoraDatastreamToBinary(datastream);
      } catch (ForbiddenException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
      } catch (AlreadyExistsException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.ALREADY_EXISTS, e);
      } catch (NotFoundException e) {
        if (createIfNotExists) {
          return createBinary(storagePath, new HashMap<String, Set<String>>(), payload, asReference);
        } else {
          throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
        }
      } catch (FedoraException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
      }

    }
  }

  @Override
  public Binary getBinary(StoragePath storagePath) throws StorageServiceException {
    try {
      FedoraDatastream ds = fedoraRepository.getDatastream(FedoraUtils.createFedoraPath(storagePath));

      if (!isDatastream(ds)) {
        throw new StorageServiceException("The resource obtained as being a datastream isn't really a datastream",
          StorageServiceException.BAD_REQUEST);
      }

      return FedoraConversionUtils.fedoraDatastreamToBinary(ds);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }
  }

  private boolean isDatastream(FedoraDatastream ds) throws FedoraException {
    Collection<String> mixins = ds.getMixins();
    return !mixins.contains(FEDORA_CONTAINER);
  }

  @Override
  public void deleteResource(StoragePath storagePath) throws StorageServiceException {
    String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
    try {
      if (fedoraRepository.exists(fedoraPath)) {
        boolean deleted = false;
        try {
          FedoraDatastream fds = fedoraRepository.getDatastream(fedoraPath);
          if (fds != null) {
            fds.forceDelete();
            deleted = true;
          }
        } catch (FedoraException e) {
          // FIXME add proper error handling
        }
        if (!deleted) {
          try {
            FedoraObject object = fedoraRepository.getObject(fedoraPath);
            if (object != null) {
              object.forceDelete();
            }
          } catch (FedoraException e) {
            // FIXME add proper error handling
          }
        }
      } else {
        throw new StorageServiceException("The resource identified by the path \"" + storagePath + "\" was not found",
          StorageServiceException.NOT_FOUND);
      }
    } catch (StorageServiceException e) {
      throw e;
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST);
    }

  }

  @Override
  public Map<String, Set<String>> getMetadata(StoragePath storagePath) throws StorageServiceException {
    String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
    boolean exist = false;
    try {
      exist = fedoraRepository.exists(fedoraPath);
    } catch (ForbiddenException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
    } catch (FedoraException e) {
      throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
    }
    if (!exist) {
      throw new StorageServiceException("The resource identified by the path \"" + storagePath + "\" was not found",
        StorageServiceException.NOT_FOUND);
    } else {
      try {
        FedoraObject fo = fedoraRepository.getObject(fedoraPath);
        return FedoraConversionUtils.tripleIteratorToMap(fo.getProperties());
      } catch (FedoraException fe) {
        try {
          FedoraDatastream fds = fedoraRepository.getDatastream(fedoraPath);
          return FedoraConversionUtils.tripleIteratorToMap(fds.getProperties());
        } catch (ForbiddenException e) {
          throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN);
        } catch (BadRequestException e) {
          throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST);
        } catch (NotFoundException e) {
          throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND);
        } catch (FedoraException e) {
          throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST);
        }
      }
    }

  }

  @Override
  public Map<String, Set<String>> updateMetadata(StoragePath storagePath, Map<String, Set<String>> metadata,
    boolean replaceAll) throws StorageServiceException {

    if (metadata != null) {
      String fedoraPath = FedoraUtils.createFedoraPath(storagePath);
      boolean exist = false;
      try {
        exist = fedoraRepository.exists(fedoraPath);
      } catch (ForbiddenException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN, e);
      } catch (FedoraException e) {
        throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST, e);
      }
      if (!exist) {
        throw new StorageServiceException("The resource identified by the path \"" + storagePath + "\" was not found",
          StorageServiceException.NOT_FOUND);
      } else {
        try {
          FedoraObject fo = fedoraRepository.getObject(fedoraPath);

          Map<String, Set<String>> old = FedoraConversionUtils.tripleIteratorToMap(fo.getProperties());

          return updateMetadata(fo, old, metadata, replaceAll);
        } catch (FedoraException fe) {
          try {
            FedoraDatastream fds = fedoraRepository.getDatastream(fedoraPath);
            Map<String, Set<String>> old = FedoraConversionUtils.tripleIteratorToMap(fds.getProperties());
            return updateMetadata(fds, old, metadata, replaceAll);
          } catch (ForbiddenException e) {
            throw new StorageServiceException(e.getMessage(), StorageServiceException.FORBIDDEN);
          } catch (BadRequestException e) {
            throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST);
          } catch (NotFoundException e) {
            throw new StorageServiceException(e.getMessage(), StorageServiceException.NOT_FOUND);
          } catch (FedoraException e) {
            throw new StorageServiceException(e.getMessage(), StorageServiceException.BAD_REQUEST);
          }
        }
      }
    } else {
      throw new StorageServiceException("Cannot update metadata to null", StorageServiceException.BAD_REQUEST);
    }

  }

  private Map<String, Set<String>> updateMetadata(FedoraResource resource, Map<String, Set<String>> oldMetadata,
    Map<String, Set<String>> newMetadata, boolean replaceAll) throws FedoraException {

    String sparqlUpdate;
    if (replaceAll) {
      sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(newMetadata, oldMetadata);
    } else {
      Map<String, Set<String>> metadataToDelete = new HashMap<String, Set<String>>();
      for (Entry<String, Set<String>> entry : newMetadata.entrySet()) {
        if (oldMetadata.containsKey(entry.getKey())) {
          metadataToDelete.put(entry.getKey(), entry.getValue());
        }
      }
      sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(newMetadata, metadataToDelete);
    }

    if (sparqlUpdate != null) {
      resource.updateProperties(sparqlUpdate);
    }

    return FedoraConversionUtils.tripleIteratorToMap(resource.getProperties());
  }

  private void addMetadataToResource(FedoraResource resource, Map<String, Set<String>> metadata)
    throws FedoraException {
    if (metadata != null) {
      final String sparqlUpdate = FedoraUtils.createSparqlUpdateQuery(metadata, null);
      if (sparqlUpdate != null) {
        LOGGER.debug("Updating properties of resource: " + resource.getName() + "\n" + sparqlUpdate);
        resource.updateProperties(sparqlUpdate);
      }
    }
  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws StorageServiceException {

    Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);

    if (fromService instanceof FedoraStorageService
      && ((FedoraStorageService) fromService).getFedoraURL().equalsIgnoreCase(getFedoraURL())) {
      copyInsideFedora(fromStoragePath, toStoragePath, rootEntity);
    } else {
      StorageServiceUtils.copyBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }

  }

  private void copyInsideFedora(StoragePath fromStoragePath, StoragePath toStoragePath,
    Class<? extends Entity> rootEntity) throws StorageServiceException {
    try {
      if (rootEntity.equals(Container.class) || rootEntity.equals(Directory.class)) {
        FedoraObject object = fedoraRepository.getObject(FedoraUtils.createFedoraPath(fromStoragePath));

        object.copy(FedoraUtils.createFedoraPath(toStoragePath));
      } else {
        FedoraDatastream datastream = fedoraRepository.getDatastream(FedoraUtils.createFedoraPath(fromStoragePath));

        datastream.copy(FedoraUtils.createFedoraPath(toStoragePath));
      }

    } catch (ForbiddenException e) {
      throw new StorageServiceException("Error while copying from one storage path to another",
        StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException("Error while copying from one storage path to another",
        StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException("Error while copying from one storage path to another",
        StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while copying from one storage path to another",
        StorageServiceException.BAD_REQUEST, e);
    }
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws StorageServiceException {

    Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);

    if (fromService instanceof FedoraStorageService
      && ((FedoraStorageService) fromService).getFedoraURL().equalsIgnoreCase(getFedoraURL())) {
      moveInsideFedora(fromStoragePath, toStoragePath, rootEntity);
    } else {
      StorageServiceUtils.moveBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  private void moveInsideFedora(StoragePath fromStoragePath, StoragePath toStoragePath,
    Class<? extends Entity> rootEntity) throws StorageServiceException {
    try {
      if (rootEntity.equals(Container.class) || rootEntity.equals(Directory.class)) {
        FedoraObject object = fedoraRepository.getObject(FedoraUtils.createFedoraPath(fromStoragePath));

        object.forceMove(FedoraUtils.createFedoraPath(toStoragePath));
      } else {
        FedoraDatastream datastream = fedoraRepository.getDatastream(FedoraUtils.createFedoraPath(fromStoragePath));

        datastream.forceMove(FedoraUtils.createFedoraPath(toStoragePath));
      }

    } catch (ForbiddenException e) {
      throw new StorageServiceException("Error while moving from one storage path to another",
        StorageServiceException.FORBIDDEN, e);
    } catch (BadRequestException e) {
      throw new StorageServiceException("Error while moving from one storage path to another",
        StorageServiceException.BAD_REQUEST, e);
    } catch (NotFoundException e) {
      throw new StorageServiceException("Error while moving from one storage path to another",
        StorageServiceException.NOT_FOUND, e);
    } catch (FedoraException e) {
      throw new StorageServiceException("Error while moving from one storage path to another",
        StorageServiceException.BAD_REQUEST, e);
    }
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath) throws StorageServiceException {
    if (storagePath.isFromAContainer()) {
      if (getContainer(storagePath) != null) {
        return Container.class;
      } else {
        throw new StorageServiceException(
          "There is no Container in the storage represented by \"" + storagePath.asString() + "\"",
          StorageServiceException.INTERNAL_SERVER_ERROR);
      }
    } else {
      // it's a directory or binary. but first let's see if that entity
      // exists in the storage
      try {
        FedoraObject object = fedoraRepository
          .getObject(FedoraUtils.createFedoraPath(storagePath) + "/" + FEDORA_RESOURCE_METADATA);

        if (object.getMixins().contains(FEDORA_CONTAINER)) {
          return Directory.class;
        } else {
          // it exists, it's not a directory, so it can only be a
          // binary
          return Binary.class;
        }
      } catch (FedoraException e) {
        throw new StorageServiceException(
          "There is no Directory or Binary in the storage represented by \"" + storagePath.asString() + "\"",
          StorageServiceException.INTERNAL_SERVER_ERROR, e);
      }

    }
  }

}
