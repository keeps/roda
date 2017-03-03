package org.roda.core.data.v2.ip;

public interface HasPermissions extends HasPermissionFilters {

  public Permissions getPermissions();

  public void setPermissions(Permissions permissions);
}
