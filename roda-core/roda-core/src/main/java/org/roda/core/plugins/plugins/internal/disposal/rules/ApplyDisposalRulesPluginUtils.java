package org.roda.core.plugins.plugins.internal.disposal.rules;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.index.IndexService;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ApplyDisposalRulesPluginUtils {

  private ApplyDisposalRulesPluginUtils() {}

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
      aip.setDisposalScheduleId(rule.getDisposalScheduleId());
      aip.setScheduleAssociationType(AIPDisposalScheduleAssociationType.RULES);

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
      aip.setDisposalScheduleId(rule.getDisposalScheduleId());
      aip.setScheduleAssociationType(AIPDisposalScheduleAssociationType.RULES);
      return Optional.of(rule);
    }

    return Optional.empty();
  }
}
