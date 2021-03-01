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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodCalculation;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalConfirmationAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.index.IndexingAdditionalInfo;
import org.roda.core.index.IndexingAdditionalInfo.Flags;
import org.roda.core.index.schema.AbstractSolrCollection;
import org.roda.core.index.schema.CopyField;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrCollection;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.storage.Directory;
import org.roda.core.storage.StorageService;

public class AIPCollection extends AbstractSolrCollection<IndexedAIP, AIP> {

  @Override
  public Class<IndexedAIP> getIndexClass() {
    return IndexedAIP.class;
  }

  @Override
  public Class<AIP> getModelClass() {
    return AIP.class;
  }

  @Override
  public String getIndexName() {
    return RodaConstants.INDEX_AIP;
  }

  @Override
  public List<String> getCommitIndexNames() {
    return Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.INDEX_REPRESENTATION, RodaConstants.INDEX_FILE);
  }

  @Override
  public String getUniqueId(AIP modelObject) {
    return modelObject.getId();
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(RodaConstants.AIP_PARENT_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_TYPE, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_ANCESTORS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_CREATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_CREATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_UPDATED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_UPDATED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_REPRESENTATION_ID, Field.TYPE_STRING).setMultiValued(true));
    fields.add(
      new Field(RodaConstants.AIP_HAS_REPRESENTATIONS, Field.TYPE_BOOLEAN).setRequired(true).setDefaultValue("false"));
    fields.add(new Field(RodaConstants.AIP_NUMBER_OF_SUBMISSION_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.AIP_NUMBER_OF_DOCUMENTATION_FILES, Field.TYPE_LONG));
    fields.add(new Field(RodaConstants.AIP_NUMBER_OF_SCHEMA_FILES, Field.TYPE_LONG));

    fields.add(new Field(RodaConstants.INGEST_SIP_IDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.INGEST_JOB_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.INGEST_UPDATE_JOB_IDS, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.ALL_INGEST_JOB_IDS, Field.TYPE_STRING).setStored(false).setMultiValued(true));

    fields.add(new Field(RodaConstants.AIP_LEVEL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_TITLE, Field.TYPE_TEXT).setMultiValued(false));
    fields.add(new Field(RodaConstants.AIP_GHOST, Field.TYPE_BOOLEAN).setDefaultValue("false"));
    fields.add(new Field(RodaConstants.AIP_HAS_SHALLOW_FILES, Field.TYPE_BOOLEAN).setDefaultValue("false"));

    fields.add(new Field(RodaConstants.AIP_DESCRIPTION, Field.TYPE_TEXT).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_DATE_INITIAL, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_DATE_FINAL, Field.TYPE_DATE));

    fields.add(new Field(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_HOLDS_ID, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_TRANSITIVE_DISPOSAL_HOLDS_ID, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_DESTROYED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_DESTROYED_BY, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_ACTION, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_SCHEDULE_ASSOCIATION_TYPE, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DURATION, Field.TYPE_INT));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_INTERVAL, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_START_DATE, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DETAILS, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION, Field.TYPE_STRING));

    fields.add(new Field(RodaConstants.AIP_OVERDUE_DATE, Field.TYPE_DATE));

    fields.add(new Field(RodaConstants.AIP_DISPOSAL_HOLDS_ID, Field.TYPE_STRING).setMultiValued(true));
    fields.add(new Field(RodaConstants.AIP_DISPOSAL_HOLD_STATUS, Field.TYPE_BOOLEAN).setDefaultValue("false"));

    fields.add(new Field(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID, Field.TYPE_STRING));
    fields.add(new Field(RodaConstants.AIP_DESTROYED_ON, Field.TYPE_DATE));
    fields.add(new Field(RodaConstants.AIP_DESTROYED_BY, Field.TYPE_STRING));

    fields.add(SolrCollection.getSortFieldOf(RodaConstants.AIP_TITLE));

    return fields;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Arrays.asList(SolrCollection.getCopyAllToSearchField(),
      SolrCollection.getSortCopyFieldOf(RodaConstants.AIP_TITLE),
      new CopyField(RodaConstants.INGEST_JOB_ID, RodaConstants.ALL_INGEST_JOB_IDS),
      new CopyField(RodaConstants.INGEST_UPDATE_JOB_IDS, RodaConstants.ALL_INGEST_JOB_IDS));
  }

  @Override
  public SolrInputDocument toSolrDocument(AIP aip, IndexingAdditionalInfo info)
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    boolean safemode = info.getFlags().contains(Flags.SAFE_MODE_ON);
    SolrInputDocument doc = super.toSolrDocument(aip, info);

    doc.addField(RodaConstants.AIP_PARENT_ID, aip.getParentId());
    doc.addField(RodaConstants.AIP_TYPE, aip.getType());

    doc.addField(RodaConstants.AIP_CREATED_ON, SolrUtils.formatDate(aip.getCreatedOn()));
    doc.addField(RodaConstants.AIP_CREATED_BY, aip.getCreatedBy());
    doc.addField(RodaConstants.AIP_UPDATED_ON, SolrUtils.formatDate(aip.getUpdatedOn()));
    doc.addField(RodaConstants.AIP_UPDATED_BY, aip.getUpdatedBy());

    if (aip.getDisposal() != null) {
      if (aip.getDisposal().getSchedule() != null) {
        DisposalScheduleAIPMetadata schedule = aip.getDisposal().getSchedule();
        doc.addField(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID, schedule.getId());
        doc.addField(RodaConstants.AIP_DISPOSAL_SCHEDULE_ASSOCIATION_TYPE, schedule.getAssociationType().name());
      }

      if (aip.getDisposal().getHolds() != null) {
        List<DisposalHoldAIPMetadata> holds = aip.getDisposal().getHolds();
        doc.addField(RodaConstants.AIP_DISPOSAL_HOLDS_ID,
          holds.stream().map(DisposalHoldAIPMetadata::getId).collect(Collectors.toList()));
      }

      if (aip.getDisposal().getTransitiveHolds() != null) {
        List<DisposalTransitiveHoldAIPMetadata> transitiveHolds = aip.getDisposal().getTransitiveHolds();
        doc.addField(RodaConstants.AIP_TRANSITIVE_DISPOSAL_HOLDS_ID,
          transitiveHolds.stream().map(DisposalTransitiveHoldAIPMetadata::getId).collect(Collectors.toList()));
      }

      if (aip.getDisposal().getConfirmation() != null) {
        DisposalConfirmationAIPMetadata confirmation = aip.getDisposal().getConfirmation();
        doc.addField(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID, confirmation.getId());
        if (confirmation.getDestruction() != null) {
          doc.addField(RodaConstants.AIP_DESTROYED_ON, confirmation.getDestruction().getDestructionOn());
          doc.addField(RodaConstants.AIP_DESTROYED_BY, confirmation.getDestruction().getDestructionBy());
        }
      }
    }

    doc.addField(RodaConstants.INGEST_SIP_IDS, aip.getIngestSIPIds());
    doc.addField(RodaConstants.INGEST_JOB_ID, aip.getIngestJobId());
    doc.addField(RodaConstants.INGEST_UPDATE_JOB_IDS, aip.getIngestUpdateJobIds());

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(DescriptiveMetadata::getId)
      .collect(Collectors.toList());

    doc.addField(RodaConstants.AIP_DESCRIPTIVE_METADATA_ID, descriptiveMetadataIds);

    List<String> representationIds = aip.getRepresentations().stream().map(Representation::getId)
      .collect(Collectors.toList());
    doc.addField(RodaConstants.AIP_REPRESENTATION_ID, representationIds);
    doc.addField(RodaConstants.AIP_HAS_REPRESENTATIONS, !representationIds.isEmpty());

    doc.addField(RodaConstants.AIP_GHOST, aip.getGhost() != null ? aip.getGhost() : false);
    doc.addField(RodaConstants.AIP_HAS_SHALLOW_FILES,
      aip.getHasShallowFiles() != null ? aip.getHasShallowFiles() : false);

    ModelService model = RodaCoreFactory.getModelService();
    if (!safemode) {
      SolrUtils.indexDescriptiveMetadataFields(RodaCoreFactory.getModelService(), aip.getId(), null,
        aip.getDescriptiveMetadata(), doc);
    }

    // Calculate number of documentation and schema files
    StorageService storage = RodaCoreFactory.getStorageService();

    Long numberOfSubmissionFiles;
    try {
      Directory submissionDirectory = model.getSubmissionDirectory(aip.getId());
      numberOfSubmissionFiles = storage.countResourcesUnderDirectory(submissionDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfSubmissionFiles = 0L;
    }

    Long numberOfDocumentationFiles;
    try {
      Directory documentationDirectory = model.getDocumentationDirectory(aip.getId());
      numberOfDocumentationFiles = storage.countResourcesUnderDirectory(documentationDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfDocumentationFiles = 0L;
    }

    Long numberOfSchemaFiles;
    try {
      Directory schemasDirectory = model.getSchemasDirectory(aip.getId());
      numberOfSchemaFiles = storage.countResourcesUnderDirectory(schemasDirectory.getStoragePath(), true);
    } catch (NotFoundException e) {
      numberOfSchemaFiles = 0L;
    }

    doc.addField(RodaConstants.AIP_NUMBER_OF_SUBMISSION_FILES, numberOfSubmissionFiles);
    doc.addField(RodaConstants.AIP_NUMBER_OF_DOCUMENTATION_FILES, numberOfDocumentationFiles);
    doc.addField(RodaConstants.AIP_NUMBER_OF_SCHEMA_FILES, numberOfSchemaFiles);

    return doc;
  }

  public static class Info extends IndexingAdditionalInfo {
    private final List<String> ancestors;
    private final boolean safeMode;
    private final DisposalSchedule disposalSchedule;
    private final Map<String, String> retentionPeriod;
    private final boolean disposalHoldStatus;

    public Info(List<String> ancestors, boolean safeMode) {
      this(ancestors, safeMode, null, null, false);
    }

    public Info(List<String> ancestors, boolean safeMode, DisposalSchedule disposalSchedule,
      Map<String, String> retentionPeriod, boolean disposalHoldStatus) {
      super();
      this.ancestors = ancestors;
      this.safeMode = safeMode;
      this.disposalSchedule = disposalSchedule;
      this.retentionPeriod = retentionPeriod;
      this.disposalHoldStatus = disposalHoldStatus;
    }

    @Override
    public Map<String, Object> getPreCalculatedFields() {
      Map<String, Object> preCalculatedFields = new HashMap<>();
      preCalculatedFields.put(RodaConstants.AIP_ANCESTORS, ancestors);
      if (disposalSchedule != null) {
        preCalculatedFields.putAll(retentionPeriod);
        preCalculatedFields.put(RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME, disposalSchedule.getTitle());
        if (!disposalSchedule.getActionCode().equals(DisposalActionCode.RETAIN_PERMANENTLY)) {
          preCalculatedFields.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DURATION,
            disposalSchedule.getRetentionPeriodDuration());
          preCalculatedFields.put(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_INTERVAL,
            disposalSchedule.getRetentionPeriodIntervalCode().name());
        }
        preCalculatedFields.put(RodaConstants.AIP_DISPOSAL_ACTION, disposalSchedule.getActionCode().name());
      }
      preCalculatedFields.put(RodaConstants.AIP_DISPOSAL_HOLD_STATUS, disposalHoldStatus);
      return preCalculatedFields;
    }

    @Override
    public List<Flags> getFlags() {
      return Arrays.asList(safeMode ? Flags.SAFE_MODE_ON : Flags.SAFE_MODE_OFF);
    }

  }

  @Override
  public IndexedAIP fromSolrDocument(SolrDocument doc, List<String> fieldsToReturn) throws GenericException {
    final IndexedAIP ret = super.fromSolrDocument(doc, fieldsToReturn);

    final String parentId = SolrUtils.objectToString(doc.get(RodaConstants.AIP_PARENT_ID), null);
    final List<String> ingestSIPIds = SolrUtils.objectToListString(doc.get(RodaConstants.INGEST_SIP_IDS));
    final String ingestJobId = SolrUtils.objectToString(doc.get(RodaConstants.INGEST_JOB_ID), "");
    final List<String> ingestUpdateJobIds = SolrUtils.objectToListString(doc.get(RodaConstants.INGEST_UPDATE_JOB_IDS));
    final List<String> allIngestJobIds = SolrUtils.objectToListString(doc.get(RodaConstants.ALL_INGEST_JOB_IDS));
    final List<String> ancestors = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_ANCESTORS));
    final String type = SolrUtils.objectToString(doc.get(RodaConstants.AIP_TYPE), "");
    final List<String> levels = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_LEVEL));
    final List<String> titles = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_TITLE));
    final List<String> descriptions = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_DESCRIPTION));
    final Date dateInitial = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_DATE_INITIAL));
    final Date dateFinal = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_DATE_FINAL));
    final Long numberOfSubmissionFiles = SolrUtils.objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_SUBMISSION_FILES),
      0L);
    final Long numberOfDocumentationFiles = SolrUtils
      .objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_DOCUMENTATION_FILES), 0L);
    final Long numberOfSchemaFiles = SolrUtils.objectToLong(doc.get(RodaConstants.AIP_NUMBER_OF_SCHEMA_FILES), 0L);

    final Boolean hasRepresentations = SolrUtils.objectToBoolean(doc.get(RodaConstants.AIP_HAS_REPRESENTATIONS),
      Boolean.FALSE);
    final Boolean ghost = SolrUtils.objectToBoolean(doc.get(RodaConstants.AIP_GHOST), Boolean.FALSE);
    final Boolean hasShallowFiles = SolrUtils.objectToBoolean(doc.get(RodaConstants.AIP_HAS_SHALLOW_FILES),
      Boolean.FALSE);

    final String title = titles.isEmpty() ? null : titles.get(0);
    final String description = descriptions.isEmpty() ? null : descriptions.get(0);

    final Date createdOn = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_CREATED_ON));
    final String createdBy = SolrUtils.objectToString(doc.get(RodaConstants.AIP_CREATED_BY), "");
    final Date updatedOn = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_UPDATED_ON));
    final String updatedBy = SolrUtils.objectToString(doc.get(RodaConstants.AIP_UPDATED_BY), "");
    final String disposalScheduleId = SolrUtils.objectToString(doc.get(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID), null);
    final String disposalScheduleName = SolrUtils.objectToString(doc.get(RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME), "");
    final Integer retentionPeriodDuration = SolrUtils
      .objectToInteger(doc.get(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DURATION), null);
    final RetentionPeriodIntervalCode retentionPeriodInterval = SolrUtils.objectToEnum(
      doc.get(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_INTERVAL), RetentionPeriodIntervalCode.class, null);
    final List<String> disposalHoldsId = SolrUtils.objectToListString(doc.get(RodaConstants.AIP_DISPOSAL_HOLDS_ID));
    final List<String> transitiveDisposalHoldsId = SolrUtils
      .objectToListString(doc.get(RodaConstants.AIP_TRANSITIVE_DISPOSAL_HOLDS_ID));
    final Date destroyedOn = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_DESTROYED_ON));
    final String destroyedBy = SolrUtils.objectToString(doc.get(RodaConstants.AIP_DESTROYED_BY), "");
    final DisposalActionCode disposalAction = SolrUtils.objectToEnum(doc.get(RodaConstants.AIP_DISPOSAL_ACTION),
      DisposalActionCode.class, null);
    final Date overdueDate = SolrUtils.objectToDate(doc.get(RodaConstants.AIP_OVERDUE_DATE));
    final Boolean disposalHoldStatus = SolrUtils.objectToBoolean(doc.get(RodaConstants.AIP_DISPOSAL_HOLD_STATUS),
      Boolean.FALSE);
    final String disposalConfirmationId = SolrUtils.objectToString(doc.get(RodaConstants.AIP_DISPOSAL_CONFIRMATION_ID),
      null);
    AIPDisposalScheduleAssociationType aipDisposalScheduleAssociationType = SolrUtils.objectToEnum(
      doc.get(RodaConstants.AIP_DISPOSAL_SCHEDULE_ASSOCIATION_TYPE), AIPDisposalScheduleAssociationType.class, null);
    final Date retentionPeriodStartDate = SolrUtils
      .objectToDate(doc.get(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_START_DATE));
    final String retentionPeriodDetails = SolrUtils
      .objectToString(doc.get(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_DETAILS), null);
    final RetentionPeriodCalculation retentionPeriodCalculation = SolrUtils.objectToEnum(
      doc.get(RodaConstants.AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION), RetentionPeriodCalculation.class, null);

    String level;
    if (ghost) {
      level = RodaConstants.AIP_GHOST;
    } else {
      level = levels.isEmpty() ? null : levels.get(0);
    }

    ret.setType(type);
    ret.setLevel(level);
    ret.setTitle(title);
    ret.setDateInitial(dateInitial);
    ret.setDateFinal(dateFinal);
    ret.setDescription(description);
    ret.setParentID(parentId);
    ret.setAncestors(ancestors);
    ret.setNumberOfSubmissionFiles(numberOfSubmissionFiles);
    ret.setNumberOfDocumentationFiles(numberOfDocumentationFiles);
    ret.setNumberOfSchemaFiles(numberOfSchemaFiles);
    ret.setHasRepresentations(hasRepresentations);
    ret.setGhost(ghost);
    ret.setHasShallowFiles(hasShallowFiles);
    ret.setIngestSIPIds(ingestSIPIds).setIngestJobId(ingestJobId).setIngestUpdateJobIds(ingestUpdateJobIds)
      .setCreatedOn(createdOn).setCreatedBy(createdBy).setUpdatedOn(updatedOn).setUpdatedBy(updatedBy)
      .setAllUpdateJobIds(allIngestJobIds);
    ret.setDisposalScheduleId(disposalScheduleId);
    ret.setDisposalScheduleName(disposalScheduleName);
    ret.setRetentionPeriodDuration(retentionPeriodDuration);
    ret.setRetentionPeriodInterval(retentionPeriodInterval);
    ret.setRetentionPeriodDetails(retentionPeriodDetails);
    ret.setRetentionPeriodStartDate(retentionPeriodStartDate);
    ret.setRetentionPeriodState(retentionPeriodCalculation);
    ret.setDisposalHoldsId(disposalHoldsId);
    ret.setTransitiveDisposalHoldsId(transitiveDisposalHoldsId);
    ret.setDestroyedOn(destroyedOn);
    ret.setDestroyedBy(destroyedBy);
    ret.setDisposalAction(disposalAction);
    ret.setOverdueDate(overdueDate);
    ret.setOnHold(disposalHoldStatus);
    ret.setDisposalConfirmationId(disposalConfirmationId);
    ret.setDisposalScheduleAssociationType(aipDisposalScheduleAssociationType);

    return ret;
  }

}
