/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.List;



import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_FILE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class File implements IsModelObject, HasId {
  private static final long serialVersionUID = 3303019735787641534L;

  private String id;
  private List<String> path;
  private String aipId;
  private String representationId;

  private boolean isDirectory;

  private boolean isReference;
  private String referenceUrl;
  private String referenceManifest;
  private String referenceUUID;

  public File() {
    super();
  }

  public File(String id, String aipId, String representationId, List<String> path, boolean isDirectory, boolean isReference, String referenceUrl, String referenceManifest, String referenceUUID) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.path = path;
    this.isDirectory = isDirectory;
    this.isReference = isReference;
    this.referenceUrl = referenceUrl;
    this.referenceManifest = referenceManifest;
    this.referenceUUID = referenceUUID;
  }

  public File(String id, String aipId, String representationId, List<String> path, boolean isDirectory) {
    this(id, aipId, representationId, path, isDirectory, false, null, null, null);
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
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

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public void setDirectory(boolean isDirectory) {
    this.isDirectory = isDirectory;
  }

  public boolean isReference() { return isReference; }

  public void setReference(boolean reference) {
    isReference = reference;
  }

  public String getReferenceUrl() {
    return referenceUrl;
  }

  public void setReferenceUrl(String referenceUrl) {
    this.referenceUrl = referenceUrl;
  }

  public String getReferenceManifest() {
    return referenceManifest;
  }

  public void setReferenceManifest(String referenceManifest) {
    this.referenceManifest = referenceManifest;
  }

  public String getReferenceUUID() {
    return referenceUUID;
  }

  public void setReferenceUUID(String referenceUUID) {
    this.referenceUUID = referenceUUID;
  }

//  @Override
//  public int hashCode() {
//    final int prime = 31;
//    int result = 1;
//    result = prime * result + ((aipId == null) ? 0 : aipId.hashCode());
//    result = prime * result + ((id == null) ? 0 : id.hashCode());
//    result = prime * result + (isDirectory ? 1231 : 1237);
//    result = prime * result + ((path == null) ? 0 : path.hashCode());
//    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
//    return result;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (obj == null)
//      return false;
//    if (getClass() != obj.getClass())
//      return false;
//    File other = (File) obj;
//    if (aipId == null) {
//      if (other.aipId != null)
//        return false;
//    } else if (!aipId.equals(other.aipId))
//      return false;
//    if (id == null) {
//      if (other.id != null)
//        return false;
//    } else if (!id.equals(other.id))
//      return false;
//    if (isDirectory != other.isDirectory)
//      return false;
//    if (path == null) {
//      if (other.path != null)
//        return false;
//    } else if (!path.equals(other.path))
//      return false;
//    if (representationId == null) {
//      if (other.representationId != null)
//        return false;
//    } else if (!representationId.equals(other.representationId))
//      return false;
//    return true;
//  }

//  @Override
//  public String toString() {
//    return "File [id=" + id + ", path=" + path + ", aipId=" + aipId + ", representationId=" + representationId
//      + ", isDirectory=" + isDirectory + "]";
//  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    File file = (File) o;

    if (isDirectory != file.isDirectory) return false;
    if (isReference != file.isReference) return false;
    if (id != null ? !id.equals(file.id) : file.id != null) return false;
    if (path != null ? !path.equals(file.path) : file.path != null) return false;
    if (aipId != null ? !aipId.equals(file.aipId) : file.aipId != null) return false;
    if (representationId != null ? !representationId.equals(file.representationId) : file.representationId != null)
      return false;
    if (referenceUrl != null ? !referenceUrl.equals(file.referenceUrl) : file.referenceUrl != null) return false;
    if (referenceManifest != null ? !referenceManifest.equals(file.referenceManifest) : file.referenceManifest != null)
      return false;
    return referenceUUID != null ? referenceUUID.equals(file.referenceUUID) : file.referenceUUID == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (aipId != null ? aipId.hashCode() : 0);
    result = 31 * result + (representationId != null ? representationId.hashCode() : 0);
    result = 31 * result + (isDirectory ? 1 : 0);
    result = 31 * result + (isReference ? 1 : 0);
    result = 31 * result + (referenceUrl != null ? referenceUrl.hashCode() : 0);
    result = 31 * result + (referenceManifest != null ? referenceManifest.hashCode() : 0);
    result = 31 * result + (referenceUUID != null ? referenceUUID.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "File{" +
        "id='" + id + '\'' +
        ", path=" + path +
        ", aipId='" + aipId + '\'' +
        ", representationId='" + representationId + '\'' +
        ", isDirectory=" + isDirectory +
        ", isReference=" + isReference +
        ", referenceUrl='" + referenceUrl + '\'' +
        ", referenceManifest='" + referenceManifest + '\'' +
        ", referenceUUID='" + referenceUUID + '\'' +
        '}';
  }
}
