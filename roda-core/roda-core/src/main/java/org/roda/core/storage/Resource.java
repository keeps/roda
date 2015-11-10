/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

/**
 * Interface of the parent model entity that can be both a directory or a
 * binary.
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public interface Resource extends Entity {

  /**
   * Check if this resource is a container and therefore has no content but has
   * sub-resources.
   * 
   * @return
   */
  public boolean isDirectory();

}
