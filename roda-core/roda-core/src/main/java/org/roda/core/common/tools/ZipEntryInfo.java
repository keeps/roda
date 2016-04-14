/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.tools;

import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.fs.FSPathContentPayload;

/**
 * Information for zipping
 * 
 * @author Luis Faria
 * 
 */
public class ZipEntryInfo {
  private final String name;

  private final ContentPayload payload;

  /**
   * Create a new zip entry info
   * 
   * @param name
   * @param file
   * @throws FileNotFoundException
   */
  public ZipEntryInfo(String name, Path path) throws FileNotFoundException {
    this(name, new FSPathContentPayload(path));
  }

  /**
   * Create a new zip entry info
   * 
   * @param name
   * @param payload
   */
  public ZipEntryInfo(String name, ContentPayload payload) {
    this.name = name;
    this.payload = payload;
  }

  /**
   * Get zip entry name
   * 
   * @return the name of the zip entry
   */
  public String getName() {
    return name;
  }

  public ContentPayload getPayload() {
    return payload;
  }

}
