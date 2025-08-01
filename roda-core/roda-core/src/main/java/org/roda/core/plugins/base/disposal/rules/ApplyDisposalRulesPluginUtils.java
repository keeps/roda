/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.disposal.rules;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.index.IndexService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ApplyDisposalRulesPluginUtils {

  private ApplyDisposalRulesPluginUtils() {
  }

  public static Optional<DisposalRule> applyRule(AIP aip, DisposalRules disposalRules, IndexService index)
    throws NotFoundException, GenericException {

    for (DisposalRule rule : disposalRules.getObjects()) {
      Optional<DisposalRule> used = Optional.empty();
      if (ConditionType.IS_CHILD_OF.equals(rule.getType())) {
        used = conditionTypeChildOf(aip, rule);
      } else if (ConditionType.METADATA_FIELD.equals(rule.getType())) {
        used = conditionTypeMetadataValue(aip, rule, index);
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

  private static Optional<DisposalRule> conditionTypeMetadataValue(AIP aip, DisposalRule rule, IndexService index)
    throws NotFoundException, GenericException {

    IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aip.getId(),
      Collections.singletonList(rule.getConditionKey()));

    Map<String, Object> fields = indexedAIP.getFields();
    Object o = fields.get(rule.getConditionKey());
    String metadataValue = (String) o;

    if (metadataValue != null && metadataValue.equals(rule.getConditionValue())) {
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
