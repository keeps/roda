/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_RISK)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IndexedRisk extends Risk implements IsIndexed {
  private static final long serialVersionUID = 2864416437668370485L;
  private int incidencesCount = 0;
  private int unmitigatedIncidencesCount = 0;

  private Map<String, Object> fields;

  public IndexedRisk() {
    super();
  }

  public IndexedRisk(IndexedRisk risk) {
    super(risk);
    this.incidencesCount = risk.getIncidencesCount();
  }

  public int getIncidencesCount() {
    return incidencesCount;
  }

  public void setIncidencesCount(int incidencesCount) {
    this.incidencesCount = incidencesCount;
  }

  public int getUnmitigatedIncidencesCount() {
    return unmitigatedIncidencesCount;
  }

  public void setUnmitigatedIncidencesCount(int unmitigatedIncidencesCount) {
    this.unmitigatedIncidencesCount = unmitigatedIncidencesCount;
  }

  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public String toString() {
    return "IndexedRisk [super=" + super.toString() + ", incidencesCount=" + incidencesCount
      + ", unmitigatedIncidencesCount=" + unmitigatedIncidencesCount + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "description", "identifiedOn", "identifiedBy", "categories", "notes",
      "preMitigationProbability", "preMitigationImpact", "preMitigationSeverity", "preMitigationNotes",
      "posMitigationProbability", "posMitigationImpact", "posMitigationSeverity", "posMitigationNotes",
      "mitigationStrategy", "mitigationOwnerType", "mitigationOwner", "mitigationRelatedEventIdentifierType",
      "mitigationRelatedEventIdentifierValue", "createdOn", "createdBy", "updatedOn", "updatedBy", "incidencesCount",
      "unmitigatedIncidencesCount", "instanceId");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(new Object[] {getId(), getName(), getDescription(), getIdentifiedOn(), getIdentifiedBy(),
      getCategories(), getNotes(), getPreMitigationProbability(), getPreMitigationImpact(), getPreMitigationSeverity(),
      getPreMitigationNotes(), getPostMitigationProbability(), getPostMitigationImpact(), getPostMitigationSeverity(),
      getPreMitigationNotes(), getMitigationStrategy(), getMitigationOwnerType(), getMitigationOwner(),
      getMitigationRelatedEventIdentifierType(), getMitigationRelatedEventIdentifierValue(), getCreatedOn(),
      getCreatedBy(), getUpdatedOn(), getUpdatedBy(), getIncidencesCount(), getUnmitigatedIncidencesCount(),
      getInstanceId()});
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
   * @param fields
   *          the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + incidencesCount;
    result = prime * result + unmitigatedIncidencesCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    IndexedRisk other = (IndexedRisk) obj;
    if (incidencesCount != other.incidencesCount)
      return false;
    if (unmitigatedIncidencesCount != other.unmitigatedIncidencesCount)
      return false;
    return true;
  }

}
