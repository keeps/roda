/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.NamedIndexedModel;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "agent")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Agent extends NamedIndexedModel implements IsIndexed, Serializable {

  private static final long serialVersionUID = 7178184202935641440L;

  private String type = null;
  private String description = null;
  private String category = null;
  private String version = null;
  private String license = null;
  private int popularity = 0;
  private String developer = null;
  private Date initialRelease = null;
  private String website = null;
  private String download = null;
  private String provenanceInformation = null;

  private List<String> platforms = new ArrayList<String>();
  private List<String> extensions = new ArrayList<String>();
  private List<String> mimetypes = new ArrayList<String>();
  private List<String> pronoms = new ArrayList<String>();
  private List<String> utis = new ArrayList<String>();
  private List<String> formatIds = new ArrayList<String>();
  private List<String> agentsRequired = new ArrayList<String>();

  public Agent() {
    super();
    this.initialRelease = new Date();
  }

  public Agent(Agent agent) {
    super(agent.getId(), agent.getName());
    this.type = agent.getType();
    this.description = agent.getDescription();
    this.category = agent.getCategory();
    this.version = agent.getVersion();
    this.license = agent.getLicense();
    this.popularity = agent.getPopularity();
    this.developer = agent.getDeveloper();
    this.initialRelease = agent.getInitialRelease();
    this.website = agent.getWebsite();
    this.download = agent.getDownload();
    this.provenanceInformation = agent.getProvenanceInformation();

    this.platforms = new ArrayList<String>(agent.getPlatforms());
    this.extensions = new ArrayList<String>(agent.getExtensions());
    this.mimetypes = new ArrayList<String>(agent.getMimetypes());
    this.pronoms = new ArrayList<String>(agent.getPronoms());
    this.utis = new ArrayList<String>(agent.getUtis());
    this.formatIds = new ArrayList<String>(agent.getFormatIds());
    this.agentsRequired = new ArrayList<String>(agent.getAgentsRequired());
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public int getPopularity() {
    return popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  public List<String> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(List<String> platforms) {
    this.platforms = platforms;
  }

  public String getDeveloper() {
    return developer;
  }

  public void setDeveloper(String developer) {
    this.developer = developer;
  }

  public Date getInitialRelease() {
    return initialRelease;
  }

  public void setInitialRelease(Date initialRelease) {
    this.initialRelease = initialRelease;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getDownload() {
    return download;
  }

  public void setDownload(String download) {
    this.download = download;
  }

  public String getProvenanceInformation() {
    return provenanceInformation;
  }

  public void setProvenanceInformation(String provenanceInformation) {
    this.provenanceInformation = provenanceInformation;
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<String> extensions) {
    this.extensions = extensions;
  }

  public List<String> getMimetypes() {
    return mimetypes;
  }

  public void setMimetypes(List<String> mimetypes) {
    this.mimetypes = mimetypes;
  }

  public List<String> getPronoms() {
    return pronoms;
  }

  public void setPronoms(List<String> pronoms) {
    this.pronoms = pronoms;
  }

  public List<String> getUtis() {
    return utis;
  }

  public void setUtis(List<String> utis) {
    this.utis = utis;
  }

  public List<String> getFormatIds() {
    return formatIds;
  }

  public void setFormatIds(List<String> formatIds) {
    this.formatIds = formatIds;
  }

  public List<String> getAgentsRequired() {
    return agentsRequired;
  }

  public void setAgentsRequired(List<String> agentsRequired) {
    this.agentsRequired = agentsRequired;
  }

  @Override
  public String toString() {
    return "Agent [id=" + getId() + ", name=" + getName() + ", type=" + type + ", description=" + description
      + ", category=" + category + ", version=" + version + ", license=" + license + ", popularity=" + popularity
      + ", developer=" + developer + ", initialRelease=" + initialRelease + ", website=" + website + ", download="
      + download + ", provenanceInformation=" + provenanceInformation + ", platforms=" + platforms + ", extensions="
      + extensions + ", mimetypes=" + mimetypes + ", pronoms=" + pronoms + ", utis=" + utis + ", formatIds=" + formatIds
      + ", agentsRequired=" + agentsRequired + "]";
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

}