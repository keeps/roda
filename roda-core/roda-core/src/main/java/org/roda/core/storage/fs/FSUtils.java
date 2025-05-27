/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.ip.ShallowFile;
import org.roda.core.data.v2.ip.ShallowFiles;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.protocols.Protocol;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultBinary;
import org.roda.core.storage.DefaultBinaryVersion;
import org.roda.core.storage.DefaultContainer;
import org.roda.core.storage.DefaultDirectory;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.DirectResourceAccess;
import org.roda.core.storage.ExternalFileManifestContentPayload;
import org.roda.core.storage.InputStreamContentPayload;
import org.roda.core.storage.JsonContentPayload;
import org.roda.core.storage.Resource;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File System related utility class
 *
 * @author Luis Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class FSUtils {

  public static final Logger LOGGER = LoggerFactory.getLogger(FSUtils.class);
  public static final char VERSION_SEP = '_';
  public static final String METADATA_SUFFIX = ".json";
  public static final String SEPARATOR = "/";
  public static final String SEPARATOR_REGEX = "/";
  public static final String SEPARATOR_REPLACEMENT = "%2F";

  public FSUtils() {
    // do nothing
  }

  /**
   * Method that safely updates a file, given an inputstream, by copying the
   * content of the stream to a temporary file which then gets moved into the
   * final location (doing an atomic move). </br>
   * </br>
   * In theory (as it depends on the file system implementation), this method is
   * useful for ensuring thread safety. </br>
   * </br>
   * NOTE: the stream is closed in the end.
   *
   * @param stream
   *          stream with the content to be updated
   * @param toPath
   *          location of the file being updated
   *
   * @throws IOException
   *           if an error occurs while copying/moving
   *
   */
  public static void safeUpdate(InputStream stream, Path toPath) throws IOException {
    try {
      Files.createDirectories(toPath.getParent());

      Path tempToPath = toPath.getParent().resolve(toPath.getFileName().toString() + ".temp" + System.nanoTime());
      Files.copy(stream, tempToPath);
      Files.move(tempToPath, toPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  /**
   * Moves a directory/file from one path to another
   *
   * @param sourcePath
   *          source path
   * @param targetPath
   *          target path
   * @param replaceExisting
   *          true if the target directory/file should be replaced if it already
   *          exists; false otherwise
   * @throws AlreadyExistsException
   * @throws GenericException
   * @throws NotFoundException
   *
   */
  public static void move(final Path sourcePath, final Path targetPath, boolean replaceExisting)
    throws AlreadyExistsException, GenericException, NotFoundException {

    // check if we can replace existing
    if (!replaceExisting && FSUtils.exists(targetPath)) {
      throw new AlreadyExistsException("Cannot copy because target path already exists: " + targetPath);
    }

    // ensure parent directory exists or can be created
    try {
      if (targetPath != null) {
        Files.createDirectories(targetPath.getParent());
      }
    } catch (FileAlreadyExistsException e) {
      // do nothing
    } catch (IOException e) {
      throw new GenericException("Error while creating target directory parent folder", e);
    }

    CopyOption[] copyOptions = replaceExisting ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING}
      : new CopyOption[] {};

    if (FSUtils.isDirectory(sourcePath)) {
      try {
        Files.move(sourcePath, targetPath, copyOptions);
      } catch (DirectoryNotEmptyException e) {
        // 20160826 hsilva: this might happen in some filesystems (e.g. XFS)
        // because moving a directory implies also moving its entries. In Ext4
        // this doesn't happen.
        LOGGER.debug("Moving recursively, as a fallback & instead of a simple move, from {} to {}", sourcePath,
          targetPath);
        moveRecursively(sourcePath, targetPath, replaceExisting);
      } catch (IOException e) {
        throw new GenericException("Error while moving directory from " + sourcePath + " to " + targetPath, e);
      }
    } else {
      try {
        Files.move(sourcePath, targetPath, copyOptions);
      } catch (NoSuchFileException e) {
        throw new NotFoundException("Could not find resource to move", e);
      } catch (IOException e) {
        throw new GenericException("Error while copying one file into another", e);
      }
    }
  }

  public static void moveRecursively(final Path sourcePath, final Path targetPath, final boolean replaceExisting)
    throws GenericException {
    final CopyOption[] copyOptions = replaceExisting ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING}
      : new CopyOption[] {};

    try {
      Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path sourceDir, BasicFileAttributes attrs) throws IOException {
          Path targetDir = targetPath.resolve(sourcePath.relativize(sourceDir));
          LOGGER.trace("Creating target directory {}", targetDir);
          Files.createDirectories(targetDir);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
          Path targetFile = targetPath.resolve(sourcePath.relativize(sourceFile));
          LOGGER.trace("Moving file from {} to {}", sourceFile, targetFile);
          Files.move(sourceFile, targetFile, copyOptions);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path sourceFile, IOException exc) throws IOException {
          LOGGER.trace("Deleting source directory {}", sourceFile);
          Files.delete(sourceFile);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      throw new GenericException("Error while moving (recursively) directory from " + sourcePath + " to " + targetPath,
        e);
    }

  }

  /**
   * Copies a directory/file from one path to another
   *
   * @param sourcePath
   *          source path
   * @param targetPath
   *          target path
   * @param replaceExisting
   *          true if the target directory/file should be replaced if it already
   *          exists; false otherwise
   * @throws AlreadyExistsException
   * @throws GenericException
   */
  public static void copy(final Path sourcePath, final Path targetPath, boolean replaceExisting)
    throws AlreadyExistsException, GenericException {

    // check if we can replace existing
    if (!replaceExisting && FSUtils.exists(targetPath)) {
      throw new AlreadyExistsException("Cannot copy because target path already exists: " + targetPath);
    }

    // ensure parent directory exists or can be created
    try {
      if (targetPath != null) {
        Files.createDirectories(targetPath.getParent());
      }
    } catch (IOException e) {
      throw new GenericException("Error while creating target directory parent folder", e);
    }

    if (FSUtils.isDirectory(sourcePath)) {
      try {
        Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        throw new GenericException("Error while copying one directory into another", e);
      }
    } else {
      try {
        CopyOption[] copyOptions = replaceExisting ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING}
          : new CopyOption[] {};
        Files.copy(sourcePath, targetPath, copyOptions);
      } catch (IOException e) {
        throw new GenericException("Error while copying one file into another", e);
      }
    }
  }

  public static void deletePathQuietly(Path path) {
    try {
      deletePath(path);
    } catch (NotFoundException | GenericException e) {
      // do nothing
    }
  }

  /**
   * Deletes a directory/file
   *
   * @param path
   *          path to the directory/file that will be deleted. in case of a
   *          directory, if not empty, everything in it will be deleted as well.
   *          in case of a file, if metadata associated to it exists, it will be
   *          deleted as well.
   * @throws NotFoundException
   * @throws GenericException
   */
  public static void deletePath(Path path) throws NotFoundException, GenericException {
    if (path == null) {
      return;
    }

    try {
      Files.delete(path);

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not delete path", e);
    } catch (DirectoryNotEmptyException e) {
      try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
          }

        });
      } catch (IOException e1) {
        throw new GenericException("Could not delete entity", e1);
      }
    } catch (IOException e) {
      throw new GenericException("Could not delete entity", e);
    }
  }

  public static String encodePathPartial(String pathPartial) {
    return pathPartial.replaceAll(SEPARATOR_REGEX, SEPARATOR_REPLACEMENT);
  }

  public static String decodePathPartial(String pathPartial) {
    return pathPartial.replaceAll(SEPARATOR_REPLACEMENT, SEPARATOR);
  }

  /**
   * Get path
   *
   * @param basePath
   *          base path
   * @param storagePath
   *          storage path, related to base path, that one wants to resolve
   */
  public static Path getEntityPath(Path basePath, StoragePath storagePath) {
    Path resourcePath = basePath;

    for (String pathPartial : storagePath.asList()) {
      resourcePath = resourcePath.resolve(encodePathPartial(pathPartial));
    }

    return resourcePath;
  }

  public static Path getEntityPath(Path basePath, StoragePath storagePath, String version)
    throws RequestNotValidException {
    if (version.indexOf(VERSION_SEP) >= 0) {
      throw new RequestNotValidException("Cannot use '" + VERSION_SEP + "' in version " + version);
    }

    Path resourcePath = basePath;

    for (Iterator<String> iterator = storagePath.asList().iterator(); iterator.hasNext();) {
      String pathPartial = iterator.next();
      if (!iterator.hasNext()) {
        pathPartial += VERSION_SEP + version;
      }
      resourcePath = resourcePath.resolve(encodePathPartial(pathPartial));
    }

    return resourcePath;

  }

  public static StoragePath getStoragePath(Path basePath, Path absolutePath) throws RequestNotValidException {
    return getStoragePath(basePath.relativize(absolutePath));
  }

  public static StoragePath getStoragePath(Path relativePath) throws RequestNotValidException {
    List<String> pathPartials = new ArrayList<>();

    for (int i = 0; i < relativePath.getNameCount(); i++) {
      String pathPartial = relativePath.getName(i).toString();
      pathPartials.add(decodePathPartial(pathPartial));
    }

    return DefaultStoragePath.parse(pathPartials);
  }

  public static String getStoragePathAsString(StoragePath storagePath, boolean skipStoragePathContainer,
    StoragePath anotherStoragePath, boolean skipAnotherStoragePathContainer) {
    return storagePath.asString(SEPARATOR, SEPARATOR_REGEX, SEPARATOR_REPLACEMENT, skipStoragePathContainer) + SEPARATOR
      + anotherStoragePath.asString(SEPARATOR, SEPARATOR_REGEX, SEPARATOR_REPLACEMENT, skipAnotherStoragePathContainer);
  }

  public static String getStoragePathAsString(StoragePath storagePath, boolean skipContainer) {
    return storagePath.asString(SEPARATOR, SEPARATOR_REGEX, SEPARATOR_REPLACEMENT, skipContainer);
  }

  /**
   * Lists direct access resources under a direct access resource
   * 
   * @param resource
   * @param recursive
   * @return
   * @throws RequestNotValidException
   * @throws AuthorizationDeniedException
   * @throws NotFoundException
   * @throws GenericException
   * @throws IOException
   */
  public static CloseableIterable<DirectResourceAccess> listDirectAccessResourceChildren(DirectResourceAccess resource,
    boolean recursive)
    throws RequestNotValidException, AuthorizationDeniedException, NotFoundException, GenericException, IOException {
    final CloseableIterable<Path> listPaths = recursive ? FSUtils.recursivelyListPaths(resource.getPath())
      : FSUtils.listPaths(resource.getPath());
    try {
      return new CloseableIterable<DirectResourceAccess>() {
        @Override
        public void close() throws IOException {
          listPaths.close();
        }

        @Override
        public Iterator<DirectResourceAccess> iterator() {
          return new Iterator<DirectResourceAccess>() {
            @Override
            public boolean hasNext() {
              return listPaths.iterator().hasNext();
            }

            @Override
            public DirectResourceAccess next() {
              Path next = listPaths.iterator().next();
              return new DirectResourceAccess() {
                @Override
                public Path getPath()
                  throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
                  return next;
                }

                @Override
                public boolean isDirectory() {
                  return Files.isDirectory(next);
                }

                @Override
                public void close() {
                  // nothing to do
                }
              };
            }
          };
        }
      };
    } finally {
      if (listPaths != null) {
        listPaths.close();
      }
    }
  }

  public static DirectResourceAccess resolve(DirectResourceAccess directResourceAccess, String... pathPartials)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Path resolved = directResourceAccess.getPath();
    for (String pathPartial : pathPartials) {
      resolved = resolved.resolve(pathPartial);
    }
    final Path path = resolved;
    return new DirectResourceAccess() {
      @Override
      public Path getPath()
        throws GenericException, RequestNotValidException, AuthorizationDeniedException, NotFoundException {
        return path;
      }

      @Override
      public boolean isDirectory() {
        return Files.isDirectory(path);
      }

      @Override
      public void close() throws IOException {
        // nothing to do
      }
    };
  }

  /**
   * List absolute paths inside certain path
   *
   * @param path
   *          path to list paths under
   * @throws NotFoundException
   * @throws GenericException
   */
  public static CloseableIterable<Path> listPaths(final Path path) throws NotFoundException, GenericException {
    CloseableIterable<Path> pathIterable;
    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
      final Iterator<Path> pathIterator = directoryStream.iterator();
      pathIterable = new CloseableIterable<Path>() {

        @Override
        public Iterator<Path> iterator() {
          return new Iterator<Path>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public Path next() {
              return pathIterator.next();
            }

          };
        }

        @Override
        public void close() throws IOException {
          directoryStream.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + path, e);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + path, e);
    }

    return pathIterable;
  }

  /**
   * List content of the certain folder
   *
   * @param basePath
   *          base path
   * @param path
   *          relative path to base path
   * @throws NotFoundException
   * @throws GenericException
   */
  public static CloseableIterable<Resource> listPathResources(final Path basePath, final Path path)
    throws NotFoundException, GenericException {
    CloseableIterable<Resource> resourceIterable;
    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
      final Iterator<Path> pathIterator = directoryStream.iterator();
      resourceIterable = new CloseableIterable<Resource>() {

        @Override
        public Iterator<Resource> iterator() {
          return new Iterator<Resource>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public Resource next() {
              Path next = pathIterator.next();
              Resource ret;
              try {
                ret = convertPathToResource(basePath, next);
              } catch (GenericException | NotFoundException | RequestNotValidException e) {
                LOGGER.error("Error while list path " + basePath + " while parsing resource " + next, e);
                ret = null;
              }

              return ret;
            }

          };
        }

        @Override
        public void close() throws IOException {
          directoryStream.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + path, e);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + path, e);
    }

    return resourceIterable;
  }

  public static CloseableIterable<Resource> listPathUnderFile(final Path basePath, final Path path)
    throws NotFoundException, GenericException {
    CloseableIterable<Resource> resourceIterable;
    try {
      LineIterator lineIterator = FileUtils.lineIterator(path.toFile());
      resourceIterable = new CloseableIterable<Resource>() {

        @Override
        public Iterator<Resource> iterator() {
          return new Iterator<Resource>() {

            @Override
            public boolean hasNext() {
              return lineIterator.hasNext();
            }

            @Override
            public Resource next() {
              String json = lineIterator.next();
              Resource ret;
              try {
                JsonContentPayload content = new JsonContentPayload(json);
                StoragePath storagePath = getStoragePath(basePath, path);
                ret = new DefaultBinary(storagePath, content, 0L, true, new HashMap<>());
              } catch (RequestNotValidException e) {
                LOGGER.error("Error while list path " + basePath + " while parsing resource " + json, e);
                ret = null;
              }

              return ret;
            }

          };
        }

        @Override
        public void close() throws IOException {
          lineIterator.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + path, e);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + path, e);
    }

    return resourceIterable;
  }

  public static Long countPath(Path directoryPath) throws NotFoundException, GenericException {
    Long count = 0L;

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
      final Iterator<Path> pathIterator = directoryStream.iterator();
      while (pathIterator.hasNext()) {
        count++;
        pathIterator.next();
      }
    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + directoryPath);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + directoryPath, e);
    }

    return count;
  }

  public static Long recursivelyCountPath(Path directoryPath) throws NotFoundException, GenericException {
    // starting at -1 because the walk counts the directory itself
    Long count = -1L;
    try (Stream<Path> walk = Files.walk(directoryPath)) {
      final Iterator<Path> pathIterator = walk.iterator();
      while (pathIterator.hasNext()) {
        count++;
        pathIterator.next();
      }
    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + directoryPath);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + directoryPath, e);
    }

    return count;
  }

  public static CloseableIterable<Path> recursivelyListPaths(final Path path)
    throws NotFoundException, GenericException {
    CloseableIterable<Path> pathIterable;
    try {
      final Stream<Path> walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
      final Iterator<Path> pathIterator = walk.iterator();

      // skip root
      if (pathIterator.hasNext()) {
        pathIterator.next();
      }

      pathIterable = new CloseableIterable<Path>() {

        @Override
        public Iterator<Path> iterator() {
          return new Iterator<Path>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public Path next() {
              return pathIterator.next();
            }

          };
        }

        @Override
        public void close() {
          walk.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + path, e);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + path, e);
    }

    return pathIterable;
  }

  public static CloseableIterable<Resource> recursivelyListPathResources(final Path basePath, final Path path)
    throws NotFoundException, GenericException {
    CloseableIterable<Resource> resourceIterable;
    try {
      final Stream<Path> walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
      final Iterator<Path> pathIterator = walk.iterator();

      // skip root
      if (pathIterator.hasNext()) {
        pathIterator.next();
      }

      resourceIterable = new CloseableIterable<Resource>() {

        @Override
        public Iterator<Resource> iterator() {
          return new Iterator<Resource>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public Resource next() {
              Path next = pathIterator.next();
              Resource ret;
              try {
                ret = convertPathToResource(basePath, next);
              } catch (GenericException | NotFoundException | RequestNotValidException e) {
                LOGGER.error("Error while list path " + basePath + " while parsing resource " + next, e);
                ret = null;
              }

              return ret;
            }

          };
        }

        @Override
        public void close() {
          walk.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not list contents of entity because it doesn't exist: " + path, e);
    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + path, e);
    }

    return resourceIterable;
  }

  /**
   * List containers
   *
   * @param basePath
   *          base path
   * @throws GenericException
   */
  public static CloseableIterable<Container> listContainers(final Path basePath) throws GenericException {
    CloseableIterable<Container> containerIterable;
    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(basePath);
      final Iterator<Path> pathIterator = directoryStream.iterator();
      containerIterable = new CloseableIterable<Container>() {

        @Override
        public Iterator<Container> iterator() {
          return new Iterator<Container>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public Container next() {
              Path next = pathIterator.next();
              Container ret;
              try {
                ret = convertPathToContainer(basePath, next);
              } catch (NoSuchElementException | GenericException | RequestNotValidException e) {
                LOGGER.error("Error while listing containers, while parsing resource " + next, e);
                ret = null;
              }

              return ret;
            }

          };
        }

        @Override
        public void close() throws IOException {
          directoryStream.close();
        }
      };

    } catch (IOException e) {
      throw new GenericException("Could not list contents of entity at: " + basePath, e);
    }

    return containerIterable;
  }

  /**
   * Converts a path into a resource
   *
   * @param basePath
   *          base path
   * @param path
   *          relative path to base path
   * @throws RequestNotValidException
   * @throws NotFoundException
   * @throws GenericException
   */
  public static Resource convertPathToResource(Path basePath, Path path)
    throws RequestNotValidException, NotFoundException, GenericException {
    Resource resource;

    // TODO support binary reference

    if (!exists(path)) {
      throw new NotFoundException("Cannot find file or directory at " + path);
    }

    // storage path
    StoragePath storagePath = getStoragePath(basePath, path);

    // construct
    if (FSUtils.isDirectory(path)) {
      resource = new DefaultDirectory(storagePath);
    } else {
      ContentPayload content = null;
      long sizeInBytes;
      try {
        if (FSUtils.isManifestOfExternalFiles(path)) {
          List<String> allLines = Files.readAllLines(path);
          ShallowFiles shallowFiles = new ShallowFiles();
          for (String line : allLines) {
            shallowFiles.addObject(JsonUtils.getObjectFromJson(line, ShallowFile.class));
          }
          content = new ExternalFileManifestContentPayload(shallowFiles);
        } else {
          content = new FSPathContentPayload(path);
        }
        sizeInBytes = Files.size(path);
        Map<String, String> contentDigest = null;
        resource = new DefaultBinary(storagePath, content, sizeInBytes, false, contentDigest);
      } catch (IOException e) {
        throw new GenericException("Could not get file size", e);
      }
    }
    return resource;
  }

  public static Resource convertReferenceToResource(StoragePath storagePath, String url, boolean calculateSize)
    throws GenericException {
    Resource resource;
    Long sizeInBytes = 0L;
    URI uri = URI.create(url);
    Protocol protocol = RodaCoreFactory.getProtocol(uri);
    if (protocol.isAvailable()) {
      if (calculateSize) {
        try {
          sizeInBytes = protocol.getSize();
        } catch (IOException e) {
          throw new GenericException("Cannot retrieve size of file at " + url);
        }
      }

      final ContentPayload contentPayload = new InputStreamContentPayload(protocol::getInputStream, uri);
      resource = new DefaultBinary(storagePath, contentPayload, sizeInBytes, true, new HashMap<>());

      return resource;
    } else {
      throw new GenericException("Resource not available in " + url);
    }
  }

  public static Path getBinaryHistoryMetadataPath(Path historyDataPath, Path historyMetadataPath, Path path) {
    Path relativePath = historyDataPath.relativize(path);
    String fileName = relativePath.getFileName().toString();
    return historyMetadataPath.resolve(relativePath.getParent().resolve(fileName + METADATA_SUFFIX));
  }

  public static BinaryVersion convertPathToBinaryVersion(Path historyDataPath, Path historyMetadataPath, Path path)
    throws RequestNotValidException, NotFoundException, GenericException {
    DefaultBinaryVersion ret;

    if (!FSUtils.exists(path)) {
      throw new NotFoundException("Cannot find file version at " + path);
    }

    // storage path
    Path relativePath = historyDataPath.relativize(path);
    String fileName = relativePath.getFileName().toString();
    int lastIndexOfDot = fileName.lastIndexOf(VERSION_SEP);

    if (lastIndexOfDot <= 0 || lastIndexOfDot == fileName.length() - 1) {
      throw new RequestNotValidException("Bad name for versioned file: " + path);
    }

    String id = fileName.substring(lastIndexOfDot + 1);
    String realFileName = fileName.substring(0, lastIndexOfDot);
    Path realFilePath = relativePath.getParent().resolve(realFileName);
    Path metadataPath = historyMetadataPath.resolve(relativePath.getParent().resolve(fileName + METADATA_SUFFIX));

    StoragePath storagePath = FSUtils.getStoragePath(realFilePath);

    // construct
    ContentPayload content = new FSPathContentPayload(path);
    long sizeInBytes;
    try {
      sizeInBytes = Files.size(path);
      Map<String, String> contentDigest = null;
      Binary binary = new DefaultBinary(storagePath, content, sizeInBytes, false, contentDigest);

      if (FSUtils.exists(metadataPath)) {
        ret = JsonUtils.readObjectFromFile(metadataPath, DefaultBinaryVersion.class);
        ret.setBinary(binary);
      } else {
        Date createdDate = new Date(Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis());
        Map<String, String> defaultProperties = new HashMap<>();
        ret = new DefaultBinaryVersion(binary, id, createdDate, defaultProperties);
      }

    } catch (IOException e) {
      throw new GenericException("Could not get file size", e);
    }

    return ret;
  }

  /**
   * Converts a path into a container
   *
   * @param basePath
   *          base path
   * @param path
   *          relative path to base path
   * @throws GenericException
   * @throws RequestNotValidException
   */
  public static Container convertPathToContainer(Path basePath, Path path)
    throws GenericException, RequestNotValidException {
    Container resource;

    // storage path
    StoragePath storagePath = FSUtils.getStoragePath(basePath, path);

    // construct
    if (FSUtils.isDirectory(path)) {
      resource = new DefaultContainer(storagePath);
    } else {
      throw new GenericException("A file is not a container!");
    }
    return resource;
  }

  public static String computeContentDigest(Path path, String algorithm) throws GenericException {
    try (FileChannel fc = FileChannel.open(path)) {
      final int bufferSize = 1073741824;
      final long size = fc.size();
      final MessageDigest hash = MessageDigest.getInstance(algorithm);
      long position = 0;
      while (position < size) {
        final MappedByteBuffer data = fc.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(size, bufferSize));
        if (!data.isLoaded()) {
          data.load();
        }
        hash.update(data);
        position += data.limit();
        if (position >= size) {
          break;
        }
      }

      byte[] mdbytes = hash.digest();
      StringBuilder hexString = new StringBuilder();

      for (int i = 0; i < mdbytes.length; i++) {
        String hexInt = Integer.toHexString((0xFF & mdbytes[i]));
        if (hexInt.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hexInt);
      }

      return hexString.toString();

    } catch (NoSuchAlgorithmException | IOException e) {
      throw new GenericException("Cannot compute content digest for " + path + " using algorithm " + algorithm);
    }
  }

  public static Path createDirectory(Path parent, String name) throws IOException {
    Path directory;
    do {
      try {
        directory = Files.createDirectory(parent.resolve(name));
      } catch (FileAlreadyExistsException e) {
        LOGGER.warn("Got colision when creating random directory", e);
        directory = null;
      }
    } while (directory == null);

    return directory;
  }

  public static Path createRandomDirectory(Path parent) throws IOException {
    return createDirectory(parent, IdUtils.createUUID());
  }

  public static Path createRandomFile(Path parent) throws IOException {
    return createFile(parent, IdUtils.createUUID());
  }

  public static Path createFile(Path parent, String name) throws IOException {
    Path file;
    do {
      try {
        file = Files.createFile(parent.resolve(name));
      } catch (FileAlreadyExistsException e) {
        LOGGER.warn("Got colision when creating random file", e);
        file = null;
      }
    } while (file == null);

    return file;
  }

  public static Path createFile(Path parent, String name, boolean createDirectoriesIfNotExists,
    boolean createIfNotExists) throws GenericException {
    // ensuring parent exists
    if (createDirectoriesIfNotExists && !FSUtils.exists(parent)) {
      try {
        Files.createDirectories(parent);
      } catch (IOException e) {
        throw new GenericException("Error creating parent folder structure to write holds into", e);
      }
    }

    Path file = parent.resolve(name);

    // verify if file exists and if not creates
    if (createIfNotExists && !FSUtils.exists(file)) {
      try {
        Files.createFile(file);
      } catch (IOException e) {
        throw new GenericException("Error creating file " + file, e);
      }
    }

    return file;
  }

  public static CloseableIterable<BinaryVersion> listBinaryVersions(final Path historyDataPath,
    final Path historyMetadataPath, final StoragePath storagePath) throws GenericException, NotFoundException {
    Path fauxPath = getEntityPath(historyDataPath, storagePath);
    final Path parent = fauxPath.getParent();
    final String baseName = fauxPath.getFileName().toString();

    CloseableIterable<BinaryVersion> iterable;

    try {
      final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent,
        new DirectoryStream.Filter<Path>() {

          @Override
          public boolean accept(Path entry) {
            String fileName = entry.getFileName().toString();
            int lastIndexOfDot = fileName.lastIndexOf(VERSION_SEP);

            return lastIndexOfDot > 0 ? fileName.substring(0, lastIndexOfDot).equals(baseName) : false;
          }
        });

      final Iterator<Path> pathIterator = directoryStream.iterator();
      iterable = new CloseableIterable<BinaryVersion>() {

        @Override
        public Iterator<BinaryVersion> iterator() {
          return new Iterator<BinaryVersion>() {

            @Override
            public boolean hasNext() {
              return pathIterator.hasNext();
            }

            @Override
            public BinaryVersion next() {
              Path next = pathIterator.next();
              try {
                return convertPathToBinaryVersion(historyDataPath, historyMetadataPath, next);
              } catch (GenericException | NotFoundException | RequestNotValidException e) {
                LOGGER.error("Error while list path " + parent + " while parsing resource " + next, e);
                return null;
              }
            }
          };
        }

        @Override
        public void close() throws IOException {
          directoryStream.close();
        }
      };

    } catch (NoSuchFileException e) {
      throw new NotFoundException("Could not find versions of " + storagePath, e);
    } catch (IOException e) {
      throw new GenericException("Error finding version of " + storagePath, e);
    }

    return iterable;
  }

  public static void deleteEmptyAncestorsQuietly(Path binVersionPath, Path upToParent) {
    if (binVersionPath == null) {
      return;
    }

    Path parent = binVersionPath.getParent();
    while (parent != null && !parent.equals(upToParent)) {
      try {
        Files.deleteIfExists(parent);
        parent = parent.getParent();
      } catch (DirectoryNotEmptyException e) {
        // cancel clean-up
        parent = null;
      } catch (IOException e) {
        LOGGER.warn("Could not cleanup binary version directories", e);
      }
    }
  }

  public static String asString(List<String> path) {
    return StringUtils.join(path, SEPARATOR);
  }

  /**
   * We are using java.io because sonar has suggested as a performance update
   * https://sonarqube.com/coding_rules#rule_key=squid%3AS3725
   *
   * @since 2017-03-16
   */
  public static boolean exists(Path file) {
    return file != null && file.toFile().exists();
  }

  public static boolean isDirectory(Path file) {
    return file != null && file.toFile().isDirectory();
  }

  /**
   * We are using java.io because sonar has suggested as a performance update
   * https://sonarqube.com/coding_rules#rule_key=squid%3AS3725
   *
   * @since 2017-03-16
   */
  public static boolean isFile(Path file) {
    return file != null && file.toFile().isFile();
  }

  public static boolean isDirEmpty(Path directory) throws IOException {
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
      return !dirStream.iterator().hasNext();
    }
  }

  /**
   * Related to Shallow files
   */

  public static boolean isManifestOfExternalFiles(String fileId) {
    return fileId != null && fileId.equals(RodaConstants.RODA_MANIFEST_EXTERNAL_FILES);
  }

  public static boolean isManifestOfExternalFiles(Path file) {
    return file != null && isManifestOfExternalFiles(file.getFileName().toString());
  }

  public static ShallowFiles retrieveManifestFileContent(Path path) throws IOException, GenericException {
    List<String> allLines = Files.readAllLines(path);
    ShallowFiles shallowFiles = new ShallowFiles();
    for (String line : allLines) {
      shallowFiles.addObject(JsonUtils.getObjectFromJson(line, ShallowFile.class));
    }
    return shallowFiles;
  }

  public static ShallowFile isResourcePresentOnManifestFile(Path path) throws GenericException {
    Path manifestFile = path.getParent().resolve(RodaConstants.RODA_MANIFEST_EXTERNAL_FILES);
    if (!exists(manifestFile)) {
      return null;
    }
    try {
      String targetFile = path.getFileName().toString();
      ShallowFiles shallowFiles = retrieveManifestFileContent(manifestFile);
      for (ShallowFile shallowFile : shallowFiles.getObjects()) {
        if (shallowFile.getName().equals(targetFile)) {
          return shallowFile;
        }
      }
    } catch (IOException e) {
      throw new GenericException("Cannot read manifest file", e);
    }
    return null;
  }

  public static void removeResourceFromManifestFile(Path path) throws GenericException {
    Path manifestFile = path.getParent().resolve(RodaConstants.RODA_MANIFEST_EXTERNAL_FILES);
    if (!exists(manifestFile)) {
      throw new GenericException("Manifest file not found");
    }
    try {
      String targetFile = path.getFileName().toString();
      ShallowFiles shallowFiles = retrieveManifestFileContent(manifestFile);
      shallowFiles.getObjects().removeIf(sf -> sf.getName().equals(targetFile));
      ExternalFileManifestContentPayload payload = new ExternalFileManifestContentPayload(shallowFiles);
      try {
        payload.writeToPath(manifestFile);
      } catch (IOException e) {
        throw new GenericException("Cannot modify content " + path, e);
      }
    } catch (IOException e) {
      throw new GenericException("Cannot read manifest file", e);
    }
  }

}
