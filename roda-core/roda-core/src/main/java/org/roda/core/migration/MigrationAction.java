package org.roda.core.migration;

import org.roda.core.data.exceptions.RODAException;

public interface MigrationAction {
  public void migrate() throws RODAException;
}
