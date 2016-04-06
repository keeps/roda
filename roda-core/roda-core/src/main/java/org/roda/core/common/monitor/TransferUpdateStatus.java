/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

public class TransferUpdateStatus {
  private static TransferUpdateStatus status;
  private static boolean isUpdatingStatus;

  private TransferUpdateStatus() {
  }

  public static TransferUpdateStatus getInstance() {
    if (status == null) {
      status = new TransferUpdateStatus();
      isUpdatingStatus = false;
    }

    return status;
  }

  public boolean isUpdatingStatus() {
    return isUpdatingStatus;
  }

  public void setUpdatingStatus(boolean isUpdatingStatus) {
    TransferUpdateStatus.isUpdatingStatus = isUpdatingStatus;
  };

}
