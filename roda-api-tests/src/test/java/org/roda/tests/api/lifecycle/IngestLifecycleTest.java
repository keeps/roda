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
import java.util.List;
import java.util.Map;

import org.roda.tests.api.AbstractApiTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;

/**
 * End-to-end lifecycle tests: SIP ingest → browse → search → edit → events.
 *
 * <p>Tests are chained with {@code dependsOnMethods} so each step builds on
 * the previous one. Shared state is kept in instance fields.
 *
 * <p>Filter JSON uses {@code "type"} as the discriminator (not {@code "@type"})
 * because RODA's {@code FilterParameter} hierarchy declares
 * {@code property = "type"} in {@code @JsonTypeInfo}.
 *
 * <p>{@code sourceObjects} in job creation uses {@code "@type"} because
 * {@code SelectedItemsRequest} uses {@code property = "@type"}.
 */
@Test(groups = {"e2e", "lifecycle"})
public class IngestLifecycleTest extends AbstractApiTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(IngestLifecycleTest.class);

  private static final String EARK_SIP_PATH =
    "../roda-core/roda-core-tests/src/main/resources/corpora/SIPs/eark_sip.zip";

  private static final String INGEST_PLUGIN = "org.roda.core.plugins.base.ingest.EARKSIPToAIPPlugin";

  private String transferredResourceUuid;
  private String jobUuid;
  private String aipId;
  private String representationId;
  private String aipMetadataId;
  private String aipMetadataType;
  private String aipMetadataVersion;

  // -------------------------------------------------------------------------
  // Step 1 — upload the SIP
  // -------------------------------------------------------------------------

  @Test
  public void uploadSip_createsTransferredResource() {
    File sipFile = new File(EARK_SIP_PATH);
    Assert.assertTrue(sipFile.exists(), "SIP file must exist at: " + sipFile.getAbsolutePath());

    Map<?, ?> resource = given()
      .multiPart("resource", sipFile, "application/zip")
      .when()
      .post("/transfers/create/resource")
      .then()
      .statusCode(201)
      .extract().as(Map.class);

    Assert.assertNotNull(resource, "TransferredResource response must not be null");
    transferredResourceUuid = (String) resource.get("uuid");
    Assert.assertNotNull(transferredResourceUuid, "TransferredResource UUID must not be null");
    LOGGER.info("Uploaded SIP; TransferredResource UUID: {}", transferredResourceUuid);
  }

  // -------------------------------------------------------------------------
  // Step 2 — create the ingest job
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "uploadSip_createsTransferredResource")
  public void createIngestJob_returnsJobWithCreatedState() {
    String body = "{"
      + "\"name\":\"E2E Ingest Test\","
      + "\"plugin\":\"" + INGEST_PLUGIN + "\","
      + "\"priority\":\"MEDIUM\","
      + "\"parallelism\":\"LIMITED\","
      + "\"sourceObjects\":{"
      + "\"@type\":\"SelectedItemsListRequest\","
      + "\"ids\":[\"" + transferredResourceUuid + "\"]"
      + "},"
      + "\"sourceObjectsClass\":\"org.roda.core.data.v2.ip.TransferredResource\","
      + "\"pluginParameters\":{}"
      + "}";

    Map<?, ?> job = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/jobs")
      .then()
      .statusCode(201)
      .extract().as(Map.class);

    Assert.assertNotNull(job, "Job response must not be null");
    jobUuid = (String) job.get("id");
    Assert.assertNotNull(jobUuid, "Job ID must not be null");
    LOGGER.info("Created ingest job; id: {}", jobUuid);
  }

  // -------------------------------------------------------------------------
  // Step 3 — poll until the job reaches a terminal state
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "createIngestJob_returnsJobWithCreatedState")
  public void waitForJobCompletion_jobCompletesSuccessfully() {
    String state = pollUntilJobTerminal(jobUuid, 60, 10_000);
    Assert.assertEquals(state, "COMPLETED",
      "Ingest job must complete successfully, got state: " + state);
    LOGGER.info("Ingest job {} completed with state: {}", jobUuid, state);
  }

  private String pollUntilJobTerminal(String uuid, int maxAttempts, long sleepMs) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      String state = given()
        .when()
        .get("/jobs/find/" + uuid)
        .then()
        .statusCode(200)
        .extract().path("state");

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
    return "COMPLETED".equals(state)
      || "FAILED_DURING_CREATION".equals(state)
      || "FAILED_TO_COMPLETE".equals(state)
      || "STOPPED".equals(state)
      || "REJECTED".equals(state);
  }

  // -------------------------------------------------------------------------
  // Step 4 — find the ingested AIP
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "waitForJobCompletion_jobCompletesSuccessfully")
  public void findAips_afterIngest_returnsAtLeastOneAip() {
    String body = "{"
      + "\"filter\":{\"parameters\":[{\"type\":\"AllFilterParameter\"}]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "AIP find result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "At least one AIP must exist after ingest, got: " + totalCount);

    List<?> results = (List<?>) result.get("results");
    Assert.assertNotNull(results, "Results must not be null when totalCount > 0");
    Assert.assertFalse(results.isEmpty(), "Results list must not be empty");

    Map<?, ?> firstAip = (Map<?, ?>) results.get(0);
    aipId = (String) firstAip.get("id");
    Assert.assertNotNull(aipId, "AIP ID must not be null");
    LOGGER.info("Found AIP; id: {}", aipId);
  }

  // -------------------------------------------------------------------------
  // Step 5 — retrieve the AIP directly by UUID
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void getAipById_returnsValidAip() {
    Map<?, ?> aip = given()
      .when()
      .get("/aips/find/" + aipId)
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(aip, "AIP must not be null");
    Assert.assertEquals(aip.get("id"), aipId, "AIP id must match");
  }

  // -------------------------------------------------------------------------
  // Step 6 — list representations for the AIP
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void findRepresentations_forAip_returnsAtLeastOne() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"SimpleFilterParameter\",\"name\":\"aipId\",\"value\":\"" + aipId + "\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/representations/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "Representation find result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "Representation totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "At least one representation must exist for AIP " + aipId);

    List<?> results = (List<?>) result.get("results");
    Assert.assertNotNull(results, "Representation results must not be null when count > 0");
    Map<?, ?> firstRep = (Map<?, ?>) results.get(0);
    representationId = (String) firstRep.get("id");
    LOGGER.info("Found {} representation(s) for AIP {}, first id: {}", totalCount, aipId, representationId);
  }

  // -------------------------------------------------------------------------
  // Step 7 — list files for the AIP
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void findFiles_forAip_returnsAtLeastOne() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"SimpleFilterParameter\",\"name\":\"aipId\",\"value\":\"" + aipId + "\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/files/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "File find result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "File totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "At least one file must exist for AIP " + aipId);
    LOGGER.info("Found {} file(s) for AIP {}", totalCount, aipId);
  }

  // -------------------------------------------------------------------------
  // Step 8 — search with SimpleFilterParameter on title
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void searchAips_withSimpleFilter_onTitle_returnsResults() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"SimpleFilterParameter\",\"name\":\"title\",\"value\":\"Title\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "SimpleFilter result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "AIP with title 'Title' must exist, got count: " + totalCount);
    LOGGER.info("SimpleFilterParameter search found {} AIP(s) with title 'Title'", totalCount);
  }

  // -------------------------------------------------------------------------
  // Step 9 — search with OneOfManyFilterParameter
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void searchAips_withOneOfManyFilter_returns200AndValidStructure() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"OneOfManyFilterParameter\",\"name\":\"state\","
      + "\"values\":[\"ACTIVE\",\"UNDER_APPRAISAL\",\"DESTROYED\"]}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "OneOfManyFilter result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() >= 0, "totalCount must be non-negative");
    LOGGER.info("OneOfManyFilterParameter search returned {} AIP(s)", totalCount);
  }

  // -------------------------------------------------------------------------
  // Step 10 — search with DateRangeFilterParameter
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void searchAips_withDateRangeFilter_returns200AndValidStructure() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"DateRangeFilterParameter\",\"name\":\"createdOn\","
      + "\"fromValue\":\"2020-01-01T00:00:00Z\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "DateRangeFilter result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "AIPs must exist with open date range, got: " + totalCount);
    LOGGER.info("DateRangeFilterParameter search returned {} AIP(s)", totalCount);
  }

  // -------------------------------------------------------------------------
  // Step 11 — search with BasicSearchFilterParameter
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void searchAips_withBasicSearchFilter_returns200AndValidStructure() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"BasicSearchFilterParameter\",\"name\":\"search\",\"value\":\"Title\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "BasicSearchFilter result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() >= 0, "totalCount must be non-negative");
    LOGGER.info("BasicSearchFilterParameter returned {} AIP(s)", totalCount);
  }

  // -------------------------------------------------------------------------
  // Step 12 — get descriptive metadata info
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "findAips_afterIngest_returnsAtLeastOneAip")
  public void getDescriptiveMetadataInfo_forAip_returnsMetadataList() {
    Map<?, ?> info = given()
      .when()
      .get("/aips/" + aipId + "/metadata/descriptive/information")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(info, "Descriptive metadata info must not be null");
    List<?> metadataList = (List<?>) info.get("descriptiveMetadataInfoList");
    Assert.assertNotNull(metadataList, "Descriptive metadata list must not be null");
    Assert.assertFalse(metadataList.isEmpty(), "At least one descriptive metadata must exist");

    Map<?, ?> firstMeta = (Map<?, ?>) metadataList.get(0);
    aipMetadataId = (String) firstMeta.get("id");
    aipMetadataType = (String) firstMeta.get("metadataType");
    aipMetadataVersion = firstMeta.get("metadataVersion") != null
      ? (String) firstMeta.get("metadataVersion") : "";
    Assert.assertNotNull(aipMetadataId, "Descriptive metadata id must not be null");
    LOGGER.info("AIP {} has {} descriptive metadata file(s); first id={}, type={}", aipId,
      metadataList.size(), aipMetadataId, aipMetadataType);
  }

  // -------------------------------------------------------------------------
  // Step 13 — update descriptive metadata
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "getDescriptiveMetadataInfo_forAip_returnsMetadataList")
  public void updateDescriptiveMetadata_forAip_returns200() {
    String updatedXml = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>"
      + "<simpledc xmlns:dc=\\\"http://purl.org/dc/elements/1.1/\\\" "
      + "xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\">"
      + "<title>Updated Title</title>"
      + "<description>Updated via E2E test</description>"
      + "</simpledc>";

    String body = "{"
      + "\"@type\":\"DescriptiveMetadataRequestXML\","
      + "\"id\":\"" + aipMetadataId + "\","
      + "\"filename\":\"" + aipMetadataId + "\","
      + "\"type\":\"" + (aipMetadataType != null ? aipMetadataType : "simpledc") + "\","
      + "\"version\":\"" + aipMetadataVersion + "\","
      + "\"xml\":\"" + updatedXml + "\""
      + "}";

    given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .put("/aips/" + aipId + "/metadata/descriptive")
      .then()
      .statusCode(200);

    LOGGER.info("Updated descriptive metadata for AIP {}", aipId);
  }

  // -------------------------------------------------------------------------
  // Step 14 — find preservation events
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = "waitForJobCompletion_jobCompletesSuccessfully")
  public void findPreservationEvents_afterIngest_returnsAtLeastOne() {
    String body = "{"
      + "\"filter\":{\"parameters\":[{\"type\":\"AllFilterParameter\"}]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/preservation/events/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "Preservation events result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "Preservation events totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "At least one preservation event must exist after ingest, got: " + totalCount);
    LOGGER.info("Found {} preservation event(s) after ingest", totalCount);
  }

  // -------------------------------------------------------------------------
  // Step 15 — find preservation events filtered by AIP
  // -------------------------------------------------------------------------

  @Test(dependsOnMethods = {"findPreservationEvents_afterIngest_returnsAtLeastOne",
    "findAips_afterIngest_returnsAtLeastOneAip"})
  public void findPreservationEvents_forAip_returnsAipEvents() {
    String body = "{"
      + "\"filter\":{\"parameters\":["
      + "{\"type\":\"SimpleFilterParameter\",\"name\":\"aipID\",\"value\":\"" + aipId + "\"}"
      + "]},"
      + "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}"
      + "}";

    Map<?, ?> result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/preservation/events/find")
      .then()
      .statusCode(200)
      .extract().as(Map.class);

    Assert.assertNotNull(result, "AIP preservation events result must not be null");
    Number totalCount = (Number) result.get("totalCount");
    Assert.assertNotNull(totalCount, "totalCount must not be null");
    Assert.assertTrue(totalCount.longValue() > 0,
      "At least one preservation event must exist for AIP " + aipId);
    LOGGER.info("Found {} preservation event(s) for AIP {}", totalCount, aipId);
  }
}
