package org.roda.core.data.v2.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "agent")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Agent implements IsIndexed, Serializable {

  private static final long serialVersionUID = 7178184202935641440L;

  private String id = null;
  private String name = null;
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

  public Agent() {
    super();
    this.initialRelease = new Date();
  }

  public Agent(Agent agent) {
    this.name = agent.getName();
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

}