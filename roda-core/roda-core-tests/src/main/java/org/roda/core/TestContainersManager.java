/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Singleton manager for test infrastructure containers (ZooKeeper, Solr,
 * PostgreSQL).
 * <p>
 * Containers are started once per JVM and stopped via a shutdown hook. System
 * properties are set so that {@link org.roda.core.config.ConfigurationManager}
 * and Spring Boot pick them up before any test or Spring context initialization
 * runs.
 *
 * @author RODA Community
 */
public class TestContainersManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersManager.class);

  private static volatile TestContainersManager INSTANCE;

  private final Network network;
  private final GenericContainer<?> zookeeper;
  private final GenericContainer<?> solr;
  private final GenericContainer<?> postgres;
  private final GenericContainer<?> mailpit;
  private final GenericContainer<?> siegfried;

  @SuppressWarnings("resource")
  private TestContainersManager() {
    LOGGER.info("Starting test infrastructure containers...");

    network = Network.newNetwork();

    // ZooKeeper — exposed so that the RODA CloudSolrClient can connect
    zookeeper = new GenericContainer<>(DockerImageName.parse("zookeeper:3.9.1-jre-17")).withNetwork(network)
      .withNetworkAliases("zookeeper").withExposedPorts(2181)
      .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
    zookeeper.start();
    LOGGER.info("ZooKeeper started at {}:{}", zookeeper.getHost(), zookeeper.getMappedPort(2181));

    // Solr — connects to ZooKeeper via the internal Docker network alias.
    // Solr registers itself in ZooKeeper using the result of
    // InetAddress.getLocalHost().getHostAddress(), which in Docker resolves to
    // the container's bridge-network IP. On Linux (CI and most developer
    // machines) this IP is directly reachable from the Docker host, so the
    // CloudSolrClient can connect without any additional port mapping.
    solr = new GenericContainer<>(DockerImageName.parse("solr:9")).withNetwork(network)
      .withEnv("ZK_HOST", "zookeeper:2181").withExposedPorts(8983).waitingFor(Wait.forHttp("/solr/admin/info/system")
        .forPort(8983).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(3)));
    solr.start();
    LOGGER.info("Solr started at {}:{}", solr.getHost(), solr.getMappedPort(8983));

    // PostgreSQL
    postgres = new GenericContainer<>(DockerImageName.parse("postgres:17")).withEnv("POSTGRES_USER", "admin")
      .withEnv("POSTGRES_PASSWORD", "roda").withEnv("POSTGRES_DB", "roda_core_db").withExposedPorts(5432)
      .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
    postgres.start();
    LOGGER.info("PostgreSQL started at {}:{}", postgres.getHost(), postgres.getMappedPort(5432));

    // Mailpit
    mailpit = new GenericContainer<>(DockerImageName.parse("axllent/mailpit:latest")).withExposedPorts(1025, 8025)
      .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
    mailpit.start();
    LOGGER.info("Mailpit started at {}:{}", mailpit.getHost(), mailpit.getMappedPort(1025));

    // Clamav
    GenericContainer<?> clamav = new GenericContainer<>(DockerImageName.parse("clamav/clamav:1.5.2"))
      .withExposedPorts(3310).withFileSystemBind("/tmp", "/tmp", BindMode.READ_WRITE)
      .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
    clamav.start();

    String configContent = """
        TCPSocket %d
        TCPAddr %s
        """.formatted(clamav.getMappedPort(3310), clamav.getHost());

    try {
      Path tempConfigFile = Paths.get("/tmp/clamd.conf");
      Files.writeString(tempConfigFile, configContent);
    } catch (IOException e) {
      stopAll();
      throw new RuntimeException("Could not write config file: " + configContent);
    }

    LOGGER.info("ClamAV started at {}:{}", clamav.getHost(), clamav.getMappedPort(3310));

    // Siegfried
    siegfried = new GenericContainer<>(DockerImageName.parse("keeps/siegfried:v1.11.0"))
      .withEnv("SIEGFRIED_HOST", "0.0.0.0").withEnv("SIEGFRIED_PORT", "5138").withExposedPorts(5138)
      .withFileSystemBind("/tmp", "/tmp", BindMode.READ_ONLY)
      .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));
    siegfried.start();
    LOGGER.info("Siegfried started at {}:{}", siegfried.getHost(), siegfried.getMappedPort(5138));

    configureSystemProperties();

    Runtime.getRuntime().addShutdownHook(new Thread(this::stopAll, "testcontainers-shutdown"));
  }

  public static TestContainersManager getInstance() {
    if (INSTANCE == null) {
      synchronized (TestContainersManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new TestContainersManager();
        }
      }
    }
    return INSTANCE;
  }

  private void configureSystemProperties() {
    String zkUrl = zookeeper.getHost() + ":" + zookeeper.getMappedPort(2181);
    System.setProperty("RODA_CORE_SOLR_TYPE", "CLOUD");
    System.setProperty("RODA_CORE_SOLR_CLOUD_URLS", zkUrl);
    LOGGER.info("Set RODA_CORE_SOLR_CLOUD_URLS={}", zkUrl);

    String pgUrl = "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/roda_core_db";
    System.setProperty("spring.datasource.url", pgUrl);
    System.setProperty("spring.datasource.username", "admin");
    System.setProperty("spring.datasource.password", "roda");
    LOGGER.info("Set spring.datasource.url={}", pgUrl);

    System.setProperty("RODA_CORE_EMAIL_HOST", mailpit.getHost());
    System.setProperty("RODA_CORE_EMAIL_PORT", mailpit.getMappedPort(1025).toString());

    System.setProperty("RODA_CORE_PLUGINS_INTERNAL_VIRUS_CHECK_CLAMAV_PARAMS", "-m --stream -c /tmp/clamd.conf");

    String siegfriedUrl = "http://" + siegfried.getHost() + ":" + siegfried.getMappedPort(5138);
    System.setProperty("RODA_CORE_TOOLS_SIEGFRIED_MODE", "server");
    System.setProperty("RODA_CORE_TOOLS_SIEGFRIED_SERVER", siegfriedUrl);
  }

  private void stopAll() {
    LOGGER.info("Stopping test infrastructure containers...");
    if (solr != null && solr.isRunning()) {
      solr.stop();
    }
    if (zookeeper != null && zookeeper.isRunning()) {
      zookeeper.stop();
    }
    if (postgres != null && postgres.isRunning()) {
      postgres.stop();
    }
    if (network != null) {
      network.close();
    }
  }
}
