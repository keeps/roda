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

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.NamedIndexedModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "risk")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Risk extends NamedIndexedModel implements IsRODAObject {

  private static final long serialVersionUID = -585753367605901060L;

  public static enum SEVERITY_LEVEL {
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

  private int posMitigationProbability = 0;
  private int posMitigationImpact = 0;
  private int posMitigationSeverity = 0;
  private SEVERITY_LEVEL posMitigationSeverityLevel = null;
  private String posMitigationNotes = null;

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

    this.posMitigationProbability = risk.getPosMitigationProbability();
    this.posMitigationImpact = risk.getPosMitigationImpact();
    this.posMitigationSeverity = risk.getPosMitigationSeverity();
    this.posMitigationSeverityLevel = risk.getPosMitigationSeverityLevel();
    this.posMitigationNotes = risk.getPosMitigationNotes();

    this.mitigationStrategy = risk.getMitigationStrategy();
    this.mitigationOwnerType = risk.getMitigationOwnerType();
    this.mitigationOwner = risk.getMitigationOwner();
    this.mitigationRelatedEventIdentifierType = risk.getMitigationRelatedEventIdentifierType();
    this.mitigationRelatedEventIdentifierValue = risk.getMitigationRelatedEventIdentifierValue();
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
    this.preMitigationSeverityLevel = Risk.SEVERITY_LEVEL.valueOf(preMitigationSeverity);
  }

  public String getPreMitigationNotes() {
    return preMitigationNotes;
  }

  public void setPreMitigationNotes(String preMitigationNotes) {
    this.preMitigationNotes = preMitigationNotes;
  }

  public int getPosMitigationProbability() {
    return posMitigationProbability;
  }

  public void setPosMitigationProbability(int posMitigationProbability) {
    this.posMitigationProbability = posMitigationProbability;
  }

  public int getPosMitigationImpact() {
    return posMitigationImpact;
  }

  public void setPosMitigationImpact(int posMitigationImpact) {
    this.posMitigationImpact = posMitigationImpact;
  }

  public int getPosMitigationSeverity() {
    return posMitigationSeverity;
  }

  public void setPosMitigationSeverity(int posMitigationSeverity) {
    this.posMitigationSeverity = posMitigationSeverity;
  }

  public SEVERITY_LEVEL getPosMitigationSeverityLevel() {
    return posMitigationSeverityLevel;
  }

  public void setPosMitigationSeverityLevel(SEVERITY_LEVEL posMitigationSeverity) {
    this.posMitigationSeverityLevel = posMitigationSeverity;
  }

  public void setPosMitigationSeverityLevel(String posMitigationSeverity) {
    this.posMitigationSeverityLevel = Risk.SEVERITY_LEVEL.valueOf(posMitigationSeverity);
  }

  public String getPosMitigationNotes() {
    return posMitigationNotes;
  }

  public void setPosMitigationNotes(String posMitigationNotes) {
    this.posMitigationNotes = posMitigationNotes;
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
      + ", posMitigationProbability=" + posMitigationProbability + ", posMitigationImpact=" + posMitigationImpact
      + ", posMitigationSeverity=" + posMitigationSeverity + ", posMitigationNotes=" + posMitigationNotes
      + ", mitigationStrategy=" + mitigationStrategy + ", mitigationOwnerType=" + mitigationOwnerType
      + ", mitigationOwner=" + mitigationOwner + ", mitigationRelatedEventIdentifierType="
      + mitigationRelatedEventIdentifierType + ", mitigationRelatedEventIdentifierValue="
      + mitigationRelatedEventIdentifierValue + ", createdOn=" + createdOn + ", createdBy=" + createdBy + ", updatedOn="
      + updatedOn + ", updatedBy=" + updatedBy + "]";
  }

  public String[] toCsvHeaders() {
    return new String[] {"id", "name", "description", "identifiedOn", "identifiedBy", "category", "notes",
      "preMitigationProbability", "preMitigationImpact", "preMitigationSeverity", "preMitigationNotes",
      "posMitigationProbability", "posMitigationImpact", "posMitigationSeverity", "posMitigationNotes",
      "mitigationStrategy", "mitigationOwnerType", "mitigationOwner", "mitigationRelatedEventIdentifierType",
      "mitigationRelatedEventIdentifierValue", "createdOn", "createdBy", "updatedOn", "updatedBy"};
  }

  public Object[] toCsvValues() {
    return new Object[] {getId(), getName(), description, identifiedOn, identifiedBy, category, notes,
      preMitigationProbability, preMitigationImpact, preMitigationSeverity, preMitigationNotes,
      posMitigationProbability, posMitigationImpact, posMitigationSeverity, posMitigationNotes, mitigationStrategy,
      mitigationOwnerType, mitigationOwner, mitigationRelatedEventIdentifierType, mitigationRelatedEventIdentifierValue,
      createdOn, createdBy, updatedOn, updatedBy};
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return this.getId();
  }

}
