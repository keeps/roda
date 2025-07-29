/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Configuration
@EnableLdapRepositories(basePackages = {"org.roda.*"})
public class LdapConfig {
  @Bean
  public LdapContextSource contextSource() {
    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl("ldap://localhost:1389");
    contextSource.setBase("dc=roda,dc=org");
    contextSource.setUserDn("cn=admin,dc=roda,dc=org");
    contextSource.setPassword("roda");
    return contextSource;
  }

  @Bean
  public LdapTemplate ldapTemplate() {
    return new LdapTemplate(contextSource());
  }
}
