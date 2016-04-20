/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.List;

public interface StoragePath {

  public String getContainerName();

  public boolean isFromAContainer();

  public List<String> getDirectoryPath();

  public String getName();

  public List<String> asList();

  // Force re-declaration of #hashCode() and #equals(Object)

  public int hashCode();

  public boolean equals(Object obj);

  public String asString(String separator, String replaceAllRegex, String replaceAllReplacement, boolean skipContainer);

}
