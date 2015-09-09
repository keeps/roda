package org.roda.storage;

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
