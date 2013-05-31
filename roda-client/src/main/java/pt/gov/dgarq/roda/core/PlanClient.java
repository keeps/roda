package pt.gov.dgarq.roda.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.multipart.FormDataMultiPart;

public class PlanClient {
  static final private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PlanClient.class);

  private static final String planResourcePath = "rest/plan";

  private URL rodacoreURL = null;

  private String username = null;
  private String password = null;

  private URL planResourceURL = null;

  /**
   * Constructs a new authenticated {@link PlanClient} for RODA file upload
   * service.
   * 
   * @param rodacoreURL
   *          the {@link URL} to the RODA Core application.
   * @param username
   *          the username to use in the connection to the service
   * @param password
   *          the password to use in the connection to the service
   * 
   * @throws MalformedURLException
   */
  public PlanClient(URL rodacoreURL, String username, String password) throws MalformedURLException {

    this.rodacoreURL = rodacoreURL;
    this.username = username;
    this.password = password;

    logger.debug("PlanClient(rodacoreURL=" + rodacoreURL + ", username=" + username + ", password=***)");

    if (rodacoreURL == null) {
      throw new MalformedURLException("rodacoreURL cannot be null");
    } else {
      if (!rodacoreURL.toString().endsWith("/")) {
        rodacoreURL = new URL(rodacoreURL.toString() + "/");
        logger.warn("rodacoreURL doesn't have a trailing '/'. Fixing it to " + rodacoreURL);
      }
    }

    this.planResourceURL = new URL(this.rodacoreURL, planResourcePath);

    logger.debug("planResourceURL=" + planResourceURL + ", username=" + username);
  }

  public void uploadPlan(File planFile) {

    logger.trace("uploadPlan(" + planFile + ")");

    Client c = Client.create();
    logger.debug("Activating follow redirects on HTTP client");
    c.setFollowRedirects(true);

    WebResource r = c.resource(planResourceURL.toString());

    String auth = new String(Base64.encode(username + ":" + password));

    FormDataMultiPart form = new FormDataMultiPart();
    form.field("plan", planFile, MediaType.MULTIPART_FORM_DATA_TYPE);

    r.header("Authorization", "Basic " + auth).type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.TEXT_PLAIN)
      .post(form);

    logger.info("POST " + planFile + " to " + planResourceURL.toString() + " successful");

    try {

      form.close();

    } catch (IOException e) {
      logger.warn("Error creating request - " + e.getMessage(), e);
    }

  }

}
