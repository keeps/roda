/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.Set;

import org.roda.wui.client.browse.MetadataValue;

public class SupportedMetadataTypeBundle implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String type;
  private String version;
  private String label;
  private String template;
  private Set<MetadataValue> values;

  public SupportedMetadataTypeBundle() {
    super();
  }

  public SupportedMetadataTypeBundle(String id, String type, String version, String label, String template) {
    super();
    this.id = id;
    this.type = type;
    this.version = version;
    this.label = label;
    this.template = template;
  }

  public SupportedMetadataTypeBundle(String id, String type, String version, String label, String template,
    Set<MetadataValue> values) {
    super();
    this.id = id;
    this.type = type;
    this.version = version;
    this.label = label;
    this.template = template;
    this.values = values;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }
}
