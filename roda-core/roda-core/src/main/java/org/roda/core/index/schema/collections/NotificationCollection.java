/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.schema.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.IndexUtils;
import org.roda.core.index.utils.SolrUtils;

public class NotificationCollection extends AbstractSolrCollection<Notification, Notification> {

  // private static final Logger LOGGER =
  // LoggerFactory.getLogger(NotificationCollection.class);

  @Override
  public Class<Notification> getIndexClass() {
    return Notification.class;
  }

  @Override
  public Class<Notification> getModelClass() {
    return Notification.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_NOTIFICATION;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_NOTIFICATION);
  }

  @Override
  public String getUniqueId(Notification modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.NOTIFICATION_SUBJECT, Field.TYPE_TEXT).setRequired(true).setMultiValued(false));
    fields.add(new Field(RodaConstants.NOTIFICATION_BODY, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.NOTIFICATION_SENT_ON, Field.TYPE_DATE).setRequired(true));
    fields.add(new Field(RodaConstants.NOTIFICATION_FROM_USER, Field.TYPE_STRING).setRequired(true));
    fields.add(
      new Field(RodaConstants.NOTIFICATION_RECIPIENT_USERS, Field.TYPE_STRING).setRequired(true).setMultiValued(true));
    fields.add(new Field(RodaConstants.NOTIFICATION_ACKNOWLEDGE_TOKEN, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED, Field.TYPE_BOOLEAN).setRequired(true));
    fields.add(new Field(RodaConstants.NOTIFICATION_STATE, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS, Field.TYPE_STRING).setIndexed(false)
      .setDocValues(false));

    fields.add(new Field(RodaConstants.INDEX_INSTANCE_ID, Field.TYPE_TEXT).setRequired(false).setMultiValued(false));
    fields.add(new Field(RodaConstants.INDEX_INSTANCE_NAME, Field.TYPE_STRING));
    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(Notification notification, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(notification, info);

    doc.addField(RodaConstants.NOTIFICATION_SUBJECT, notification.getSubject());
    doc.addField(RodaConstants.NOTIFICATION_BODY, notification.getBody());
    doc.addField(RodaConstants.NOTIFICATION_SENT_ON, SolrUtils.formatDate(notification.getSentOn()));
    doc.addField(RodaConstants.NOTIFICATION_FROM_USER, notification.getFromUser());
    doc.addField(RodaConstants.NOTIFICATION_RECIPIENT_USERS, notification.getRecipientUsers());
    doc.addField(RodaConstants.NOTIFICATION_ACKNOWLEDGE_TOKEN, notification.getAcknowledgeToken());
    doc.addField(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED, notification.isAcknowledged());
    doc.addField(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS,
      JsonUtils.getJsonFromObject(notification.getAcknowledgedUsers()));
    doc.addField(RodaConstants.NOTIFICATION_STATE, notification.getState().toString());
    doc.addField(RodaConstants.INDEX_INSTANCE_ID, notification.getInstanceId());

    String name = IndexUtils.giveNameFromLocalInstanceIdentifier(notification.getInstanceId());
    
    doc.addField(RodaConstants.INDEX_INSTANCE_NAME, name);

    return doc;
  }

  @Override
  public Notification fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final Notification notification = super.fromSolrDocument(doc, fieldsToReturn);

    notification.setSubject(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_SUBJECT), null));
    notification.setBody(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_BODY), null));
    notification.setSentOn(SolrUtils.objectToDate(doc.get(RodaConstants.NOTIFICATION_SENT_ON)));
    notification.setFromUser(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_FROM_USER), null));
    notification.setRecipientUsers(SolrUtils.objectToListString(doc.get(RodaConstants.NOTIFICATION_RECIPIENT_USERS)));
    notification
      .setAcknowledgeToken(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_ACKNOWLEDGE_TOKEN), null));
    notification
      .setAcknowledged(SolrUtils.objectToBoolean(doc.get(RodaConstants.NOTIFICATION_IS_ACKNOWLEDGED), Boolean.FALSE));
    if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS)) {
      notification.setAcknowledgedUsers(
        JsonUtils.getMapFromJson(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_ACKNOWLEDGED_USERS), "")));
    }

    if (doc.containsKey(RodaConstants.NOTIFICATION_STATE)) {
      String defaultState = NotificationState.COMPLETED.toString();
      notification.setState(
        NotificationState.valueOf(SolrUtils.objectToString(doc.get(RodaConstants.NOTIFICATION_STATE), defaultState)));
    }

    notification.setInstanceId(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_ID), null));
    notification.setInstanceName(SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_NAME), null));
    return notification;

  }

}
