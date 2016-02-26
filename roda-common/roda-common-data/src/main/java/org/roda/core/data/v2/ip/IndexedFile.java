/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.metadata.FileFormat;

public class IndexedFile implements IsIndexed {

  private static final long serialVersionUID = 3303019735787641534L;

  private String uuid = null;

  private String aipId = null;
  private String representationId = null;
  private List<String> path = null;
  private String id = null;

  private FileFormat fileFormat = null;
  private String originalName = null;
  private long size = 0;
  private boolean isDirectory = false;
  private String creatingApplicationName;
  private String creatingApplicationVersion;
  private String dateCreatedByApplication;
  private List<String> hash;
  private String storagePath;

  public IndexedFile() {
    super();
  }

  public IndexedFile(String uuid, String aipId, String representationId, List<String> path, String id,
    boolean entryPoint, FileFormat fileFormat, String originalName, long size, boolean isDirectory,
    String creatingApplicationName, String creatingApplicationVersion, String dateCreatedByApplication,
    List<String> hash, String storagePath) {
    this.uuid = uuid;

    this.aipId = aipId;
    this.representationId = representationId;
    this.path = path;
    this.id = id;

    this.fileFormat = fileFormat;
    this.originalName = originalName;
    this.size = size;
    this.isDirectory = isDirectory;
    this.creatingApplicationName = creatingApplicationName;
    this.creatingApplicationVersion = creatingApplicationVersion;
    this.dateCreatedByApplication = dateCreatedByApplication;
    this.hash = hash;
    this.storagePath = storagePath;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
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

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public FileFormat getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(FileFormat fileFormat) {
    this.fileFormat = fileFormat;
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

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public String getCreatingApplicationName() {
    return creatingApplicationName;
  }

  public void setCreatingApplicationName(String creatingApplicationName) {
    this.creatingApplicationName = creatingApplicationName;
  }

  public String getCreatingApplicationVersion() {
    return creatingApplicationVersion;
  }

  public void setCreatingApplicationVersion(String creatingApplicationVersion) {
    this.creatingApplicationVersion = creatingApplicationVersion;
  }

  public String getDateCreatedByApplication() {
    return dateCreatedByApplication;
  }

  public void setDateCreatedByApplication(String dateCreatedByApplication) {
    this.dateCreatedByApplication = dateCreatedByApplication;
  }

  public List<String> getHash() {
    return hash;
  }

  public void setHash(List<String> hash) {
    this.hash = hash;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((creatingApplicationName == null) ? 0 : creatingApplicationName.hashCode());
    result = prime * result + ((creatingApplicationVersion == null) ? 0 : creatingApplicationVersion.hashCode());
    result = prime * result + ((dateCreatedByApplication == null) ? 0 : dateCreatedByApplication.hashCode());
    result = prime * result + ((fileFormat == null) ? 0 : fileFormat.hashCode());
    result = prime * result + ((hash == null) ? 0 : hash.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (isDirectory ? 1231 : 1237);
    result = prime * result + ((originalName == null) ? 0 : originalName.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedFile other = (IndexedFile) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
      return false;
    if (creatingApplicationName == null) {
      if (other.creatingApplicationName != null)
        return false;
    } else if (!creatingApplicationName.equals(other.creatingApplicationName))
      return false;
    if (creatingApplicationVersion == null) {
      if (other.creatingApplicationVersion != null)
        return false;
    } else if (!creatingApplicationVersion.equals(other.creatingApplicationVersion))
      return false;
    if (dateCreatedByApplication == null) {
      if (other.dateCreatedByApplication != null)
        return false;
    } else if (!dateCreatedByApplication.equals(other.dateCreatedByApplication))
      return false;
    if (fileFormat == null) {
      if (other.fileFormat != null)
        return false;
    } else if (!fileFormat.equals(other.fileFormat))
      return false;
    if (hash == null) {
      if (other.hash != null)
        return false;
    } else if (!hash.equals(other.hash))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (isDirectory != other.isDirectory)
      return false;
    if (originalName == null) {
      if (other.originalName != null)
        return false;
    } else if (!originalName.equals(other.originalName))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (representationId == null) {
      if (other.representationId != null)
        return false;
    } else if (!representationId.equals(other.representationId))
      return false;
    if (size != other.size)
      return false;
    if (storagePath == null) {
      if (other.storagePath != null)
        return false;
    } else if (!storagePath.equals(other.storagePath))
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedFile [uuid=" + uuid + ", aipId=" + aipId + ", representationId=" + representationId + ", path="
      + path + ", id=" + id + ", fileFormat=" + fileFormat + ", originalName=" + originalName + ", size=" + size
      + ", isDirectory=" + isDirectory + ", creatingApplicationName=" + creatingApplicationName
      + ", creatingApplicationVersion=" + creatingApplicationVersion + ", dateCreatedByApplication="
      + dateCreatedByApplication + ", hash=" + hash + ", storagePath=" + storagePath + "]";
  }

}
