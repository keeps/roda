/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.messages;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "message")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Message implements IsIndexed, Serializable {

  private static final long serialVersionUID = -585753367605901060L;

  private String id = null;
  // FIXME uuid is needed but should it?
  private String uuid = null;
  private String subject = null;
  private String body = null;
  private Date sentOn = null;
  private String fromUser = null;
  private String recipientUser = null;
  private String acknowledgeToken = null;
  private boolean isAcknowledged = false;
  private Date acknowledgedOn = null;

  public Message() {
    super();
    this.sentOn = new Date();
  }

  public Message(Message message) {
    this.id = message.getId();
    this.subject = message.getSubject();
    this.body = message.getBody();
    this.sentOn = message.getSentOn();
    this.fromUser = message.getFromUser();
    this.recipientUser = message.getRecipientUser();
    this.acknowledgeToken = message.getAcknowledgeToken();
    this.isAcknowledged = message.isAcknowledged();
    this.acknowledgedOn = message.getAcknowledgedOn();
  }

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

  public String getRecipientUser() {
    return recipientUser;
  }

  public void setRecipientUser(String recipientUser) {
    this.recipientUser = recipientUser;
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

  public Date getAcknowledgedOn() {
    return acknowledgedOn;
  }

  public void setAcknowledgedOn(Date acknowledgedOn) {
    this.acknowledgedOn = acknowledgedOn;
  }

  @Override
  public String toString() {
    return "Format [id=" + id + ", subject=" + subject + ", body=" + body + ", sentOn=" + sentOn + ", fromUser="
      + fromUser + ", recipientUser=" + recipientUser + ", acknowledgeToken=" + acknowledgeToken + ", isAcknowledged="
      + isAcknowledged + ", acknowledgedOn=" + acknowledgedOn + "]";
  }

  @Override
  public String getUUID() {
    return getId();
  }

}
