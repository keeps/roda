package org.roda.wui.api.v2.controller;

import com.google.json.JsonSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalScheduleService;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/disposal/schedules")
public class DisposalScheduleController implements DisposalScheduleRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  DisposalScheduleService disposalScheduleService;

  @Override
  public DisposalSchedules listDisposalSchedules() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalScheduleService.getDisposalSchedules();
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException | IOException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalScheduleService.retrieveDisposalSchedule(id);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state, RodaConstants.DISPOSAL_SCHEDULE_ID, id);
    }
  }

  @Override
  public DisposalSchedule createDisposalSchedule(@RequestBody DisposalSchedule schedule) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
      schedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

      // validate disposal schedule
      disposalScheduleService.validateDisposalSchedule(schedule);

      return disposalScheduleService.createDisposalSchedule(schedule, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state,
        RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM, schedule);
    }
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(@RequestBody DisposalSchedule schedule) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
      schedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

      // validate disposal schedule
      disposalScheduleService.validateDisposalScheduleWhenUpdating(schedule);

      // delegate action to service
      return disposalScheduleService.updateDisposalSchedule(schedule, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, schedule.getId(), state,
        RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM, schedule);
    }
  }

  @Override
  public Void deleteDisposalSchedule(String id) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate action to service
      disposalScheduleService.deleteDisposalSchedule(id);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state,
        RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM, id);
    }
    return null;
  }

  @Override
  public Job associatedDisposalSchedule(@RequestBody SelectedItems<IndexedAIP> selectedItems,
    String disposalScheduleId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalScheduleService.associateDisposalSchedule(requestContext.getUser(), selectedItems,
        disposalScheduleId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selectedItems, RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM, disposalScheduleId);
    }
  }

  @Override
  public Job disassociatedDisposalSchedule(SelectedItems<IndexedAIP> selectedItems) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalScheduleService.disassociateDisposalSchedule(requestContext.getUser(), selectedItems);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        selectedItems);
    }
  }
}
