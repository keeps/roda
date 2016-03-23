package org.roda.core.data.v2.formats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "format")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Format implements IsIndexed, Serializable {

  private static final long serialVersionUID = 7178184202935641440L;

  private String id = null;
  private String name = null;
  private String definition = null;
  private String category = null;
  private String latestVersion = null;
  private int popularity = 0;
  private String developer = null;
  private Date initialRelease = null;
  private String standard = null;
  private boolean isOpenFormat = true;
  private String website = null;
  private String provenanceInformation = null;

  private List<String> extensions = new ArrayList<String>();
  private List<String> mimetypes = new ArrayList<String>();
  private List<String> pronoms = new ArrayList<String>();
  private List<String> utis = new ArrayList<String>();

  public Format() {
    super();
    this.initialRelease = new Date();
  }

  public Format(Format format) {
    this.id = format.getId();
    this.name = format.getName();
    this.definition = format.getDefinition();
    this.category = format.getCategory();
    this.latestVersion = format.getLatestVersion();
    this.popularity = format.getPopularity();
    this.developer = format.getDeveloper();
    this.initialRelease = format.getInitialRelease();
    this.standard = format.getStandard();
    this.isOpenFormat = format.isOpenFormat();
    this.website = format.getWebsite();
    this.provenanceInformation = format.getProvenanceInformation();

    this.extensions = new ArrayList<String>(format.getExtensions());
    this.mimetypes = new ArrayList<String>(format.getMimetypes());
    this.pronoms = new ArrayList<String>(format.getPronoms());
    this.utis = new ArrayList<String>(format.getUtis());
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

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  public int getPopularity() {
    return popularity;
  }

  public void setPopularity(int popularity) {
    this.popularity = popularity;
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

  public String getStandard() {
    return standard;
  }

  public void setStandard(String standard) {
    this.standard = standard;
  }

  public boolean isOpenFormat() {
    return isOpenFormat;
  }

  public void setOpenFormat(boolean isOpenFormat) {
    this.isOpenFormat = isOpenFormat;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
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

  @Override
  public String toString() {
    return "Format [id=" + id + ", name=" + name + ", definition=" + definition + ", category=" + category
      + ", latestVersion=" + latestVersion + ", popularity=" + popularity + ", developer=" + developer
      + ", initialRelease=" + initialRelease + ", standard=" + standard + ", isOpenFormat=" + isOpenFormat
      + ", website=" + website + ", provenanceInformation=" + provenanceInformation + ", extensions=" + extensions
      + ", mimetypes=" + mimetypes + ", pronoms=" + pronoms + ", utis=" + utis + "]";
  }

  @Override
  public String getUUID() {
    // FIXME 20160323 hsilva: see if this is the right way to do it
    return getId();
  }

}