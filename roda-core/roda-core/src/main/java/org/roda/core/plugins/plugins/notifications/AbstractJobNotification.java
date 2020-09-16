/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
