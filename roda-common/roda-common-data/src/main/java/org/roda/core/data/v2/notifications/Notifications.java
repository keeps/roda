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

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.RODAObjectList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_NOTIFICATIONS)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notifications implements RODAObjectList<Notification> {
  private static final long serialVersionUID = -6470632839283128013L;
  private List<Notification> notificationList;

  public Notifications() {
    super();
    notificationList = new ArrayList<>();
  }

  public Notifications(List<Notification> notifications) {
    super();
    this.notificationList = notifications;
  }

  @Override
  @JsonProperty(value = RodaConstants.RODA_OBJECT_NOTIFICATIONS)
  @XmlElement(name = RodaConstants.RODA_OBJECT_NOTIFICATION)
  public List<Notification> getObjects() {
    return notificationList;
  }

  @Override
  public void setObjects(List<Notification> notifications) {
    this.notificationList = notifications;
  }

  @Override
  public void addObject(Notification notification) {
    this.notificationList.add(notification);
  }

}
