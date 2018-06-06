/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.List;

public interface StoragePath extends Serializable {

  String getContainerName();

  boolean isFromAContainer();

  List<String> getDirectoryPath();

  String getName();

  List<String> asList();

  // Force re-declaration of #hashCode() and #equals(Object)

  @Override
  int hashCode();

  @Override
  boolean equals(Object obj);

  String asString(String separator, String replaceAllRegex, String replaceAllReplacement, boolean skipContainer);

}
