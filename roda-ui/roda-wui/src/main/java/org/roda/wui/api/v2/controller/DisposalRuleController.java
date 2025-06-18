package org.roda.wui.api.v2.controller;

import java.io.IOException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalRuleService;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
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
  DisposalRuleService disposalRuleService;

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
        throws RODAException, RESTException, IOException {
        // check user permissions
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.DISPOSAL_RULE_ID, id);

        return disposalRuleService.retrieveDisposalHold(id, requestContext.getModelService());
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
}
