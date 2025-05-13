package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.util.Objects;

import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicalMetadata implements IsModelObject {

  @Serial
  private static final long serialVersionUID = 5460845130599948857L;

  private String id;
  private String aipId;
  private String representationId;
  private String type;

  public TechnicalMetadata() {
    super();
  }

  public TechnicalMetadata(String id, String aipId, String type) {
    this(id, aipId, null, type);
  }

  public TechnicalMetadata(String id, String aipId, String representationId, String type) {
    super();
    this.id = id;
    this.aipId = aipId;
    this.representationId = representationId;
    this.type = type;
  }

  @Override
  @JsonIgnore
  public int getClassVersion() {
    return 1;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAipId() {
    return aipId;
  }

  public void setAipId(String aipId) {
    this.aipId = aipId;
  }

  public String getRepresentationId() {
    return representationId;
  }

  public void setRepresentationId(String representationId) {
    this.representationId = representationId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    TechnicalMetadata that = (TechnicalMetadata) o;
    return Objects.equals(id, that.id) && Objects.equals(aipId, that.aipId)
      && Objects.equals(representationId, that.representationId) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, aipId, representationId, type);
  }

  @Override
  public String toString() {
    return "TechnicalMetadata [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", type="
      + type + "]";
  }

  @JsonIgnore
  public boolean isFromAIP() {
    return representationId == null;
  }
}
