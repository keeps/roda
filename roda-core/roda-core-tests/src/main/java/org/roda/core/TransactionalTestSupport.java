/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import org.roda.core.config.TestConfig;
import org.roda.core.transaction.RODATransactionManager;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Lets tests that are not Spring tests run with RODA transactions on: plugins
 * then run inside transactions, exactly as in a running RODA.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public final class TransactionalTestSupport {
  private static ConfigurableApplicationContext context;

  private TransactionalTestSupport() {
    // utility class
  }

  /**
   * @return the transaction manager to give to
   *         {@code RodaCoreFactory.instantiateTest}
   */
  public static synchronized RODATransactionManager transactionManager() {
    if (context == null || !context.isActive()) {
      context = new SpringApplicationBuilder(TestConfig.class).web(WebApplicationType.NONE).run();
    }
    return context.getBean(RODATransactionManager.class);
  }

  /**
   * Turns transactions on, once {@code RodaCoreFactory} has been instantiated.
   * The catalog is emptied, as each test class starts with an empty storage.
   */
  public static void activate(RODATransactionManager transactionManager) {
    context.getBean(JdbcTemplate.class).update("DELETE FROM catalog_aip");
    transactionManager.setMainModelService(RodaCoreFactory.getModelService());
    transactionManager.setInitialized(true);
  }

  public static JdbcTemplate jdbcTemplate() {
    return context.getBean(JdbcTemplate.class);
  }
}
