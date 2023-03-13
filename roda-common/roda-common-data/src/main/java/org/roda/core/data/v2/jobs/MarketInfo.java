/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketInfo implements Serializable {
  private static final long serialVersionUID = 7826618040202731061L;

  private String id = null;

  private String name = null;
  private PluginType type = PluginType.MISC;
  private String version = null;
  private String description = null;
  private List<String> categories = null;

  private Set<String> objectClasses = new HashSet<>();
  private LicenseInfo license = null;
  private String installation = null;
  private String homepage = null;

  private String vendor = null;

  private String minSupportedVersion = null;

  private String maxSupportedVersion = null;

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

  public Set<String> getObjectClasses() {
    return objectClasses;
  }

  public void setObjectClasses(Set<String> objectClasses) {
    this.objectClasses = objectClasses;
  }

  public void addObjectClass(String objectClass) {
    this.objectClasses.add(objectClass);
  }

  public LicenseInfo getLicense() {
    return license;
  }

  public void setLicense(LicenseInfo license) {
    this.license = license;
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

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public String getMinSupportedVersion() {
    return minSupportedVersion;
  }

  public void setMinSupportedVersion(String minSupportedVersion) {
    this.minSupportedVersion = minSupportedVersion;
  }

  public String getMaxSupportedVersion() {
    return maxSupportedVersion;
  }

  public void setMaxSupportedVersion(String maxSupportedVersion) {
    this.maxSupportedVersion = maxSupportedVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MarketInfo that = (MarketInfo) o;
    return Objects.equals(id, that.id) && Objects.equals(name, that.name) && type == that.type
      && Objects.equals(version, that.version) && Objects.equals(description, that.description)
      && Objects.equals(categories, that.categories) && Objects.equals(objectClasses, that.objectClasses)
      && Objects.equals(license, that.license) && Objects.equals(installation, that.installation)
      && Objects.equals(homepage, that.homepage) && Objects.equals(vendor, that.vendor)
      && Objects.equals(minSupportedVersion, that.minSupportedVersion)
      && Objects.equals(maxSupportedVersion, that.maxSupportedVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, version, description, categories, objectClasses, license, installation,
      homepage, vendor, minSupportedVersion, maxSupportedVersion);
  }
}
