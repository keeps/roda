/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.storage;

import java.util.List;

public interface StoragePath {

  public static final char SEPARATOR = '/';
  public static final String SEPARATOR_REGEX = "/";

  public String getContainerName();

  public boolean isFromAContainer();

  public List<String> getDirectoryPath();

  public String getName();

  public String asString();

}
