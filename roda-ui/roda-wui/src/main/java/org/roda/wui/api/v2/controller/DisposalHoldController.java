package org.roda.wui.api.v2.controller;

import java.io.IOException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.ip.disposalhold.UpdateDisposalHoldRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalHoldService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DisposalHoldRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
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

  @Autowired
  RequestHandler requestHandler;

  @Override
  public DisposalHolds listDisposalHolds() {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalHolds>() {
      @Override
      public DisposalHolds process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        return disposalHoldService.getDisposalHolds(requestContext.getModelService());
      }
    });
  }

  @Override
  public DisposalHold retrieveDisposalHold(String id) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalHold>() {
      @Override
      public DisposalHold process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.DISPOSAL_HOLD_ID, id);
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        return disposalHoldService.retrieveDisposalHold(id, requestContext.getModelService());
      }
    });
  }

  @Override
  public DisposalHold createDisposalHold(@RequestBody DisposalHold hold) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalHold>() {
      @Override
      public DisposalHold process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM, hold);
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        // sanitize the input
        String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(hold));
        DisposalHold sanitizedHold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

        // validate disposal hold
        disposalHoldService.validateDisposalHold(sanitizedHold);

        return disposalHoldService.createDisposalHold(sanitizedHold, requestContext);
      }
    });
  }

  @Override
  public DisposalHold updateDisposalHold(@RequestBody UpdateDisposalHoldRequest updateDisposalHoldRequest) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalHold>() {
      @Override
      public DisposalHold process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(updateDisposalHoldRequest.getDisposalHold().getId());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_HOLD_PARAM,
          updateDisposalHoldRequest.getDisposalHold());
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        // sanitize the input
        String sanitize = JsonSanitizer
          .sanitize(JsonUtils.getJsonFromObject(updateDisposalHoldRequest.getDisposalHold()));
        DisposalHold sanitizedHold = JsonUtils.getObjectFromJson(sanitize, DisposalHold.class);

        // delegate action to service
        return disposalHoldService.updateDisposalHold(sanitizedHold, updateDisposalHoldRequest.getDetails(),
          requestContext);
      }
    });
  }

  @Override
  public Job applyDisposalHold(@RequestBody SelectedItemsRequest items, String disposalHoldId, boolean override) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(disposalHoldId);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, items,
          RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM, disposalHoldId,
          RodaConstants.CONTROLLER_DISPOSAL_HOLD_OVERRIDE_PARAM, override);
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        // delegate
        return disposalHoldService.applyDisposalHold(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(items, IndexedAIP.class), disposalHoldId, override);
      }
    });
  }

  @Override
  public Job liftDisposalHold(String id, String details) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM, id);
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());

        // delegate
        return disposalHoldService.liftDisposalHold(requestContext.getUser(), id, details);
      }
    });
  }

  @Override
  public Job disassociateDisposalHold(@RequestBody DisassociateDisposalHoldRequest disassociateDisposalHoldRequest,
    String disposalHoldId) {

    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM,
          disassociateDisposalHoldRequest.getSelectedItems(), RodaConstants.CONTROLLER_DISPOSAL_HOLD_ID_PARAM,
          disposalHoldId, RodaConstants.CONTROLLER_DISPOSAL_HOLD_DISASSOCIATE_ALL,
          disassociateDisposalHoldRequest.getClear());
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());
        // delegate
        return disposalHoldService.disassociateDisposalHold(requestContext.getUser(), disassociateDisposalHoldRequest,
          disposalHoldId);
      }
    });
  }

  @Override
  public DisposalTransitiveHoldsAIPMetadata listTransitiveHolds(String aipId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalTransitiveHoldsAIPMetadata>() {
      @Override
      public DisposalTransitiveHoldsAIPMetadata process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException, IOException {
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());
        return disposalHoldService.listTransitiveDisposalHolds(aipId, requestContext.getModelService());
      }
    });
  }

  @Override
  public DisposalHoldsAIPMetadata listDisposalHoldsAssociation(String aipId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalHoldsAIPMetadata>() {
      @Override
      public DisposalHoldsAIPMetadata process(RequestContext requestContext,
        RequestControllerAssistant controllerAssistant) throws RODAException, RESTException, IOException {
        // check user permissions
        controllerAssistant.checkRoles(requestContext.getUser());
        return requestContext.getModelService().listDisposalHoldsAssociation(aipId);
      }
    });
  }
}
