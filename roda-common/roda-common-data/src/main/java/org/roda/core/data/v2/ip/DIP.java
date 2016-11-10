/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "dip")
public class DIP implements IsModelObject, IsIndexed {

  private static final long serialVersionUID = -7335470043357396783L;
  public static int VERSION = 1;

  private String id;
  private String title;
  private String description;

  private Date dateCreated;
  private Date lastModified;
  private Boolean isPermanent;

  private String openExternalURL;
  private String deleteExternalURL;

  private List<AIPLink> aipIds;
  private List<RepresentationLink> representationIds;
  private List<FileLink> fileIds;

  private Permissions permissions = new Permissions();

  public DIP() {
    super();

    this.title = "";
    this.description = "";
    this.openExternalURL = "";
    this.deleteExternalURL = "";
    this.dateCreated = new Date();
    this.lastModified = new Date();
    this.aipIds = new ArrayList<AIPLink>();
    this.representationIds = new ArrayList<RepresentationLink>();
    this.fileIds = new ArrayList<FileLink>();
  }

  public DIP(DIP other) {
    this(other.getId(), other.getTitle(), other.getDescription(), other.getDateCreated(), other.getLastModified(),
      other.getIsPermanent(), other.getOpenExternalURL(), other.getDeleteExternalURL(), other.getAipIds(),
      other.getRepresentationIds(), other.getFileIds(), other.getPermissions());
  }

  public DIP(String id, String title, String description, Date dateCreated, Date lastModified, Boolean isPermanent,
    String openExternalURL, String deleteExternalURL, List<AIPLink> aipIds, List<RepresentationLink> representationIds,
    List<FileLink> fileIds, Permissions permissions) {
    super();

    this.id = id;
    this.title = title;
    this.description = description;
    this.dateCreated = dateCreated;
    this.lastModified = lastModified;
    this.isPermanent = isPermanent;
    this.openExternalURL = openExternalURL;
    this.deleteExternalURL = deleteExternalURL;
    this.aipIds = aipIds;
    this.representationIds = representationIds;
    this.fileIds = fileIds;
    this.permissions = permissions;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
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

  public String getOpenExternalURL() {
    return openExternalURL;
  }

  public void setOpenExternalURL(String openExternalURL) {
    this.openExternalURL = openExternalURL;
  }

  public String getDeleteExternalURL() {
    return deleteExternalURL;
  }

  public void setDeleteExternalURL(String deleteExternalURL) {
    this.deleteExternalURL = deleteExternalURL;
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
    if (openExternalURL != null ? !openExternalURL.equals(that.openExternalURL) : that.openExternalURL != null)
      return false;
    if (deleteExternalURL != null ? !deleteExternalURL.equals(that.deleteExternalURL) : that.deleteExternalURL != null)
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
    result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
    result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0);
    result = 31 * result + (isPermanent != null ? isPermanent.hashCode() : 0);
    result = 31 * result + (openExternalURL != null ? openExternalURL.hashCode() : 0);
    result = 31 * result + (deleteExternalURL != null ? deleteExternalURL.hashCode() : 0);
    result = 31 * result + (aipIds != null ? aipIds.hashCode() : 0);
    result = 31 * result + (representationIds != null ? representationIds.hashCode() : 0);
    result = 31 * result + (fileIds != null ? fileIds.hashCode() : 0);
    result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DIP [id=" + id + ", title=" + title + ", description=" + description + ", dateCreated=" + dateCreated
      + ", lastModified=" + lastModified + ", isPermanent=" + isPermanent + ", openExternalURL=" + openExternalURL
      + ", deleteExternalURL=" + deleteExternalURL + ", aipIds=" + aipIds + ", representationIds=" + representationIds
      + ", fileIds=" + fileIds + ", permissions=" + permissions + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "title", "description", "dateCreated", "lastModified", "isPermanent", "openExternalURL",
      "deleteExternalURL", "aipIds", "representationIds", "fileIds", "permissions");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, title, description, dateCreated, lastModified, isPermanent, openExternalURL,
      deleteExternalURL, aipIds, representationIds, fileIds, permissions);
  }

}
