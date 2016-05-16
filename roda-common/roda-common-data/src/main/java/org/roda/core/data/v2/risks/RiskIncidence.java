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

  private String id = null;
  private String aipId = null;
  private String representationId = null;
  private List<String> filePath = null;
  private String fileId = null;
  private String objectClass = null;
  private List<String> risks;

  public RiskIncidence() {
    super();
    this.setRisks(new ArrayList<String>());
    this.setFilePath(new ArrayList<String>());
  }

  public RiskIncidence(RiskIncidence incidence) {
    this.id = incidence.getId();
    this.setAipId(incidence.getAipId());
    this.setRepresentationId(incidence.getRepresentationId());
    this.setFilePath(incidence.getFilePath());
    this.setFileId(incidence.getFileId());
    this.setObjectClass(incidence.getObjectClass());
    this.setRisks(incidence.getRisks());
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
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

  public List<String> getFilePath() {
    return filePath;
  }

  public void setFilePath(List<String> filePath) {
    this.filePath = filePath;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getObjectClass() {
    return objectClass;
  }

  public void setObjectClass(String objectClass) {
    this.objectClass = objectClass;
  }

  @Override
  public String toString() {
    return "RiskIncidence [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", fileId="
      + fileId + ", objectClass=" + objectClass + ", risks=" + risks + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

}
