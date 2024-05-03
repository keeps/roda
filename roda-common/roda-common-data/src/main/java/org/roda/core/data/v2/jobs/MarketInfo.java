/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketInfo implements Serializable {
  private static final long serialVersionUID = 7826618040202731061L;
  @JsonProperty("id")
  private String id = null;
  @JsonProperty("name")
  private String name = null;
  // plugin, service, component
  @JsonProperty("type")
  private String type = null;
  @JsonProperty("version")
  private String version = null;
  @JsonProperty("description")
  private String description = null;
  @JsonProperty("plugin")
  private PluginProperties pluginProperties = new PluginProperties();
  @JsonProperty("license")
  private LicenseInfo license = new LicenseInfo();
  private String installation = null;
  @JsonProperty("homepage")
  private String homepage = null;
  @JsonProperty("vendor")
  private Vendor vendor = new Vendor();
  @JsonProperty("compatibility")
  private List<String> compatibility = null;
  @JsonProperty("linkToQuote")
  private Map<String, String> linkToQuote = new HashMap<>();

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @JsonIgnore
  public PluginType getPluginType() {
    return pluginProperties.getType();
  }

  @JsonIgnore
  public void setPluginType(PluginType type) {
    this.pluginProperties.setType(type);
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

  @JsonIgnore
  public List<String> getCategories() {
    return pluginProperties.getCategories();
  }

  @JsonIgnore
  public void setCategories(List<String> categories) {
    this.pluginProperties.setCategories(categories);
  }

  @JsonIgnore
  public Set<String> getObjectClasses() {
    return pluginProperties.getObjectClasses();
  }

  @JsonIgnore
  public void setObjectClasses(Set<String> objectClasses) {
    this.pluginProperties.setObjectClasses(objectClasses);
  }

  public void addObjectClass(String objectClass) {
    this.pluginProperties.addObjectClass(objectClass);
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

  public Vendor getVendor() {
    return vendor;
  }

  public void setVendor(Vendor vendor) {
    this.vendor = vendor;
  }

  @JsonIgnore
  public String getVendorName() {
    return vendor.getName();
  }

  @JsonIgnore
  public void setVendorName(String vendor) {
    this.vendor.setName(vendor);
  }

  public PluginProperties getPluginProperties() {
    return pluginProperties;
  }

  public void setPluginProperties(PluginProperties pluginProperties) {
    this.pluginProperties = pluginProperties;
  }

  public List<String> getCompatibility() {
    return compatibility;
  }

  public void setCompatibility(List<String> compatibility) {
    this.compatibility = compatibility;
  }

  public Map<String, String> getLinkToQuote() {
    return linkToQuote;
  }

  public void setLinkToQuote(Map<String, String> linkToQuote) {
    this.linkToQuote = linkToQuote;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MarketInfo that = (MarketInfo) o;
    return Objects.equals(id, that.id) && Objects.equals(name, that.name)
      && pluginProperties.getType() == that.pluginProperties.getType() && Objects.equals(version, that.version)
      && Objects.equals(description, that.description)
      && Objects.equals(pluginProperties.getCategories(), that.pluginProperties.getCategories())
      && Objects.equals(pluginProperties.getObjectClasses(), that.pluginProperties.getObjectClasses())
      && Objects.equals(license, that.license) && Objects.equals(installation, that.installation)
      && Objects.equals(homepage, that.homepage) && Objects.equals(vendor, that.vendor)
      && Objects.equals(compatibility, that.compatibility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, pluginProperties.getType(), version, description, pluginProperties.getCategories(),
      pluginProperties.getCategories(), license, installation, homepage, vendor, compatibility);
  }
}
