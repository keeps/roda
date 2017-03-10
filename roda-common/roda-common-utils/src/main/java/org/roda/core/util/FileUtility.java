/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.roda.core.data.common.RodaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rui Castro
 * @author Hélder Silva <hsilva@keep.pt>
 * 
 */
public class FileUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtility.class);

  public static InputStream getConfigurationFile(Path configPath, String relativePath) {
    InputStream ret;
    Path staticConfig = configPath.resolve(relativePath);

    if (Files.exists(staticConfig)) {
      try {
        ret = new FileInputStream(staticConfig.toFile());
        LOGGER.debug("Using static configuration");
      } catch (FileNotFoundException e) {
        LOGGER.debug("Couldn't find static configuration file - {}", staticConfig);
        LOGGER.debug("Using internal configuration");
        ret = FileUtility.class.getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + relativePath);
      }
    } else {
      LOGGER.debug("Using internal configuration");
      ret = FileUtility.class.getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + relativePath);
    }
    return ret;
  }

  /**
   * 
   * @param is
   * @param digestAlgorithm
   * @return a {@link java.lang.String} with calculated checksum in hexadecimal.
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  public static String checksum(InputStream is, String digestAlgorithm) throws NoSuchAlgorithmException, IOException {
    return byteArrayToHexString(digest(is, digestAlgorithm));
  }

  /**
   * 
   * @param is
   * @param digestAlgorithm
   * @return a byte array with the calculated digest.
   * @throws NoSuchAlgorithmException
   * @throws IOException
   */
  private static byte[] digest(InputStream is, String digestAlgorithm) throws NoSuchAlgorithmException, IOException {

    MessageDigest digestor = MessageDigest.getInstance(digestAlgorithm);

    // reads file in 1Mbyte chunks (1048576 bytes - 2^20 bytes)
    int bufSize = 1048576; // 1 MByte
    byte[] buffer = new byte[bufSize];
    int n = is.read(buffer, 0, bufSize);
    while (n != -1) {
      digestor.update(buffer, 0, n);
      n = is.read(buffer, 0, bufSize);
    }
    is.close();

    return digestor.digest();
  }

  public static Map<String, String> checksums(InputStream is, Collection<String> algorithms)
    throws NoSuchAlgorithmException, IOException {
    Map<String, String> ret = new HashMap<>();
    Map<String, MessageDigest> digestors = new HashMap<>();

    for (String algorithm : algorithms) {
      digestors.put(algorithm, MessageDigest.getInstance(algorithm));
    }

    // reads file in 1Mbyte chunks (1048576 bytes - 2^20 bytes)
    int bufSize = 1048576; // 1 MByte
    byte[] buffer = new byte[bufSize];
    int n = is.read(buffer, 0, bufSize);
    while (n != -1) {

      for (MessageDigest digestor : digestors.values()) {
        digestor.update(buffer, 0, n);
      }

      n = is.read(buffer, 0, bufSize);
    }

    for (Entry<String, MessageDigest> entry : digestors.entrySet()) {
      ret.put(entry.getKey(), byteArrayToHexString(entry.getValue().digest()));
    }

    IOUtils.closeQuietly(is);

    return ret;
  }

  public static Map<String, String> copyAndChecksums(InputStream in, OutputStream out, Collection<String> algorithms)
    throws NoSuchAlgorithmException, IOException {
    Map<String, String> ret = new HashMap<>();
    Map<String, DigestInputStream> streams = new HashMap<>();

    InputStream stream = in;

    for (String algorithm : algorithms) {
      stream = new DigestInputStream(stream, MessageDigest.getInstance(algorithm));
      streams.put(algorithm, (DigestInputStream) stream);
    }

    IOUtils.copyLarge(stream, out);

    for (Entry<String, DigestInputStream> entry : streams.entrySet()) {
      ret.put(entry.getKey(), byteArrayToHexString(entry.getValue().getMessageDigest().digest()));
    }

    return ret;
  }

  /**
   * Convert a byte[] array to readable string format. This makes the "hex"
   * readable!
   * 
   * @return result String buffer in String format
   * @param in
   *          byte[] buffer to convert to string format
   */
  private static String byteArrayToHexString(byte in[]) {

    byte ch = 0x00;

    int i = 0;

    if (in == null || in.length <= 0) {
      return null;
    }

    String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    StringBuffer out = new StringBuffer(in.length * 2);

    while (i < in.length) {

      ch = (byte) (in[i] & 0xF0); // Strip off high nibble

      ch = (byte) (ch >>> 4);
      // shift the bits down

      ch = (byte) (ch & 0x0F);
      // must do this is high order bit is on!

      out.append(pseudo[(int) ch]); // convert the nibble to a String
      // Character

      ch = (byte) (in[i] & 0x0F); // Strip off low nibble

      out.append(pseudo[(int) ch]); // convert the nibble to a String
      // Character

      i++;
    }

    return new String(out);
  }

  /**
   * Returns a list with all the files inside a given directory.
   * 
   * @param dir
   *          the directory to list the contents.
   * @return a list with all the files and directories.
   */
  public static List<File> listFilesRecursively(File dir) {

    ArrayList<File> files = new ArrayList<>();

    File[] list = dir.listFiles();

    for (int i = 0; i < list.length; i++) {
      if (list[i].isDirectory()) {
        files.addAll(listFilesRecursively(list[i]));
      } else {
        files.add(list[i]);
      }
    }

    return files;
  }

  /**
   * Reads bytes from a file
   * 
   * @param file
   * @return an array of bytes read from file
   * @throws IOException
   */
  public static byte[] readBytesFromFile(File file) throws IOException {
    FileInputStream inputStream = new FileInputStream(file);
    byte[] bytes = StreamUtility.byteArrayFromInputStream(inputStream);
    inputStream.close();
    return bytes;
  }

  /**
   * Gets file extension from filename
   * 
   * @param filename
   * @return file extension if filename has at least one dot ; otherwise an
   *         empty string
   */
  public static String getFileNameExtension(String filename) {
    String res = "";
    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf != -1) {
      res = filename.substring(lastIndexOf + 1);
    }
    return res;
  }

}
