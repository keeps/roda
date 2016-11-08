/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.migration;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsModelObject;

public interface MigrationAction<T extends IsModelObject> {
  /**
   * Method that knows how to migrate a certain model class to a certain version
   */
  public void migrate() throws RODAException;

  /**
   * Validates the toVersion configured in {@link MigrationManager} against
   * internal defined toVersion. We need to do this because one might code this
   * migration action to migrate to version X & some other person might setup it
   * incorrectly in {@link MigrationManager#setupModelMigrations()}to migrate to
   * version Y
   * 
   * @param toVersion
   *          injected by {@link MigrationManager} because, even if this class
   *          is specific to migrate a certain model class to a certain version,
   *          someone might change the version when doing the setup in the
   *          {@link MigrationManager}
   */
  public boolean isToVersionValid(int toVersion);
}
