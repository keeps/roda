/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;

public class IndexedPreservationAgent implements IsIndexed, HasId {
  private static final long serialVersionUID = 7864328669898523851L;
  private String id;
  private String name;
  private String version;
  private String type;
  private String note;
  private String extension;
  private List<String> roles;
  private Map<String, Object> fields;
  private String instanceId;
  private String instanceName;

  private Date createdOn;

  public IndexedPreservationAgent() {
    super();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
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
  public String toString() {
    return "IndexedPreservationAgent [id=" + id + ", name=" + name + ", type=" + type + ", note=" + note
      + ", extension=" + extension + ", roles=" + roles + ", instanceId=" + instanceId + ", instanceName="
      + instanceName + ", createdOn=" + createdOn + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "type", "note", "extension", "roles", "instanceId", "instanceName",
      "createdOn");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, name, type, note, extension, roles, instanceId, instanceName, createdOn);
  }

  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

}
