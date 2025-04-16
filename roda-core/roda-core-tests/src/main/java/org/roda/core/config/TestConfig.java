package org.roda.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
@ComponentScan(basePackages = "org.roda.core")
@EnableJpaRepositories(basePackages = "org.roda.core.repository")
@EntityScan(basePackages = "org.roda.core.model")
public class TestConfig {
}
