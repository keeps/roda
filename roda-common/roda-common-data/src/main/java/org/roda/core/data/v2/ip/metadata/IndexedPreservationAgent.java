/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

public class IndexedPreservationAgent implements IsIndexed {
  private static final long serialVersionUID = 7864328669898523851L;
  private String id;
  private String name;
  private String version;
  private String type;
  private String note;
  private String extension;
  private List<String> roles;

  public IndexedPreservationAgent() {
    super();
  }

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

  @Override
  public String toString() {
    return "IndexedPreservationAgent [id=" + id + ", name=" + name + ", type=" + type + ", note=" + note
      + ", extension=" + extension + ", roles=" + roles + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "type", "note", "extension", "roles");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, name, type, note, extension, roles);
  }

  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

}
