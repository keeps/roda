package org.roda.wui.api.v2;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.controllers.BrowserHelper;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.client.services.JobReportService;
import org.roda.wui.client.services.TransferredResourceService;
import org.roda.wui.common.ControllerAssistant;

import java.util.ArrayList;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path(JobReportResource.ENDPOINT)
@Tag(name = JobReportResource.SWAGGER_ENDPOINT)
public class JobReportResource implements JobReportService {
  public static final String ENDPOINT = "/job-report";
  public static final String SWAGGER_ENDPOINT = "v2 job report";
  @Context
  private HttpServletRequest request;

  @Override
  public IndexedReport findByUuid(String uuid) {
    final User user = UserUtility.getApiUser(request);
    try {
      final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

      // check user permissions
      controllerAssistant.checkRoles(user, org.roda.core.data.v2.ip.TransferredResource.class);

      LogEntryState state = LogEntryState.SUCCESS;

      try {
        // delegate
        final IndexedReport ret = BrowserHelper.retrieve(IndexedReport.class, uuid, new ArrayList<>());

        // checking object permissions
        controllerAssistant.checkObjectPermissions(user, ret, IndexedReport.class);

        return ret;
      } catch (RODAException e) {
        state = LogEntryState.FAILURE;
        throw new RESTException(e);
      } finally {
        // register action
        controllerAssistant.registerAction(user, uuid, state, RodaConstants.CONTROLLER_CLASS_PARAM,
          IndexedReport.class.getSimpleName());
      }
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }
}