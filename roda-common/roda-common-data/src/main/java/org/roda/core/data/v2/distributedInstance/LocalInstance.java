package org.roda.core.data.v2.distributedInstance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import java.util.Date;
import java.util.Objects;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_LOCAL_INSTANCE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalInstance implements IsModelObject {
  private static final int VERSION = 1;
  private static final long serialVersionUID = -1056506624373739060L;

  private String id;
  private String accessKey;
  private String centralInstanceURL;

  private LocalInstanceIdentifierState instanceIdentifierState = LocalInstanceIdentifierState.INACTIVE;

  private Date createdOn;
  private String createdBy;
  private Date updatedOn;
  private String updatedBy;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getCentralInstanceURL() {
    return centralInstanceURL;
  }

  public void setCentralInstanceURL(String centralInstanceURL) {
    this.centralInstanceURL = centralInstanceURL;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getUpdatedOn() {
    return updatedOn;
  }

  public void setUpdatedOn(Date updatedOn) {
    this.updatedOn = updatedOn;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public LocalInstanceIdentifierState getInstanceIdentifierState() {
    return instanceIdentifierState;
  }

  public void setInstanceIdentifierState(LocalInstanceIdentifierState instanceIdentifierState) {
    this.instanceIdentifierState = instanceIdentifierState;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return VERSION;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    LocalInstance that = (LocalInstance) o;
    return Objects.equals(id, that.id) && Objects.equals(accessKey, that.accessKey)
      && Objects.equals(centralInstanceURL, that.centralInstanceURL)
      && instanceIdentifierState == that.instanceIdentifierState && Objects.equals(createdOn, that.createdOn)
      && Objects.equals(createdBy, that.createdBy) && Objects.equals(updatedOn, that.updatedOn)
      && Objects.equals(updatedBy, that.updatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, accessKey, centralInstanceURL, instanceIdentifierState, createdOn, createdBy, updatedOn,
      updatedBy);
  }

  @Override
  public String toString() {
    return "LocalInstance{" + "id='" + id + '\'' + ", accessKey='" + accessKey + '\'' + ", centralInstanceURL='"
      + centralInstanceURL + '\'' + ", instanceIdentifierState=" + instanceIdentifierState + ", createdOn=" + createdOn
      + ", createdBy='" + createdBy + '\'' + ", updatedOn=" + updatedOn + ", updatedBy='" + updatedBy + '\'' + '}';
  }
}
