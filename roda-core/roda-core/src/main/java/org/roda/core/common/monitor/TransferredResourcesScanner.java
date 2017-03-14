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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.model.LiteRODAObjectFactory;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourcesScanner {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourcesScanner.class);
  private static final List<String> fieldsToReturn = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.TRANSFERRED_RESOURCE_RELATIVEPATH);

  private final Path basePath;
  private IndexService index;

  public TransferredResourcesScanner(Path basePath, IndexService index) {
    this.basePath = basePath;
    this.index = index;
  }

  public void commit() throws GenericException {
    index.commit(TransferredResource.class);
  }

  public Path getBasePath() {
    return basePath;
  }

  public TransferredResource createFolder(String parentUUID, String folderName)
    throws GenericException, RequestNotValidException, NotFoundException {
    Path parentPath;
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
    throws GenericException, RequestNotValidException, NotFoundException, AlreadyExistsException {
    Path parentPath;
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
      tr.setParentUUID(UUID.nameUUIDFromBytes(parentId.getBytes()).toString());
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
    throws NotFoundException, GenericException, RequestNotValidException {
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

  public void updateTransferredResources(Optional<String> folderRelativePath, boolean waitToFinish)
    throws IsStillUpdatingException, GenericException {
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
  }

  public void updateTransferredResource(Optional<String> folderRelativePath, ContentPayload payload, String name,
    boolean waitToFinish) throws NotFoundException, GenericException, IOException, IsStillUpdatingException {
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
    boolean reindexResources)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {

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
      return UUID.nameUUIDFromBytes(relativeToBase.toString().getBytes()).toString();
    } else {
      throw new NotFoundException("Transferred resource was moved or does not exist");
    }
  }

  public Map<String, String> moveTransferredResource(String newRelativePath, List<String> resourcesUUIDs,
    boolean replaceExisting)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {
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
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {
    return moveTransferredResource(resources, newRelativePath, replaceExisting, reindexResources, false);
  }

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources, boolean addOldRelativePathToNewRelativePath)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {

    Map<String, String> oldToNewTransferredResourceIds = new HashMap<>();
    List<TransferredResource> resourcesToIndex = new ArrayList<>();
    boolean notFoundResources = false;

    String baseFolder = RodaCoreFactory.getRodaConfiguration().getString("core.ingest.processed.base_folder",
      "PROCESSED");
    String successFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.successfully_ingested", "SUCCESSFULLY_INGESTED");
    String unsuccessFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.unsuccessfully_ingested", "UNSUCCESSFULLY_INGESTED");

    for (TransferredResource resource : resources) {
      if (FSUtils.exists(Paths.get(resource.getFullPath()))) {
        Path newResourcePath = basePath.resolve(newRelativePath);
        if (addOldRelativePathToNewRelativePath) {
          newResourcePath = newResourcePath.resolve(resource.getRelativePath()
            .replace(baseFolder + "/" + successFolder + "/", "").replace(baseFolder + "/" + unsuccessFolder + "/", ""));
        } else {
          newResourcePath = newResourcePath.resolve(resource.getName());
        }

        FSUtils.move(Paths.get(resource.getFullPath()), newResourcePath, replaceExisting);

        // create & index transferred resource in the new location
        TransferredResource newResource = instantiateTransferredResource(newResourcePath, basePath);
        Date creationDate = resource.getCreationDate();
        try {
          BasicFileAttributes attr = Files.readAttributes(newResourcePath, BasicFileAttributes.class);
          creationDate = new Date(attr.creationTime().toMillis());
        } catch (IOException e) {
          creationDate = new Date();
        }

        newResource.setCreationDate(creationDate);
        newResource.setSize(resource.getSize());
        newResource.setLastScanDate(new Date());
        try {
          index.create(TransferredResource.class, newResource);
        } catch (RequestNotValidException e) {
          // do nothing
        }

        oldToNewTransferredResourceIds.put(resource.getUUID(), newResource.getUUID());
        resourcesToIndex.add(resource);
      } else {
        notFoundResources = true;
      }
    }

    if (reindexResources) {
      updateTransferredResources(Optional.of(newRelativePath), true);
    }
    reindexOldResourcesParentsAfterMove(resourcesToIndex);

    // doing the throw after the moving process to reindex the moved ones
    if (notFoundResources) {
      throw new NotFoundException("Some transferred resources were moved or do not exist");
    }

    return oldToNewTransferredResourceIds;
  }

  public void reindexOldResourcesParentsAfterMove(List<TransferredResource> resources)
    throws IsStillUpdatingException, GenericException {

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
        public void close() throws IOException {
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
