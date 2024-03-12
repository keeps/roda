package org.roda.core.config;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.model.utils.Pbkdf2PasswordEncoderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

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
