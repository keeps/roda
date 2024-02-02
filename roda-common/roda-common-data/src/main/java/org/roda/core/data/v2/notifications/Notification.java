/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.notifications;

import java.io.Serial;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.IsModelObject;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.HasId;
import org.roda.core.data.v2.ip.HasInstanceID;
import org.roda.core.data.v2.ip.HasInstanceName;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@jakarta.xml.bind.annotation.XmlRootElement(name = RodaConstants.RODA_OBJECT_NOTIFICATION)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Notification implements IsModelObject, IsIndexed, HasId, HasInstanceID, HasInstanceName {
  @Serial
  private static final long serialVersionUID = -585753367605901060L;

  private String id = null;
  private String subject = null;
  private String body = null;
  private Date sentOn = null;
  private String fromUser = null;
  private List<String> recipientUsers = null;
  private String acknowledgeToken = null;
  private boolean isAcknowledged = false;
  private Map<String, String> acknowledgedUsers = null;
  private NotificationState state;

  private Map<String, Object> fields;

  private String instanceId = null;

  private String instanceName = null;

  public Notification() {
    super();
    this.sentOn = new Date();
    this.acknowledgedUsers = new HashMap<>();
    this.state = NotificationState.CREATED;
  }

  public Notification(Notification notification) {
    this.id = notification.getId();
    this.subject = notification.getSubject();
    this.body = notification.getBody();
    this.sentOn = notification.getSentOn();
    this.fromUser = notification.getFromUser();
    this.recipientUsers = notification.getRecipientUsers();
    this.acknowledgeToken = notification.getAcknowledgeToken();
    this.isAcknowledged = notification.isAcknowledged();
    this.acknowledgedUsers = notification.getAcknowledgedUsers();
    this.state = NotificationState.CREATED;
    this.instanceId = notification.getInstanceId();
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Date getSentOn() {
    return sentOn;
  }

  public void setSentOn(Date sentOn) {
    this.sentOn = sentOn;
  }

  public String getFromUser() {
    return fromUser;
  }

  public void setFromUser(String fromUser) {
    this.fromUser = fromUser;
  }

  public String getAcknowledgeToken() {
    return acknowledgeToken;
  }

  public void setAcknowledgeToken(String acknowledgeToken) {
    this.acknowledgeToken = acknowledgeToken;
  }

  public boolean isAcknowledged() {
    return isAcknowledged;
  }

  public void setAcknowledged(boolean isAcknowledged) {
    this.isAcknowledged = isAcknowledged;
  }

  public List<String> getRecipientUsers() {
    return recipientUsers;
  }

  public void setRecipientUsers(List<String> recipientUsers) {
    this.recipientUsers = recipientUsers;
  }

  public Map<String, String> getAcknowledgedUsers() {
    return acknowledgedUsers;
  }

  public void setAcknowledgedUsers(Map<String, String> acknowledgedUsers) {
    this.acknowledgedUsers = acknowledgedUsers;
  }

  public void addAcknowledgedUser(String recipientUser, String acknowledgedOn) {
    this.acknowledgedUsers.put(recipientUser, acknowledgedOn);
  }

  public NotificationState getState() {
    return state;
  }

  public void setState(NotificationState state) {
    this.state = state;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public String toString() {
    return "Notification [id=" + id + ", subject=" + subject + ", body=" + body + ", sentOn=" + sentOn + ", fromUser="
      + fromUser + ", recipientUsers=" + recipientUsers + ", acknowledgeToken=" + acknowledgeToken + ", isAcknowledged="
      + isAcknowledged + ", acknowledgedUsers=" + acknowledgedUsers + ", state=" + state + ", instanceId=" + instanceId
      + ", instanceName=" + instanceName + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "subject", "body", "sentOn", "fromUser", "recipientUsers", "acknowledgeToken",
      "isAcknowledged", "acknowledgedUsers", "state", "instanceId", "instanceName");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, subject, body, sentOn, fromUser, recipientUsers, acknowledgeToken, isAcknowledged,
      acknowledgedUsers, state, instanceId, instanceName);
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

  /**
   * @return the fields
   */
  public Map<String, Object> getFields() {
    return fields;
  }

  /**
   * @param fields
   *          the fields to set
   */
  public void setFields(Map<String, Object> fields) {
    this.fields = fields;
  }
}
