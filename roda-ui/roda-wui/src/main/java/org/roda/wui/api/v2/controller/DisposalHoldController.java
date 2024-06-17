package org.roda.wui.api.v2.controller;

import java.io.IOException;
import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalHoldService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DisposalHoldRestService;
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
@RequestMapping(path = "/api/v2/disposal/holds")
public class DisposalHoldController implements DisposalHoldRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  DisposalHoldService disposalHoldService;

  @Override
  public DisposalHolds listDisposalHolds() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalHoldService.getDisposalHolds();
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
  public DisposalHold updateDisposalHold(@RequestBody DisposalHold hold) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(hold));
      hold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

      // delegate action to service
      return disposalHoldService.updateDisposalHold(hold, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | IllegalOperationException | RequestNotValidException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, hold.getId(), state,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM, hold);
    }
  }

  @Override
  public DisposalHold createDisposalHold(@RequestBody DisposalHold hold) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // sanitize the input
      String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(hold));
      hold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

      // validate disposal hold
      disposalHoldService.validateDisposalHold(hold);

      return disposalHoldService.createDisposalHold(hold, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (AlreadyExistsException | RequestNotValidException | NotFoundException | GenericException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM, hold);
    }
  }

  @Override
  public DisposalHold retrieveDisposalHold(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return disposalHoldService.retrieveDisposalHold(id);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state, RodaConstants.DISPOSAL_HOLD_ID, id);
    }
  }

  @Override
  public Job applyDisposalHold(@RequestBody SelectedItemsRequest items, String disposalHoldId, boolean override) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      // delegate
      return disposalHoldService.applyDisposalHold(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(items, IndexedAIP.class), disposalHoldId, override);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, items,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM, disposalHoldId,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_OVERRIDE_PARAM, override);
    }
  }

  @Override
  public Job liftDisposalHoldBySelectedItems(@RequestBody SelectedItemsRequest items, String disposalHoldId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return disposalHoldService.liftDisposalHold(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(items, IndexedAIP.class), disposalHoldId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, items,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM, disposalHoldId);
    }
  }

  @Override
  public DisposalHold liftDisposalHold(String id) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      DisposalHold disposalHold = disposalHoldService.retrieveDisposalHold(id);

      disposalHold.setState(DisposalHoldState.LIFTED);
      disposalHold.setLiftedBy(requestContext.getUser().getName());
      disposalHold.setLiftedOn(new Date());
      // delegate
      return disposalHoldService.updateDisposalHold(disposalHold, requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException | IllegalOperationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, id, state, RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM,
        id);
    }
  }

  @Override
  public Job disassociateDisposalHold(@RequestBody SelectedItemsRequest items, String disposalHoldId, boolean clear) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return disposalHoldService.disassociateDisposalHold(requestContext.getUser(),
        CommonServicesUtils.convertSelectedItems(items, IndexedAIP.class), disposalHoldId, clear);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, items,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM, disposalHoldId,
        RodaConstants.CONTROLLER_DISPOSAL_HOLD_DISASSOCIATE_ALL, clear);
    }
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveHolds(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return disposalHoldService.listTransitiveDisposalHolds(aipId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }
  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().listDisposalHoldsAssociation(aipId);
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state);
    }
  }
}
