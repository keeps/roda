package org.roda.core.plugins.plugins.notifications;

public abstract class AbstractJobNotification implements JobNotification {
  private String to;
  private boolean whenFailed;

  public AbstractJobNotification(String to) {
    this.to = to;
    this.whenFailed = false;
  }

  public AbstractJobNotification(String to, boolean whenFailed) {
    this.to = to;
    this.whenFailed = whenFailed;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  @Override
  public boolean whenFailed() {
    return whenFailed;
  }

  public void setWhenFailed(boolean whenFailed) {
    this.whenFailed = whenFailed;
  }
}
