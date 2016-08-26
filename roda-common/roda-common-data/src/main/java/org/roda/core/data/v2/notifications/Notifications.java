/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.notifications;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = "notifications")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifications implements RODAObjectList<Notification> {
  private List<Notification> notifications;

  public Notifications() {
    super();
    notifications = new ArrayList<Notification>();
  }

  public Notifications(List<Notification> notifications) {
    super();
    this.notifications = notifications;
  }

  @JsonProperty(value = "notifications")
  @XmlElement(name = "notification")
  public List<Notification> getObjects() {
    return notifications;
  }

  public void setObjects(List<Notification> notifications) {
    this.notifications = notifications;
  }

  public void addObject(Notification notification) {
    this.notifications.add(notification);
  }

}
