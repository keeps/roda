package pt.gov.dgarq.roda.services.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Mock class for PlanClient test.
 * 
 * @author Rui Castro
 */
public class PlanAPIMock implements Runnable {
  private static final Logger LOG = Logger.getLogger(PlanAPIMock.class);

  private final int port;
  // private final String path;
  private SocketConnection conn;
  private volatile boolean running = false;
  private PlanAPIMockContainer container;
  private long startupMem;

  public PlanAPIMock(int port) {
    // this.path = System.getProperty("java.io.tmpdir") + "/scape-tck-" +
    // System.getProperty("user.name");
    this.port = port;
  }

  public int getPort() {
    return this.port;
  }

  public synchronized boolean isRunning() {
    return this.running;
  }

  public void run() {
    try {

      this.start();

      // Notify that the server is running
      synchronized (this) {
        this.notify();
      }

    } catch (IOException e) {
      LOG.error("Error starting server - " + e.getMessage(), e);
    }
  }

  private synchronized void start() throws IOException {

    if (this.running) {
      LOG.warn("Server is already started");
    } else {
      LOG.info("Starting server...");

      this.container = new PlanAPIMockContainer();
      this.conn = new SocketConnection(this.container);
      this.startupMem = Runtime.getRuntime().totalMemory();
      this.conn.connect(new InetSocketAddress(this.port));

      this.running = true;

      LOG.info("Server started and listening for connections on port " + this.port);
    }
  }

  public synchronized void stop() throws IOException {
    if (this.running) {
      DecimalFormat fmt = new DecimalFormat("#.##");
      if (this.conn == null) {
        throw new IOException("Connection is null");
      }
      this.conn.close();
      this.running = false;
      LOG
        .debug(">> total used:\t" + fmt.format((double) Runtime.getRuntime().totalMemory() / (1024d * 1024d)) + " MB ");
      LOG.debug(">> after start:\t" + fmt.format(startupMem / (1024d * 1024d)) + " MB");
      LOG.debug(">> growth:\t\t" + fmt.format((Runtime.getRuntime().totalMemory() - startupMem) / (1024d * 1024d))
        + " MB");
    } else {
      LOG.warn("Server is already stoped");
    }
  }

  public static void main(String... args) {
    new Thread(new PlanAPIMock(9090)).start();
  }

}
