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
import java.util.Date;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEntryCollection extends AbstractSolrCollection<LogEntry, LogEntry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryCollection.class);

  @Override
  public Class<LogEntry> getIndexClass() {
    return LogEntry.class;
  }

  @Override
  public Class<LogEntry> getModelClass() {
    return LogEntry.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_ACTION_LOG;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_ACTION_LOG);
  }

  @Override
  public String getUniqueId(LogEntry modelObject) {
    return modelObject.getUUID();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.LOG_ACTION_COMPONENT, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.LOG_ACTION_METHOD, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.LOG_ADDRESS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.LOG_DATETIME, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.LOG_DURATION, Field.TYPE_LONG).setRequired(true));
    fields.add(new Field(RodaConstants.LOG_RELATED_OBJECT_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.LOG_USERNAME, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.LOG_STATE, Field.TYPE_STRING).setRequired(true));
    fields.add(new Field(RodaConstants.LOG_PARAMETERS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));
    fields.add(new Field(RodaConstants.INDEX_INSTANCE_ID, Field.TYPE_STRING).setIndexed(true));
    fields.add(new Field(RodaConstants.LOG_LINE_NUMBER, Field.TYPE_LONG).setIndexed(true));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public SolrInputDocument toSolrDocument(LogEntry logEntry, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(logEntry, info);

    doc.addField(RodaConstants.LOG_ACTION_COMPONENT, logEntry.getActionComponent());
    doc.addField(RodaConstants.LOG_ACTION_METHOD, logEntry.getActionMethod());
    doc.addField(RodaConstants.LOG_ADDRESS, logEntry.getAddress());
    doc.addField(RodaConstants.LOG_DATETIME, SolrUtils.formatDate(logEntry.getDatetime()));
    doc.addField(RodaConstants.LOG_DURATION, logEntry.getDuration());
    doc.addField(RodaConstants.LOG_PARAMETERS, JsonUtils.getJsonFromObject(logEntry.getParameters()));
    doc.addField(RodaConstants.LOG_RELATED_OBJECT_ID, logEntry.getRelatedObjectID());
    doc.addField(RodaConstants.LOG_USERNAME, logEntry.getUsername());
    doc.addField(RodaConstants.LOG_STATE, logEntry.getState().toString());
    doc.addField(RodaConstants.INDEX_INSTANCE_ID, logEntry.getInstanceId().toString());
    doc.addField(RodaConstants.LOG_LINE_NUMBER, logEntry.getLineNumber());

    return doc;
  }

  @Override
  public LogEntry fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {

    final String actionComponent = SolrUtils.objectToString(doc.get(RodaConstants.LOG_ACTION_COMPONENT), null);
    final String actionMethod = SolrUtils.objectToString(doc.get(RodaConstants.LOG_ACTION_METHOD), null);
    final String address = SolrUtils.objectToString(doc.get(RodaConstants.LOG_ADDRESS), null);
    final Date datetime = SolrUtils.objectToDate(doc.get(RodaConstants.LOG_DATETIME));
    final long duration = SolrUtils.objectToLong(doc.get(RodaConstants.LOG_DURATION), 0L);

    final String parameters = SolrUtils.objectToString(doc.get(RodaConstants.LOG_PARAMETERS), null);
    final String relatedObjectId = SolrUtils.objectToString(doc.get(RodaConstants.LOG_RELATED_OBJECT_ID), null);
    final String username = SolrUtils.objectToString(doc.get(RodaConstants.LOG_USERNAME), null);
    LogEntryState state = null;

    if (doc.containsKey(RodaConstants.LOG_STATE)) {
      state = LogEntryState
        .valueOf(SolrUtils.objectToString(doc.get(RodaConstants.LOG_STATE), LogEntryState.UNKNOWN.toString()));
    }
    final String instanceId = SolrUtils.objectToString(doc.get(RodaConstants.INDEX_INSTANCE_ID), "");
    final long lineNumber = SolrUtils.objectToLong(doc.get(RodaConstants.LOG_LINE_NUMBER), -1L);

    LogEntry entry = super.fromSolrDocument(doc, fieldsToReturn);
    entry.setActionComponent(actionComponent);
    entry.setActionMethod(actionMethod);
    entry.setAddress(address);
    entry.setDatetime(datetime);
    entry.setDuration(duration);
    entry.setState(state);
    try {
      if (fieldsToReturn.isEmpty() || fieldsToReturn.contains(RodaConstants.LOG_PARAMETERS)) {
        entry.setParameters(JsonUtils.getListFromJson(parameters == null ? "" : parameters, LogEntryParameter.class));
      }
    } catch (GenericException e) {
      LOGGER.error("Error parsing log entry parameters", e);
    }

    entry.setRelatedObjectID(relatedObjectId);
    entry.setUsername(username);
    entry.setInstanceId(instanceId);
    entry.setLineNumber(lineNumber);
    return entry;
  }

}
