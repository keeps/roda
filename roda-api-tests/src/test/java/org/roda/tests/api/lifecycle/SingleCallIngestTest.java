/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.tests.api.lifecycle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import org.roda.tests.api.AbstractApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * E2E tests for the single-call ingest endpoint
 * ({@code POST /transfers/create/resource/ingest}), which uploads a SIP and
 * creates its ingest job in one request.
 *
 * <p>
 * No webhook destination is allowed in the E2E configuration
 * ({@code core.notification.webhook.allowed_destinations} is not set), so every
 * webhook must be rejected. Rejected requests must not leave the uploaded file
 * in the transfer area.
 */
@Test(groups = {"e2e", "lifecycle"})
public class SingleCallIngestTest extends AbstractApiTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SingleCallIngestTest.class);

  private static final String EARK_SIP_PATH = "../roda-core/roda-core-tests/src/main/resources/corpora/SIPs/e-ark-sip-2.1.0-with-custom-representation-type.zip";
  private static final String INGEST_PATH = "/transfers/create/resource/ingest";
  private static final String INGEST_PARAMETERS = "{\"parameter.sip_to_aip_class\":\"org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin\",\"parameter.do_auto_accept\":\"true\"}";

  private String jobUuid;

  private static byte[] sipContent() {
    File sipFile = new File(EARK_SIP_PATH);
    Assert.assertTrue(sipFile.exists(), "SIP file must exist at: " + sipFile.getAbsolutePath());
    try {
      return Files.readAllBytes(sipFile.toPath());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String uniqueFileName() {
    return "single-call-ingest-" + UUID.randomUUID() + ".zip";
  }

  private static RequestSpecification upload(String fileName) {
    return given().multiPart("resource", fileName, sipContent(), "application/zip");
  }

  private static long countTransferredResourcesNamed(String fileName) {
    String body = "{\"filter\":{\"parameters\":[{\"type\":\"SimpleFilterParameter\",\"name\":\"name\",\"value\":\""
      + fileName + "\"}]},\"onlyActive\":false}";
    Number count = given().contentType(ContentType.JSON).body(body).when().post("/transfers/count").then()
      .statusCode(200).extract().path("result");
    return count.longValue();
  }

  private static void assertRejectedAndNotStored(RequestSpecification request, String fileName) {
    request.when().post(INGEST_PATH).then().statusCode(400);
    Assert.assertEquals(countTransferredResourcesNamed(fileName), 0,
      "A rejected request must not leave " + fileName + " in the transfer area");
  }

  // -------------------------------------------------------------------------
  // Successful ingest
  // -------------------------------------------------------------------------

  @Test
  public void ingest_uploadsSipAndCreatesIngestJob() {
    String fileName = uniqueFileName();

    Map<?, ?> job = upload(fileName).multiPart("parameters", INGEST_PARAMETERS)
      .multiPart("name", "E2E Single Call Ingest").when().post(INGEST_PATH).then().statusCode(201).extract()
      .as(Map.class);

    jobUuid = (String) job.get("id");
    Assert.assertNotNull(jobUuid, "Job ID must not be null");
    Assert.assertEquals(job.get("name"), "E2E Single Call Ingest");
    Assert.assertEquals(job.get("plugin"), "org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin");
    Assert.assertEquals(countTransferredResourcesNamed(fileName), 1, "The SIP must be stored in the transfer area");
    LOGGER.info("Created single-call ingest job; id: {}", jobUuid);
  }

  @Test(dependsOnMethods = "ingest_uploadsSipAndCreatesIngestJob")
  public void ingest_jobCompletesAndCreatesAip() {
    String state = pollUntilJobTerminal(jobUuid, 60, 10_000);
    Assert.assertEquals(state, "COMPLETED", "Ingest job must complete successfully, got state: " + state);

    String body = "{\"filter\":{\"parameters\":[{\"type\":\"SimpleFilterParameter\",\"name\":\"ingestJobId\",\"value\":\""
      + jobUuid + "\"}]},\"onlyActive\":true}";
    Number count = given().contentType(ContentType.JSON).body(body).when().post("/aips/count").then().statusCode(200)
      .extract().path("result");
    Assert.assertEquals(count.longValue(), 1, "The ingest job must create one active AIP");
  }

  @Test
  public void ingest_withDefaultsCreatesJobWithDefaultName() {
    Map<?, ?> job = upload(uniqueFileName()).multiPart("parameters", INGEST_PARAMETERS).when().post(INGEST_PATH)
      .then().statusCode(201).extract().as(Map.class);

    Assert.assertEquals(job.get("name"), "API Ingest workflow");
    Assert.assertEquals(job.get("plugin"), "org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin");
    pollUntilJobTerminal((String) job.get("id"), 60, 10_000);
  }

  // -------------------------------------------------------------------------
  // Rejected requests
  // -------------------------------------------------------------------------

  @DataProvider
  public Object[][] webhooks() {
    return new Object[][] {{"https://example.org/hook"}, {"http://127.0.0.1:8080/hook"},
      {"http://169.254.169.254/latest/meta-data"}, {"ftp://example.org/hook"}, {"not a url"}};
  }

  @Test(dataProvider = "webhooks")
  public void ingest_withNotAllowedWebhook_isRejected(String webhook) {
    String fileName = uniqueFileName();
    assertRejectedAndNotStored(upload(fileName).multiPart("webhook", webhook), fileName);
  }

  @DataProvider
  public Object[][] webhookParameters() {
    return new Object[][] {{"ingest.http_notification.endpoint"},
      {"parameter.notification.HttpGenericNotification.to"}};
  }

  @Test(dataProvider = "webhookParameters")
  public void ingest_withWebhookInParameters_isRejected(String key) {
    String fileName = uniqueFileName();
    String parameters = "{\"" + key + "\":\"http://127.0.0.1:8080/hook\"}";
    assertRejectedAndNotStored(upload(fileName).multiPart("parameters", parameters), fileName);
  }

  @DataProvider
  public Object[][] invalidParameters() {
    return new Object[][] {{"not json"}, {"[\"a\"]"}, {"{\"a\":{\"b\":\"c\"}}"}};
  }

  @Test(dataProvider = "invalidParameters")
  public void ingest_withInvalidParameters_isRejected(String parameters) {
    String fileName = uniqueFileName();
    assertRejectedAndNotStored(upload(fileName).multiPart("parameters", parameters), fileName);
  }

  @Test
  public void ingest_withNonIngestPlugin_isRejected() {
    String fileName = uniqueFileName();
    assertRejectedAndNotStored(
      upload(fileName).multiPart("plugin", "org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin"), fileName);
  }

  @Test
  public void ingest_withUnknownPlugin_isRejected() {
    String fileName = uniqueFileName();
    assertRejectedAndNotStored(upload(fileName).multiPart("plugin", "org.example.UnknownPlugin"), fileName);
  }

  @Test
  public void ingest_withWebhookAndUnsupportedPlugin_isRejected() {
    String fileName = uniqueFileName();
    assertRejectedAndNotStored(upload(fileName).multiPart("plugin", "org.example.UnknownPlugin")
      .multiPart("webhook", "https://example.org/hook"), fileName);
  }

  private String pollUntilJobTerminal(String uuid, int maxAttempts, long sleepMs) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      String state = given().when().get("/jobs/find/" + uuid).then().statusCode(200).extract().path("state");

      LOGGER.info("Job {} state (attempt {}): {}", uuid, attempt, state);

      if (isTerminalJobState(state)) {
        return state;
      }

      try {
        Thread.sleep(sleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return state;
      }
    }
    return given().when().get("/jobs/find/" + uuid).then().statusCode(200).extract().path("state");
  }

  private boolean isTerminalJobState(String state) {
    return "COMPLETED".equals(state) || "FAILED_DURING_CREATION".equals(state) || "FAILED_TO_COMPLETE".equals(state)
      || "STOPPED".equals(state) || "REJECTED".equals(state);
  }
}
