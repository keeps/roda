package org.roda.core.repository;

import org.roda.core.model.utils.LdapGroup;
import org.springframework.data.ldap.repository.LdapRepository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface LdapGroupRepository extends LdapRepository<LdapGroup> {
  LdapGroup findByCommonName(String commonName);
}
