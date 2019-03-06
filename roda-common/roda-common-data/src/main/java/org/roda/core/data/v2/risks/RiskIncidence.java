/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_INCIDENCE)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class RiskIncidence implements IsModelObject, IsIndexed, HasId {
  private static final long serialVersionUID = -1089167070045254627L;

  public enum INCIDENCE_STATUS {
    UNMITIGATED, MITIGATED, ACCEPT_RISK, FALSE_POSITIVE;
  }

  private String id = null;
  private String aipId = null;
  private String representationId = null;
  private List<String> filePath = new ArrayList<>();
  private String fileId = null;
  private String objectClass = null;
  private String riskId;
  private String description = null;
  private boolean byPlugin = false;

  private INCIDENCE_STATUS status = null;
  private SEVERITY_LEVEL severity = null;
  private Date detectedOn = null;
  private String detectedBy = null;
  private Date mitigatedOn = null;
  private String mitigatedBy = null;
  private String mitigatedDescription = null;
  
  private Map<String, Object> fields;

  public RiskIncidence() {
    super();
  }

  public RiskIncidence(RiskIncidence incidence) {
    this.id = incidence.getId();
    this.setAipId(incidence.getAipId());
    this.setRepresentationId(incidence.getRepresentationId());
    this.setFilePath(incidence.getFilePath());
    this.setFileId(incidence.getFileId());
    this.setObjectClass(incidence.getObjectClass());
    this.setRiskId(incidence.getRiskId());
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRiskId() {
    return riskId;
  }

  public void setRiskId(String riskId) {
    this.riskId = riskId;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isByPlugin() {
    return byPlugin;
  }

  public void setByPlugin(boolean byPlugin) {
    this.byPlugin = byPlugin;
  }

  public INCIDENCE_STATUS getStatus() {
    return status;
  }

  public void setStatus(INCIDENCE_STATUS status) {
    this.status = status;
  }

  public SEVERITY_LEVEL getSeverity() {
    return severity;
  }

  public void setSeverity(SEVERITY_LEVEL severity) {
    this.severity = severity;
  }

  public Date getDetectedOn() {
    return detectedOn;
  }

  public void setDetectedOn(Date detectedOn) {
    this.detectedOn = detectedOn;
  }

  public String getDetectedBy() {
    return detectedBy;
  }

  public void setDetectedBy(String detectedBy) {
    this.detectedBy = detectedBy;
  }

  public Date getMitigatedOn() {
    return mitigatedOn;
  }

  public void setMitigatedOn(Date mitigatedOn) {
    this.mitigatedOn = mitigatedOn;
  }

  public String getMitigatedBy() {
    return mitigatedBy;
  }

  public void setMitigatedBy(String mitigatedBy) {
    this.mitigatedBy = mitigatedBy;
  }

  public String getMitigatedDescription() {
    return mitigatedDescription;
  }

  public void setMitigatedDescription(String mitigatedDescription) {
    this.mitigatedDescription = mitigatedDescription;
  }

  @Override
  public String toString() {
    return "RiskIncidence [id=" + id + ", aipId=" + aipId + ", representationId=" + representationId + ", filePath="
      + filePath + ", fileId=" + fileId + ", objectClass=" + objectClass + ", riskId=" + riskId + ", description="
      + description + ", byPlugin=" + byPlugin + ", status=" + status + ", severity=" + severity + ", detectedOn="
      + detectedOn + ", detectedBy=" + detectedBy + ", mitigatedOn=" + mitigatedOn + ", mitigatedBy=" + mitigatedBy
      + ", mitigatedDescription=" + mitigatedDescription + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "aipId", "representationId", "filePath", "fileId", "objectClass", "riskId",
      "description", "byPlugin", "status", "severity", "detectedOn", "detectedBy", "mitigatedOn", "mitigatedBy",
      "mitigatedDescription");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, aipId, representationId, filePath, fileId, objectClass, riskId, description, byPlugin,
      status, severity, detectedOn, detectedBy, mitigatedOn, mitigatedBy, mitigatedDescription);
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

}
