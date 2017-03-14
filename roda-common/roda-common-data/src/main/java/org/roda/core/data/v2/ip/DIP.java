/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_DIP)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DIP implements IsModelObject {

  private static final long serialVersionUID = -7335470043357396783L;
  private static final int VERSION = 1;

  private String id;
  private String title;
  private String description;
  private String type;

  private Date dateCreated;
  private Date lastModified;
  private Boolean isPermanent;

  private Map<String, String> properties;

  private List<AIPLink> aipIds;
  private List<RepresentationLink> representationIds;
  private List<FileLink> fileIds;

  private Permissions permissions = new Permissions();

  public DIP() {
    super();

    this.id = UUID.randomUUID().toString();
    this.title = "";
    this.description = "";
    this.type = "";
    this.properties = new HashMap<>();
    this.dateCreated = new Date();
    this.lastModified = new Date();
    this.aipIds = new ArrayList<>();
    this.representationIds = new ArrayList<>();
    this.fileIds = new ArrayList<>();
  }

  public DIP(DIP other) {
    this(other.getId(), other.getTitle(), other.getDescription(), other.getType(), other.getDateCreated(),
      other.getLastModified(), other.getIsPermanent(), other.getProperties(), other.getAipIds(),
      other.getRepresentationIds(), other.getFileIds(), other.getPermissions());
  }

  public DIP(String id, String title, String description, String type, Date dateCreated, Date lastModified,
    Boolean isPermanent, Map<String, String> properties, List<AIPLink> aipIds,
    List<RepresentationLink> representationIds, List<FileLink> fileIds, Permissions permissions) {
    super();

    this.id = id;
    this.title = title;
    this.description = description;
    this.type = type;
    this.dateCreated = dateCreated;
    this.lastModified = lastModified;
    this.isPermanent = isPermanent;
    this.properties = properties;
    this.aipIds = aipIds;
    this.representationIds = representationIds;
    this.fileIds = fileIds;
    this.permissions = permissions;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getIsPermanent() {
    return isPermanent;
  }

  public void setIsPermanent(Boolean isPermanent) {
    this.isPermanent = isPermanent;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public List<AIPLink> getAipIds() {
    return aipIds;
  }

  public void setAipIds(List<AIPLink> aipIds) {
    this.aipIds = aipIds;
  }

  public List<RepresentationLink> getRepresentationIds() {
    return representationIds;
  }

  public void setRepresentationIds(List<RepresentationLink> representationIds) {
    this.representationIds = representationIds;
  }

  public List<FileLink> getFileIds() {
    return fileIds;
  }

  public void setFileIds(List<FileLink> fileIds) {
    this.fileIds = fileIds;
  }

  @JsonIgnore
  public void addAip(AIPLink link) {
    this.aipIds.add(link);
  }

  @JsonIgnore
  public void addRepresentation(RepresentationLink link) {
    this.representationIds.add(link);
  }

  @JsonIgnore
  public void addFile(FileLink link) {
    this.fileIds.add(link);
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DIP that = (DIP) o;

    if (id != null ? !id.equals(that.id) : that.id != null)
      return false;
    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (title != null ? !title.equals(that.title) : that.title != null)
      return false;
    if (dateCreated != null ? !dateCreated.equals(that.dateCreated) : that.dateCreated != null)
      return false;
    if (lastModified != null ? !lastModified.equals(that.lastModified) : that.lastModified != null)
      return false;
    if (isPermanent != null ? !isPermanent.equals(that.isPermanent) : that.isPermanent != null)
      return false;
    if (properties != null ? !properties.equals(that.properties) : that.properties != null)
      return false;
    if (permissions != null ? !permissions.equals(that.permissions) : that.permissions != null)
      return false;
    if (aipIds != null ? !aipIds.equals(that.aipIds) : that.aipIds != null)
      return false;
    if (representationIds != null ? !representationIds.equals(that.representationIds) : that.representationIds != null)
      return false;
    return fileIds != null ? !fileIds.equals(that.fileIds) : that.fileIds != null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
    result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
    result = 31 * result + (isPermanent != null ? isPermanent.hashCode() : 0);
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    result = 31 * result + (aipIds != null ? aipIds.hashCode() : 0);
    result = 31 * result + (representationIds != null ? representationIds.hashCode() : 0);
    result = 31 * result + (fileIds != null ? fileIds.hashCode() : 0);
    result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DIP [id=" + id + ", title=" + title + ", description=" + description + ", type=" + type + ", dateCreated="
      + dateCreated + ", lastModified=" + lastModified + ", isPermanent=" + isPermanent + ", properties=" + properties
      + ", aipIds=" + aipIds + ", representationIds=" + representationIds + ", fileIds=" + fileIds + ", permissions="
      + permissions + "]";
  }

}
