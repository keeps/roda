/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2;

import java.util.Map;

public class FileFormat extends RODAObject {
  private static final long serialVersionUID = -6855712550409310949L;
  private String mimeType;
  private String version;
  private Map<String, String> formatRegistries;

  public FileFormat(){
    super(null,null);
  }
  
  public FileFormat(String mimeType, String version, Map<String, String> formatRegistries) {
    super();
    this.mimeType = mimeType;
    this.version = version;
    this.formatRegistries = formatRegistries;
  }

  /**
   * @return the mimeType
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the formatRegistries
   */
  public Map<String, String> getFormatRegistries() {
    return formatRegistries;
  }

  @Override
  public String toString() {
    return "FileFormat [mimeType=" + mimeType + ", version=" + version + ", formatRegistries=" + formatRegistries + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((formatRegistries == null) ? 0 : formatRegistries.hashCode());
    result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FileFormat other = (FileFormat) obj;
    if (formatRegistries == null) {
      if (other.formatRegistries != null) {
        return false;
      }
    } else if (!formatRegistries.equals(other.formatRegistries)) {
      return false;
    }
    if (mimeType == null) {
      if (other.mimeType != null) {
        return false;
      }
    } else if (!mimeType.equals(other.mimeType)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    } else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

}
