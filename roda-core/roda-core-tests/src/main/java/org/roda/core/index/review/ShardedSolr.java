/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * A Solr of its own for a review test, with Solr's defaults (so HTTP/2 between
 * shards), that RODA is pointed at with the given settings, e.g. the shard
 * count of the collections it creates ({@code SOLR_NUM_SHARDS}). The shared
 * test Solr creates its collections with one shard, where nothing is forwarded
 * between shards and nothing is split across them.
 * <p>
 * Start it before {@code RodaCoreFactory.instantiateTest} and stop it after
 * {@code RodaCoreFactory.shutdown}: it sets, and then restores, the system
 * properties RODA reads the ZooKeeper address and those settings from.
 * <p>
 * Parameters (system properties, defaults in brackets):
 * {@code solrreview.image} [solr:10.0.0], {@code solrreview.heap} [2g].
 */
public final class ShardedSolr implements AutoCloseable {
  private static final String SOLR_CLOUD_URLS_PROPERTY = "RODA_CORE_SOLR_CLOUD_URLS";
  // Solr's embedded ZooKeeper listens on the Solr port + 1000
  private static final int ZOOKEEPER_PORT_OFFSET = 1000;

  private final GenericContainer<?> container;
  // the value each property set had before, null when it was not set
  private final Map<String, String> previous = new HashMap<>();

  private ShardedSolr(GenericContainer<?> container) {
    this.container = container;
  }

  /** With every collection on the given number of shards ({@code SOLR_NUM_SHARDS}). */
  public static ShardedSolr start(int shards) throws IOException {
    return start(Map.of("SOLR_NUM_SHARDS", String.valueOf(shards)));
  }

  /**
   * With the given system properties set while it runs, e.g.
   * {@code SOLR_NUM_SHARDS} or {@code RODA_CORE_SOLR_SHARDS_LARGE}.
   */
  public static ShardedSolr start(Map<String, String> properties) throws IOException {
    int port = findFreePort();
    int zookeeperPort = port + ZOOKEEPER_PORT_OFFSET;

    // listen and advertise on the same host port, so that RODA on the host reaches the node at the address it
    // reads from the cluster state
    GenericContainer<?> container = new GenericContainer<>(
      DockerImageName.parse(System.getProperty("solrreview.image", "solr:10.0.0")))
      .withExposedPorts(port, zookeeperPort).withEnv("SOLR_PORT_LISTEN", String.valueOf(port))
      .withEnv("SOLR_HOST_ADVERTISE", "localhost").withEnv("SOLR_HEAP", System.getProperty("solrreview.heap", "2g"))
      .waitingFor(Wait.forHttp("/solr/admin/info/system").forPort(port).forStatusCode(200)
        .withStartupTimeout(Duration.ofMinutes(3)));
    container.setPortBindings(List.of(port + ":" + port, zookeeperPort + ":" + zookeeperPort));
    container.start();

    ShardedSolr solr = new ShardedSolr(container);
    solr.set(SOLR_CLOUD_URLS_PROPERTY, "localhost:" + zookeeperPort);
    properties.forEach(solr::set);
    return solr;
  }

  /** The Solr container, e.g. to read its logs. */
  public GenericContainer<?> container() {
    return container;
  }

  @Override
  public void close() {
    previous.forEach((property, value) -> {
      if (value == null) {
        System.clearProperty(property);
      } else {
        System.setProperty(property, value);
      }
    });
    container.stop();
  }

  private void set(String property, String value) {
    previous.putIfAbsent(property, System.getProperty(property));
    System.setProperty(property, value);
  }

  private static int findFreePort() throws IOException {
    // the ZooKeeper port (port + 1000) must be free too
    for (int attempt = 0; attempt < 20; attempt++) {
      int port;
      try (ServerSocket socket = new ServerSocket(0)) {
        port = socket.getLocalPort();
      }
      if (port + ZOOKEEPER_PORT_OFFSET < 65536) {
        try (ServerSocket zookeeper = new ServerSocket(port + ZOOKEEPER_PORT_OFFSET)) {
          return port;
        } catch (IOException e) {
          // taken, try another
        }
      }
    }
    throw new IOException("No free pair of ports for Solr and its ZooKeeper");
  }
}
