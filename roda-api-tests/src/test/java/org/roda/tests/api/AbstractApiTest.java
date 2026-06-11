/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.tests.api;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.time.Duration;

import io.restassured.builder.RequestSpecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import io.restassured.RestAssured;

/**
 * Base class for all RODA API E2E tests.
 *
 * <p>Lifecycle: a single {@link ComposeContainer} is started once before the
 * entire TestNG suite and stopped after it. All concrete test classes inherit the
 * pre-configured {@link RestAssured} base URI/port/auth, so individual tests need
 * only declare {@code given()...when()...then()} chains.
 *
 * <p>Prerequisites:
 * <ul>
 *   <li>Docker with Compose V2 ({@code docker compose}) available on {@code PATH}</li>
 *   <li>{@code docker.io/keeps/roda:development} image available locally or pullable</li>
 * </ul>
 */
public abstract class AbstractApiTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractApiTest.class);

  protected static final String ADMIN_USER = "admin";
  protected static final String ADMIN_PASSWORD = "roda";

  private static final String RODA_SERVICE = "roda";
  private static final int RODA_PORT = 8080;

  /**
   * Path to the compose file relative to this Maven module's base directory.
   * Maven Surefire sets {@code user.dir} to {@code ${project.basedir}} at test time,
   * so the relative path resolves correctly regardless of where Maven is invoked from.
   */
  private static final File COMPOSE_FILE =
    new File("../deploys/standalone/docker-compose-e2e.yaml");

  // Declared non-final so construction is deferred to @BeforeSuite.
  // Initialising ComposeContainer in a static field triggers Testcontainers'
  // Docker-detection code at class-load time (during TestNG's discovery phase),
  // before any lifecycle method runs, which causes a cryptic "instantiation: null" error.
  private static ComposeContainer COMPOSE;

  /**
   * Builds, starts the Docker Compose stack, and configures REST Assured.
   *
   * <p>Two-phase readiness check:
   * <ol>
   *   <li>Testcontainers polls {@code /actuator/health} until Spring Boot reports
   *       {@code "status":"UP"} — this covers RODA startup including Solr and database.</li>
   *   <li>Poll {@code GET /members/users/authenticated} until RODA returns the admin user,
   *       confirming LDAP authentication is operational (LDAP readiness lags slightly behind
   *       the actuator health signal).</li>
   * </ol>
   *
   * <p>TestNG guarantees this runs exactly once before any test method in the suite,
   * regardless of how many test classes extend this base.
   */
  @BeforeSuite(alwaysRun = true)
  public void startEnvironment() {
    if (COMPOSE != null) {
      LOGGER.info("RODA Docker Compose stack already running; skipping re-initialization");
      return;
    }
    LOGGER.info("Starting RODA Docker Compose stack from {}", COMPOSE_FILE.getAbsolutePath());

    COMPOSE = new ComposeContainer(COMPOSE_FILE)
      .withExposedService(RODA_SERVICE, RODA_PORT)
      .waitingFor(
        RODA_SERVICE,
        Wait.forHttp("/actuator/health")
          .forStatusCode(200)
          .forResponsePredicate(body -> body.contains("\"status\":\"UP\""))
          .withStartupTimeout(Duration.ofMinutes(10)));

    COMPOSE.start();

    String host = COMPOSE.getServiceHost(RODA_SERVICE, RODA_PORT);
    int mappedPort = COMPOSE.getServicePort(RODA_SERVICE, RODA_PORT);

    LOGGER.info("RODA available at http://{}:{}/api/v2", host, mappedPort);

    RestAssured.baseURI = "http://" + host;
    RestAssured.port = mappedPort;
    RestAssured.basePath = "/api/v2";
    RestAssured.defaultParser = io.restassured.parsing.Parser.JSON;

    // Phase 2: /actuator/health reports UP before LDAP is fully operational.
    // Poll until the admin user is returned using basic auth, confirming LDAP auth works.
    LOGGER.info("Waiting for LDAP authentication to be ready...");
    pollUntil("LDAP auth", () -> {
      String name = given().auth().preemptive().basic(ADMIN_USER, ADMIN_PASSWORD)
        .when().get("/members/users/authenticated").then().extract().path("name");
      return ADMIN_USER.equals(name);
    });

    // Create an access key via basic auth, then use the returned key as bearer token
    // for all subsequent requests in the test suite.
    LOGGER.info("Creating access token for test suite...");
    long expirationTimeMs = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L); // 1 year from now
    String accessKey = given().auth().preemptive().basic(ADMIN_USER, ADMIN_PASSWORD)
      .contentType("application/json")
      .accept("application/json")
      .body("{\"name\":\"e2e-test\",\"expirationDate\":" + expirationTimeMs + "}")
      .when()
      .post("/members/users/" + ADMIN_USER + "/access-keys")
      .then()
      .statusCode(201)
      .extract()
      .path("key");

    LOGGER.info("Access token created; switching to bearer authentication for all tests");
    RestAssured.requestSpecification = new RequestSpecBuilder()
      .addHeader("Authorization", "Bearer " + accessKey)
      .build();

  }

  private void pollUntil(String label, java.util.function.BooleanSupplier condition) {
    long deadline = System.currentTimeMillis() + Duration.ofMinutes(5).toMillis();
    int attempt = 0;
    while (System.currentTimeMillis() < deadline) {
      attempt++;
      try {
        if (condition.getAsBoolean()) {
          LOGGER.info("{} ready after {} poll attempts", label, attempt);
          return;
        }
        LOGGER.info("Attempt {}: {} not ready yet", attempt, label);
      } catch (Exception e) {
        LOGGER.warn("Attempt {}: {} check failed: {}", attempt, label, e.getMessage());
      }
      try {
        Thread.sleep(10000);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        return;
      }
    }
    LOGGER.warn("{} readiness timed out after {} attempts — proceeding anyway", label, attempt);
  }

  /**
   * Stops the Docker Compose stack after all suite tests have finished.
   */
  @AfterSuite(alwaysRun = true)
  public void stopEnvironment() {
    if (COMPOSE != null) {
      LOGGER.info("Stopping RODA Docker Compose stack");
      COMPOSE.stop();
    }
  }
}
