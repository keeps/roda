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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

public class IndexedRisk extends Risk implements IsIndexed {

  private static final long serialVersionUID = 2864416437668370485L;
  private int objectsSize = 0;

  public IndexedRisk() {
    super();
  }

  public IndexedRisk(IndexedRisk risk) {
    super(risk);
    this.objectsSize = risk.getObjectsSize();
  }

  public int getObjectsSize() {
    return objectsSize;
  }

  public void setObjectsSize(int objectsSize) {
    this.objectsSize = objectsSize;
  }

  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public String toString() {
    return "IndexedRisk [super=" + super.toString() + ", objectsSize=" + objectsSize + "]";
  }

  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "description", "identifiedOn", "identifiedBy", "category", "notes",
      "preMitigationProbability", "preMitigationImpact", "preMitigationSeverity", "preMitigationNotes",
      "posMitigationProbability", "posMitigationImpact", "posMitigationSeverity", "posMitigationNotes",
      "mitigationStrategy", "mitigationOwnerType", "mitigationOwner", "mitigationRelatedEventIdentifierType",
      "mitigationRelatedEventIdentifierValue", "createdOn", "createdBy", "updatedOn", "updatedBy", "objectsSize");
  }

  public List<Object> toCsvValues() {
    return Arrays.asList(new Object[] {getId(), getName(), getDescription(), getIdentifiedOn(), getIdentifiedBy(),
      getCategory(), getNotes(), getPreMitigationProbability(), getPreMitigationImpact(), getPreMitigationSeverity(),
      getPreMitigationNotes(), getPostMitigationProbability(), getPostMitigationImpact(), getPostMitigationSeverity(),
      getPreMitigationNotes(), getMitigationStrategy(), getMitigationOwnerType(), getMitigationOwner(),
      getMitigationRelatedEventIdentifierType(), getMitigationRelatedEventIdentifierValue(), getCreatedOn(),
      getCreatedBy(), getUpdatedOn(), getUpdatedBy(), objectsSize});
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

}
