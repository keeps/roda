/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.disposal.aipMetadata;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Gabriel Barros <gbarrps@keep.pt>
 */

@javax.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_DISPOSAL_DESTRUCTION_AIP_METADATA)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisposalDestructionAIPMetadata implements Serializable {
  private static final long serialVersionUID = 3434965815309088344L;

  private Date destructionOn;
  private String destructionBy;

  public DisposalDestructionAIPMetadata() {
  }

  public Date getDestructionOn() {
    return destructionOn;
  }

  public void setDestructionOn(Date destructionOn) {
    this.destructionOn = destructionOn;
  }

  public String getDestructionBy() {
    return destructionBy;
  }

  public void setDestructionBy(String destructionBy) {
    this.destructionBy = destructionBy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DisposalDestructionAIPMetadata that = (DisposalDestructionAIPMetadata) o;

    if (!Objects.equals(destructionOn, that.destructionOn))
      return false;
    return Objects.equals(destructionBy, that.destructionBy);
  }

  @Override
  public int hashCode() {
    int result = destructionOn != null ? destructionOn.hashCode() : 0;
    result = 31 * result + (destructionBy != null ? destructionBy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DisposalDestructionAIPMetadata{" + "destructionOn='" + destructionOn + '\'' + ", destructionBy='"
      + destructionBy + '\'' + '}';
  }
}
