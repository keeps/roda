/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.List;

public class File implements Serializable {

  private static final long serialVersionUID = 3303019735787641534L;

  private String id;
  private String aipId;
  private String representationId;

  private boolean isFile;
  private List<String> filesDirectlyUnder;

  public File() {
    super();
  }

  public File(String id, String aipId, String representationId, boolean isFile, List<String> filesDirectlyUnder) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.isFile = isFile;
    this.filesDirectlyUnder = filesDirectlyUnder;
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

  public boolean isFile() {
    return isFile;
  }

  public void setFile(boolean isFile) {
    this.isFile = isFile;
  }

  public List<String> getFilesDirectlyUnder() {
    return filesDirectlyUnder;
  }

  public void setFilesDirectlyUnder(List<String> filesDirectlyUnder) {
    this.filesDirectlyUnder = filesDirectlyUnder;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
    result = prime * result + ((filesDirectlyUnder == null) ? 0 : filesDirectlyUnder.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (isFile ? 1231 : 1237);
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
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
    File other = (File) obj;
    if (aipId == null) {
      if (other.aipId != null)
        return false;
    } else if (!aipId.equals(other.aipId))
      return false;
    if (filesDirectlyUnder == null) {
      if (other.filesDirectlyUnder != null)
        return false;
    } else if (!filesDirectlyUnder.equals(other.filesDirectlyUnder))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (isFile != other.isFile)
      return false;
    if (representationId == null) {
      if (other.representationId != null)
        return false;
    } else if (!representationId.equals(other.representationId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "File [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", isFile=" + isFile
      + ", filesDirectlyUnder=" + filesDirectlyUnder + "]";
  }

}
