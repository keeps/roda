package pt.gov.dgarq.roda.core.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.jersey.multipart.FormDataParam;

@Path("plan")
public class PlanResource {
  static final private Logger logger = Logger.getLogger(PlanResource.class);

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadPlan(@FormDataParam("plan") InputStream planFileStream) {

    logger.trace("uploadPlan(...)");

    File rodaHome = null;
    if (System.getProperty("roda.home") != null) {
      rodaHome = new File(System.getProperty("roda.home"));
    } else {
      logger.error("roda.home is not defined");
      return Response.status(Status.PRECONDITION_FAILED).entity("roda.home is not defined").build();
    }

    try {

      File planFile = new File(rodaHome, "data/plan.xml");

      logger.debug("Writing plan to " + planFile);

      FileOutputStream fosPlan = new FileOutputStream(planFile);
      long bytesCopied = IOUtils.copyLarge(planFileStream, fosPlan);
      fosPlan.close();

      logger.debug("Wrote " + bytesCopied + " bytes to " + planFile);
      logger.info("New plan uploaded");

      return Response.ok().build();

    } catch (IOException e) {
      logger.error("Error writting plan file - " + e.getMessage(), e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error writting plan file - " + e.getMessage())
        .build();
    }

  }
}
