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
