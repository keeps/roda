/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.formats;

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

@XmlRootElement(name = "format")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Format extends NamedIndexedModel implements IsModelObject, IsIndexed {

  private static final long serialVersionUID = 7178184202935641440L;

  private String definition = null;
  private final List<String> categories = new ArrayList<>();
  private String latestVersion = null;
  private Integer popularity;
  private String developer = null;
  private Date initialRelease = null;
  private String standard = null;
  private boolean isOpenFormat = true;
  private final List<String> websites = new ArrayList<>();
  private String provenanceInformation = null;

  private final List<String> extensions = new ArrayList<>();
  private final List<String> mimetypes = new ArrayList<>();
  private final List<String> pronoms = new ArrayList<>();
  private final List<String> utis = new ArrayList<>();

  private final List<String> alternativeDesignations = new ArrayList<>();
  private final List<String> versions = new ArrayList<>();

  public Format() {
    super();
  }

  public Format(Format format) {
    super(format.getId(), format.getName());
    this.definition = format.getDefinition();
    setCategories(format.getCategories());
    this.latestVersion = format.getLatestVersion();
    this.popularity = format.getPopularity();
    this.developer = format.getDeveloper();
    this.initialRelease = format.getInitialRelease();
    this.standard = format.getStandard();
    this.isOpenFormat = format.isOpenFormat();
    setWebsites(format.getWebsites());
    this.provenanceInformation = format.getProvenanceInformation();

    setExtensions(format.getExtensions());
    setMimetypes(format.getMimetypes());
    setPronoms(format.getPronoms());
    setUtis(format.getUtis());
    setAlternativeDesignations(format.getAlternativeDesignations());
    setVersions(format.getVersions());

  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 2;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  public Integer getPopularity() {
    return popularity;
  }

  public void setPopularity(Integer popularity) {
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

  public String getProvenanceInformation() {
    return provenanceInformation;
  }

  public void setProvenanceInformation(String provenanceInformation) {
    this.provenanceInformation = provenanceInformation;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories.clear();
    this.categories.addAll(categories);
  }

  public List<String> getWebsites() {
    return websites;
  }

  public void setWebsites(List<String> websites) {
    this.websites.clear();
    this.websites.addAll(websites);
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<String> extensions) {
    this.extensions.clear();
    this.extensions.addAll(extensions);
  }

  public List<String> getMimetypes() {
    return mimetypes;
  }

  public void setMimetypes(List<String> mimetypes) {
    this.mimetypes.clear();
    this.mimetypes.addAll(mimetypes);

  }

  public List<String> getPronoms() {
    return pronoms;
  }

  public void setPronoms(List<String> pronoms) {
    this.pronoms.clear();
    this.pronoms.addAll(pronoms);
  }

  public List<String> getUtis() {
    return utis;
  }

  public void setUtis(List<String> utis) {
    this.utis.clear();
    this.utis.addAll(utis);
  }

  public List<String> getAlternativeDesignations() {
    return alternativeDesignations;
  }

  public void setAlternativeDesignations(List<String> alternativeDesignations) {
    this.alternativeDesignations.clear();
    this.alternativeDesignations.addAll(alternativeDesignations);
  }

  public List<String> getVersions() {
    return versions;
  }

  public void setVersions(List<String> versions) {
    this.versions.clear();
    this.versions.addAll(versions);
  }

  @Override
  public String toString() {
    return "Format [id=" + getId() + ", name=" + getName() + ", definition=" + definition + ", categories=" + categories
      + ", latestVersion=" + latestVersion + ", popularity=" + popularity + ", developer=" + developer
      + ", initialRelease=" + initialRelease + ", standard=" + standard + ", isOpenFormat=" + isOpenFormat
      + ", websites=" + websites + ", provenanceInformation=" + provenanceInformation + ", extensions=" + extensions
      + ", mimetypes=" + mimetypes + ", pronoms=" + pronoms + ", utis=" + utis + ", alternativeDesignations="
      + alternativeDesignations + ", versions=" + versions + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "definition", "categories", "latestVersion", "popularity", "developer",
      "initialRelease", "standard", "isOpenFormat", "websites", "provenanceInformation", "extensions", "mimetypes",
      "pronoms", "utis", "alternativeDesignations", "versions");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(getId(), getName(), definition, categories, latestVersion, popularity, developer,
      initialRelease, standard, isOpenFormat, websites, provenanceInformation, extensions, mimetypes, pronoms, utis,
      alternativeDesignations, versions);
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