package org.roda.core.plugins.plugins.internal.synchronization.bundle;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class PackageState implements Serializable {
  private static final long serialVersionUID = -8011849103435939717L;
  Status status = Status.CREATED;
  int count = 0;

  public enum Status {
    CREATED, FAILED, SUCCESS
  }

  public PackageState() {
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
