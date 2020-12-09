package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */
@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_CONFIRMATION_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalConfirmationAIPMetadata implements IsModelObject {

  private static final long serialVersionUID = -5359948610641515785L;
  private String id;
  private String confirmationOn;
  private String confirmationBy;
  private DisposalDestructionAIPMetadata destruction;

  public DisposalConfirmationAIPMetadata() {
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getConfirmationOn() {
    return confirmationOn;
  }

  public void setConfirmationOn(String confirmationOn) {
    this.confirmationOn = confirmationOn;
  }

  public String getConfirmationBy() {
    return confirmationBy;
  }

  public void setConfirmationBy(String confirmationBy) {
    this.confirmationBy = confirmationBy;
  }

  public DisposalDestructionAIPMetadata getDestruction() {
    return destruction;
  }

  public void setDestruction(DisposalDestructionAIPMetadata destruction) {
    this.destruction = destruction;
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 0;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalConfirmationAIPMetadata that = (DisposalConfirmationAIPMetadata) o;

    if (!Objects.equals(id, that.id))
      return false;
    if (!Objects.equals(confirmationOn, that.confirmationOn))
      return false;
    if (!Objects.equals(confirmationBy, that.confirmationBy))
      return false;
    return Objects.equals(destruction, that.destruction);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (confirmationOn != null ? confirmationOn.hashCode() : 0);
    result = 31 * result + (confirmationBy != null ? confirmationBy.hashCode() : 0);
    result = 31 * result + (destruction != null ? destruction.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalConfirmationAIPMetadata{" + "id='" + id + '\'' + ", confirmationOn='" + confirmationOn + '\''
      + ", confirmationBy='" + confirmationBy + '\'' + ", destruction=" + destruction + '}';
  }
}
