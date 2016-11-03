/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "dip_file")
public class DIPFile implements IsModelObject, IsIndexed {

  private static final long serialVersionUID = 1L;
  public static int VERSION = 1;

  private String uuid;
  private String id;
  private String dipId;
  private List<String> path;
  private List<String> ancestorsPath;
  private long size = 0;
  private boolean isDirectory;
  private String storagePath;

  public DIPFile() {
    super();
  }

  public DIPFile(String id, String dipId, List<String> path, boolean isDirectory) {
    super();
    this.id = id;
    this.dipId = dipId;
    this.path = path;
    this.isDirectory = isDirectory;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDipId() {
    return dipId;
  }

  public void setDipId(String dipId) {
    this.dipId = dipId;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  public List<String> getAncestorsPath() {
    return ancestorsPath;
  }

  public void setAncestorsPath(List<String> ancestorsPath) {
    this.ancestorsPath = ancestorsPath;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ancestorsPath == null) ? 0 : ancestorsPath.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + (isDirectory ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((dipId == null) ? 0 : dipId.hashCode());
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
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
    DIPFile other = (DIPFile) obj;
    if (ancestorsPath == null) {
      if (other.ancestorsPath != null)
        return false;
    } else if (!ancestorsPath.equals(other.ancestorsPath))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (storagePath == null) {
      if (other.storagePath != null)
        return false;
    } else if (!storagePath.equals(other.storagePath))
      return false;
    if (isDirectory != other.isDirectory)
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (dipId == null) {
      if (other.dipId != null)
        return false;
    } else if (!dipId.equals(other.dipId))
      return false;
    if (size != other.size)
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
    return "TransferredResource [uuid=" + uuid + ", id=" + id + ", dipId=" + dipId + ", path=" + path
      + ", ancestorsPath=" + ancestorsPath + ", size=" + size + ", storagePath=" + storagePath + ", isDirectory="
      + isDirectory + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("uuid", "id", "dipId", "path", "ancestorsPath", "size", "storagePath", "isDirectory");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(uuid, id, dipId, path, ancestorsPath, size, storagePath, isDirectory);
  }
}
