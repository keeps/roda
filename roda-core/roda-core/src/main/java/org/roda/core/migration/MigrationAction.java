package org.roda.core.migration;

import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsModelObject;

public interface MigrationAction<T extends IsModelObject> {
  /**
   * Method that knows how to migrate a certain model class to a certain version
   * 
   * @param toVersion
   *          injected by {@link MigrationManager} because, even if this class
   *          is specific to migrate a certain model class to a certain version,
   *          someone might change the version when doing the setup in the
   *          {@link MigrationManager}
   */
  public void migrate(int toVersion) throws RODAException;
}
