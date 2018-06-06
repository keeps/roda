/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultContainer;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Entity;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceUtils;
import org.roda.core.storage.fedora.utils.FedoraConversionUtils;
import org.roda.core.storage.fedora.utils.FedoraUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.storage.utils.StorageRecursiveListingUtils;
import org.roda.core.util.Base64;
import org.roda.core.util.IdUtils;
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
  public boolean exists(StoragePath storagePath) {
    return hasBinary(storagePath) || hasDirectory(storagePath);
  }

  @Override
  public CloseableIterable<Container> listContainers()
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return new IterableContainer(fedoraRepository);
  }

  @Override
  public Container createContainer(StoragePath storagePath)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException {

    try {
      fedoraRepository.createObject(FedoraUtils.storagePathToFedoraPath(storagePath));
      return new DefaultContainer(storagePath);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Could not create container", e);
    } catch (org.fcrepo.client.AlreadyExistsException e) {
      throw new AlreadyExistsException("Could not create container", e);
    } catch (FedoraException e) {
      throw new GenericException("Could not create container", e);
    }
  }

  @Override
  public Container getContainer(StoragePath storagePath)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException {

    if (!storagePath.isFromAContainer()) {
      throw new RequestNotValidException("The storage path provided isn't from a container: " + storagePath);
    }

    try {
      return FedoraConversionUtils
        .fedoraObjectToContainer(fedoraRepository.getObject(FedoraUtils.storagePathToFedoraPath(storagePath)));
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Could not get container", e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Could not get container", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Could not get container", e);
    } catch (FedoraException e) {
      throw new GenericException("Could not get container", e);
    }
  }

  @Override
  public void deleteContainer(StoragePath storagePath)
    throws AuthorizationDeniedException, NotFoundException, GenericException {
    try {
      fedoraRepository.getObject(FedoraUtils.storagePathToFedoraPath(storagePath)).forceDelete();
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Could not delete container", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Could not delete container", e);
    } catch (FedoraException e) {
      throw new GenericException("Could not get container", e);
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (recursive == true) {
      return StorageRecursiveListingUtils.listAllUnderContainer(this, storagePath);
    } else {
      return new IterableResource(fedoraRepository, storagePath);
    }
  }

  @Override
  public Long countResourcesUnderContainer(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    if (recursive == true) {
      return StorageRecursiveListingUtils.countAllUnderContainer(this, storagePath);
    } else {
      try {
        Collection<FedoraResource> children = fedoraRepository
          .getObject(FedoraUtils.storagePathToFedoraPath(storagePath)).getChildren(null);
        return Long.valueOf(children.size());
      } catch (ForbiddenException e) {
        throw new AuthorizationDeniedException("Could not count resource under directory", e);
      } catch (BadRequestException e) {
        throw new RequestNotValidException("Could not count resource under directory", e);
      } catch (org.fcrepo.client.NotFoundException e) {
        throw new NotFoundException("Could not count resource under directory", e);
      } catch (FedoraException e) {
        throw new GenericException("Could not count resource under directory", e);
      }
    }
  }

  @Override
  public Directory createDirectory(StoragePath storagePath)
    throws AuthorizationDeniedException, AlreadyExistsException, GenericException {
    try {
      fedoraRepository.createObject(FedoraUtils.storagePathToFedoraPath(storagePath));
      return new DefaultDirectory(storagePath);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Could not create directory", e);
    } catch (org.fcrepo.client.AlreadyExistsException e) {
      throw new AlreadyExistsException("Could not create directory", e);
    } catch (FedoraException e) {
      throw new GenericException("Could not create directory", e);
    }

  }

  @Override
  public Directory createRandomDirectory(StoragePath parentStoragePath)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    FedoraObject directory;
    try {
      StoragePath storagePath = DefaultStoragePath.parse(parentStoragePath, IdUtils.createUUID());
      do {
        try {
          // XXX may want to change create object to native Fedora method that
          // creates a random object
          directory = fedoraRepository.createObject(FedoraUtils.storagePathToFedoraPath(storagePath));
        } catch (org.fcrepo.client.AlreadyExistsException e) {
          directory = null;
          LOGGER.warn("Got a colision when creating random directory", e);
        }
      } while (directory == null);
      return new DefaultDirectory(storagePath);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error creating random directory under " + parentStoragePath, e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Error creating random directory under " + parentStoragePath, e);
    } catch (FedoraException e) {
      throw new GenericException("Error creating random directory under " + parentStoragePath, e);
    }
  }

  @Override
  public Directory getDirectory(StoragePath storagePath)
    throws RequestNotValidException, GenericException, AuthorizationDeniedException, NotFoundException {
    if (storagePath.isFromAContainer()) {
      throw new RequestNotValidException("Invalid storage path for a directory: " + storagePath);
    }
    try {
      FedoraObject object = fedoraRepository.getObject(FedoraUtils.storagePathToFedoraPath(storagePath));
      return FedoraConversionUtils.fedoraObjectToDirectory(fedoraRepository.getRepositoryUrl(), object);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error getting directory " + storagePath, e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Error getting directory " + storagePath, e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Error getting directory " + storagePath, e);
    } catch (FedoraException e) {
      // Unfortunately Fedora does not give a better error when requesting a
      // file as a directory
      throw new RequestNotValidException("Error getting directory " + storagePath, e);
    }
  }

  @Override
  public CloseableIterable<Resource> listResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    if (recursive) {
      return StorageRecursiveListingUtils.listAllUnderDirectory(this, storagePath);
    } else {
      return new IterableResource(fedoraRepository, storagePath);
    }
  }

  @Override
  public Long countResourcesUnderDirectory(StoragePath storagePath, boolean recursive)
    throws NotFoundException, GenericException, AuthorizationDeniedException, RequestNotValidException {
    if (recursive) {
      return StorageRecursiveListingUtils.countAllUnderDirectory(this, storagePath);
    } else {
      try {
        Collection<FedoraResource> children = fedoraRepository
          .getObject(FedoraUtils.storagePathToFedoraPath(storagePath)).getChildren(null);
        return Long.valueOf(children.size());
      } catch (ForbiddenException e) {
        throw new AuthorizationDeniedException("Could not count resource under directory", e);
      } catch (BadRequestException e) {
        throw new RequestNotValidException("Could not count resource under directory", e);
      } catch (org.fcrepo.client.NotFoundException e) {
        throw new NotFoundException("Could not count resource under directory", e);
      } catch (FedoraException e) {
        throw new GenericException("Could not count resource under directory", e);
      }
    }
  }

  @Override
  public Binary createBinary(StoragePath storagePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException {
    if (asReference) {
      // TODO method to create binary as reference.
      throw new GenericException("Creating binary as reference not yet supported");
    } else {
      try {
        String path = FedoraUtils.storagePathToFedoraPath(storagePath);
        LOGGER.debug("PATH {}", path);
        FedoraContent fedoraContentPayload = FedoraConversionUtils.contentPayloadToFedoraContent(payload);
        FedoraDatastream binary = fedoraRepository.createDatastream(path, fedoraContentPayload);
        IOUtils.closeQuietly(fedoraContentPayload.getContent());
        return FedoraConversionUtils.fedoraDatastreamToBinary(binary);
      } catch (ForbiddenException e) {
        throw new AuthorizationDeniedException("Error creating binary", e);
      } catch (org.fcrepo.client.AlreadyExistsException e) {
        throw new AlreadyExistsException("Error creating binary", e);
      } catch (org.fcrepo.client.NotFoundException e) {
        throw new NotFoundException("Error creating binary", e);
      } catch (FedoraException e) {
        LOGGER.error(e.getMessage(), e);
        throw new GenericException("Error creating binary", e);
      }
    }

  }

  @Override
  public Binary createRandomBinary(StoragePath parentStoragePath, ContentPayload payload, boolean asReference)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    if (asReference) {
      // TODO method to create binary as reference.
      throw new GenericException("Creating binary as reference not yet supported");
    } else {
      try {
        FedoraDatastream binary;
        StoragePath storagePath = DefaultStoragePath.parse(parentStoragePath, IdUtils.createUUID());
        do {
          try {
            // XXX may want to change create object to native Fedora method that
            // creates a random datastream
            FedoraContent fedoraContentPayload = FedoraConversionUtils.contentPayloadToFedoraContent(payload);
            binary = fedoraRepository.createDatastream(FedoraUtils.storagePathToFedoraPath(storagePath),
              fedoraContentPayload);
            IOUtils.closeQuietly(fedoraContentPayload.getContent());
          } catch (org.fcrepo.client.AlreadyExistsException e) {
            binary = null;
            LOGGER.warn("Got a colision when creating random bianry", e);
          }
        } while (binary == null);

        return FedoraConversionUtils.fedoraDatastreamToBinary(binary);
      } catch (ForbiddenException e) {
        throw new AuthorizationDeniedException(e.getMessage(), e);
      } catch (org.fcrepo.client.NotFoundException e) {
        throw new NotFoundException(e.getMessage(), e);
      } catch (FedoraException e) {
        throw new GenericException(e.getMessage(), e);
      }
    }
  }

  @Override
  public Binary updateBinaryContent(StoragePath storagePath, ContentPayload payload, boolean asReference,
    boolean createIfNotExists)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException {
    if (asReference) {
      // TODO method to update binary as reference.
      throw new GenericException("Updating binary as reference not yet supported");
    } else {
      try {
        FedoraDatastream datastream = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));
        FedoraContent fedoraContentPayload = FedoraConversionUtils.contentPayloadToFedoraContent(payload);
        datastream.updateContent(fedoraContentPayload);
        IOUtils.closeQuietly(fedoraContentPayload.getContent());
        return FedoraConversionUtils.fedoraDatastreamToBinary(datastream);
      } catch (ForbiddenException e) {
        throw new AuthorizationDeniedException("Error updating binary content", e);
      } catch (org.fcrepo.client.NotFoundException e) {
        if (createIfNotExists) {
          try {
            return createBinary(storagePath, payload, asReference);
          } catch (AlreadyExistsException e1) {
            throw new GenericException("Error updating binary content", e1);
          }
        } else {
          throw new NotFoundException("Error updating binary content", e);
        }
      } catch (FedoraException e) {
        throw new GenericException("Error updating binary content", e);
      }

    }
  }

  @Override
  public Binary getBinary(StoragePath storagePath)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      FedoraDatastream ds = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));

      if (!isDatastream(ds)) {
        throw new RequestNotValidException("The resource obtained as being a datastream isn't really a datastream");
      }

      return FedoraConversionUtils.fedoraDatastreamToBinary(ds);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException(e.getMessage(), e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException(e.getMessage(), e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (FedoraException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  private boolean isDatastream(FedoraDatastream ds) throws FedoraException {
    Collection<String> mixins = ds.getMixins();
    return !mixins.contains(FEDORA_CONTAINER);
  }

  @Override
  public void deleteResource(StoragePath storagePath)
    throws NotFoundException, AuthorizationDeniedException, GenericException {
    String fedoraPath = FedoraUtils.storagePathToFedoraPath(storagePath);
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
        throw new NotFoundException("The resource identified by the path \"" + storagePath + "\" was not found");
      }
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error deleting resource: " + storagePath, e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (FedoraException e) {
      throw new GenericException("Error deleting resource: " + storagePath, e);
    }

  }

  @Override
  public void copy(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException,
    AlreadyExistsException {

    Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);

    if (fromService instanceof FedoraStorageService
      && ((FedoraStorageService) fromService).getFedoraURL().equalsIgnoreCase(getFedoraURL())) {
      copyInsideFedora(fromStoragePath, toStoragePath, rootEntity);
    } else {
      StorageServiceUtils.copyBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }

  }

  private void copyInsideFedora(StoragePath fromStoragePath, StoragePath toStoragePath,
    Class<? extends Entity> rootEntity)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    try {
      if (rootEntity.equals(Container.class) || rootEntity.equals(Directory.class)) {
        FedoraObject object = fedoraRepository.getObject(FedoraUtils.storagePathToFedoraPath(fromStoragePath));

        object.copy(FedoraUtils.storagePathToFedoraPath(toStoragePath));
      } else {
        FedoraDatastream datastream = fedoraRepository
          .getDatastream(FedoraUtils.storagePathToFedoraPath(fromStoragePath));

        datastream.copy(FedoraUtils.storagePathToFedoraPath(toStoragePath));
      }

    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error while copying from one storage path to another", e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Error while copying from one storage path to another", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Error while copying from one storage path to another", e);
    } catch (FedoraException e) {
      throw new GenericException("Error while copying from one storage path to another", e);
    }
  }

  @Override
  public void move(StorageService fromService, StoragePath fromStoragePath, StoragePath toStoragePath)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, NotFoundException,
    AlreadyExistsException {

    Class<? extends Entity> rootEntity = fromService.getEntity(fromStoragePath);

    if (fromService instanceof FedoraStorageService
      && ((FedoraStorageService) fromService).getFedoraURL().equalsIgnoreCase(getFedoraURL())) {
      moveInsideFedora(fromStoragePath, toStoragePath, rootEntity);
    } else {
      StorageServiceUtils.moveBetweenStorageServices(fromService, fromStoragePath, this, toStoragePath, rootEntity);
    }
  }

  private void moveInsideFedora(StoragePath fromStoragePath, StoragePath toStoragePath,
    Class<? extends Entity> rootEntity)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    try {
      if (rootEntity.equals(Container.class) || rootEntity.equals(Directory.class)) {
        FedoraObject object = fedoraRepository.getObject(FedoraUtils.storagePathToFedoraPath(fromStoragePath));

        object.forceMove(FedoraUtils.storagePathToFedoraPath(toStoragePath));
      } else {
        FedoraDatastream datastream = fedoraRepository
          .getDatastream(FedoraUtils.storagePathToFedoraPath(fromStoragePath));

        datastream.forceMove(FedoraUtils.storagePathToFedoraPath(toStoragePath));
      }

    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException("Error while moving from one storage path to another", e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException("Error while moving from one storage path to another", e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException("Error while moving from one storage path to another", e);
    } catch (FedoraException e) {
      throw new GenericException("Error while moving from one storage path to another", e);
    }
  }

  @Override
  public Class<? extends Entity> getEntity(StoragePath storagePath)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
    if (storagePath.isFromAContainer()) {
      if (getContainer(storagePath) != null) {
        return Container.class;
      } else {
        throw new GenericException("There is no Container in the storage represented by \"" + storagePath + "\"");
      }
    } else {
      // it's a directory or binary. but first let's see if that entity
      // exists in the storage
      try {
        FedoraObject object = fedoraRepository
          .getObject(FedoraUtils.storagePathToFedoraPath(storagePath) + "/" + FEDORA_RESOURCE_METADATA);

        if (object.getMixins().contains(FEDORA_CONTAINER)) {
          return Directory.class;
        } else {
          // it exists, it's not a directory, so it can only be a
          // binary
          return Binary.class;
        }
      } catch (FedoraException e) {
        throw new GenericException(
          "There is no Directory or Binary in the storage represented by \"" + storagePath + "\"", e);
      }

    }
  }

  @Override
  public DirectResourceAccess getDirectAccess(final StoragePath storagePath) {
    return new DirectResourceAccess() {
      Path temp = null;

      @Override
      public Path getPath()
        throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
        Class<? extends Entity> entity = getEntity(storagePath);
        Path path;
        try {
          temp = Files.createTempDirectory("temp", getTempDirFilePermissions());
          if (entity.equals(Container.class) || entity.equals(Directory.class)) {
            StorageService tempStorage = new FileStorageService(temp);
            tempStorage.copy(FedoraStorageService.this, storagePath, storagePath);
            path = temp;
          } else {
            path = temp.resolve(entity.getName());
            Binary binary = getBinary(storagePath);
            ContentPayload payload = binary.getContent();
            try (InputStream inputStream = payload.createInputStream()) {
              Files.copy(inputStream, path);
            }
          }
        } catch (IOException | AlreadyExistsException e) {
          throw new GenericException(e);
        }
        return path;
      }

      @Override
      public void close() throws IOException {
        if (temp != null) {
          Files.delete(temp);
          temp = null;
        }
      }
    };
  }

  private static FileAttribute<Set<PosixFilePermission>> getTempDirFilePermissions() {
    Set<PosixFilePermission> perms = new HashSet<>();
    // add owners permission
    perms.add(PosixFilePermission.OWNER_READ);
    perms.add(PosixFilePermission.OWNER_WRITE);
    perms.add(PosixFilePermission.OWNER_EXECUTE);
    // add group permissions
    perms.add(PosixFilePermission.GROUP_READ);
    perms.add(PosixFilePermission.GROUP_WRITE);
    perms.add(PosixFilePermission.GROUP_EXECUTE);
    // add others permissions
    perms.add(PosixFilePermission.OTHERS_READ);
    perms.add(PosixFilePermission.OTHERS_EXECUTE);
    return PosixFilePermissions.asFileAttribute(perms);
  }

  class ListBinaryVersionsIterable implements CloseableIterable<BinaryVersion> {

    private final String fedoraPath;
    private final Iterator<String> versionsIterator;

    public ListBinaryVersionsIterable(String fedoraPath, List<String> versions) {
      this.fedoraPath = fedoraPath;
      this.versionsIterator = versions.iterator();
    }

    @Override
    public Iterator<BinaryVersion> iterator() {
      return new Iterator<BinaryVersion>() {

        @Override
        public boolean hasNext() {
          return versionsIterator.hasNext();
        }

        @Override
        public BinaryVersion next() {
          String next = versionsIterator.next();
          String id = next.substring(0, 36);
          String propertiesString = next.substring(36);

          Map<String, String> properties = decodeProperties(propertiesString);

          BinaryVersion ret;
          try {
            FedoraDatastream version = fedoraRepository.getDatastreamVersion(fedoraPath, next);
            ret = FedoraConversionUtils.convertDataStreamToBinaryVersion(version, id, properties);
          } catch (FedoraException | GenericException | RequestNotValidException e) {
            ret = null;
          }

          return ret;
        }

      };
    }

    @Override
    public void close() throws IOException {
      // nothing to do
    }
  }

  @Override
  public CloseableIterable<BinaryVersion> listBinaryVersions(StoragePath storagePath)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {
    try {
      String fedoraPath = FedoraUtils.storagePathToFedoraPath(storagePath);
      FedoraDatastream ds = fedoraRepository.getDatastream(fedoraPath);

      if (!isDatastream(ds)) {
        throw new RequestNotValidException("The resource obtained as being a datastream isn't really a datastream");
      }
      List<String> versions = ds.getVersionsName();
      return new ListBinaryVersionsIterable(fedoraPath, versions);
    } catch (ForbiddenException e) {
      throw new AuthorizationDeniedException(e.getMessage(), e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException(e.getMessage(), e);
    } catch (org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (FedoraException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  @Override
  public BinaryVersion getBinaryVersion(StoragePath storagePath, String version)
    throws RequestNotValidException, NotFoundException, GenericException {
    try {
      FedoraDatastream binary = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));
      String fullVersionID = getFullVersionID(binary, version);
      LOGGER.debug("FULL {}", fullVersionID);
      FedoraDatastream ds = fedoraRepository.getDatastreamVersion(FedoraUtils.storagePathToFedoraPath(storagePath),
        fullVersionID);
      if (!isDatastream(ds)) {
        throw new RequestNotValidException("The resource obtained as being a datastream isn't really a datastream");
      }

      Map<String, String> properties;
      if (fullVersionID != null) {
        String propertiesString = fullVersionID.replace(version, "");
        properties = decodeProperties(propertiesString);
      } else {
        properties = new HashMap<>();
      }
      return FedoraConversionUtils.convertDataStreamToBinaryVersion(ds, version, properties);
    } catch (ForbiddenException | org.fcrepo.client.NotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);
    } catch (BadRequestException e) {
      throw new RequestNotValidException(e.getMessage(), e);
    } catch (FedoraException | GenericException | RequestNotValidException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  private static String encodeProperties(Map<String, String> properties) {
    String propertiesAsJSON = JsonUtils.getJsonFromObject(properties);
    String propertiesAsJsonAsBase64 = new String(Base64.encode(propertiesAsJSON.getBytes()));
    return propertiesAsJsonAsBase64.replace('=', '_');
  }

  private static Map<String, String> decodeProperties(String encodedProperties) {
    String propertiesAsJsonAsBase64 = encodedProperties.replace('_', '=');
    String propertiesAsJSON = new String(Base64.decode(propertiesAsJsonAsBase64.toCharArray()));
    return JsonUtils.getMapFromJson(propertiesAsJSON);
  }

  @Override
  public BinaryVersion createBinaryVersion(StoragePath storagePath, Map<String, String> properties)
    throws RequestNotValidException, GenericException {
    try {
      String id = IdUtils.createUUID();
      FedoraDatastream binary = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));
      String versionID = id + encodeProperties(properties);
      binary.createVersionSnapshot(versionID);
      return FedoraConversionUtils.convertDataStreamToBinaryVersion(binary, id, properties);
    } catch (FedoraException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  @Override
  public void revertBinaryVersion(StoragePath storagePath, String version) throws GenericException {
    try {
      FedoraDatastream binary = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));
      String fullVersionID = getFullVersionID(binary, version);
      binary.revertToVersion(fullVersionID);
    } catch (FedoraException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  @Override
  public void deleteBinaryVersion(StoragePath storagePath, String version) throws GenericException {
    try {
      FedoraDatastream binary = fedoraRepository.getDatastream(FedoraUtils.storagePathToFedoraPath(storagePath));
      String fullVersionID = getFullVersionID(binary, version);
      binary.deleteVersion(fullVersionID);
    } catch (FedoraException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  private String getFullVersionID(FedoraDatastream binary, String shortVersion) {
    LOGGER.debug("Getting full from {}", shortVersion);
    String fullID = null;
    try {
      List<String> versions = binary.getVersionsName();
      if (versions != null) {
        for (String version : versions) {
          LOGGER.debug("V: {}", version);
          if (version.startsWith(shortVersion)) {
            fullID = version;
            break;
          }
        }
      }
    } catch (FedoraException e) {
      LOGGER.error("Error getting full version Id", e);
    }
    return fullID;
  }

  @Override
  public boolean hasDirectory(StoragePath storagePath) {
    try {
      this.getDirectory(storagePath);
      return true;
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      return false;
    }
  }

  @Override
  public boolean hasBinary(StoragePath storagePath) {
    try {
      this.getBinary(storagePath);
      return true;
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      return false;
    }
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    return getStoragePathAsString(storagePath, false);
  }

  @Override
  public String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    return FedoraUtils.storagePathToFedoraPath(storagePath);
  }
}
