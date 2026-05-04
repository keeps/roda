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
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedules;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.DisposalScheduleService;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.services.DisposalScheduleRestService;
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
@RequestMapping(path = "/api/v2/disposal/schedules")
public class DisposalScheduleController implements DisposalScheduleRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

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
        DisposalSchedule disposalSchedule = disposalScheduleService
          .retrieveDisposalSchedule(requestContext.getModelService(), id);

        CountRequest countRequest = new CountRequest();
        SimpleFilterParameter filterParameter = new SimpleFilterParameter(RodaConstants.DISPOSAL_RULE_SCHEDULE_ID, id);
        Filter filter = new Filter();
        filter.add(filterParameter);

        countRequest.setFilter(filter);
        Long count = indexService.count(DisposalRule.class, countRequest);
        if (count > 0) {
          disposalSchedule.setUsedInDisposalRule(true);
        }

        return disposalSchedule;
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

  @Override
  public DisposalSchedule findByUuid(String uuid, String localeString) {
    return indexService.retrieve(DisposalSchedule.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<DisposalSchedule> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(DisposalSchedule.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_FILE)) {
      return new LongResponse(indexService.count(DisposalSchedule.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(@RequestBody SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, DisposalSchedule.class);
  }
}
