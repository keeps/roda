/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class loader that has multiple loaders and uses them for loading classes
 * and resources.
 *
 * @author Decebal Suiu
 * @author Luis Faria <lfaria@keep.pt>
 */
public class CompoundClassLoader extends ClassLoader {
    
  private Set<ClassLoader> loaders = new HashSet<>();

  public void addLoader(ClassLoader loader) {
    loaders.add(loader);
  }

  public void removeLoader(ClassLoader loader) {
    loaders.remove(loader);
  }

  @Override
  public Class<?> findClass(String name) throws ClassNotFoundException {
    for (ClassLoader loader : loaders) {
      try {
        return loader.loadClass(name);
      } catch (ClassNotFoundException e) {
        // try next
      }
    }

    throw new ClassNotFoundException(name);
  }

  @Override
  public URL findResource(String name) {
    for (ClassLoader loader : loaders) {
      URL url = loader.getResource(name);
      if (url != null) {
        return url;
      }
    }

    return null;
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    List<URL> resources = new ArrayList<>();
    for (ClassLoader loader : loaders) {
      resources.addAll(Collections.list(loader.getResources(name)));
    }

    return Collections.enumeration(resources);
  }

}