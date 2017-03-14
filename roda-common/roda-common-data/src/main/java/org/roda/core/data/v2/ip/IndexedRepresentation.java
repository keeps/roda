/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexedRepresentation extends Representation implements IsIndexed, HasPermissionFilters {
  private static final long serialVersionUID = -950545608880793468L;

  private String uuid;

  private long sizeInBytes;
  private long numberOfDataFiles;

  private long numberOfDocumentationFiles;
  private long numberOfSchemaFiles;

  private List<String> ancestors;

  public IndexedRepresentation() {
    super();
  }

  public IndexedRepresentation(String uuid, String id, String aipId, boolean original, String type, long sizeInBytes,
    long totalNumberOfFiles, long numberOfDocumentationFiles, long numberOfSchemaFiles, List<String> ancestors) {
    super(id, aipId, original, type);
    this.uuid = uuid;
    this.sizeInBytes = sizeInBytes;
    this.numberOfDataFiles = totalNumberOfFiles;
    this.numberOfDocumentationFiles = numberOfDocumentationFiles;
    this.numberOfSchemaFiles = numberOfSchemaFiles;
    this.ancestors = ancestors;
  }

  @Override
  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
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

  public List<String> getAncestors() {
    return ancestors;
  }

  public void setAncestors(List<String> ancestors) {
    this.ancestors = ancestors;
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
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "IndexedRepresentation [uuid=" + uuid + ", sizeInBytes=" + sizeInBytes + ", numberOfDataFiles="
      + numberOfDataFiles + ", numberOfDocumentationFiles=" + numberOfDocumentationFiles + ", numberOfSchemaFiles="
      + numberOfSchemaFiles + ", ancestors=" + ancestors + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("uuid", "sizeInBytes", "numberOfDataFiles", "numberOfDocumentationFiles",
      "numberOfSchemaFiles", "ancestors");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(uuid, sizeInBytes, numberOfDataFiles, numberOfDocumentationFiles, numberOfSchemaFiles,
      ancestors);
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.REPRESENTATION_AIP_ID,
      RodaConstants.REPRESENTATION_ID);
  }

}
