package org.roda.disseminators.common.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Information for zipping
 * 
 * @author Luis Faria
 * 
 */
public class ZipEntryInfo {
  private final String name;

  private final InputStream inputStream;

  /**
   * Create a new zip entry info
   * 
   * @param name
   * @param file
   * @throws FileNotFoundException
   */
  public ZipEntryInfo(String name, File file) throws FileNotFoundException {
    this(name, new FileInputStream(file));
  }

  /**
   * Create a new zip entry info
   * 
   * @param name
   * @param is
   */
  public ZipEntryInfo(String name, InputStream is) {
    this.name = name;
    this.inputStream = is;
  }

  /**
   * Get zip entry name
   * 
   * @return the name of the zip entry
   */
  public String getName() {
    return name;
  }

  /**
   * Get zip entry input stream
   * 
   * @return the inputstream where read the file to add to zip
   */
  public InputStream getInputStream() {
    return inputStream;
  }
}
