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

@XmlRootElement(name = RodaConstants.RODA_OBJECT_FORMAT)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Format extends NamedIndexedModel implements IsModelObject, IsIndexed {

  private static final long serialVersionUID = 7178184202935641440L;

  private String definition = null;
  private List<String> categories = null;
  private String latestVersion = null;
  private Integer popularity;
  private String developer = null;
  private Date initialRelease = null;
  private String standard = null;
  private boolean isOpenFormat = true;
  private List<String> websites = null;
  private String provenanceInformation = null;

  private List<String> extensions = new ArrayList<>();
  private List<String> mimetypes = new ArrayList<>();
  private List<String> pronoms = new ArrayList<>();
  private List<String> utis = new ArrayList<>();

  private List<String> alternativeDesignations = new ArrayList<>();
  private List<String> versions = new ArrayList<>();

  public Format() {
    super();
  }

  public Format(Format format) {
    super(format.getId(), format.getName());
    this.definition = format.getDefinition();
    this.categories = format.getCategories();
    this.latestVersion = format.getLatestVersion();
    this.popularity = format.getPopularity();
    this.developer = format.getDeveloper();
    this.initialRelease = format.getInitialRelease();
    this.standard = format.getStandard();
    this.isOpenFormat = format.isOpenFormat();
    this.websites = format.getWebsites();
    this.provenanceInformation = format.getProvenanceInformation();

    this.extensions = new ArrayList<>(format.getExtensions());
    this.mimetypes = new ArrayList<>(format.getMimetypes());
    this.pronoms = new ArrayList<>(format.getPronoms());
    this.utis = new ArrayList<>(format.getUtis());
    this.alternativeDesignations = new ArrayList<>(format.getAlternativeDesignations());
    this.versions = new ArrayList<>(format.getVersions());
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
    this.categories = categories;
  }

  public List<String> getWebsites() {
    return websites;
  }

  public void setWebsites(List<String> websites) {
    this.websites = websites;
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

  public List<String> getAlternativeDesignations() {
    return alternativeDesignations;
  }

  public void setAlternativeDesignations(List<String> alternativeDesignations) {
    this.alternativeDesignations = alternativeDesignations;
  }

  public List<String> getVersions() {
    return versions;
  }

  public void setVersions(List<String> versions) {
    this.versions = versions;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((alternativeDesignations == null) ? 0 : alternativeDesignations.hashCode());
    result = prime * result + ((categories == null) ? 0 : categories.hashCode());
    result = prime * result + ((definition == null) ? 0 : definition.hashCode());
    result = prime * result + ((developer == null) ? 0 : developer.hashCode());
    result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
    result = prime * result + ((initialRelease == null) ? 0 : initialRelease.hashCode());
    result = prime * result + (isOpenFormat ? 1231 : 1237);
    result = prime * result + ((latestVersion == null) ? 0 : latestVersion.hashCode());
    result = prime * result + ((mimetypes == null) ? 0 : mimetypes.hashCode());
    result = prime * result + ((popularity == null) ? 0 : popularity.hashCode());
    result = prime * result + ((pronoms == null) ? 0 : pronoms.hashCode());
    result = prime * result + ((provenanceInformation == null) ? 0 : provenanceInformation.hashCode());
    result = prime * result + ((standard == null) ? 0 : standard.hashCode());
    result = prime * result + ((utis == null) ? 0 : utis.hashCode());
    result = prime * result + ((versions == null) ? 0 : versions.hashCode());
    result = prime * result + ((websites == null) ? 0 : websites.hashCode());
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
    Format other = (Format) obj;
    if (alternativeDesignations == null) {
      if (other.alternativeDesignations != null)
        return false;
    } else if (!alternativeDesignations.equals(other.alternativeDesignations))
      return false;
    if (categories == null) {
      if (other.categories != null)
        return false;
    } else if (!categories.equals(other.categories))
      return false;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    if (developer == null) {
      if (other.developer != null)
        return false;
    } else if (!developer.equals(other.developer))
      return false;
    if (extensions == null) {
      if (other.extensions != null)
        return false;
    } else if (!extensions.equals(other.extensions))
      return false;
    if (initialRelease == null) {
      if (other.initialRelease != null)
        return false;
    } else if (!initialRelease.equals(other.initialRelease))
      return false;
    if (isOpenFormat != other.isOpenFormat)
      return false;
    if (latestVersion == null) {
      if (other.latestVersion != null)
        return false;
    } else if (!latestVersion.equals(other.latestVersion))
      return false;
    if (mimetypes == null) {
      if (other.mimetypes != null)
        return false;
    } else if (!mimetypes.equals(other.mimetypes))
      return false;
    if (popularity == null) {
      if (other.popularity != null)
        return false;
    } else if (!popularity.equals(other.popularity))
      return false;
    if (pronoms == null) {
      if (other.pronoms != null)
        return false;
    } else if (!pronoms.equals(other.pronoms))
      return false;
    if (provenanceInformation == null) {
      if (other.provenanceInformation != null)
        return false;
    } else if (!provenanceInformation.equals(other.provenanceInformation))
      return false;
    if (standard == null) {
      if (other.standard != null)
        return false;
    } else if (!standard.equals(other.standard))
      return false;
    if (utis == null) {
      if (other.utis != null)
        return false;
    } else if (!utis.equals(other.utis))
      return false;
    if (versions == null) {
      if (other.versions != null)
        return false;
    } else if (!versions.equals(other.versions))
      return false;
    if (websites == null) {
      if (other.websites != null)
        return false;
    } else if (!websites.equals(other.websites))
      return false;
    return true;
  }

}