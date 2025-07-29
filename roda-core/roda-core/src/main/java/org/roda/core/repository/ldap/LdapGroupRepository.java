/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.repository.ldap;

import org.roda.core.model.utils.LdapGroup;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Repository
public interface LdapGroupRepository extends LdapRepository<LdapGroup> {
  LdapGroup findByCommonName(String commonName);
  Set<LdapGroup> findAllByUniqueMember(String uniqueMember);
  Set<LdapGroup> findAllByUniqueMemberAndShadowInactiveEquals(String uniqueMember, int shadowInactive);
}
