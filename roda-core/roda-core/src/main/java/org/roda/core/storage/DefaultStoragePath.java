/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;

public class DefaultStoragePath implements StoragePath {

  @Serial
  private static final long serialVersionUID = 2199265942023315218L;
  private final List<String> path;

  private DefaultStoragePath(List<String> path) {
    super();
    this.path = path;
  }

  public static DefaultStoragePath parse(String... pathPartials) throws RequestNotValidException {
    return parse(Arrays.asList(pathPartials));
  }

  public static DefaultStoragePath parse(List<String> pathPartials) throws RequestNotValidException {
    if (isValid(pathPartials)) {
      return new DefaultStoragePath(pathPartials);
    } else {
      throw new RequestNotValidException("The path \"" + pathPartials + "\" is not valid");
    }
  }

  public static DefaultStoragePath parse(StoragePath base, String... resourceNames) throws RequestNotValidException {
    List<String> pathPartials = new ArrayList<>(base.asList());
    for (String resourceName : resourceNames) {
      pathPartials.add(resourceName);
    }
    return parse(pathPartials);
  }

  public static DefaultStoragePath empty() {
    return new DefaultStoragePath(new ArrayList<>());
  }

  private static boolean isValid(List<String> path) {
    return path != null && !path.isEmpty();
  }

  @Override
  public String getContainerName() {
    return path.get(0);
  }

  @Override
  public List<String> getDirectoryPath() {
    List<String> directoryPath = new ArrayList<>(path);
    if (directoryPath.size() > 2) {
      directoryPath = directoryPath.subList(1, directoryPath.size() - 1);
      return directoryPath;
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public String getName() {
    return path.get(path.size() - 1);
  }

  @Override
  public List<String> asList() {
    return path;
  }

  @Override
  public boolean isFromAContainer() {
    return path.size() == 1;
  }

  @Override
  public String toString() {
    return asString("/", null, null, false);
  }

  @Override
  public String asString(String separator, String replaceAllRegex, String replaceAllReplacement,
    boolean skipContainer) {
    StringBuilder sb = new StringBuilder();
    boolean dontSkip = !skipContainer;

    for (Iterator<String> iterator = path.iterator(); iterator.hasNext();) {
      String string = iterator.next();
      if (dontSkip) {
        if (StringUtils.isNotBlank(replaceAllRegex) && replaceAllReplacement != null) {
          string = string.replaceAll(replaceAllRegex, replaceAllReplacement);
        }
        if (iterator.hasNext()) {
          string += separator;
        }
        sb.append(string);
      }
      dontSkip = true;
    }

    return sb.toString();
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DefaultStoragePath other = (DefaultStoragePath) obj;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    return true;
  }

}
