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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferredResourcesScanner {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferredResourcesScanner.class);

  private final Path basePath;
  private IndexService index;

  public TransferredResourcesScanner(Path basePath, IndexService index) throws Exception {
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
      TransferredResource parent = index.retrieve(TransferredResource.class, parentUUID);
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
      TransferredResource parent = index.retrieve(TransferredResource.class, parentUUID);
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
    if (!Files.exists(p)) {
      throw new NotFoundException("File not found: " + path);
    } else if (!Files.isRegularFile(p)) {
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
    try {
      Path relativeToBase = basePath.relativize(resourcePath);
      TransferredResource tr = new TransferredResource();

      tr.setFile(!Files.isDirectory(resourcePath));
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

      List<String> ancestors = new ArrayList<String>();

      // FIXME does this have to change ?
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
    } catch (Throwable e) {
      return null;
    }
  }

  public void deleteTransferredResource(List<String> ids)
    throws NotFoundException, GenericException, RequestNotValidException {
    for (String uuid : ids) {
      TransferredResource tr = index.retrieve(TransferredResource.class, uuid);
      Path relative = Paths.get(tr.getRelativePath());
      Path fullPath = basePath.resolve(relative);
      if (Files.exists(fullPath)) {
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

    if (Files.exists(Paths.get(resource.getFullPath()))) {
      Path resourcePath = Paths.get(resource.getFullPath());
      Path newPath = resourcePath.getParent().resolve(newName);
      FSUtils.move(resourcePath, newPath, replaceExisting);

      if (reindexResources) {
        if (resource.getParentUUID() != null) {
          try {
            TransferredResource parent = index.retrieve(TransferredResource.class, resource.getParentUUID());
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

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {
    return moveTransferredResource(resources, newRelativePath, replaceExisting, reindexResources, false);
  }

  public Map<String, String> moveTransferredResource(String newRelativePath, List<String> resourcesUUIDs,
    boolean replaceExisting)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {
    List<TransferredResource> resources = Collections.emptyList();
    try {
      resources = index.retrieve(TransferredResource.class, resourcesUUIDs);
    } catch (NotFoundException e) {
      // do nothing and pass it an empty list
    }
    return moveTransferredResource(resources, newRelativePath, replaceExisting, true, false, true);
  }

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources, boolean areResourcesFromSameFolder)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {
    return moveTransferredResource(resources, newRelativePath, replaceExisting, reindexResources,
      areResourcesFromSameFolder, false);
  }

  public Map<String, String> moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting, boolean reindexResources, boolean areResourcesFromSameFolder,
    boolean addOldRelativePathToNewRelativePath)
    throws AlreadyExistsException, GenericException, IsStillUpdatingException, NotFoundException {

    Map<String, String> oldToNewTransferredResourceIds = new HashMap<String, String>();
    List<TransferredResource> resourcesToIndex = new ArrayList<TransferredResource>();
    boolean notFoundResources = false;

    String baseFolder = RodaCoreFactory.getRodaConfiguration().getString("core.ingest.processed.base_folder",
      "PROCESSED");
    String successFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.successfully_ingested", "SUCCESSFULLY_INGESTED");
    String unsuccessFolder = RodaCoreFactory.getRodaConfiguration()
      .getString("core.ingest.processed.unsuccessfully_ingested", "UNSUCCESSFULLY_INGESTED");

    for (TransferredResource resource : resources) {
      if (Files.exists(Paths.get(resource.getFullPath()))) {
        Path newResourcePath = basePath.resolve(newRelativePath);
        if (addOldRelativePathToNewRelativePath) {
          newResourcePath = newResourcePath.resolve(resource.getRelativePath()
            .replace(baseFolder + "/" + successFolder + "/", "").replace(baseFolder + "/" + unsuccessFolder + "/", ""));
        } else {
          newResourcePath = newResourcePath.resolve(resource.getName());
        }

        FSUtils.move(Paths.get(resource.getFullPath()), newResourcePath, replaceExisting);
        oldToNewTransferredResourceIds.put(resource.getUUID(),
          IdUtils.getTransferredResourceUUID(basePath.relativize(newResourcePath)));
        resourcesToIndex.add(resource);
      } else {
        notFoundResources = true;
      }
    }

    if (reindexResources) {
      updateTransferredResources(Optional.of(newRelativePath), true);
      reindexOldResourcesParentsAfterMove(resourcesToIndex, areResourcesFromSameFolder);
    }

    // doing the throw after the moving process to reindex the moved ones
    if (notFoundResources) {
      throw new NotFoundException("Some transferred resources were moved or do not exist");
    }

    return oldToNewTransferredResourceIds;
  }

  public void reindexOldResourcesParentsAfterMove(List<TransferredResource> resources,
    boolean areResourcesFromSameFolder) throws IsStillUpdatingException, GenericException {

    try {
      List<String> resourceUUIDs = resources.stream().map(tr -> tr.getUUID()).collect(Collectors.toList());
      index.delete(TransferredResource.class, resourceUUIDs);
      index.commit(TransferredResource.class);
    } catch (RequestNotValidException e) {
      LOGGER.error("Could not delete old transferred resources");
    }
  }

}
