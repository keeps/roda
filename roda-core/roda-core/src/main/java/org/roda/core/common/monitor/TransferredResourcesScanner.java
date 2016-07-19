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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
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
    throws GenericException, RequestNotValidException, NotFoundException {
    Path parentPath;
    if (StringUtils.isNotBlank(parentUUID)) {
      TransferredResource parent = index.retrieve(TransferredResource.class, parentUUID);
      parentPath = basePath.resolve(parent.getRelativePath());
    } else {
      parentPath = basePath;
    }

    try {
      try {
        Files.createDirectories(parentPath);
      } catch (FileAlreadyExistsException e) {
        // do nothing and carry on
      }
      Path file = parentPath.resolve(fileName);
      Files.copy(inputStream, file);
      BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
      TransferredResource resource = createTransferredResource(file, attrs, attrs.size(), basePath, new Date());
      index.create(TransferredResource.class, resource);
      return resource;
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
    Path relativeToBase = basePath.relativize(resourcePath);
    TransferredResource tr = new TransferredResource();

    Date d = new Date(attr.creationTime().toMillis());
    tr.setCreationDate(d);

    tr.setFile(!Files.isDirectory(resourcePath));
    tr.setFullPath(resourcePath.toString());
    String id = relativeToBase.toString();
    tr.setId(id);
    tr.setUUID(UUID.nameUUIDFromBytes(id.getBytes()).toString());
    tr.setName(resourcePath.getFileName().toString());

    tr.setRelativePath(relativeToBase.toString());
    if (relativeToBase.getParent() != null) {
      String parentId = relativeToBase.getParent().toString();
      tr.setParentId(parentId);
      tr.setParentUUID(UUID.nameUUIDFromBytes(parentId.getBytes()).toString());
    }
    tr.setSize(size);

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

    tr.setLastScanDate(lastScanDate);

    return tr;
  }

  public void removeTransferredResource(List<String> ids)
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

  public void updateAllTransferredResources(String folderUUID, boolean waitToFinish) throws IsStillUpdatingException {
    if (!RodaCoreFactory.getTransferredResourcesScannerUpdateStatus()) {
      if (index != null) {
        ReindexTransferredResourcesRunnable reindexRunnable;
        reindexRunnable = new ReindexTransferredResourcesRunnable(basePath, folderUUID, index);

        if (waitToFinish) {
          reindexRunnable.run();
        } else {
          Thread threadReindex = new Thread(reindexRunnable, "ReindexThread");
          threadReindex.start();
        }
      }
    } else {
      throw new IsStillUpdatingException();
    }
  }

  public void renameTransferredResource(TransferredResource resource, String newName, boolean replaceExisting)
    throws AlreadyExistsException, GenericException {
    Path resourcePath = Paths.get(resource.getFullPath());
    FSUtils.move(resourcePath, resourcePath.getParent().resolve(newName), replaceExisting);
  }

  public void moveTransferredResource(List<TransferredResource> resources, String newRelativePath,
    boolean replaceExisting) throws AlreadyExistsException, GenericException {
    for (TransferredResource resource : resources) {
      FSUtils.move(Paths.get(resource.getFullPath()), basePath.resolve(newRelativePath).resolve(resource.getName()),
        replaceExisting);
    }
  }
}
