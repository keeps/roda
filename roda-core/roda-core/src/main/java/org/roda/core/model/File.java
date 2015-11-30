/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.Serializable;

import org.roda.core.data.v2.FileFormat;
import org.roda.core.data.v2.SimpleFile;
import org.roda.core.storage.StoragePath;

public class File extends SimpleFile implements Serializable {

  private static final long serialVersionUID = 3303019735787641534L;
  private final StoragePath storagePath;

  public File(String id, String aipId, String representationId, boolean entryPoint, FileFormat fileFormat,
    StoragePath storagePath) {
    super(id,aipId,representationId,entryPoint,fileFormat);
    this.storagePath = storagePath;
  }

  /**
   * @return the storagePath
   */
  public StoragePath getStoragePath() {
    return storagePath;
  }
}
