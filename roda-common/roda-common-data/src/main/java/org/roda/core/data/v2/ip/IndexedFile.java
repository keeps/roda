/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.metadata.FileFormat;

import com.fasterxml.jackson.annotation.JsonInclude;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_FILE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexedFile
  implements IsIndexed, HasId, SetsUUID, HasPermissionFilters, HasStateFilter, HasInstanceID, HasInstanceName {
  private static final long serialVersionUID = 3303019735787641534L;

  private String uuid;
  private String parentUUID;

  private String aipId;
  private String representationId;
  private String representationUUID;
  private List<String> path;
  private List<String> ancestorsPath;
  private String id;
  private String referenceUUID;
  private String referenceURL;
  private String referenceManifest;

  private FileFormat fileFormat;
  private String originalName;
  private long size = 0;
  private boolean isDirectory = false;
  private boolean isReference = false;
  private String creatingApplicationName;
  private String creatingApplicationVersion;
  private String dateCreatedByApplication;
  private List<String> hash;
  private String storagePath;

  private List<String> ancestors;
  private Map<String, List<String>> otherProperties;

  private Map<String, Object> fields;

  private String instanceId;

  private String instanceName = null;

  private Date createdOn = null;

  public IndexedFile() {
    super();
  }

  public IndexedFile(String uuid, String parentUUID, String aipId, String representationId, String representationUUID,
    List<String> path, List<String> ancestorsPath, String id, String referenceUUID, String referenceURL,
    String referenceManifest, FileFormat fileFormat, String originalName, long size, boolean isDirectory,
    boolean isReference, String creatingApplicationName, String creatingApplicationVersion,
    String dateCreatedByApplication, List<String> hash, String storagePath, List<String> ancestors,
    Map<String, List<String>> otherProperties, String instanceId, String instanceName) {
    this.uuid = uuid;
    this.parentUUID = parentUUID;

    this.aipId = aipId;
    this.representationId = representationId;
    this.representationUUID = representationUUID;
    this.path = path;
    this.ancestorsPath = ancestorsPath;
    this.id = id;

    this.referenceUUID = referenceUUID;
    this.referenceURL = referenceURL;
    this.referenceManifest = referenceManifest;

    this.fileFormat = fileFormat;
    this.originalName = originalName;
    this.size = size;
    this.isDirectory = isDirectory;
    this.isReference = isReference;
    this.creatingApplicationName = creatingApplicationName;
    this.creatingApplicationVersion = creatingApplicationVersion;
    this.dateCreatedByApplication = dateCreatedByApplication;
    this.hash = hash;
    this.storagePath = storagePath;
    this.ancestors = ancestors;
    this.otherProperties = otherProperties;
    this.instanceId = instanceId;
    this.instanceName = instanceName;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public String getParentUUID() {
    return parentUUID;
  }

  public void setParentUUID(String parentUUID) {
    this.parentUUID = parentUUID;
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

  public String getRepresentationUUID() {
    return representationUUID;
  }

  public void setRepresentationUUID(String representationUUID) {
    this.representationUUID = representationUUID;
  }

  public List<String> getPath() {
    return path;
  }

  public void setPath(List<String> path) {
    this.path = path;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getReferenceUUID() {
    return referenceUUID;
  }

  public void setReferenceUUID(String referenceUUID) {
    this.referenceUUID = referenceUUID;
  }

  public String getReferenceURL() {
    return referenceURL;
  }

  public void setReferenceURL(String referenceURL) {
    this.referenceURL = referenceURL;
  }

  public String getReferenceManifest() {
    return referenceManifest;
  }

  public void setReferenceManifest(String referenceManifest) {
    this.referenceManifest = referenceManifest;
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

  public boolean isReference() {
    return isReference;
  }

  public void setReference(boolean reference) {
    isReference = reference;
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

  public List<String> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<String> ancestors) {
    this.ancestors = ancestors;
  }

  public Map<String, List<String>> getOtherProperties() {
    return otherProperties;
  }

  public void setOtherProperties(Map<String, List<String>> otherProperties) {
    this.otherProperties = otherProperties;
  }

  public List<String> getAncestorsPath() {
    return ancestorsPath;
  }

  public void setAncestorsPath(List<String> ancestorsPath) {
    this.ancestorsPath = ancestorsPath;
  }

  public Map<String, Object> getFields() {
    return fields;
  }

  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
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
    result = prime * result + ((otherProperties == null) ? 0 : otherProperties.hashCode());
    result = prime * result + ((parentUUID == null) ? 0 : parentUUID.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((ancestorsPath == null) ? 0 : ancestorsPath.hashCode());
    result = prime * result + ((representationId == null) ? 0 : representationId.hashCode());
    result = prime * result + ((representationUUID == null) ? 0 : representationUUID.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    result = prime * result + ((storagePath == null) ? 0 : storagePath.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
    result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
    result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
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
    if (otherProperties == null) {
      if (other.otherProperties != null)
        return false;
    } else if (!otherProperties.equals(other.otherProperties))
      if (parentUUID == null) {
        if (other.parentUUID != null)
          return false;
      } else if (!parentUUID.equals(other.parentUUID))
        return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (ancestorsPath == null) {
      if (other.ancestorsPath != null)
        return false;
    } else if (!ancestorsPath.equals(other.ancestorsPath))
      return false;
    if (representationId == null) {
      if (other.representationId != null)
        return false;
    } else if (!representationId.equals(other.representationId))
      return false;
    if (representationUUID == null) {
      if (other.representationUUID != null)
        return false;
    } else if (!representationUUID.equals(other.representationUUID))
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
    if (instanceId == null) {
      if (other.instanceId != null)
        return false;
    } else if (!instanceId.equals(other.instanceId))
      return false;
    if (instanceName == null) {
      if (other.instanceName != null)
        return false;
    } else if (!instanceName.equals(other.instanceName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedFile [uuid=" + uuid + ", parentUUID=" + parentUUID + ", aipId=" + aipId + ", representationId="
      + representationId + ", representationUUID=" + representationUUID + ", path=" + path + ", ancestorsPath="
      + ancestorsPath + ", id=" + id + ", fileFormat=" + fileFormat + ", originalName=" + originalName + ", size="
      + size + ", isDirectory=" + isDirectory + ", creatingApplicationName=" + creatingApplicationName
      + ", creatingApplicationVersion=" + creatingApplicationVersion + ", dateCreatedByApplication="
      + dateCreatedByApplication + ", hash=" + hash + ", storagePath=" + storagePath + ", ancestors=" + ancestors
      + ", otherProperties=" + otherProperties + ", instanceId=" + instanceId + ", instanceName=" + instanceName
      + ", createdOn=" + createdOn + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("uuid", "parentUUID", "aipId", "representationId", "representationUUID", "path",
      "ancestorsPath", "id", "fileFormat", "originalName", "size", "isDirectory", "creatingApplicationName",
      "creatingApplicationVersion", "dateCreatedByApplication", "hash", "storagePath", "ancestors", "otherProperties",
      "instanceId", "instanceName", "createdOn");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(uuid, parentUUID, aipId, representationId, representationUUID, path, ancestorsPath, id,
      fileFormat, originalName, size, isDirectory, creatingApplicationName, creatingApplicationVersion,
      dateCreatedByApplication, hash, storagePath, ancestors, otherProperties, instanceId, instanceName, createdOn);
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_AIP_ID, RodaConstants.FILE_REPRESENTATION_ID,
      RodaConstants.FILE_PATH, RodaConstants.INDEX_ID);
  }

  public String getSHA256Checksum() {
    if (hash.size() != 0) {
      return hash.get(1).split(" ")[0];
    } else
      return null;
  }

  public String getSHA256Type() {
    if (hash.size() != 0) {
      return hash.get(1).split("\\(")[1].split(",")[0];
    } else
      return null;
  }

}
