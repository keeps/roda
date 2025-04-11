package org.roda.core.common.transaction;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

@SpringBootConfiguration
public class LockService {

  @Bean
  public LockRegistry localLockRegistry() {
    LockRegistry lockRegistry = new DefaultLockRegistry();
    return lockRegistry;
  }
}
