/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui;

import org.roda.core.RodaBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {OAuth2ClientAutoConfiguration.class, SecurityAutoConfiguration.class,
  UserDetailsServiceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.roda.*"})
@EnableJpaRepositories(basePackages = "org.roda.core.repository")
@EntityScan(basePackages = "org.roda.core.entity")
@ServletComponentScan
@EnableScheduling
public class RODA {
  public static void main(String[] args) {
    RodaBootstrap.instantiate();
    SpringApplication.run(RODA.class);
  }
}