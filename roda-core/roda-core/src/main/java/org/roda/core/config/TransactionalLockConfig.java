package org.roda.core.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
public class TransactionalLockConfig {
  @Bean
  public DefaultLockRepository DefaultLockRepository(DataSource dataSource) {
    return new DefaultLockRepository(dataSource);
  }

  @Bean
  public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
    return new JdbcLockRegistry(lockRepository);
  }
}
