/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;

public class DefaultStoragePath implements StoragePath {

  private final String path;

  public static DefaultStoragePath parse(String path) throws RequestNotValidException {
    if (isValid(path)) {
      return new DefaultStoragePath(sanitizePath(path));
    } else {
      throw new RequestNotValidException("The path \"" + path + "\" is not valid");
    }
  }

  public static DefaultStoragePath parse(String... pathPartials) throws RequestNotValidException {
    StringBuilder builder = new StringBuilder();
    boolean empty = true;
    for (String pathPartial : pathPartials) {
      if (!empty) {
        builder.append(SEPARATOR);
      } else {
        empty = false;
      }
      builder.append(pathPartial);

    }
    return parse(builder.toString());
  }
  
  public static DefaultStoragePath parse(Iterable<String> pathPartials) throws RequestNotValidException {
    StringBuilder builder = new StringBuilder();
    boolean empty = true;
    for (String pathPartial : pathPartials) {
      if (!empty) {
        builder.append(SEPARATOR);
      } else {
        empty = false;
      }
      builder.append(pathPartial);

    }
    return parse(builder.toString());
  }

  public static DefaultStoragePath parse(StoragePath base, String resourceName) throws RequestNotValidException {
    if (resourceName.contains(SEPARATOR_REGEX)) {
      throw new RequestNotValidException("The resource name is not valid: " + resourceName);
    } else {
      return parse(base.asString() + SEPARATOR + resourceName);
    }
  }

  private static String sanitizePath(String path) {
    int init = 0, ends = path.length();
    if (path.charAt(0) == SEPARATOR) {
      init = 1;
    }
    if (path.charAt(ends - 1) == SEPARATOR) {
      ends--;
    }
    return path.substring(init, ends);
  }

  private static boolean isValid(String path) {
    return path != null && path.trim().length() > 0;
  }

  private DefaultStoragePath(String path) {
    super();
    this.path = path.trim();

  }

  @Override
  public String getContainerName() {
    String containerName;
    int firstIndexOfSlash = path.indexOf(SEPARATOR);
    if (firstIndexOfSlash < 0) {
      containerName = path;
    } else {
      containerName = path.substring(0, firstIndexOfSlash);
    }

    return containerName;
  }

  @Override
  public List<String> getDirectoryPath() {
    List<String> directoryPath = new ArrayList<String>(Arrays.asList(path.split(SEPARATOR_REGEX)));
    if (directoryPath.size() > 2) {
      directoryPath.remove(0);
      directoryPath.remove(directoryPath.size() - 1);
      return directoryPath;
    } else {
      return new ArrayList<String>();
    }
  }

  @Override
  public String getName() {
    String name;
    int lastIndexOfSlash = path.lastIndexOf(SEPARATOR);
    if (lastIndexOfSlash < 0) {
      name = path;
    } else {
      name = path.substring(lastIndexOfSlash + 1);
    }

    return name;
  }

  @Override
  public String asString() {
    return path;
  }

  @Override
  public boolean isFromAContainer() {
    return path.indexOf(SEPARATOR) == -1;
  }

  @Override
  public String toString() {
    return asString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
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
    DefaultStoragePath other = (DefaultStoragePath) obj;
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    return true;
  }

}
