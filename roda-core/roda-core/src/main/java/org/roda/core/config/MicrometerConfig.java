package org.roda.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
@EnableAspectJAutoProxy
public class MicrometerConfig {
  @Bean
  @ConditionalOnProperty(name = "management.metrics.micrometer.timed.aspect.enabled", havingValue = "true", matchIfMissing = false)
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  @Bean
  @ConditionalOnProperty(name = "management.metrics.micrometer.counted.aspect.enabled", havingValue = "true", matchIfMissing = false)
  public CountedAspect countedAspect(MeterRegistry registry) {
    return new CountedAspect(registry);
  }
}
