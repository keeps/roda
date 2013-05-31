package pt.gov.dgarq.roda.services.client;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import pt.gov.dgarq.roda.core.PlanClient;

@Ignore
public class UploadPlanTest {
  static final private org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(UploadPlanTest.class);

  private static PlanAPIMock planMock = null;

  @BeforeClass
  static public void setup() {
    planMock = new PlanAPIMock(9090);
    new Thread(planMock).start();
    try {
      synchronized (planMock) {
        LOG.info("Waiting for server to start and notify");
        planMock.wait();
        LOG.info("Server notified. Continuing...");
      }
    } catch (InterruptedException e) {
      LOG.error("Error waiting for plan mock to start " + e.getMessage(), e);
    }
  }

  @AfterClass
  static public void cleanup() throws IOException {
    if (planMock != null) {
      planMock.stop();
    }
  }

  @Test
  public void testUploadPlan() {
    try {

      PlanClient planClient = new PlanClient(new URL("http://localhost:9090/"), "admin", "admin");

      URL planURL = getClass().getResource("/plan.xml");
      LOG.debug("plan.xml resource URL: " + planURL);

      File planFile = new File(planURL.toURI());

      planClient.uploadPlan(planFile);

    } catch (MalformedURLException e) {
      fail("MalformedURLException - " + e.getMessage());
    } catch (URISyntaxException e) {
      fail("URISyntaxException - " + e.getMessage());
    }

  }

}
