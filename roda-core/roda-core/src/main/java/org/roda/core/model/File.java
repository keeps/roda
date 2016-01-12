/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import java.io.Serializable;

import org.roda.core.storage.StoragePath;

public class File implements Serializable {

  private static final long serialVersionUID = 3303019735787641534L;
  private final StoragePath storagePath;
  private String id;
  private String aipId;
  private String representationId;
  private boolean entryPoint;
  private String originalName;
  private long size;
  boolean isFile;

  public File(String id, String aipId, String representationId, boolean entryPoint, StoragePath storagePath,
    String originalName, long size, boolean isFile) {
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.entryPoint = entryPoint;
    this.size = size;
    this.originalName = originalName;
    this.isFile = isFile;
    this.storagePath = storagePath;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public boolean isEntryPoint() {
    return entryPoint;
  }

  public void setEntryPoint(boolean entryPoint) {
    this.entryPoint = entryPoint;
  }

  public String getOriginalName() {
    return originalName;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public boolean isFile() {
    return isFile;
  }

  public void setFile(boolean isFile) {
    this.isFile = isFile;
  }

  public StoragePath getStoragePath() {
    return storagePath;
  }

}
