/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.roda.core.common.RodaUtils;
import org.roda.core.data.exceptions.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.google.common.collect.Sets;

/**
 * File System based metadata related utility class
 * 
 * @author Luís Faria <lfaria@keep.pt>
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class FSYamlMetadataUtils {

  private static final String PROPERTIES_FOLDER = ".properties";
  private static final String PROPERTIES_SUFFIX = ".properties.yaml";

  /**
   * {@code FileFilter} for properties files
   */
  public static final FileFilter FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(File pathname) {
      boolean isPropertiesFolder = pathname.isDirectory() && pathname.getName().equals(PROPERTIES_FOLDER);
      return !isPropertiesFolder;
    }
  };

  /**
   * {@code Filter} for properties directory
   */
  public static final Filter<Path> PATH_FILTER = new Filter<Path>() {

    @Override
    public boolean accept(Path path) throws IOException {
      boolean isPropertiesFolder = path.endsWith(PROPERTIES_FOLDER);
      return !isPropertiesFolder;
    }

  };

  private static final Logger LOGGER = LoggerFactory.getLogger(FSYamlMetadataUtils.class);

  /**
   * Private empty constructor
   */
  private FSYamlMetadataUtils() {

  }

  private static Path getPropertiesPath(Path path) {
    Path properties;
    if (Files.isDirectory(path)) {
      Path propertiesFolder = path.resolve(PROPERTIES_FOLDER);
      properties = propertiesFolder.resolve(PROPERTIES_SUFFIX);
    } else {
      Path propertiesFolder = path.getParent().resolve(PROPERTIES_FOLDER);
      properties = propertiesFolder.resolve(path.getFileName() + PROPERTIES_SUFFIX);
    }
    return properties;
  }

  /**
   * Create properties directory (if it doesn't exists already)
   * 
   * @param directory
   *          path to the directory where the properties directory is going to
   *          be created
   */
  public static void createPropertiesDirectory(Path directory) throws IOException {
    try {
      Files.createDirectory(directory.resolve(FSYamlMetadataUtils.PROPERTIES_FOLDER));
    } catch (FileAlreadyExistsException e) {
      // do nothing
    } catch (IOException e) {
      throw e;
    }
  }

  /**
   * Writes metadata to a file
   * 
   * @param path
   *          file where the metadata should be written into
   * @param metadata
   *          metadata to be written
   * @param replaceAll
   *          true indicates that metadata available in the file before will be
   *          discarded and only the new metadata will be written; false
   *          indicates that metadata already available in the file will be
   *          updated to the new values (by replacing the old values with the
   *          new ones)
   * @throws GenericException
   */
  public static Map<String, Set<String>> writeMetadata(Path path, Map<String, Set<String>> metadata, boolean replaceAll)
    throws GenericException {
    Path properties = getPropertiesPath(path);
    return writeMetadataToPath(properties, metadata, replaceAll);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Set<String>> writeMetadataToPath(Path properties, Map<String, Set<String>> newMetadata,
    boolean replaceAll) throws GenericException {
    Yaml yaml = new Yaml();
    Map<String, Set<String>> metadata;
    if (replaceAll) {
      metadata = newMetadata;
    } else {
      Map<String, Set<String>> oldMetadata = new HashMap<String, Set<String>>();
      if (Files.exists(properties)) {
        BufferedReader bufferedReader = null;
        try {
          bufferedReader = Files.newBufferedReader(properties, Charset.defaultCharset());
          Object o = yaml.load(bufferedReader);
          if (o instanceof Map) {
            oldMetadata = (Map<String, Set<String>>) o;
          }
        } catch (IOException e) {
          throw new GenericException("Could not load from properties file " + properties, e);
        } finally {
          if (bufferedReader != null) {
            try {
              bufferedReader.close();
            } catch (IOException e) {
              LOGGER.warn("Cannot close buffered reader", e);
            }
          }
        }
      }

      metadata = replaceMetadataIfExists(oldMetadata, newMetadata);
    }
    BufferedWriter bufferedWriter = null;
    try {
      // ensure parent exists
      Path parent = properties.getParent();
      if (!Files.exists(parent)) {
        Files.createDirectories(parent);
      }

      // write file
      bufferedWriter = Files.newBufferedWriter(properties, Charset.defaultCharset(), StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
      yaml.dump(metadata, bufferedWriter);
      return metadata;

    } catch (IOException e) {
      throw new GenericException("Could not write properties back to file " + properties, e);
    } finally {
      if (bufferedWriter != null) {
        try {
          bufferedWriter.close();
        } catch (IOException e) {
          LOGGER.warn("Cannot close buffered writer", e);
        }
      }
    }

  }

  private static Map<String, Set<String>> replaceMetadataIfExists(Map<String, Set<String>> oldMetadata,
    Map<String, Set<String>> newMetadata) {
    Map<String, Set<String>> metadata = new HashMap<String, Set<String>>(oldMetadata);

    metadata.putAll(newMetadata);

    return metadata;
  }

  /**
   * Reads metadata from a file
   * 
   * @param file
   *          file where the metadata is written
   * @throws GenericException
   */
  public static Map<String, Set<String>> readMetadata(Path path) throws GenericException {
    Path properties = getPropertiesPath(path);
    return readMetadataFromPath(properties);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Map<String, Set<String>> readMetadataFromPath(Path properties) throws GenericException {
    Yaml yaml = new Yaml();
    Map<String, Set<String>> metadata;
    if (Files.exists(properties)) {
      BufferedReader bufferedReader = null;
      try {
        bufferedReader = Files.newBufferedReader(properties, Charset.defaultCharset());
        Object o = yaml.load(bufferedReader);

        if (o instanceof Map) {
          Map<Object, Object> m = (Map) o;
          // convert map of list to set
          metadata = new HashMap<String, Set<String>>();
          for (Entry<Object, Object> entry : m.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Set) {
              Set<String> valueSet = (Set) value;
              metadata.put(key.toString(), valueSet);
            } else if (value instanceof List) {
              List<String> valueList = (List) value;
              metadata.put(key.toString(), Sets.newHashSet(valueList));
            } else if (value instanceof Boolean) {
              Boolean valueBoolean = (Boolean) value;
              metadata.put(key.toString(), Sets.newHashSet(valueBoolean.toString()));
            } else if (value instanceof Date) {
              Date valueDate = (Date) value;
              metadata.put(key.toString(), Sets.newHashSet(RodaUtils.dateToString(valueDate)));
            } else if (value instanceof String) {
              String valueString = (String) value;
              metadata.put(key.toString(), Sets.newHashSet(valueString));
            } else if (value instanceof Double) {
              metadata.put(key.toString(), Sets.newHashSet(value.toString()));
            } else if (value != null) {
              LOGGER.warn("Unsupported value class for YAML properties parse: " + value.getClass().getName());
              metadata.put(key.toString(), Sets.newHashSet(value.toString()));
            } else {
              // if null the value is empty
              metadata.put(key.toString(), new HashSet<String>());
            }
          }
        } else {
          throw new GenericException("Could not serialize properties to a map on " + properties);
        }
      } catch (IOException | ScannerException e) {
        throw new GenericException("Could not load from properties file " + properties, e);
      } finally {
        if (bufferedReader != null) {
          try {
            bufferedReader.close();
          } catch (IOException e) {
            LOGGER.warn("Cannot close buffered reader", e);
          }
        }
      }
    } else {
      metadata = new HashMap<String, Set<String>>();
    }
    return metadata;
  }

  public static void copyMetadata(Path source, Path target, boolean replaceExisting) throws GenericException {
    Path sourceProperties = getPropertiesPath(source);
    Path targetProperties = getPropertiesPath(target);
    try {
      CopyOption[] copyOptions = replaceExisting ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING}
        : new CopyOption[] {};
      Files.copy(sourceProperties, targetProperties, copyOptions);
    } catch (IOException e) {
      throw new GenericException("Could not copy metadata", e);
    }
  }

  public static void moveMetadata(Path source, Path target, boolean replaceExisting) throws GenericException {
    Path sourceProperties = getPropertiesPath(source);
    Path targetProperties = getPropertiesPath(target);
    try {
      CopyOption[] copyOptions = replaceExisting ? new CopyOption[] {StandardCopyOption.REPLACE_EXISTING}
        : new CopyOption[] {};
      Files.move(sourceProperties, targetProperties, copyOptions);
    } catch (IOException e) {
      throw new GenericException("Could not copy metadata", e);
    }
  }

  public static void deleteMetadata(Path path) throws GenericException {
    Path properties = getPropertiesPath(path);
    try {
      Files.deleteIfExists(properties);
    } catch (IOException e) {
      throw new GenericException("Could not delete metadata", e);
    }
  }

  public static void addContentDigestToMetadata(Map<String, Set<String>> metadata, Map<String, String> contentDigest) {
    for (Entry<String, String> entry : contentDigest.entrySet()) {
      metadata.put(entry.getKey(), Sets.newHashSet(entry.getValue()));
    }
  }
}
