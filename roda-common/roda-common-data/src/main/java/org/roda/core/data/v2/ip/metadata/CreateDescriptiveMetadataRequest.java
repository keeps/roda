package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.ip.Permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DescriptiveMetadataRequestForm.class, name = "DescriptiveMetadataRequestForm"),
  @JsonSubTypes.Type(value = DescriptiveMetadataRequestXML.class, name = "DescriptiveMetadataRequestXML")})
@Schema(type = "object", subTypes = {DescriptiveMetadataRequestForm.class,
  DescriptiveMetadataRequestXML.class}, discriminatorMapping = {
    @DiscriminatorMapping(value = "DescriptiveMetadataRequestForm", schema = DescriptiveMetadataRequestForm.class),
    @DiscriminatorMapping(value = "DescriptiveMetadataRequestXML", schema = DescriptiveMetadataRequestXML.class)}, discriminatorProperty = "@type")
public abstract class CreateDescriptiveMetadataRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = 7673112012182240190L;

  private String id;
  private String filename;
  private String type;
  private String version;
  private boolean similar;
  private Permissions permissions;

  protected CreateDescriptiveMetadataRequest() {
    // empty constructor
  }

  protected CreateDescriptiveMetadataRequest(String id, String filename, String type, String version, boolean similar, Permissions permissions) {
    this.id = id;
    this.filename = filename;
    this.type = type;
    this.version = version;
    this.similar = similar;
    this.permissions = permissions;
  }

  protected CreateDescriptiveMetadataRequest(String id, String filename, String type, String version,
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

  public abstract String getXml();

  public abstract void setXml(String xml);

  public abstract Set<MetadataValue> getValues();
}
