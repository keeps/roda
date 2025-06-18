package org.roda.wui.api.v2.controller;

import java.io.IOException;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalScheduleService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DisposalScheduleRestService;
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
@RequestMapping(path = "/api/v2/disposal/schedules")
public class DisposalScheduleController implements DisposalScheduleRestService {

  @Autowired
  DisposalScheduleService disposalScheduleService;

  @Autowired
  RequestHandler requestHandler;

  @Override
  public DisposalSchedules listDisposalSchedules() {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<DisposalSchedules>() {
      @Override
      public DisposalSchedules process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {

        return disposalScheduleService.getDisposalSchedules(requestContext.getModelService());
      }
    });
  }

  @Override
  public DisposalSchedule retrieveDisposalSchedule(String id) {
    return requestHandler.processRequest((new RequestHandler.RequestProcessor<DisposalSchedule>() {
      @Override
      public DisposalSchedule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setRelatedObjectId(id);

        // delegate action to service
        return disposalScheduleService.retrieveDisposalSchedule(requestContext.getModelService(), id);
      }
    }));
  }

  @Override
  public DisposalSchedule createDisposalSchedule(@RequestBody DisposalSchedule schedule) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalSchedule>() {
      @Override
      public DisposalSchedule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM, schedule);

        // sanitize the input
        String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
        DisposalSchedule sanitizedSchedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

        // validate disposal schedule
        disposalScheduleService.validateDisposalSchedule(sanitizedSchedule);

        return disposalScheduleService.createDisposalSchedule(sanitizedSchedule, requestContext);
      }
    });
  }

  @Override
  public DisposalSchedule updateDisposalSchedule(@RequestBody DisposalSchedule schedule) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<DisposalSchedule>() {
      @Override
      public DisposalSchedule process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_PARAM, schedule);

        // sanitize the input
        String sanitize = JsonSanitizer.sanitize(JsonUtils.getJsonFromObject(schedule));
        DisposalSchedule sanitizedSchedule = JsonUtils.getObjectFromJson(sanitize, DisposalSchedule.class);

        // validate disposal schedule
        disposalScheduleService.validateDisposalScheduleWhenUpdating(sanitizedSchedule);

        // delegate action to service
        return disposalScheduleService.updateDisposalSchedule(sanitizedSchedule, requestContext);
      }
    });
  }

  @Override
  public Void deleteDisposalSchedule(String id) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Void>() {
      @Override
      public Void process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(id);
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM, id);

        // delegate action to service
        disposalScheduleService.deleteDisposalSchedule(id, requestContext.getModelService());

        return null;
      }
    });
  }

  @Override
  public Job associatedDisposalSchedule(@RequestBody SelectedItemsRequest selectedItems, String disposalScheduleId) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException, IOException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems,
          RodaConstants.CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM, disposalScheduleId);

        // delegate
        return disposalScheduleService.associateDisposalSchedule(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selectedItems, IndexedAIP.class), disposalScheduleId);
      }
    });
  }

  @Override
  public Job disassociatedDisposalSchedule(@RequestBody SelectedItemsRequest selectedItems) {
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<Job>() {
      @Override
      public Job process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_SELECTED_PARAM, selectedItems);

        // delegate
        return disposalScheduleService.disassociateDisposalSchedule(requestContext.getUser(),
          CommonServicesUtils.convertSelectedItems(selectedItems, IndexedAIP.class));
      }
    });
  }
}
