package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "requestType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DescriptiveMetadataRequestForm.class, name = "DescriptiveMetadataRequestForm"),
  @JsonSubTypes.Type(value = DescriptiveMetadataRequestXML.class, name = "DescriptiveMetadataRequestXML")})
public abstract class CreateDescriptiveMetadataRequest implements Serializable {

  private String id;
  private String filename;
  private String type;
  private String version;
  private String rawTemplate;
  private boolean similar;
  private Permissions permissions;

  public CreateDescriptiveMetadataRequest() {
    // do nothing
  }

  public CreateDescriptiveMetadataRequest(String id, String filename, String type, String version, String rawTemplate,
    boolean similar, Permissions permissions) {
    this.id = id;
    this.filename = filename;
    this.type = type;
    this.version = version;
    this.rawTemplate = rawTemplate;
    this.similar = similar;
    this.permissions = permissions;
  }

  public CreateDescriptiveMetadataRequest(String id, String filename, String type, String version,
    Permissions permissions) {
    this.id = id;
    this.filename = filename;
    this.type = type;
    this.version = version;
    this.permissions = permissions;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
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

  public String getRawTemplate() {
    return rawTemplate;
  }

  public void setRawTemplate(String rawTemplate) {
    this.rawTemplate = rawTemplate;
  }

  public boolean isSimilar() {
    return similar;
  }

  public void setSimilar(boolean similar) {
    this.similar = similar;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public String getXml() {
    return null;
  }

  public void setXml(String xml) {
    // do nothing
  }

  public Set<MetadataValue> getValues() {
    return null;
  }
}
