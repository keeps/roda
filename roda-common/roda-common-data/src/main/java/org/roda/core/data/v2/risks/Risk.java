/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.NamedIndexedModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_RISK)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Risk extends NamedIndexedModel implements IsModelObject {

  private static final long serialVersionUID = -585753367605901060L;

  public enum SEVERITY_LEVEL {
    LOW, MODERATE, HIGH;
  }

  private String description = null;
  private Date identifiedOn = null;
  private String identifiedBy = null;
  private String category = null;
  private String notes = null;

  private int preMitigationProbability = 0;
  private int preMitigationImpact = 0;
  private int preMitigationSeverity = 0;
  private SEVERITY_LEVEL preMitigationSeverityLevel = null;
  private String preMitigationNotes = null;

  private int postMitigationProbability = 0;
  private int postMitigationImpact = 0;
  private int postMitigationSeverity = 0;
  private SEVERITY_LEVEL postMitigationSeverityLevel = null;
  private String postMitigationNotes = null;

  private String mitigationStrategy = null;
  private String mitigationOwnerType = null;
  private String mitigationOwner = null;
  private String mitigationRelatedEventIdentifierType = null;
  private String mitigationRelatedEventIdentifierValue = null;

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  public Risk() {
    super();
    this.identifiedOn = new Date();
    this.createdOn = new Date();
    this.updatedOn = new Date();
  }

  public Risk(Risk risk) {
    super(risk.getId(), risk.getName());
    this.description = risk.getDescription();
    this.identifiedOn = risk.getIdentifiedOn();
    this.identifiedBy = risk.getIdentifiedBy();
    this.category = risk.getCategory();
    this.notes = risk.getNotes();

    this.preMitigationProbability = risk.getPreMitigationProbability();
    this.preMitigationImpact = risk.getPreMitigationImpact();
    this.preMitigationSeverity = risk.getPreMitigationSeverity();
    this.preMitigationSeverityLevel = risk.getPreMitigationSeverityLevel();
    this.preMitigationNotes = risk.getPreMitigationNotes();

    this.postMitigationProbability = risk.getPostMitigationProbability();
    this.postMitigationImpact = risk.getPostMitigationImpact();
    this.postMitigationSeverity = risk.getPostMitigationSeverity();
    this.postMitigationSeverityLevel = risk.getPostMitigationSeverityLevel();
    this.postMitigationNotes = risk.getPostMitigationNotes();

    this.mitigationStrategy = risk.getMitigationStrategy();
    this.mitigationOwnerType = risk.getMitigationOwnerType();
    this.mitigationOwner = risk.getMitigationOwner();
    this.mitigationRelatedEventIdentifierType = risk.getMitigationRelatedEventIdentifierType();
    this.mitigationRelatedEventIdentifierValue = risk.getMitigationRelatedEventIdentifierValue();
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 2;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getIdentifiedOn() {
    return identifiedOn;
  }

  public void setIdentifiedOn(Date identifiedOn) {
    this.identifiedOn = identifiedOn;
  }

  public String getIdentifiedBy() {
    return identifiedBy;
  }

  public void setIdentifiedBy(String identifiedBy) {
    this.identifiedBy = identifiedBy;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public int getPreMitigationProbability() {
    return preMitigationProbability;
  }

  public void setPreMitigationProbability(int preMitigationProbability) {
    this.preMitigationProbability = preMitigationProbability;
  }

  public int getPreMitigationImpact() {
    return preMitigationImpact;
  }

  public void setPreMitigationImpact(int preMitigationImpact) {
    this.preMitigationImpact = preMitigationImpact;
  }

  public int getPreMitigationSeverity() {
    return preMitigationSeverity;
  }

  public void setPreMitigationSeverity(int preMitigationSeverity) {
    this.preMitigationSeverity = preMitigationSeverity;
  }

  public SEVERITY_LEVEL getPreMitigationSeverityLevel() {
    return preMitigationSeverityLevel;
  }

  public void setPreMitigationSeverityLevel(SEVERITY_LEVEL preMitigationSeverity) {
    this.preMitigationSeverityLevel = preMitigationSeverity;
  }

  public void setPreMitigationSeverityLevel(String preMitigationSeverity) {
    this.preMitigationSeverityLevel = preMitigationSeverity != null ? Risk.SEVERITY_LEVEL.valueOf(preMitigationSeverity)
      : null;
  }

  public String getPreMitigationNotes() {
    return preMitigationNotes;
  }

  public void setPreMitigationNotes(String preMitigationNotes) {
    this.preMitigationNotes = preMitigationNotes;
  }

  public int getPostMitigationProbability() {
    return postMitigationProbability;
  }

  public void setPostMitigationProbability(int postMitigationProbability) {
    this.postMitigationProbability = postMitigationProbability;
  }

  public int getPostMitigationImpact() {
    return postMitigationImpact;
  }

  public void setPostMitigationImpact(int postMitigationImpact) {
    this.postMitigationImpact = postMitigationImpact;
  }

  public int getPostMitigationSeverity() {
    return postMitigationSeverity;
  }

  public void setPostMitigationSeverity(int postMitigationSeverity) {
    this.postMitigationSeverity = postMitigationSeverity;
  }

  public SEVERITY_LEVEL getPostMitigationSeverityLevel() {
    return postMitigationSeverityLevel;
  }

  public void setPostMitigationSeverityLevel(SEVERITY_LEVEL postMitigationSeverity) {
    this.postMitigationSeverityLevel = postMitigationSeverity;
  }

  public void setPostMitigationSeverityLevel(String postMitigationSeverity) {
    this.postMitigationSeverityLevel = postMitigationSeverity != null
      ? Risk.SEVERITY_LEVEL.valueOf(postMitigationSeverity) : null;
  }

  public String getPostMitigationNotes() {
    return postMitigationNotes;
  }

  public void setPostMitigationNotes(String postMitigationNotes) {
    this.postMitigationNotes = postMitigationNotes;
  }

  @JsonIgnore
  public SEVERITY_LEVEL getCurrentSeverityLevel() {
    if (postMitigationSeverityLevel != null) {
      return postMitigationSeverityLevel;
    } else {
      return preMitigationSeverityLevel;
    }
  }

  public String getMitigationStrategy() {
    return mitigationStrategy;
  }

  public void setMitigationStrategy(String mitigationStrategy) {
    this.mitigationStrategy = mitigationStrategy;
  }

  public String getMitigationOwnerType() {
    return mitigationOwnerType;
  }

  public void setMitigationOwnerType(String mitigationOwnerType) {
    this.mitigationOwnerType = mitigationOwnerType;
  }

  public String getMitigationOwner() {
    return mitigationOwner;
  }

  public void setMitigationOwner(String mitigationOwner) {
    this.mitigationOwner = mitigationOwner;
  }

  public String getMitigationRelatedEventIdentifierType() {
    return mitigationRelatedEventIdentifierType;
  }

  public void setMitigationRelatedEventIdentifierType(String mitigationRelatedEventIdentifierType) {
    this.mitigationRelatedEventIdentifierType = mitigationRelatedEventIdentifierType;
  }

  public String getMitigationRelatedEventIdentifierValue() {
    return mitigationRelatedEventIdentifierValue;
  }

  public void setMitigationRelatedEventIdentifierValue(String mitigationRelatedEventIdentifierValue) {
    this.mitigationRelatedEventIdentifierValue = mitigationRelatedEventIdentifierValue;
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

  @Override
  public String toString() {
    return "Risk [id=" + getId() + ", name=" + getName() + ", description=" + description + ", identifiedOn="
      + identifiedOn + ", identifiedBy=" + identifiedBy + ", category=" + category + ", notes=" + notes
      + ", preMitigationProbability=" + preMitigationProbability + ", preMitigationImpact=" + preMitigationImpact
      + ", preMitigationSeverity=" + preMitigationSeverity + ", preMitigationNotes=" + preMitigationNotes
      + ", postMitigationProbability=" + postMitigationProbability + ", postMitigationImpact=" + postMitigationImpact
      + ", postMitigationSeverity=" + postMitigationSeverity + ", postMitigationNotes=" + postMitigationNotes
      + ", mitigationStrategy=" + mitigationStrategy + ", mitigationOwnerType=" + mitigationOwnerType
      + ", mitigationOwner=" + mitigationOwner + ", mitigationRelatedEventIdentifierType="
      + mitigationRelatedEventIdentifierType + ", mitigationRelatedEventIdentifierValue="
      + mitigationRelatedEventIdentifierValue + ", createdOn=" + createdOn + ", createdBy=" + createdBy + ", updatedOn="
      + updatedOn + ", updatedBy=" + updatedBy + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return this.getId();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
    result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((identifiedBy == null) ? 0 : identifiedBy.hashCode());
    result = prime * result + ((identifiedOn == null) ? 0 : identifiedOn.hashCode());
    result = prime * result + ((mitigationOwner == null) ? 0 : mitigationOwner.hashCode());
    result = prime * result + ((mitigationOwnerType == null) ? 0 : mitigationOwnerType.hashCode());
    result = prime * result
      + ((mitigationRelatedEventIdentifierType == null) ? 0 : mitigationRelatedEventIdentifierType.hashCode());
    result = prime * result
      + ((mitigationRelatedEventIdentifierValue == null) ? 0 : mitigationRelatedEventIdentifierValue.hashCode());
    result = prime * result + ((mitigationStrategy == null) ? 0 : mitigationStrategy.hashCode());
    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
    result = prime * result + postMitigationImpact;
    result = prime * result + ((postMitigationNotes == null) ? 0 : postMitigationNotes.hashCode());
    result = prime * result + postMitigationProbability;
    result = prime * result + postMitigationSeverity;
    result = prime * result + ((postMitigationSeverityLevel == null) ? 0 : postMitigationSeverityLevel.hashCode());
    result = prime * result + preMitigationImpact;
    result = prime * result + ((preMitigationNotes == null) ? 0 : preMitigationNotes.hashCode());
    result = prime * result + preMitigationProbability;
    result = prime * result + preMitigationSeverity;
    result = prime * result + ((preMitigationSeverityLevel == null) ? 0 : preMitigationSeverityLevel.hashCode());
    result = prime * result + ((updatedBy == null) ? 0 : updatedBy.hashCode());
    result = prime * result + ((updatedOn == null) ? 0 : updatedOn.hashCode());
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
    Risk other = (Risk) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    if (createdBy == null) {
      if (other.createdBy != null)
        return false;
    } else if (!createdBy.equals(other.createdBy))
      return false;
    if (createdOn == null) {
      if (other.createdOn != null)
        return false;
    } else if (!createdOn.equals(other.createdOn))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (identifiedBy == null) {
      if (other.identifiedBy != null)
        return false;
    } else if (!identifiedBy.equals(other.identifiedBy))
      return false;
    if (identifiedOn == null) {
      if (other.identifiedOn != null)
        return false;
    } else if (!identifiedOn.equals(other.identifiedOn))
      return false;
    if (mitigationOwner == null) {
      if (other.mitigationOwner != null)
        return false;
    } else if (!mitigationOwner.equals(other.mitigationOwner))
      return false;
    if (mitigationOwnerType == null) {
      if (other.mitigationOwnerType != null)
        return false;
    } else if (!mitigationOwnerType.equals(other.mitigationOwnerType))
      return false;
    if (mitigationRelatedEventIdentifierType == null) {
      if (other.mitigationRelatedEventIdentifierType != null)
        return false;
    } else if (!mitigationRelatedEventIdentifierType.equals(other.mitigationRelatedEventIdentifierType))
      return false;
    if (mitigationRelatedEventIdentifierValue == null) {
      if (other.mitigationRelatedEventIdentifierValue != null)
        return false;
    } else if (!mitigationRelatedEventIdentifierValue.equals(other.mitigationRelatedEventIdentifierValue))
      return false;
    if (mitigationStrategy == null) {
      if (other.mitigationStrategy != null)
        return false;
    } else if (!mitigationStrategy.equals(other.mitigationStrategy))
      return false;
    if (notes == null) {
      if (other.notes != null)
        return false;
    } else if (!notes.equals(other.notes))
      return false;
    if (postMitigationImpact != other.postMitigationImpact)
      return false;
    if (postMitigationNotes == null) {
      if (other.postMitigationNotes != null)
        return false;
    } else if (!postMitigationNotes.equals(other.postMitigationNotes))
      return false;
    if (postMitigationProbability != other.postMitigationProbability)
      return false;
    if (postMitigationSeverity != other.postMitigationSeverity)
      return false;
    if (postMitigationSeverityLevel != other.postMitigationSeverityLevel)
      return false;
    if (preMitigationImpact != other.preMitigationImpact)
      return false;
    if (preMitigationNotes == null) {
      if (other.preMitigationNotes != null)
        return false;
    } else if (!preMitigationNotes.equals(other.preMitigationNotes))
      return false;
    if (preMitigationProbability != other.preMitigationProbability)
      return false;
    if (preMitigationSeverity != other.preMitigationSeverity)
      return false;
    if (preMitigationSeverityLevel != other.preMitigationSeverityLevel)
      return false;
    if (updatedBy == null) {
      if (other.updatedBy != null)
        return false;
    } else if (!updatedBy.equals(other.updatedBy))
      return false;
    if (updatedOn == null) {
      if (other.updatedOn != null)
        return false;
    } else if (!updatedOn.equals(other.updatedOn))
      return false;
    return true;
  }

}
