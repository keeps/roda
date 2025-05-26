package org.roda.core.security;

import org.roda.core.config.LdapConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.model.utils.LdapUtility;
import org.roda.core.repository.LdapGroupRepository;
import org.roda.core.repository.LdapRoleRepository;
import org.roda.core.repository.LdapUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LdapUtilityTestHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtilityTestHelper.class);

  private GenericContainer<?> openldap;
  private LdapUtility ldapUtility;

  public LdapUtility getLdapUtility() {
    return ldapUtility;
  }

  public LdapUtilityTestHelper() {
    final String ldapBaseDN = "dc=roda,dc=org";
    DockerImageName OPENLDAP_IMAGE = DockerImageName.parse("docker.io/bitnami/openldap:2.6");

    openldap = new GenericContainer<>(OPENLDAP_IMAGE);
    openldap.withExposedPorts(1389);
    openldap.withEnv("BITNAMI_DEBUG", "true");
    openldap.withEnv("LDAP_ROOT", ldapBaseDN);
    openldap.withEnv("LDAP_SKIP_DEFAULT_TREE", "yes");
    openldap.withEnv("LDAP_ADMIN_USERNAME", "admin");
    openldap.withEnv("LDAP_ADMIN_PASSWORD", "roda");
    openldap.withEnv("LDAP_EXTRA_SCHEMAS", "cosine,inetorgperson,nis,pbkdf2");
    openldap.withCopyFileToContainer(MountableFile.forClasspathResource("/config/ldap/schema/pbkdf2.ldif"),
      "/opt/bitnami/openldap/etc/schema/pbkdf2.ldif");
    openldap.waitingFor(Wait.forLogMessage(".* Starting slapd .*", 1));
    openldap.start();

    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(LdapConfig.class);
    context.refresh();
    LdapTemplate ldapTemplate = context.getBean(LdapTemplate.class);
    LdapContextSource contextSource = (LdapContextSource) ldapTemplate.getContextSource();
    contextSource.setUrl(RodaConstants.CORE_LDAP_DEFAULT_URL + ":" + openldap.getMappedPort(1389));
    contextSource.afterPropertiesSet();
    LdapUserRepository ldapUserRepository = context.getBean(LdapUserRepository.class);
    LdapGroupRepository ldapGroupRepository = context.getBean(LdapGroupRepository.class);
    LdapRoleRepository ldapRoleRepository = context.getBean(LdapRoleRepository.class);

    ldapUtility = new LdapUtility(ldapTemplate, ldapUserRepository, ldapGroupRepository, ldapRoleRepository);

    LOGGER.info("LDAP server running at: {}", contextSource.getUrls()[0]);
  }

  public void shutdown() {
    openldap.close();
  }
}
