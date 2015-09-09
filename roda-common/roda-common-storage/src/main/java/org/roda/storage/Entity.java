package org.roda.storage;

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
