package pt.gov.dgarq.roda.servlet;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This is a RODA request wrapper for a HttpServletRequest.
 * 
 * @author Rui Castro
 */
public class RodaServletRequestWrapper extends HttpServletRequestWrapper {

	// static final private Logger logger =
	// Logger.getLogger(RodaServletRequestWrapper.class);

	private UserPrincipal ldapUserPrincipal = null;

	/**
	 * @param request
	 */
	public RodaServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	/**
	 * @return the ldapUser
	 */
	public UserPrincipal getLdapUserPrincipal() {
		return ldapUserPrincipal;
	}

	/**
	 * @param ldapUser
	 *            the ldapUser to set
	 */
	public void setLdapUserPrincipal(UserPrincipal ldapUser) {
		this.ldapUserPrincipal = ldapUser;
		setAttribute(LdapAuthenticationFilter.USERNAME, ldapUser.getName());
		// setAttribute(LdapAuthenticationFilter.FEDORA_AUX_SUBJECT_ATTRIBUTES,ldapUserPrincipal.getFedoraAttributeMap());
	}

	/**
	 * @see HttpServletRequestWrapper#getUserPrincipal()
	 */
	@Override
	public Principal getUserPrincipal() {
		return getLdapUserPrincipal();
	}

	/**
	 * @see HttpServletRequestWrapper#isUserInRole(String)
	 */
	@Override
	public boolean isUserInRole(String role) {

		boolean isInRole;

		if (getLdapUserPrincipal() == null) {
			isInRole = false;
		} else {
			isInRole = getLdapUserPrincipal().hasRole(role);
		}

		return isInRole;
	}
}
