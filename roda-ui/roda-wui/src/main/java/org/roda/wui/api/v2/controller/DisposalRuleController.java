package org.roda.wui.api.v2.controller;

import java.io.IOException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalRuleService;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.json.JsonSanitizer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/disposal/rules")
public class DisposalRuleController implements DisposalRuleRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  DisposalRuleService disposalRuleService;

  @Override
  public DisposalRules listDisposalRules() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalRuleService.listDisposalRules();
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state);
    }
  }

  @Override
  public DisposalRule updateDisposalRule(@RequestBody DisposalRule disposalRule) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(disposalRule));
      disposalRule = JsonUtils.getObjectFromJson(sanitize, DisposalRule.class);

      // delegate action to service
      return disposalRuleService.updateDisposalRule(disposalRule, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), disposalRule.getId(), state,
        RodaConstants.CONTROLLER_DISPOSAL_RULE_PARAM, disposalRule);
    }
  }

  @Override
  public DisposalRule createDisposalRule(@RequestBody DisposalRule disposalRule) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(disposalRule));
      disposalRule = JsonUtils.getObjectFromJson(sanitize, DisposalRule.class);

      // validate disposal rule
      controllerAssistant.checkRoles(requestContext.getUser(), DisposalSchedule.class);
      disposalRuleService.validateDisposalRule(disposalRule);

      return disposalRuleService.createDisposalRule(disposalRule, requestContext.getUser());
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_DISPOSAL_RULE_PARAM,
        disposalRule);
    }
  }

  @Override
  public DisposalRule retrieveDisposalRule(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalRuleService.retrieveDisposalHold(id);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), id, state, RodaConstants.DISPOSAL_RULE_ID, id);
    }
  }

  @Override
  public Void deleteDisposalRule(String id) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      disposalRuleService.deleteDisposalRule(id, requestContext.getUser());
      return null;
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), id, state,
        RodaConstants.CONTROLLER_DISPOSAL_RULE_ID_PARAM, id);
    }
  }

  @Override
  public Job applyDisposalRules(boolean includeManually) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return disposalRuleService.applyDisposalRules(requestContext.getUser(), includeManually);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state,
        RodaConstants.CONTROLLER_DISPOSAL_RULE_OVERRIDE_MANUAL_PARAM, includeManually);
    }
  }
}
