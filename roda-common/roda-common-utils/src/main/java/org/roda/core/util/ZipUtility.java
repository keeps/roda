/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rui Castro
 * @author Vladislav Korecký <vladislav_korecky@gordic.cz>
 *
 * @since 20160909 hsilva: RODA is not using any of these methods, but external
 *        plugins are
 */
public final class ZipUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtility.class);
  private static final int BUFFER_SIZE = 1024;

  private ZipUtility() {
    // do nothing
  }

  /**
   * Extract files in zipFilename to outputDir.
   *
   * @param zipFilename
   *          the zip file to extract files from.
   * @param outputDir
   *          the output directory to extract files to.
   * @param filesWithAbsolutePath
   *          determines if the output list of files will contain the absolute or
   *          relative path to the files
   *
   * @return a {@link List} of with all the extracted {@link File}s.
   *
   * @throws IOException
   *           if a input/output operation fails, like opening a file or
   *           reading/writing from/to a stream.
   */
  public static List<File> extractFilesFromZIP(File zipFilename, File outputDir, boolean filesWithAbsolutePath)
    throws IOException {
    return extractFilesFromInputStream(new FileInputStream(zipFilename), outputDir, filesWithAbsolutePath);

  }

  private static List<File> extractFilesFromInputStream(InputStream inputStream, File outputDir,
    boolean filesWithAbsolutePath) throws IOException {
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    String canonicalDestinationDirPath = outputDir.getCanonicalPath();

    if (zipEntry == null) {
      zipInputStream.close();
      throw new IOException("No files inside ZIP");
    } else {
      List<File> extractedFiles = new ArrayList<>();

      while (zipEntry != null) {
        // for each entry to be extracted
        String entryName = FilenameUtils.normalize(zipEntry.getName());
        LOGGER.debug("Extracting {}", entryName);

        File newFile = new File(outputDir, entryName);
        String canonicalDestinationFile = newFile.getCanonicalPath();
        if (!canonicalDestinationFile.startsWith(canonicalDestinationDirPath + File.separator)) {
          LOGGER.debug("Removing already extracted files due to suspicious zip file");
          for (File f : extractedFiles) {
            Files.delete(Paths.get(FilenameUtils.normalize(f.getCanonicalPath())));
          }
          throw new IOException("Detected a ZIP entry outside of the target dir: " + entryName);
        }

        if (filesWithAbsolutePath) {
          extractedFiles.add(newFile);
        } else {
          extractedFiles.add(new File(entryName));
        }

        if (zipEntry.isDirectory()) {
          newFile.mkdirs();
        } else {

          if (newFile.getParentFile() != null && (!newFile.getParentFile().exists())) {
            newFile.getParentFile().mkdirs();
          }

          FileOutputStream newFileOutputStream = new FileOutputStream(newFile);

          // copyLarge returns a long instead of int
          IOUtils.copyLarge(zipInputStream, newFileOutputStream);

          newFileOutputStream.close();
          zipInputStream.closeEntry();
        }

        zipEntry = zipInputStream.getNextEntry();
      }

      zipInputStream.close();
      return extractedFiles;
    }
  }

  /**
   * Creates ZIP file with the files inside directory <code>contentsDir</code> .
   *
   * @param newZipFile
   *          the ZIP file to create
   * @param contentsDir
   *          the directory containing the files to compress.
   * @return the created ZIP file.
   * @throws IOException
   *           if something goes wrong with creation of the ZIP file or the
   *           reading of the files to compress.
   */
  public static File createZIPFile(File newZipFile, File contentsDir) throws IOException {

    List<File> contentAbsoluteFiles = FileUtility.listFilesRecursively(contentsDir);

    FileOutputStream zipStream = new FileOutputStream(newZipFile);
    JarOutputStream jarOutputStream = new JarOutputStream(new BufferedOutputStream(zipStream));

    // Create a buffer for reading the files
    byte[] buffer = new byte[BUFFER_SIZE];

    for (File absoluteFile : contentAbsoluteFiles) {
      String relativeFile = getFilePathRelativeTo(absoluteFile, contentsDir);

      FileInputStream inputStream = new FileInputStream(absoluteFile);
      BufferedInputStream in = new BufferedInputStream(inputStream);

      // Add ZIP entry to output stream.
      jarOutputStream.putNextEntry(new JarEntry(relativeFile));

      LOGGER.trace("Adding {}", relativeFile);

      int length;
      while ((length = in.read(buffer)) > 0) {
        jarOutputStream.write(buffer, 0, length);
      }

      // Complete the entry
      jarOutputStream.closeEntry();
      in.close();
      inputStream.close();
    }

    // Complete the ZIP file
    jarOutputStream.close();
    zipStream.close();

    return newZipFile;
  }

  /**
   * @param file
   *          the {@link File} to make relative
   * @param relativeTo
   *          the {@link File} (or directory) that file should be made relative
   *          to.
   * @return a {@link String} with the relative file path.
   */
  public static String getFilePathRelativeTo(File file, File relativeTo) {
    return relativeTo.getAbsoluteFile().toURI().relativize(file.getAbsoluteFile().toURI()).toString();
  }
}
