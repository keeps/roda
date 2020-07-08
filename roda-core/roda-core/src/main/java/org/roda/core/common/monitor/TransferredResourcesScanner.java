/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.exceptions.*;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourcesScanner {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourcesScanner.class);
  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH);

  private final Path basePath;
  private IndexService index;
  private NodeType nodeType;

  public TransferredResourcesScanner(Path basePath, IndexService index, NodeType nodeType) {
    this.basePath = basePath;
    this.index = index;
    this.nodeType = nodeType;
  }

  public void commit() throws GenericException, AuthorizationDeniedException {
    index.commit(TransferredResource.class);
  }

  public Path getBasePath() {
    return basePath;
  }

  public TransferredResource createFolder(String parentUUID, String folderName)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    Path parentPath;

    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (parentUUID != null) {
      TransferredResource parent = index.retrieve(TransferredResource.class, parentUUID, fieldsToReturn);
      parentPath = basePath.resolve(parent.getRelativePath());
    } else {
      parentPath = basePath;
    }

    try {
      Path createdPath = Files.createDirectories(parentPath.resolve(folderName));
      BasicFileAttributes attrs = Files.readAttributes(createdPath, BasicFileAttributes.class);
      TransferredResource resource = createTransferredResource(createdPath, attrs, 0L, basePath, new Date());
      index.create(TransferredResource.class, resource);
      return resource;
    } catch (IOException e) {
      LOGGER.error("Cannot create folder", e);
      throw new GenericException("Cannot create folder", e);
    }
  }

  public TransferredResource createFile(String parentUUID, String fileName, InputStream inputStream)
    throws GenericException, NotFoundException, AlreadyExistsException, AuthorizationDeniedException {
    Path parentPath;

    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (StringUtils.isNotBlank(parentUUID)) {
      TransferredResource parent = index.retrieve(TransferredResource.class, parentUUID, fieldsToReturn);
      parentPath = basePath.resolve(parent.getRelativePath());
    } else {
      parentPath = basePath;
    }

    Path file = parentPath.resolve(fileName);
    try {
      try {
        Files.createDirectories(parentPath);
      } catch (FileAlreadyExistsException e) {
        // do nothing and carry on
      }

      Files.copy(inputStream, file);
      BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
      TransferredResource resource = createTransferredResource(file, attrs, attrs.size(), basePath, new Date());
      index.create(TransferredResource.class, resource);
      return resource;
    } catch (FileAlreadyExistsException e) {
      LOGGER.error("Cannot create file", e);
      throw new AlreadyExistsException(file.toString());
    } catch (IOException e) {
      LOGGER.error("Cannot create file", e);
      throw new GenericException("Cannot create file", e);
    }
  }

  public InputStream retrieveFile(String path) throws NotFoundException, RequestNotValidException, GenericException {
    InputStream ret;
    Path p = basePath.resolve(path);
    if (!FSUtils.exists(p)) {
      throw new NotFoundException("File not found: " + path);
    } else if (!FSUtils.isFile(p)) {
      throw new RequestNotValidException("Requested file is not a regular file: " + path);
    } else {
      try {
        ret = Files.newInputStream(p);
      } catch (IOException e) {
        throw new GenericException("Could not create input stream: " + e.getMessage());
      }
    }
    return ret;
  }

  public Path retrieveFilePath(String path) throws NotFoundException, RequestNotValidException, GenericException {
    Path p = basePath.resolve(path);
    if (!FSUtils.exists(p)) {
      throw new NotFoundException("File not found: " + path);
    } else if (!FSUtils.isFile(p)) {
      throw new RequestNotValidException("Requested file is not a regular file: " + path);
    }
    return p;
  }

  public boolean fileExists(String path) {
    Path p = basePath.resolve(path);
    return FSUtils.exists(p);
  }

  protected static TransferredResource createTransferredResource(Path resourcePath, BasicFileAttributes attr, long size,
    Path basePath, Date lastScanDate) {
    Date d = new Date(attr.creationTime().toMillis());

    TransferredResource tr = instantiateTransferredResource(resourcePath, basePath);
    tr.setSize(size);
    tr.setCreationDate(d);
    tr.setLastScanDate(lastScanDate);
    return tr;
  }

  public static TransferredResource instantiateTransferredResource(Path resourcePath, Path basePath) {
    Path relativeToBase = basePath.relativize(resourcePath);
    TransferredResource tr = new TransferredResource();

    tr.setFile(!FSUtils.isDirectory(resourcePath));
    tr.setFullPath(resourcePath.toString());
    String id = relativeToBase.toString();
    tr.setId(id);
    tr.setUUID(IdUtils.getTransferredResourceUUID(relativeToBase));
    tr.setName(resourcePath.getFileName().toString());

    tr.setRelativePath(relativeToBase.toString());
    if (relativeToBase.getParent() != null) {
      String parentId = relativeToBase.getParent().toString();
      tr.setParentId(parentId);
      tr.setParentUUID(IdUtils.createUUID(parentId));
    }

    List<String> ancestors = new ArrayList<>();

    StringBuilder temp = new StringBuilder();
    Iterator<Path> pathIterator = relativeToBase.iterator();
    while (pathIterator.hasNext()) {
      temp.append(pathIterator.next().toString());
      ancestors.add(temp.toString());
      temp.append("/");
    }
    ancestors.remove(ancestors.size() - 1);
    tr.setAncestorsPaths(ancestors);

    return tr;
  }

  public void deleteTransferredResource(List<String> ids)
    throws NotFoundException, GenericException, RequestNotValidException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    for (String uuid : ids) {
      TransferredResource tr = index.retrieve(TransferredResource.class, uuid, fieldsToReturn);
      Path relative = Paths.get(tr.getRelativePath());
      Path fullPath = basePath.resolve(relative);
      if (FSUtils.exists(fullPath)) {
        FSUtils.deletePath(fullPath);

        Filter filter = new Filter(
          new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ANCESTORS, relative.toString()));
        index.delete(TransferredResource.class, filter);
      } else {
        throw new NotFoundException("Path does not exist: " + fullPath);
      }
    }

    index.delete(TransferredResource.class, ids);
    index.commit(TransferredResource.class);
  }

  public Optional<String> updateTransferredResources(Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    boolean isWithin;
    Path resolvedBasePath;
    Optional<String> normalizedRelativePath;
    if(folderRelativePath.isPresent() && !folderRelativePath.get().isEmpty()) {
      resolvedBasePath = basePath.resolve(Paths.get(folderRelativePath.get())).normalize();
      isWithin = RodaCoreFactory.checkPathIsWithin(resolvedBasePath, basePath);
      if(resolvedBasePath.equals(basePath)){
        normalizedRelativePath = Optional.empty();
      }else{
        normalizedRelativePath = Optional.of(basePath.relativize(resolvedBasePath).toString());
      }
    }else{
      isWithin = true;
      normalizedRelativePath = Optional.empty();
    }
    if (isWithin) {
      if (!RodaCoreFactory.getTransferredResourcesScannerUpdateStatus(folderRelativePath)) {
        if (index != null) {
          ReindexTransferredResourcesRunnable reindexRunnable = new ReindexTransferredResourcesRunnable(index, basePath,
                  folderRelativePath);

          if (waitToFinish) {
            reindexRunnable.run();
          } else {
            Thread threadReindex = new Thread(reindexRunnable, "ReindexThread");
            threadReindex.start();
          }
        } else {
          throw new GenericException("Could not update transferred resources because index was not initialized");
        }
      } else {
        LOGGER.warn("Could not update transferred resources because it is still updating");
        throw new IsStillUpdatingException();
      }
    }else{
        LOGGER.warn("Request trying to access folders outside the transfer resources folder ({})", folderRelativePath.get());
        throw new AuthorizationDeniedException("Request trying to access folders outside the transfer resources folder (" + folderRelativePath.get() + ")");
    }
    return normalizedRelativePath;
  }

  public void updateTransferredResource(Optional<String> folderRelativePath, ContentPayload payload, String name,
    boolean waitToFinish)
    throws NotFoundException, GenericException, IOException, IsStillUpdatingException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (folderRelativePath.isPresent()) {
      Path path = basePath.resolve(folderRelativePath.get());
      Path parent = path.getParent();
      Path parentToBase = basePath.relativize(parent);
      FSUtils.deletePath(path);

      payload.writeToPath(parent.resolve(name));
      updateTransferredResources(Optional.ofNullable(parentToBase.toString()), waitToFinish);
    }
  }

  public String renameTransferredResource(TransferredResource resource, String newName, boolean replaceExisting,
    boolean reindexResources) throws AlreadyExistsException, GenericException, IsStillUpdatingException,
    NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    if (FSUtils.exists(Paths.get(resource.getFullPath()))) {
      Path resourcePath = Paths.get(resource.getFullPath());
      Path newPath = resourcePath.getParent().resolve(newName);
      FSUtils.move(resourcePath, newPath, replaceExisting);

      if (reindexResources) {
        if (resource.getParentUUID() != null) {
          try {
            TransferredResource parent = index.retrieve(TransferredResource.class, resource.getParentUUID(),
              fieldsToReturn);
            if (parent != null) {
              updateTransferredResources(Optional.of(parent.getRelativePath()), true);
            } else {
              updateTransferredResources(Optional.empty(), true);
            }
          } catch (GenericException | NotFoundException e) {
            LOGGER.error("Could not reindex transferred resources after renaming");
          }
        } else {
          updateTransferredResources(Optional.empty(), true);
        }
      }

      Path relativeToBase = basePath.relativize(resourcePath.getParent().resolve(newName));
      return IdUtils.createUUID(relativeToBase.toString());
    } else {
      throw new NotFoundException("Transferred resource was moved or does not exist");
    }
  }

  public Map<String, String> moveTransferredResource(String newRelativePath, List<String> resourcesUUIDs,
    boolean replaceExisting)
    throws GenericException, IsStillUpdatingException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    List<TransferredResource> resources = Collections.emptyList();
    try {
      List<String> resourceFields = Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.TRANSFERRED_RESOURCE_FULLPATH,
        RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH, RodaConstants.TRANSFERRED_RESOURCE_NAME);
      resources = index.retrieve(TransferredResource.class, resourcesUUIDs, resourceFields);
    } catch (NotFoundException e) {
      // do nothing and pass it an empty list
    }
    return moveTransferredResource(resources, newRelativePath, replaceExisting, false, true);
  }

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources)
    throws GenericException, IsStillUpdatingException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    return moveTransferredResource(resources, newRelativePath, replaceExisting, reindexResources, false);
  }

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources, boolean addOldRelativePathToNewRelativePath)
    throws IsStillUpdatingException, GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    Map<String, String> oldToNewTransferredResourceIds = new HashMap<>();
    List<TransferredResource> resourcesToIndex = new ArrayList<>();
    boolean notFoundResources = false;

    String baseFolder = RodaCoreFactory.getRodaConfiguration().getString("core.ingest.processed.base_folder",
      RodaConstants.TRANSFERRED_RESOURCES_PROCESSED_FOLDER);
    String successFolder = RodaCoreFactory.getRodaConfiguration().getString(
      "core.ingest.processed.successfully_ingested", RodaConstants.TRANSFERRED_RESOURCES_SUCCESSFULLY_INGESTED_FOLDER);
    String unsuccessFolder = RodaCoreFactory.getRodaConfiguration().getString(
      "core.ingest.processed.unsuccessfully_ingested",
      RodaConstants.TRANSFERRED_RESOURCES_UNSUCCESSFULLY_INGESTED_FOLDER);

    for (TransferredResource resource : resources) {
      try {
        if (FSUtils.exists(Paths.get(resource.getFullPath()))) {
          Path newResourcePath = basePath.resolve(newRelativePath);
          if (addOldRelativePathToNewRelativePath) {
            newResourcePath = newResourcePath
              .resolve(resource.getRelativePath().replace(baseFolder + "/" + successFolder + "/", "")
                .replace(baseFolder + "/" + unsuccessFolder + "/", ""));
          } else {
            newResourcePath = newResourcePath.resolve(resource.getName());
          }

          FSUtils.move(Paths.get(resource.getFullPath()), newResourcePath, replaceExisting);

          // create & index transferred resource in the new location
          TransferredResource newResource = instantiateTransferredResource(newResourcePath, basePath);

          try {
            BasicFileAttributes attr = Files.readAttributes(newResourcePath, BasicFileAttributes.class);
            newResource.setCreationDate(new Date(attr.creationTime().toMillis()));
          } catch (IOException e) {
            newResource.setCreationDate(new Date());
          }

          newResource.setSize(resource.getSize());
          newResource.setLastScanDate(new Date());
          index.create(TransferredResource.class, newResource);

          oldToNewTransferredResourceIds.put(resource.getUUID(), newResource.getUUID());
          resourcesToIndex.add(resource);
        } else {
          notFoundResources = true;
        }
      } catch (Exception e) {
        // do nothing
      }
    }

    if (reindexResources) {
      updateTransferredResources(Optional.of(newRelativePath), true);
    }

    if (!resourcesToIndex.isEmpty()) {
      reindexOldResourcesParentsAfterMove(resourcesToIndex);
    }

    // doing the throw after the moving process to reindex the moved ones
    if (notFoundResources) {
      throw new NotFoundException("Some transferred resources were moved or do not exist");
    }

    return oldToNewTransferredResourceIds;
  }

  public void reindexOldResourcesParentsAfterMove(List<TransferredResource> resources)
    throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);

    try {
      List<String> resourceUUIDs = resources.stream().map(tr -> tr.getUUID()).collect(Collectors.toList());
      index.delete(TransferredResource.class, resourceUUIDs);
    } catch (RequestNotValidException e) {
      LOGGER.error("Could not delete old transferred resources");
    }
  }

  public CloseableIterable<OptionalWithCause<LiteRODAObject>> listTransferredResources() {
    CloseableIterable<OptionalWithCause<LiteRODAObject>> resources = null;

    try {
      final Stream<Path> files = Files.walk(basePath, FileVisitOption.FOLLOW_LINKS)
        .filter(path -> !path.equals(basePath));
      final Iterator<Path> fileIterator = files.iterator();

      resources = new CloseableIterable<OptionalWithCause<LiteRODAObject>>() {
        @Override
        public void close() {
          files.close();
        }

        @Override
        public Iterator<OptionalWithCause<LiteRODAObject>> iterator() {

          return new Iterator<OptionalWithCause<LiteRODAObject>>() {
            @Override
            public boolean hasNext() {
              return fileIterator.hasNext();
            }

            @Override
            public OptionalWithCause<LiteRODAObject> next() {
              Path file = fileIterator.next();
              Optional<LiteRODAObject> liteResource = LiteRODAObjectFactory.get(TransferredResource.class,
                Arrays.asList(file.toString()), false);
              return OptionalWithCause.of(liteResource);
            }
          };
        }
      };
    } catch (IOException e) {
      LOGGER.error("Errored when file walking to list transferred resources");
    }

    return resources;
  }
}
