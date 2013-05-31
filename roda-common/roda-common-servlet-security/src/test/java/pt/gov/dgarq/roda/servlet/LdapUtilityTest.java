package pt.gov.dgarq.roda.servlet;

import java.util.ArrayList;
import java.util.List;

/**
 * LDAP utility test class.
 * 
 * @author Rui Castro
 */
public class LdapUtilityTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String ldapHost = "localhost";
		int ldapPort = 389;
		String ldapAdminDN = "cn=admin,dc=roda,dc=dgarq,dc=gov,dc=pt";
		String ldapAdminPassword = "froda";
		String ldapPeopleDN = "ou=people,dc=roda,dc=dgarq,dc=gov,dc=pt";
		String ldapGroupsDN = "ou=groups,dc=roda,dc=dgarq,dc=gov,dc=pt";
		String ldapRolesDN = "ou=roles,dc=roda,dc=dgarq,dc=gov,dc=pt";
		String ldapPasswordDigestAlgorithm = "MD5";
		List<String> ldapProtectedUsers = new ArrayList<String>();
		List<String> ldapProtectedGroups = new ArrayList<String>();

		LdapUtility ldapUtility = new LdapUtility(ldapHost, ldapPort,
				ldapPeopleDN, ldapGroupsDN, ldapRolesDN, ldapAdminDN,
				ldapAdminPassword, ldapPasswordDigestAlgorithm,
				ldapProtectedUsers, ldapProtectedGroups);

		try {

			ldapUtility.getUserWithEmail("rcastro_@iantt.pt");

		} catch (LdapUtilityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
