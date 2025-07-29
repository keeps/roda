/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.ldap;

import org.roda.core.model.utils.LdapUser;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface LdapUserRepository extends LdapRepository<LdapUser> {
  LdapUser findByCommonName(String commonName);
  LdapUser findByUid(String uid);
  LdapUser findFirstByEmail(String email);
}
