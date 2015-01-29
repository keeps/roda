package pt.gov.dgarq.roda.servlet;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import pt.gov.dgarq.roda.servlet.cas.CASAuthenticationFilter;

/**
 * This is a RODA request wrapper for a HttpServletRequest.
 * 
 * @author Rui Castro
 */
public class RodaServletRequestWrapper extends HttpServletRequestWrapper {

	private pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal  CASUserPrincipal = null;
	
	
	

	public pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal getCASUserPrincipal() {
		return CASUserPrincipal;
	}

	public void setCASUserPrincipal(
			pt.gov.dgarq.roda.servlet.cas.CASUserPrincipal CASUserPrincipal) {
		this.CASUserPrincipal = CASUserPrincipal;
		setAttribute(CASAuthenticationFilter.USERNAME, CASUserPrincipal.getName());
	}

	/**
	 * @param request
	 */
	public RodaServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	/**
	 * @see HttpServletRequestWrapper#getUserPrincipal()
	 */
	@Override
	public Principal getUserPrincipal() {
		return getCASUserPrincipal();
	}

	/**
	 * @see HttpServletRequestWrapper#isUserInRole(String)
	 */
	@Override
	public boolean isUserInRole(String role) {

		boolean isInRole;

		if (getCASUserPrincipal() == null) {
			isInRole = false;
		} else {
			isInRole = getCASUserPrincipal().hasRole(role);
		}
		return isInRole;
	}
}
