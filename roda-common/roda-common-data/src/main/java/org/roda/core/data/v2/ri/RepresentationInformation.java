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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.NamedIndexedModel;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = RodaConstants.RODA_OBJECT_REPRESENTATION_INFORMATION)
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepresentationInformation extends NamedIndexedModel implements IsModelObject, IsIndexed {
  private static final long serialVersionUID = 8766448064705416130L;

  private String description = null;
  private String family = null;
  private List<String> tags = null;

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
    this.tags = representationInformation.getTags();
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

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getExtras() {
    return extras;
  }

  @XmlElement(name = "extras")
  @XmlJavaTypeAdapter(value = ExtrasHandler.class)
  public void setExtras(String extras) {
    this.extras = extras;
  }

  public RepresentationInformationSupport getSupport() {
    return support;
  }

  public void setSupport(RepresentationInformationSupport support) {
    this.support = support;
  }

  @XmlElement(name = "relation")
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
      + ", family=" + family + ", tags=" + tags + ", extras=" + extras + ", support=" + support + ", relations="
      + relations + ", filters=" + filters + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "description", "family", "tags", "extras", "support", "relations", "filters");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(getId(), getName(), description, family, tags, extras, support, relations, filters);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
    result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((extras == null) ? 0 : extras.hashCode());
    result = prime * result + ((family == null) ? 0 : family.hashCode());
    result = prime * result + ((filters == null) ? 0 : filters.hashCode());
    result = prime * result + ((relations == null) ? 0 : relations.hashCode());
    result = prime * result + ((support == null) ? 0 : support.hashCode());
    result = prime * result + ((tags == null) ? 0 : tags.hashCode());
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
    RepresentationInformation other = (RepresentationInformation) obj;
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
    if (extras == null) {
      if (other.extras != null)
        return false;
    } else if (!extras.equals(other.extras))
      return false;
    if (family == null) {
      if (other.family != null)
        return false;
    } else if (!family.equals(other.family))
      return false;
    if (filters == null) {
      if (other.filters != null)
        return false;
    } else if (!filters.equals(other.filters))
      return false;
    if (relations == null) {
      if (other.relations != null)
        return false;
    } else if (!relations.equals(other.relations))
      return false;
    if (support != other.support)
      return false;
    if (tags == null) {
      if (other.tags != null)
        return false;
    } else if (!tags.equals(other.tags))
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