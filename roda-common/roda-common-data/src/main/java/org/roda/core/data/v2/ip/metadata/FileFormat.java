/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip.metadata;

import java.io.Serializable;
import java.util.Map;

public class FileFormat implements Serializable {
  private static final long serialVersionUID = -6855712550409310949L;

  private String formatDesignationName;
  private String formatDesignationVersion;
  private String formatDesignation;

  private String mimeType;
  private String pronom;
  private String extension;
  private Map<String, String> formatRegistries;

  public FileFormat() {
    super();
  }

  public FileFormat(String formatDesignationName, String formatDesignationVersion, String mimeType, String pronom,
    String extension, Map<String, String> formatRegistries) {
    super();
    this.formatDesignationName = formatDesignationName;
    this.formatDesignationVersion = formatDesignationVersion;

    if (formatDesignationVersion != null && !formatDesignationVersion.isEmpty()) {
      this.formatDesignation = formatDesignationName + " " + formatDesignationVersion;
    } else {
      this.formatDesignation = formatDesignationName;
    }

    this.mimeType = mimeType;
    this.pronom = pronom;
    this.extension = extension;
    this.formatRegistries = formatRegistries;
  }

  public String getFormatDesignationName() {
    return formatDesignationName;
  }

  public void setFormatDesignationName(String formatDesignationName) {
    this.formatDesignationName = formatDesignationName;
  }

  public String getFormatDesignationVersion() {
    return formatDesignationVersion;
  }

  public void setFormatDesignationVersion(String formatDesignationVersion) {
    this.formatDesignationVersion = formatDesignationVersion;
  }

  public String getFormatDesignation() {
    return formatDesignation;
  }

  public void setFormatDesignation(String formatDesignation) {
    this.formatDesignation = formatDesignation;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getPronom() {
    return pronom;
  }

  public void setPronom(String pronom) {
    this.pronom = pronom;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public Map<String, String> getFormatRegistries() {
    return formatRegistries;
  }

  public void setFormatRegistries(Map<String, String> formatRegistries) {
    this.formatRegistries = formatRegistries;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((extension == null) ? 0 : extension.hashCode());
    result = prime * result + ((formatDesignationName == null) ? 0 : formatDesignationName.hashCode());
    result = prime * result + ((formatDesignationVersion == null) ? 0 : formatDesignationVersion.hashCode());
    result = prime * result + ((formatRegistries == null) ? 0 : formatRegistries.hashCode());
    result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
    result = prime * result + ((pronom == null) ? 0 : pronom.hashCode());
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
    FileFormat other = (FileFormat) obj;
    if (extension == null) {
      if (other.extension != null)
        return false;
    } else if (!extension.equals(other.extension))
      return false;
    if (formatDesignationName == null) {
      if (other.formatDesignationName != null)
        return false;
    } else if (!formatDesignationName.equals(other.formatDesignationName))
      return false;
    if (formatDesignationVersion == null) {
      if (other.formatDesignationVersion != null)
        return false;
    } else if (!formatDesignationVersion.equals(other.formatDesignationVersion))
      return false;
    if (formatRegistries == null) {
      if (other.formatRegistries != null)
        return false;
    } else if (!formatRegistries.equals(other.formatRegistries))
      return false;
    if (mimeType == null) {
      if (other.mimeType != null)
        return false;
    } else if (!mimeType.equals(other.mimeType))
      return false;
    if (pronom == null) {
      if (other.pronom != null)
        return false;
    } else if (!pronom.equals(other.pronom))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FileFormat [formatDesignationName=" + formatDesignationName + ", formatDesignationVersion="
      + formatDesignationVersion + ", formatDesignation=" + formatDesignation + ", mimeType=" + mimeType + ", pronom="
      + pronom + ", extension=" + extension + ", formatRegistries=" + formatRegistries + "]";
  }

}
