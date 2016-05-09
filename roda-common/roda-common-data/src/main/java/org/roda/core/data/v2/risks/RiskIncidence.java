/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "riskincidence")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RiskIncidence implements IsIndexed, Serializable {

  private static final long serialVersionUID = -1089167070045254627L;

  private String id;
  private String objectId;
  private List<String> risks;

  public RiskIncidence() {
    super();
    this.setRisks(new ArrayList<String>());
  }

  public RiskIncidence(RiskIncidence incidence) {
    this.id = incidence.getId();
    this.setObjectId(incidence.getObjectId());
    this.setRisks(incidence.getRisks());
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public List<String> getRisks() {
    return risks;
  }

  public void setRisks(List<String> risks) {
    this.risks = risks;
  }

  public void addRisk(String riskId) {
    this.risks.add(riskId);
  }

  public void removeRisk(String riskId) {
    this.risks.remove(riskId);
  }

  @Override
  public String toString() {
    return "Format [id=" + id + ", objectId=" + objectId + ", risks=" + risks + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

}
