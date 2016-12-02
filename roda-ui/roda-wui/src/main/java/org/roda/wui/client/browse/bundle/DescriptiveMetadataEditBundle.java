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

public class DescriptiveMetadataEditBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private String id;
  private String type;
  private String xml;
  private String version;
  private String rawTemplate;
  private Set<MetadataValue> values;
  private boolean similar;

  public DescriptiveMetadataEditBundle() {
    super();
  }

  public DescriptiveMetadataEditBundle(String id, String type, String version, String xml, String rawTemplate,
    Set<MetadataValue> values, boolean similar) {
    super();
    this.id = id;
    this.type = type;
    this.xml = xml;
    this.version = version;
    this.rawTemplate = rawTemplate;
    this.values = values;
    this.similar = similar;
  }

  public DescriptiveMetadataEditBundle(String id, String type, String version, String xml) {
    super();
    this.id = id;
    this.type = type;
    this.xml = xml;
    this.version = version;
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

  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getRawTemplate() {
    return rawTemplate;
  }

  public void setRawTemplate(String rawTemplate) {
    this.rawTemplate = rawTemplate;
  }

  public Set<MetadataValue> getValues() {
    return values;
  }

  public void setValues(Set<MetadataValue> values) {
    this.values = values;
  }

  public boolean isSimilar() {
    return similar;
  }

  public void setSimilar(boolean similar) {
    this.similar = similar;
  }
}
