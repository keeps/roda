/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.rule.ChangeOrderRequest;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalRuleService;
import org.roda.wui.api.v2.services.DisposalScheduleService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.json.JsonSanitizer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/disposal/rules")
public class DisposalRuleController implements DisposalRuleRestService {
  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Autowired
  DisposalRuleService disposalRuleService;

  @Autowired
  DisposalScheduleService disposalScheduleService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public DisposalRules listDisposalRules() {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalRules>() {
      @Override
      public DisposalRules process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        return disposalRuleService.listDisposalRules(requestContext.getModelService());
      }
    });
  }

  @Override
  public DisposalRule retrieveDisposalRule(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalRule>() {
      @Override
      public DisposalRule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        // check user permissions
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.DISPOSAL_RULE_ID, id);

        DisposalRule disposalRule = disposalRuleService.retrieveDisposalHold(id, requestContext.getModelService());
        DisposalSchedule disposalSchedule = disposalScheduleService
          .retrieveDisposalSchedule(requestContext.getModelService(), disposalRule.getDisposalScheduleId());
        disposalRule.setDisposalScheduleName(disposalSchedule.getTitle());

        return disposalRule;
      }
    });
  }

  @Override
  public DisposalRule createDisposalRule(@RequestBody DisposalRule disposalRule) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalRule>() {
      @Override
      public DisposalRule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_RULE_PARAM, disposalRule);

        // sanitize the input
        String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(disposalRule));
        DisposalRule sanitizedDisposalRule = JsonUtils.getObjectFromJson(sanitize, DisposalRule.class);

        // validate disposal rule
        disposalRuleService.validateDisposalRule(sanitizedDisposalRule, requestContext.getModelService());

        return disposalRuleService.createDisposalRule(sanitizedDisposalRule, requestContext);
      }
    });
  }

  @Override
  public DisposalRule updateDisposalRule(@RequestBody DisposalRule disposalRule) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalRule>() {
      @Override
      public DisposalRule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(disposalRule.getId());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_RULE_PARAM, disposalRule);

        // sanitize the input
        String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(disposalRule));
        DisposalRule sanitizedDisposalRule = JsonUtils.getObjectFromJson(sanitize, DisposalRule.class);

        // validate disposal rule
        disposalRuleService.validateDisposalRule(sanitizedDisposalRule, requestContext.getModelService());

        // delegate action to service
        return disposalRuleService.updateDisposalRule(sanitizedDisposalRule, requestContext);
      }
    });
  }

  @Override
  public Void deleteDisposalRule(String id) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Void>() {
      @Override
      public Void process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_RULE_ID_PARAM, id);

        // delegate
        disposalRuleService.deleteDisposalRule(id, requestContext);
        return null;
      }
    });
  }

  @Override
  public Job applyDisposalRules(boolean overrideManualAssociations) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_RULE_OVERRIDE_MANUAL_PARAM,
          Boolean.toString(overrideManualAssociations));

        // delegate action to service
        return disposalRuleService.applyDisposalRules(requestContext.getUser(), overrideManualAssociations);
      }
    });
  }

  @Override
  public Void changeDisposalRuleOrder(@RequestBody ChangeOrderRequest request) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Void>() {
      @Override
      public Void process(RequestContext requestContext, RequestControllerAssistant controllerAssistant) throws RODAException, RESTException, IOException {

        // delegate action to service
        disposalRuleService.changeDisposalRuleOrder(requestContext, request);
        return null;
      }
    });
  }

  @Override
  public DisposalRule findByUuid(String uuid, String localeString) {
    return indexService.retrieve(DisposalRule.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<DisposalRule> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(DisposalRule.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_FILE)) {
      return new LongResponse(indexService.count(DisposalRule.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, DisposalRule.class);
  }
}
