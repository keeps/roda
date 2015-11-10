/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.util.Map;
import java.util.Set;

/**
 * Interface with the minimal/common set of methods that should be implemented
 * by all roda storage model "entities"
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * */
public interface Entity {
  /**
   * The path of the entity in storage
   * 
   * @return
   */
  public StoragePath getStoragePath();

  /**
   * The metadata of the current resource.
   * 
   * @return
   */
  public Map<String, Set<String>> getMetadata();
}
