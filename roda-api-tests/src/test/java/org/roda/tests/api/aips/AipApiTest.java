/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.tests.api.aips;

import static io.restassured.RestAssured.given;

import org.roda.tests.api.AbstractApiTest;
import org.roda.tests.api.model.IndexResult;
import org.roda.tests.api.model.LongResponse;
import org.roda.tests.api.model.User;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;

/**
 * E2E tests for the {@code /api/v2/aips} resource.
 *
 * <p>All tests run against a live RODA instance managed by Testcontainers.
 * The Docker Compose stack is started once per suite in {@link AbstractApiTest}.
 *
 * <p>A freshly started RODA instance has no AIPs, so count/find assertions
 * verify structure (non-null, non-negative) rather than exact counts.
 *
 * <p>Filter JSON is built as raw strings because the generated OpenAPI model
 * uses {@code "@type"} as the Jackson discriminator while RODA expects {@code "type"}.
 */
@Test(groups = {"e2e", "aips"})
public class AipApiTest extends AbstractApiTest {

  private static final String ALL_FILTER_BODY =
    "{\"filter\":{\"parameters\":[{\"type\":\"AllFilterParameter\"}]}}";

  /**
   * Sanity check: the authenticated admin user is returned correctly.
   */
  @Test
  public void getAuthenticatedUser_returnsAdminUser() {
    User user = given()
      .when()
      .get("/members/users/authenticated")
      .then()
      .statusCode(200)
      .extract().as(User.class);

    Assert.assertNotNull(user, "Authenticated user must not be null");
    Assert.assertEquals(user.getName(), ADMIN_USER,
      "Expected the admin user to be authenticated");
  }

  /**
   * {@code POST /aips/count} with an AllFilterParameter must return HTTP 200
   * and a non-negative count.
   */
  @Test
  public void countAips_withAllFilter_returns200AndNonNegativeCount() {
    LongResponse response = given()
      .contentType(ContentType.JSON)
      .body(ALL_FILTER_BODY)
      .when()
      .post("/aips/count")
      .then()
      .statusCode(200)
      .extract().as(LongResponse.class);

    Assert.assertNotNull(response, "Count response must not be null");
    Assert.assertTrue(response.getResult() >= 0,
      "AIP count must be non-negative, got: " + response.getResult());
  }

  /**
   * {@code POST /aips/find} with an AllFilterParameter and limit=10 must return
   * HTTP 200 and a structurally valid {@link IndexResult}.
   */
  @Test
  public void findAips_withAllFilter_returns200AndValidIndexResult() {
    String body =
      "{\"filter\":{\"parameters\":[{\"type\":\"AllFilterParameter\"}]}," +
      "\"sublist\":{\"firstElementIndex\":0,\"maximumElementCount\":10}}";

    IndexResult result = given()
      .contentType(ContentType.JSON)
      .body(body)
      .when()
      .post("/aips/find")
      .then()
      .statusCode(200)
      .extract().as(IndexResult.class);

    Assert.assertNotNull(result, "IndexResult must not be null");
    Assert.assertTrue(result.getTotalCount() >= 0,
      "Total count must be non-negative, got: " + result.getTotalCount());
    // RODA returns results:null (not []) when totalCount == 0
    if (result.getTotalCount() > 0) {
      Assert.assertNotNull(result.getResults(), "IndexResult.results must not be null when totalCount > 0");
    }
  }

  /**
   * {@code GET /aips/configuration/types} must return HTTP 200.
   */
  @Test
  public void getAipConfigurationTypes_returns200() {
    given()
      .when()
      .get("/aips/configuration/types")
      .then()
      .statusCode(200);
  }
}
