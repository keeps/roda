/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import java.io.Serializable;

import org.roda.core.data.v2.ip.StoragePath;

/**
 * Interface with the minimal/common set of methods that should be implemented
 * by all roda storage model "entities"
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
@FunctionalInterface
public interface Entity extends Serializable {
  /**
   * The path of the entity in storage
   * 
   * @return
   */
  public StoragePath getStoragePath();

}
