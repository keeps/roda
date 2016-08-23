/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rui Castro
 * 
 * @deprecated 20160824 hsilva: not seeing any method using it, so it will be
 *             removed soon
 */
public final class TempDir {
  private static final Logger LOGGER = LoggerFactory.getLogger(TempDir.class);

  private static DirDeleter deleterThread = null;

  static {
    deleterThread = new DirDeleter();
    Runtime.getRuntime().addShutdownHook(deleterThread);
  }

  /**
   * Creates a temporary directory with an automatically generated name (given a
   * certain prefix) in the system temporary directory. The directory (and all
   * its content) will be destroyed on exit.
   * 
   * @param prefix
   *          the prefix for the directory name.
   * 
   * @return a {@link File} of the created temporary directory.
   * 
   * @throws IOException
   */
  public static File createUniqueTemporaryDirectory(String prefix) throws IOException {

    File tempFile = File.createTempFile(prefix, "temp", getTemporaryDirectory());

    if (tempFile.exists() && !tempFile.delete()) {

      LOGGER.debug("Temporary directory " + tempFile + " already exists and couldn't be deleted.");
      throw new IOException("Temporary directory " + tempFile + " already exists and couldn't be deleted.");

    } else if (!tempFile.mkdir()) {

      LOGGER.debug("Could not create temporary directory " + tempFile);
      throw new IOException("Could not create temporary directory " + tempFile);

    } else {

      LOGGER.trace("Temporary directory " + tempFile + " created successfully.");

      deleterThread.add(tempFile);

      LOGGER.debug("Temporary directory " + tempFile + " scheduled for removal.");

      return tempFile;
    }

  }

  /**
   * Creates a temporary directory with an automatically generated name (given a
   * certain prefix) in a given directory. The directory (and all its content)
   * will be destroyed on exit.
   * 
   * @param prefix
   *          the prefix for the directory name.
   * @param directory
   *          the directory where the new directory is going to be created.
   * 
   * @return a {@link File} representing the created temporary directory.
   * 
   * @throws IOException
   */
  public static File createUniqueTemporaryDirectory(String prefix, File directory) throws IOException {

    File tempFile = File.createTempFile(prefix, "temp", directory);

    if (tempFile.exists() && !tempFile.delete()) {
      LOGGER.debug("Temporary directory " + tempFile + " already exists and couldn't be deleted.");
      throw new IOException("Temporary directory " + tempFile + " already exists and couldn't be deleted.");
    }

    if (!tempFile.mkdir()) {
      LOGGER.debug("Could not create temporary directory " + tempFile);
      throw new IOException("Could not create temporary directory " + tempFile);
    }

    LOGGER.trace("Temporary directory " + tempFile + " created successfully.");

    deleterThread.add(tempFile);

    LOGGER.debug("Temporary directory " + tempFile + " scheduled for removal.");

    return tempFile;
  }

  /**
   * Creates a temporary directory with a given name in the given directory. The
   * directory (and all its content) will be destroyed on exit.
   * 
   * @param name
   *          the name of the directory to create.
   * 
   * @param directory
   *          the directory where the new directory is going to be created.
   * 
   * @return a {@link File} representing the created directory.
   * 
   * @throws IOException
   */
  public static File createTemporaryDirectory(String name, File directory) throws IOException {

    File tempFile = new File(directory, name);

    if (tempFile.exists() && !tempFile.delete()) {
      LOGGER.debug("Temporary directory " + tempFile + " already exists and couldn't be deleted.");
      throw new IOException("Temporary directory " + tempFile + " already exists and couldn't be deleted.");
    }
    if (!tempFile.mkdir()) {
      LOGGER.debug("Could not create temporary directory " + tempFile);
      throw new IOException("Could not create temporary directory " + tempFile);
    }

    LOGGER.trace("Temporary directory " + tempFile + " created successfully.");

    deleterThread.add(tempFile);

    LOGGER.debug("Temporary directory " + tempFile + " scheduled for removal.");

    return tempFile;
  }

  /**
   * Creates a directory with an automatically generated name (given a certain
   * prefix) in a given directory.
   * 
   * @param prefix
   *          the prefix for the directory name.
   * @param directory
   *          the directory where the new directory is going to be created.
   * 
   * @return a {@link File} representing the created directory.
   * 
   * @throws IOException
   */
  public static File createUniqueDirectory(String prefix, File directory) throws IOException {

    File tempFile = File.createTempFile(prefix, "temp", directory);

    if (tempFile.exists() && !tempFile.delete()) {
      LOGGER.debug("Directory " + tempFile + " already exists and couldn't be deleted.");
      throw new IOException("Directory " + tempFile + " already exists and couldn't be deleted.");
    }

    if (!tempFile.mkdir()) {
      LOGGER.debug("Could not create directory " + tempFile);
      throw new IOException("Could not create directory " + tempFile);
    }

    return tempFile;
  }

  /**
   * Creates a directory with an automatically generated name (given a certain
   * prefix) in the system temporary directory. If the directory already exists
   * it will be overwritten.
   * 
   * @param prefix
   *          the prefix for the directory name.
   * 
   * @return a {@link File} representing the created directory.
   * 
   * @throws IOException
   */
  public static File createUniqueDirectory(String prefix) throws IOException {
    return createUniqueDirectory(prefix, getTemporaryDirectory());
  }

  /**
   * Get defined temporary directory
   * 
   * @return the RODA temporary directory, defined by environment variable
   *         RODA_TEMP_DIR. If the variable doesn't exist, the java.io.tmpdir
   *         will be used instead
   */
  public static File getTemporaryDirectory() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    String rodaTempDirPath = System.getenv("RODA_TEMP_DIR");

    if (!StringUtils.isBlank(rodaTempDirPath)) {
      File rodaTempDir = new File(rodaTempDirPath);
      if (rodaTempDir.isDirectory() && rodaTempDir.canWrite()) {
        tempDir = rodaTempDir;
      }
    }
    return tempDir;
  }
}
