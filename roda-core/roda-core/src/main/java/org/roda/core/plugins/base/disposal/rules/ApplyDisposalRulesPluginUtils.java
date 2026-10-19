/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ApplyDisposalRulesPluginUtils {

  private ApplyDisposalRulesPluginUtils() {
  }

  public static Optional<DisposalRule> applyRule(AIP aip, DisposalRules disposalRules, IndexService index,
    ModelService model) throws NotFoundException, GenericException {

    // index fields produced from the AIP's descriptive metadata by the ingest
    // crosswalks, computed from the model: the AIP may have been written by the
    // current transaction, and the index only holds committed data
    SolrInputDocument metadataFields = null;
    for (DisposalRule rule : disposalRules.getObjects()) {
      Optional<DisposalRule> used = Optional.empty();
      if (ConditionType.IS_CHILD_OF.equals(rule.getType())) {
        used = conditionTypeChildOf(aip, rule);
      } else if (ConditionType.METADATA_FIELD.equals(rule.getType())) {
        if (metadataFields == null) {
          metadataFields = descriptiveMetadataFields(aip, model);
        }
        used = conditionTypeMetadataValue(aip, rule, index, metadataFields);
      }

      if (used.isPresent()) {
        return used;
      }
    }

    return Optional.empty();
  }

  private static Optional<DisposalRule> conditionTypeChildOf(AIP aip, DisposalRule rule) {
    if (aip.getParentId() != null && aip.getParentId().equals(rule.getConditionKey())) {
      DisposalAIPMetadata disposal = getDisposalAipMetadata(aip, rule);
      aip.setDisposal(disposal);
      return Optional.of(rule);
    }

    return Optional.empty();
  }

  private static SolrInputDocument descriptiveMetadataFields(AIP aip, ModelService model) throws GenericException {
    SolrInputDocument doc = new SolrInputDocument();
    try {
      SolrUtils.indexDescriptiveMetadataFields(model, aip.getId(), null, aip.getDescriptiveMetadata(), doc);
    } catch (RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      throw new GenericException("Could not read the descriptive metadata of AIP " + aip.getId(), e);
    }
    return doc;
  }

  private static Optional<DisposalRule> conditionTypeMetadataValue(AIP aip, DisposalRule rule, IndexService index,
    SolrInputDocument metadataFields) throws NotFoundException, GenericException {

    Object o;
    if (metadataFields.containsKey(rule.getConditionKey())) {
      o = metadataFields.getFieldValue(rule.getConditionKey());
      if (metadataFields.getField(rule.getConditionKey()).getValueCount() > 1) {
        o = new ArrayList<>(metadataFields.getFieldValues(rule.getConditionKey()));
      }
    } else {
      // not produced by the descriptive metadata: fall back to the (committed) index
      try {
        IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aip.getId(),
          Collections.singletonList(rule.getConditionKey()));
        Map<String, Object> fields = indexedAIP.getFields();
        o = fields.get(rule.getConditionKey());
      } catch (NotFoundException e) {
        o = null;
      }
    }

    boolean match = false;

    if (o instanceof String s) {
      match = s.equals(rule.getConditionValue());
    } else if (o instanceof Collection<?> list && !list.isEmpty()) {
      match = list.contains(rule.getConditionValue());
    }

    if (match) {
      DisposalAIPMetadata disposal = getDisposalAipMetadata(aip, rule);
      aip.setDisposal(disposal);
      return Optional.of(rule);
    }

    return Optional.empty();
  }

  private static DisposalAIPMetadata getDisposalAipMetadata(AIP aip, DisposalRule rule) {
    DisposalAIPMetadata disposal = aip.getDisposal();
    if (disposal == null) {
      disposal = new DisposalAIPMetadata();
      disposal.setSchedule(new DisposalScheduleAIPMetadata());
    } else if (disposal.getSchedule() == null) {
      disposal.setSchedule(new DisposalScheduleAIPMetadata());
    }
    disposal.getSchedule().setId(rule.getDisposalScheduleId());
    disposal.getSchedule().setAssociationType(AIPDisposalScheduleAssociationType.RULES);

    return disposal;
  }
}
