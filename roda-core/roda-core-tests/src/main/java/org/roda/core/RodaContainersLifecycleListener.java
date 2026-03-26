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
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * TestNG suite listener that starts all required test infrastructure containers
 * (ZooKeeper, Solr, PostgreSQL) before any test or Spring context runs.
 * <p>
 * Registered via {@code testng.xml} so it fires at the very beginning of the
 * test suite, ahead of any class loading or Spring Boot context initialization.
 *
 * @author RODA Community
 */
public class RodaContainersLifecycleListener implements ISuiteListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(RodaContainersLifecycleListener.class);

  @Override
  public void onStart(ISuite suite) {
    LOGGER.info("RodaContainersLifecycleListener: initializing test containers for suite '{}'", suite.getName());
    TestContainersManager.getInstance();
  }

  @Override
  public void onFinish(ISuite suite) {
    // Containers are stopped via JVM shutdown hook in TestContainersManager.
  }
}
