/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.DisposalRuleNotValidException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.disposal.rule.ChangeOrderRequest;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.disposal.rules.ApplyDisposalRulesPlugin;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.model.RequestContext;
import org.springframework.stereotype.Service;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

@Service
public class DisposalRuleService {
  public DisposalRules listDisposalRules(ModelService model)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {
    return model.listDisposalRules();
  }

  public DisposalRule updateDisposalRule(DisposalRule disposalRule, RequestContext context)
    throws GenericException, AuthorizationDeniedException, NotFoundException, RequestNotValidException {
    return context.getModelService().updateDisposalRule(disposalRule, context.getUser().getName());
  }

  public DisposalRule createDisposalRule(DisposalRule disposalRule, RequestContext context) throws GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, RequestNotValidException {
    return context.getModelService().createDisposalRule(disposalRule, context.getUser().getName());
  }

  public DisposalRule retrieveDisposalHold(String id, ModelService model)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    return model.retrieveDisposalRule(id);
  }

  public void deleteDisposalRule(String id, RequestContext context)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException, IOException {
    context.getModelService().deleteDisposalRule(id, context.getUser().getName());
  }

  public Job applyDisposalRules(User user, boolean overrideManualAssociations)
    throws NotFoundException, AuthorizationDeniedException, GenericException, RequestNotValidException {
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL,
      Boolean.toString(overrideManualAssociations));

    return CommonServicesUtils.createAndExecuteInternalJob("Apply disposal rules to repository",
      new SelectedItemsFilter<>(new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())),
        IndexedAIP.class.getName(), true),
      ApplyDisposalRulesPlugin.class, user, pluginParameters, "Could not execute apply disposal rules to repository");
  }

  public void changeDisposalRuleOrder(RequestContext context, ChangeOrderRequest request)
    throws AuthorizationDeniedException, RequestNotValidException, IOException, GenericException, NotFoundException {
    ModelService modelService = context.getModelService();
    DisposalRules disposalRules = modelService.listDisposalRules();

    SelectedItems<DisposalRule> disposalRuleSelectedItems = CommonServicesUtils.convertSelectedItems(request.getItems(),
      DisposalRule.class);
    if (disposalRuleSelectedItems instanceof SelectedItemsList<DisposalRule> itemsList) {
      Set<String> selectedIds = new HashSet<>(itemsList.getIds());

      List<DisposalRule> selectedRules = new ArrayList<>();
      List<DisposalRule> remainingRules = new ArrayList<>();

      for (DisposalRule rule : disposalRules.getObjects()) {
        if (selectedIds.contains(rule.getId())) {
          selectedRules.add(rule);
        } else {
          remainingRules.add(rule);
        }
      }

      // 3. Calculate the 0-based insertion index
      int targetIndex = 0;

      switch (request.getPosition()) {
        case TOP:
          break;
        case BOTTOM:
          targetIndex = remainingRules.size();
          break;
        case POSITION:
          // Convert the 1-based UI position to a 0-based list index
          targetIndex = request.getNewOrder() - 1;

          // Safety bounds checks
          if (targetIndex < 0) {
            targetIndex = 0;
          } else if (targetIndex > remainingRules.size()) {
            targetIndex = remainingRules.size();
          }
          break;
      }

      // 4. Insert the selected block into the remaining list
      remainingRules.addAll(targetIndex, selectedRules);

      // 5. Reassign the "order" integer and collect the ones that actually changed
      List<DisposalRule> rulesToUpdate = new ArrayList<>();

      for (int i = 0; i < remainingRules.size(); i++) {
        DisposalRule rule = remainingRules.get(i);
        int newExpectedOrder = i; // 1-based ordering for the database

        // Only update if the order actually changed to save database/index calls
        if (rule.getOrder() == null || rule.getOrder() != newExpectedOrder) {
          rule.setOrder(newExpectedOrder);
          rulesToUpdate.add(rule);
        }
      }

      // 6. Save the updated rules to your IndexService / Database
      for (DisposalRule rule : rulesToUpdate) {
        context.getModelService().updateDisposalRule(rule, context.getUser().getName());
      }
    }
  }

  public void validateDisposalRule(DisposalRule disposalRule, ModelService model)
    throws GenericException, AuthorizationDeniedException, RequestNotValidException, IOException {
    DisposalSchedules disposalSchedules = model.listDisposalSchedules();

    if (StringUtils.isBlank(disposalRule.getTitle())) {
      throw new DisposalRuleNotValidException("The disposal rule title is mandatory");
    }

    if (!isConditionTypeValid(disposalRule.getType())) {
      throw new DisposalRuleNotValidException("The disposal rule condition type is not valid");
    }

    if (!isRuleScheduleValid(disposalRule, disposalSchedules)) {
      throw new DisposalRuleNotValidException("The disposal rule schedule is not valid");
    }
  }

  private boolean isConditionTypeValid(ConditionType type) {
    if (StringUtils.isNotBlank(type.toString())) {
      return ConditionType.IS_CHILD_OF.equals(type) || ConditionType.METADATA_FIELD.equals(type);
    }
    return false;
  }

  private boolean isRuleScheduleValid(DisposalRule rule, DisposalSchedules disposalSchedules) {
    for (DisposalSchedule schedule : disposalSchedules.getObjects()) {
      if (schedule.getId().equals(rule.getDisposalScheduleId())
        && DisposalScheduleState.ACTIVE.equals(schedule.getState())) {
        return true;
      }
    }
    return false;
  }
}
