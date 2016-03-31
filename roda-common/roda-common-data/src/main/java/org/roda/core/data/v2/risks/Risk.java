/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.risks;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "risk")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Risk implements IsIndexed, Serializable {

  private static final long serialVersionUID = -585753367605901060L;

  private String id = null;
  // FIXME uuid is needed but should it?
  private String uuid = null;
  private String name = null;
  private String description = null;
  private Date identifiedOn = null;
  private String identifiedBy = null;
  private String category = null;
  private String notes = null;

  private int preMitigationProbability = 0;
  private int preMitigationImpact = 0;
  private int preMitigationSeverity = 0;
  private String preMitigationNotes = null;

  private int posMitigationProbability = 0;
  private int posMitigationImpact = 0;
  private int posMitigationSeverity = 0;
  private String posMitigationNotes = null;

  private String mitigationStrategy = null;
  private String mitigationOwnerType = null;
  private String mitigationOwner = null;
  private String mitigationRelatedEventIdentifierType = null;
  private String mitigationRelatedEventIdentifierValue = null;

  private Map<String, String> affectedObjects = new HashMap<String, String>();

  public Risk() {
    super();
    this.identifiedOn = new Date();
  }

  public Risk(Risk risk) {
    this.id = risk.getId();
    this.name = risk.getName();
    this.description = risk.getDescription();
    this.identifiedOn = risk.getIdentifiedOn();
    this.identifiedBy = risk.getIdentifiedBy();
    this.category = risk.getCategory();
    this.notes = risk.getNotes();

    this.preMitigationProbability = risk.getPreMitigationProbability();
    this.preMitigationImpact = risk.getPreMitigationImpact();
    this.preMitigationSeverity = risk.getPreMitigationSeverity();
    this.preMitigationNotes = risk.getPreMitigationNotes();

    this.posMitigationProbability = risk.getPosMitigationProbability();
    this.posMitigationImpact = risk.getPosMitigationImpact();
    this.posMitigationSeverity = risk.getPosMitigationSeverity();
    this.posMitigationNotes = risk.getPosMitigationNotes();

    this.mitigationStrategy = risk.getMitigationStrategy();
    this.mitigationOwnerType = risk.getMitigationOwnerType();
    this.mitigationOwner = risk.getMitigationOwner();
    this.mitigationRelatedEventIdentifierType = risk.getMitigationRelatedEventIdentifierType();
    this.mitigationRelatedEventIdentifierValue = risk.getMitigationRelatedEventIdentifierValue();

    this.affectedObjects = new HashMap<String, String>(risk.getAffectedObjects());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Map<String, String> getAffectedObjects() {
    return affectedObjects;
  }

  public void setAffectedObjects(Map<String, String> affectedObjects) {
    this.affectedObjects = affectedObjects;
  }

  @Override
  public String toString() {
    return "Format [id=" + id + ", name=" + name + ", description=" + description + ", identifiedOn=" + identifiedOn
      + ", identifiedBy=" + identifiedBy + ", category=" + category + ", notes=" + notes
      + ", preMitigationProbability=" + preMitigationProbability + ", preMitigationImpact=" + preMitigationImpact
      + ", preMitigationSeverity=" + preMitigationSeverity + ", preMitigationNotes=" + preMitigationNotes
      + ", posMitigationProbability=" + posMitigationProbability + ", posMitigationImpact=" + posMitigationImpact
      + ", posMitigationSeverity=" + posMitigationSeverity + ", posMitigationNotes=" + posMitigationNotes
      + ", mitigationStrategy=" + mitigationStrategy + ", mitigationOwnerType=" + mitigationOwnerType
      + ", mitigationOwner=" + mitigationOwner + ", mitigationRelatedEventIdentifierType="
      + mitigationRelatedEventIdentifierType + ", mitigationRelatedEventIdentifierValue="
      + mitigationRelatedEventIdentifierValue + ", affectedObjects=" + affectedObjects + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

}
