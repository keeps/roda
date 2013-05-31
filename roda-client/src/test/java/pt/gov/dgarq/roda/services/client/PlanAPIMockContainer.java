package pt.gov.dgarq.roda.services.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 * Plan API mock container.
 * 
 * @author Rui Castro
 */
public class PlanAPIMockContainer implements Container {
  private static final Logger LOG = Logger.getLogger(PlanAPIMockContainer.class);

  public PlanAPIMockContainer() {
  }

  @Override
  public void handle(Request request, Response response) {

    LOG.info("-- HTTP/1.1 " + request.getMethod() + " " + request.getPath() + " from "
      + request.getClientAddress().getAddress().getHostAddress());

    if (request.getMethod().equals("POST")) {
      handlePost(request, response);
    } else if (request.getMethod().equals("DELETE")) {
      // nothing
      LOG.warn("DELETE method not implemented - " + request.getMethod() + " " + request.getPath());
    } else if (request.getMethod().equals("PUT")) {
      // nothing
      LOG.warn("PUT method not implemented - " + request.getMethod() + " " + request.getPath());
    } else if (request.getMethod().equals("GET")) {
      // nothing
      LOG.warn("GET method not implemented - " + request.getMethod() + " " + request.getPath());
    } else {
      LOG.error("Unable to handle method of type " + request.getMethod() + " " + request.getPath());
    }

    try {
      response.close();
    } catch (IOException e) {
      LOG.warn("Error closing response - " + e.getMessage(), e);
    }
  }

  private void handlePost(Request request, Response response) {

    String contextPath = request.getPath().getPath();

    try {

      if (contextPath.startsWith("/rest/plan")) {
        handleUploadPlan(request, response);
        response.setCode(200);
      } else {
        response.setCode(404);
      }

    } catch (IOException e) {
      response.setCode(500);
      LOG.error("Error handling upload - " + e.getMessage(), e);
    }
  }

  private void handleUploadPlan(Request request, Response response) throws IOException {

    InputStream isPlan = request.getInputStream();
    String plan = IOUtils.toString(isPlan);

    LOG.debug("Plan content is " + plan);
  }
}
