package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MarketInfo implements Serializable {
  private static final long serialVersionUID = 7826618040202731061L;

  private String id = null;

  private String name = null;
  private PluginType type = PluginType.MISC;
  private String version = null;
  private String description = null;
  private List<String> categories = null;

  private LicenseInfo license = null;
  private String documentation = null;
  private String installation = null;
  private String homepage = null;

  public MarketInfo() {
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

  public PluginType getType() {
    return type;
  }

  public void setType(PluginType type) {
    this.type = type;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  public LicenseInfo getLicense() {
    return license;
  }

  public void setLicense(LicenseInfo license) {
    this.license = license;
  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  public String getInstallation() {
    return installation;
  }

  public void setInstallation(String installation) {
    this.installation = installation;
  }

  public String getHomepage() {
    return homepage;
  }

  public void setHomepage(String homepage) {
    this.homepage = homepage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MarketInfo that = (MarketInfo) o;
    return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type && Objects.equals(version, that.version) && Objects.equals(description, that.description) && Objects.equals(categories, that.categories) && Objects.equals(license, that.license) && Objects.equals(documentation, that.documentation) && Objects.equals(installation, that.installation) && Objects.equals(homepage, that.homepage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, version, description, categories, license, documentation, installation, homepage);
  }
}
