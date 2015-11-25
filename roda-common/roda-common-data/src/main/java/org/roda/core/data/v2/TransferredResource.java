/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class TransferredResource implements Serializable {
  /**
   * 
   */
  
  private static final long serialVersionUID = 1L;
  private String id;
  private String fullPath;
  private String relativePath;
  private String parentPath;
  private long size;
  private Date creationDate;
  private String name;
  private boolean file;
  private String owner;
  private List<String> ancestorsPaths;
  
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public boolean isFile() {
    return file;
  }

  public void setFile(boolean file) {
    this.file = file;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFullPath() {
    return fullPath;
  }

  public void setFullPath(String fullPath) {
    this.fullPath = fullPath;
  }

  public String getRelativePath() {
    return relativePath;
  }

  public void setRelativePath(String relativePath) {
    this.relativePath = relativePath;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getParentId() {
    return parentPath;
  }

  public void setParentId(String parentId) {
    this.parentPath = parentId;
  }

  
  public List<String> getAncestorsPaths() {
    return ancestorsPaths;
  }

  public void setAncestorsPaths(List<String> ancestorsPaths) {
    this.ancestorsPaths = ancestorsPaths;
  }

  @Override
  public String toString() {
    return "TransferredResource [id=" + id + ", fullPath=" + fullPath + ", relativePath=" + relativePath
      + ", parentPath=" + parentPath + ", size=" + size + ", creationDate=" + creationDate + ", name=" + name
      + ", file=" + file + ", owner=" + owner + "]";
  }
  
  

}
