/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.preservation;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class FormatRegistry implements Serializable {
  private static final long serialVersionUID = -17213218000184427L;

  private String formatRegistryName = null;
  private String formatRegistryKey = null;
  private String formatRegistryRole = null;

  /**
	 * 
	 */
  public FormatRegistry() {
  }

  /**
   * @return the formatRegistryName
   */
  public String getFormatRegistryName() {
    return formatRegistryName;
  }

  /**
   * @param formatRegistryName
   *          the formatRegistryName to set
   */
  public void setFormatRegistryName(String formatRegistryName) {
    this.formatRegistryName = formatRegistryName;
  }

  /**
   * @return the formatRegistryKey
   */
  public String getFormatRegistryKey() {
    return formatRegistryKey;
  }

  /**
   * @param formatRegistryKey
   *          the formatRegistryKey to set
   */
  public void setFormatRegistryKey(String formatRegistryKey) {
    this.formatRegistryKey = formatRegistryKey;
  }

  /**
   * @return the formatRegistryRole
   */
  public String getFormatRegistryRole() {
    return formatRegistryRole;
  }

  /**
   * @param formatRegistryRole
   *          the formatRegistryRole to set
   */
  public void setFormatRegistryRole(String formatRegistryRole) {
    this.formatRegistryRole = formatRegistryRole;
  }
}
