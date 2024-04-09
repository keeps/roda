package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.JobReportRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/job-report")
@Tag(name = JobReportController.SWAGGER_ENDPOINT)
public class JobReportController implements JobReportRestService {
  public static final String SWAGGER_ENDPOINT = "v2 job reports";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedReport findByUuid(String uuid) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser(), TransferredResource.class);

      // delegate
      final IndexedReport ret = indexService.retrieve(IndexedReport.class, uuid, new ArrayList<>());

      // checking object permissions
      controllerAssistant.checkObjectPermissions(requestContext.getUser(), ret, IndexedReport.class);

      return ret;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), uuid, state, RodaConstants.CONTROLLER_CLASS_PARAM,
        IndexedReport.class.getSimpleName(), RodaConstants.CONTROLLER_ID_PARAM, uuid);
    }
  }
}