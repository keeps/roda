/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexedRepresentation extends Representation
  implements IsIndexed, SetsUUID, HasPermissionFilters, HasStateFilter, HasInstanceID, HasInstanceName {
  @Serial
  private static final long serialVersionUID = -950545608880793468L;

  private String uuid;
  private String title;

  private long sizeInBytes;
  private long numberOfDataFiles;
  private long numberOfDataFolders;

  private long numberOfDocumentationFiles;
  private long numberOfSchemaFiles;

  private String instanceName;
  private boolean isLocalInstance = false;

  private List<String> ancestors;

  private Map<String, Object> fields;


  public IndexedRepresentation() {
    super();
  }

  public IndexedRepresentation(String uuid, String id, String aipId, boolean original, String type, String title,
    String instanceId, String instanceName, long sizeInBytes, long totalNumberOfFiles, long totalNumberOfFolders,
    long numberOfDocumentationFiles, long numberOfSchemaFiles, List<String> ancestors) {
    super(id, aipId, original, type);
    this.uuid = uuid;
    this.setTitle(title);
    this.setInstanceId(instanceId);
    this.sizeInBytes = sizeInBytes;
    this.numberOfDataFiles = totalNumberOfFiles;
    this.setNumberOfDataFolders(totalNumberOfFolders);
    this.numberOfDocumentationFiles = numberOfDocumentationFiles;
    this.numberOfSchemaFiles = numberOfSchemaFiles;
    this.ancestors = ancestors;
    this.instanceName = instanceName;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

  public long getNumberOfDataFiles() {
    return numberOfDataFiles;
  }

  public void setNumberOfDataFiles(long numberOfDataFiles) {
    this.numberOfDataFiles = numberOfDataFiles;
  }

  public long getNumberOfDataFolders() {
    return numberOfDataFolders;
  }

  public void setNumberOfDataFolders(long numberOfDataFolders) {
    this.numberOfDataFolders = numberOfDataFolders;
  }

  public long getNumberOfDocumentationFiles() {
    return numberOfDocumentationFiles;
  }

  public void setNumberOfDocumentationFiles(long numberOfDocumentationFiles) {
    this.numberOfDocumentationFiles = numberOfDocumentationFiles;
  }

  public long getNumberOfSchemaFiles() {
    return numberOfSchemaFiles;
  }

  public void setNumberOfSchemaFiles(long numberOfSchemaFiles) {
    this.numberOfSchemaFiles = numberOfSchemaFiles;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public List<String> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<String> ancestors) {
    this.ancestors = ancestors;
  }

  public Map<String, Object> getFields() {
    return fields;
  }

  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

  public boolean isLocalInstance() {
    return isLocalInstance;
  }

  public void setLocalInstance(boolean localInstance) {
    isLocalInstance = localInstance;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (int) (numberOfDocumentationFiles ^ (numberOfDocumentationFiles >>> 32));
    result = prime * result + (int) (numberOfSchemaFiles ^ (numberOfSchemaFiles >>> 32));
    result = prime * result + (int) (sizeInBytes ^ (sizeInBytes >>> 32));
    result = prime * result + (int) (numberOfDataFiles ^ (numberOfDataFiles >>> 32));
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedRepresentation other = (IndexedRepresentation) obj;
    if (numberOfDocumentationFiles != other.numberOfDocumentationFiles)
      return false;
    if (numberOfSchemaFiles != other.numberOfSchemaFiles)
      return false;
    if (sizeInBytes != other.sizeInBytes)
      return false;
    if (numberOfDataFiles != other.numberOfDataFiles)
      return false;
    if (numberOfDataFolders != other.numberOfDataFolders)
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
    return "IndexedRepresentation [uuid=" + uuid + ", title=" + title + ", instanceId=" + super.getInstanceId()
      + ", instanceName=" + instanceName + ", sizeInBytes=" + sizeInBytes + ", numberOfDataFiles=" + numberOfDataFiles
      + ", numberOfDataFolders=" + numberOfDataFolders + " numberOfDocumentationFiles=" + numberOfDocumentationFiles
      + ", numberOfSchemaFiles=" + numberOfSchemaFiles + ", ancestors=" + ancestors + ", createdOn="
      + super.getCreatedOn() + ", createdBy=" + super.getCreatedBy() + ", updatedOn=" + super.getUpdatedOn()
      + ", updatedBy=" + super.getUpdatedBy() + ", representationStates=" + super.getRepresentationStates() + ']';
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("uuid", "title", "instanceId", "instanceName", "sizeInBytes", "numberOfDataFiles",
      "numberOfDataFolders", "numberOfDocumentationFiles", "numberOfSchemaFiles", "ancestors", "createdOn", "createdBy",
      "updatedOn", "updatedBy", "representationStates");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(uuid, title, super.getInstanceId(), instanceName, sizeInBytes, numberOfDataFiles,
      numberOfDataFolders, numberOfDocumentationFiles, numberOfSchemaFiles, ancestors, super.getCreatedOn(),
      super.getCreatedBy(), super.getUpdatedOn(), super.getUpdatedBy(), super.getRepresentationStates());
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID,
      RodaConstants.REPRESENTATION_ID);
  }
}
