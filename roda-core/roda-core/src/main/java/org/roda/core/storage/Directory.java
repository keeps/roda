/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

public interface Directory extends Resource {

  /**
   * OTHER METHODS TO CONSIDER:
   * 
   * * The total number of bytes of content of all sub-resources if this is a
   * directory.
   * 
   * public Long geTotalSizeInBytes();
   * 
   * 
   * * The number of resources under this directory.
   * 
   * public int getResourceCount();
   * 
   * 
   */
}
