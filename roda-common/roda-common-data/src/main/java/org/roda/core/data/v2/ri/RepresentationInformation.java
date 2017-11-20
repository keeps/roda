/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.NamedIndexedModel;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentationInformation extends NamedIndexedModel implements IsModelObject, IsIndexed {
  private static final long serialVersionUID = 8766448064705416130L;

  private String description = null;
  private String family = null;
  private List<String> categories = null;
  private String extras = null;
  private RepresentationInformationSupport support = null;

  private List<RepresentationInformationRelation> relations = new ArrayList<>();
  private List<String> filters = new ArrayList<>();

  private Date createdOn = null;
  private String createdBy = null;
  private Date updatedOn = null;
  private String updatedBy = null;

  public RepresentationInformation() {
    super();
  }

  public RepresentationInformation(RepresentationInformation representationInformation) {
    super(representationInformation.getId(), representationInformation.getName());
    this.description = representationInformation.getDescription();
    this.family = representationInformation.getFamily();
    this.categories = representationInformation.getCategories();
    this.extras = representationInformation.getExtras();
    this.support = representationInformation.getSupport();
    this.relations = new ArrayList<>(representationInformation.getRelations());
    this.filters = new ArrayList<>(representationInformation.getFilters());
    this.createdOn = representationInformation.getCreatedOn();
    this.createdBy = representationInformation.getCreatedBy();
    this.updatedOn = representationInformation.getUpdatedOn();
    this.updatedBy = representationInformation.getUpdatedBy();
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  public String getExtras() {
    return extras;
  }

  public void setExtras(String extras) {
    this.extras = extras;
  }

  public RepresentationInformationSupport getSupport() {
    return support;
  }

  public void setSupport(RepresentationInformationSupport support) {
    this.support = support;
  }

  public List<RepresentationInformationRelation> getRelations() {
    return relations;
  }

  public void setRelations(List<RepresentationInformationRelation> relations) {
    this.relations = relations;
  }

  public List<String> getFilters() {
    return filters;
  }

  public void setFilters(List<String> filters) {
    this.filters = filters;
  }

  public void addFilter(String filter) {
    this.filters.add(filter);
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
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
    return "RepresentationInformation [id=" + getId() + ", name=" + getName() + ", description=" + description
      + ", family=" + family + ", categories=" + categories + ", extras=" + extras + ", support=" + support
      + ", relations=" + relations + ", filters=" + filters + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "description", "family", "categories", "extras", "support", "relations",
      "filters");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(getId(), getName(), description, family, categories, extras, support, relations, filters);
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

}