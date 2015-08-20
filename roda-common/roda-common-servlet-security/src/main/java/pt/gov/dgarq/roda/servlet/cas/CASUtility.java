package pt.gov.dgarq.roda.servlet.cas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.data.v2.User;

public class CASUtility {
	static final private Logger logger = Logger.getLogger(CASUtility.class);

	private URL casURL;
	private URL callbackURL;
	private URL serviceURL;

	public CASUtility(URL casURL, URL callbackURL, URL serviceURL) throws MalformedURLException {
		this.casURL = casURL;
		this.callbackURL = callbackURL;
		this.serviceURL = serviceURL;
	}

	public CASUtility(URL casURL, URL callbackURL) throws MalformedURLException {
		this.casURL = casURL;
		this.callbackURL = callbackURL;
		this.serviceURL = callbackURL;
	}

	public String getProxyGrantingTicket(String username, String password) throws AuthenticationException {
		String pgt = null;
		try {
			String tgt = getTgt(username, password);
			String serviceTicket = getServiceTicket(tgt);
			Cas20ServiceTicketValidator cstv = new Cas20ServiceTicketValidator(casURL.toString());
			cstv.setProxyCallbackUrl(this.callbackURL.toString());
			Assertion a = cstv.validate(serviceTicket, serviceURL.toString());
			AttributePrincipal principal = a.getPrincipal();
			final Map attributes = principal.getAttributes();
			if (attributes != null) {
				Object iouAttribute = attributes.get("proxyGrantingTicket");
				String IOU = null;
				if (iouAttribute instanceof String) {
					IOU = (String) iouAttribute;
				} else if (iouAttribute instanceof List) {
					IOU = ((List<String>) iouAttribute).get(0);
				}
				if (IOU != null) {
					try {
						pgt = getPGT(this.callbackURL, IOU);
					} catch (Exception e) {
						logger.error("Error getting PGT:" + e.getMessage());
					}
				}
			} else {
				logger.debug("No attributes in AttributePrincipal");
			}
		} catch (TicketValidationException e) {
			throw new AuthenticationException(e.getMessage());
		}
		return pgt;
	}

	private CASUserPrincipal getAuthenticatedUser(String username, String password, String clientIpAddress)
			throws AuthenticationException {
		try {
			String originalTGT = getTgt(username, password);
			if (originalTGT != null) {
				String tgt = originalTGT.substring(originalTGT.lastIndexOf("/") + 1);
				String serviceTicket = getServiceTicket(tgt);
				if (serviceTicket != null) {
					AttributePrincipal attributePrincipal = getAttributePrincipal(serviceTicket);
					User u = getUserFromAttributes(attributePrincipal.getAttributes());
					if (u != null) {
						return new CASUserPrincipal(u, originalTGT, clientIpAddress);
					} else {
						throw new AuthenticationException("Error while getting User");
					}
				} else {
					throw new AuthenticationException("Error while getting Service Ticket");
				}
			} else {
				throw new AuthenticationException("Error while getting TGT");
			}
		} catch (Exception e) {
			throw new AuthenticationException("Error while getting authenticated user");
		}
	}

	public String generateProxyTicket(String proxyGrantingTicket) {
		Cas20ProxyRetriever proxyTicketRetriver = new Cas20ProxyRetriever(casURL.toString(), "UTF-8");
		return proxyTicketRetriver.getProxyTicketIdFor(proxyGrantingTicket, serviceURL.toString());
	}

	private User getUserFromAttributes(Map<String, Object> userAttributes) {

		User user = new User((String) userAttributes.get("uid"));

		if (userAttributes.get("shadowInactive") != null) {
			String zeroOrOne = (String) userAttributes.get("shadowInactive");
			user.setActive("0".equalsIgnoreCase(zeroOrOne));
		} else {
			user.setActive(true);
		}

		if (userAttributes.get("documentTitle") != null) {
			user.setIdDocumentType((String) userAttributes.get("documentTitle"));
		}
		if (userAttributes.get("documentIdentifier") != null) {
			user.setIdDocument((String) userAttributes.get("documentIdentifier"));
		}
		if (userAttributes.get("documentLocation") != null) {
			user.setIdDocumentLocation((String) userAttributes.get("documentLocation"));
		}
		if (userAttributes.get("documentVersion") != null) {
			try {
				user.setIdDocumentDate(DateParser.parse((String) userAttributes.get("documentVersion")));
			} catch (InvalidDateException e) {
				logger.warn("Error parsing ID document date (documentVersion) - " + e.getMessage(), e);
			}
		}

		if (userAttributes.get("serialNumber") != null) {
			user.setFinanceIdentificationNumber((String) userAttributes.get("serialNumber"));
		}

		if (userAttributes.get("co") != null) {
			user.setBirthCountry((String) userAttributes.get("co"));
		}

		if (userAttributes.get("cn") != null) {
			user.setFullName((String) userAttributes.get("cn"));
		}
		if (userAttributes.get("postalAddress") != null) {
			user.setPostalAddress((String) userAttributes.get("postalAddress"));
		}
		if (userAttributes.get("postalCode") != null) {
			user.setPostalCode((String) userAttributes.get("postalCode"));
		}
		if (userAttributes.get("l") != null) {
			user.setLocalityName((String) userAttributes.get("l"));
		}
		if (userAttributes.get("c") != null) {
			user.setCountryName((String) userAttributes.get("c"));
		}
		if (userAttributes.get("telephoneNumber") != null) {
			user.setTelephoneNumber((String) userAttributes.get("telephoneNumber"));
		}
		if (userAttributes.get("facsimileTelephoneNumber") != null) {
			user.setFax((String) userAttributes.get("facsimileTelephoneNumber"));
		}

		if (userAttributes.get("email") != null) {
			user.setEmail((String) userAttributes.get("email"));
		}

		if (userAttributes.get("businessCategory") != null) {
			user.setBusinessCategory((String) userAttributes.get("businessCategory"));
		}

		if (userAttributes.get("info") != null) {
			String infoStr = (String) userAttributes.get("info");

			// emailValidationToken;emailValidationTokenValidity;resetPasswordToken;resetPasswordTokenValidity

			String[] parts = infoStr.split(";");

			if (parts.length >= 1 && parts[0].trim().length() > 0) {
				user.setEmailConfirmationToken(parts[0].trim());
			}
			if (parts.length >= 2 && parts[1].trim().length() > 0) {
				user.setEmailConfirmationTokenExpirationDate(parts[1].trim());
			}
			if (parts.length >= 3 && parts[2].trim().length() > 0) {
				user.setResetPasswordToken(parts[2].trim());
			}
			if (parts.length >= 4 && parts[3].trim().length() > 0) {
				user.setResetPasswordTokenExpirationDate(parts[3].trim());
			}
		}

		if (userAttributes.get("directRoles") != null) {
			Object directRolesObject = userAttributes.get("directRoles");
			if (directRolesObject instanceof String) {
				String directRolesString = (String) directRolesObject;
				if (directRolesString != null && directRolesString.length() > 2) {
					if (directRolesString.startsWith("[")) {
						directRolesString = directRolesString.substring(1);
					}
					if (directRolesString.endsWith("]")) {
						directRolesString = directRolesString.substring(0, directRolesString.length() - 1);
					}
					String[] directRoles = directRolesString.split(",");
					Set<String> directRolesSet = new HashSet<String>();
					for (int i = 0; i < directRoles.length; i++) {
						directRolesSet.add(directRoles[i].trim());
					}
					
					user.setDirectRoles(directRolesSet);
				}
			} else if (directRolesObject instanceof List<?>) {
				List<String> directRoles = (List<String>) directRolesObject;
				Set<String> directRolesSet = new HashSet<String>(directRoles);
				user.setDirectRoles(directRolesSet);
			}
		}
		if (userAttributes.get("roles") != null) {
			Object rolesObject = userAttributes.get("roles");
			if (rolesObject instanceof String) {
				String rolesString = (String) rolesObject;
				if (rolesString != null && rolesString.length() > 2) {
					if (rolesString.startsWith("[")) {
						rolesString = rolesString.substring(1);
					}
					if (rolesString.endsWith("]")) {
						rolesString = rolesString.substring(0, rolesString.length() - 1);
					}

					String[] roles = rolesString.split(",");
					for (int i = 0; i < roles.length; i++) {
						roles[i] = roles[i].trim();
					}
					Set<String> rolesSet = new HashSet<String>();
					rolesSet.addAll(Arrays.asList(roles));
					user.setAllRoles(rolesSet);
				}
			} else if (rolesObject instanceof List<?>) {
				List<String> roles = (List<String>) rolesObject;
				Set<String> rolesSet = new HashSet<String>();
				rolesSet.addAll(roles);
				user.setAllRoles(rolesSet);
			}
		}
		if (userAttributes.get("groups") != null) {
			Object groupsObject = userAttributes.get("groups");
			if (groupsObject instanceof String) {
				String groupsString = (String) groupsObject;
				if (groupsString != null && groupsString.length() > 2) {
					if (groupsString.startsWith("[")) {
						groupsString = groupsString.substring(1);
					}
					if (groupsString.endsWith("]")) {
						groupsString = groupsString.substring(0, groupsString.length() - 1);
					}

					String[] groups = groupsString.split(",");
					Set<String> groupsSet = new HashSet<String>();
					for (int i = 0; i < groups.length; i++) {
						groupsSet.add(groups[i].trim());
					}
					user.setAllGroups(groupsSet);
				}
			} else if (groupsObject instanceof List<?>) {
				List<String> groups = (List<String>) groupsObject;
				Set<String> groupsSet = new HashSet<String>();
				groupsSet.addAll(groups);groups.toArray(new String[groups.size()]);
				user.setAllGroups(groupsSet);
			}
		}
		return user;
	}

	private AttributePrincipal getAttributePrincipal(String proxyTicket) throws AuthenticationException {
		try {
			AttributePrincipal principal = null;
			Cas20ProxyTicketValidator cptv = new Cas20ProxyTicketValidator(casURL.toString());
			cptv.setProxyCallbackUrl(callbackURL.toString());
			cptv.setAcceptAnyProxy(true);
			Assertion a = cptv.validate(proxyTicket, serviceURL.toString());
			principal = a.getPrincipal();
			return principal;
		} catch (TicketValidationException e) {
			throw new AuthenticationException("Error while getting AttributePrincipal with PGT");
		}
	}

	private AttributePrincipalWithProxyGrantingTicket getAttributePrincipalWithProxyGrantingTicket(String proxyTicket)
			throws AuthenticationException {
		AttributePrincipal attributePrincipal = null;
		String proxyGrantingTicket = null;
		try {
			Cas20ProxyTicketValidator cptv = new Cas20ProxyTicketValidator(casURL.toString());
			cptv.setProxyCallbackUrl(callbackURL.toString());
			cptv.setAcceptAnyProxy(true);

			logger.debug("Validating proxy ticket: " + proxyTicket + " in service: " + serviceURL);
			Assertion a = cptv.validate(proxyTicket, serviceURL.toString());
			attributePrincipal = a.getPrincipal();
			final Map attributes = attributePrincipal.getAttributes();
			if (attributes != null) {
				Object iouAttribute = attributes.get("proxyGrantingTicket");
				String IOU = null;
				if (iouAttribute instanceof String) {
					IOU = (String) iouAttribute;
				} else if (iouAttribute instanceof List) {
					IOU = ((List<String>) iouAttribute).get(0);
				}
				if (IOU != null) {
					try {
						proxyGrantingTicket = getPGT(this.callbackURL, IOU);
					} catch (Exception e) {
						logger.error("Error getting PGT:" + e.getMessage());
					}
					if (proxyGrantingTicket != null) {
						AttributePrincipalWithProxyGrantingTicket apwpgt = new AttributePrincipalWithProxyGrantingTicket(
								attributePrincipal, proxyGrantingTicket);
						return apwpgt;
					}
				}
			} else {
				logger.debug("No attributes in AttributePrincipal");
			}
		} catch (TicketValidationException tve) {
			throw new AuthenticationException("Error while getting AttributePrincipal: " + tve.getMessage());
		}
		throw new AuthenticationException("Error while getting AttributePrincipal with PGT");
	}

	public String getPGT(URL url, String IOU) {

		try {
			URL getURL = new URL(url.toExternalForm() + "?iou=" + IOU);

			HttpsURLConnection hsu = (HttpsURLConnection) openConn(getURL.toExternalForm());
			TrustModifier.relaxHostChecking(hsu);
			hsu.setRequestProperty("Request-Method", "GET");
			hsu.setDoInput(true);
			hsu.setDoOutput(false);
			hsu.connect();
			BufferedReader br = new BufferedReader(new InputStreamReader(hsu.getInputStream()));
			StringBuffer newData = new StringBuffer(10000);
			String s = "";
			while (null != ((s = br.readLine()))) {
				newData.append(s);
			}
			br.close();
			String ticket = new String(newData);
			if (ticket != null && ticket.trim().equals("")) {
				ticket = null;
			}
			return ticket;
		} catch (Exception e) {
			logger.error("Error while getting PGT:" + e.getMessage());
		}
		return null;
	}

	private String getServiceTicket(String tgt) {
		try {
			String casTicketsUrl = casURL.toString() + "/v1/tickets";
			String encodedServiceURL = URLEncoder.encode("service", "utf-8") + "="
					+ URLEncoder.encode(serviceURL.toString(), "utf-8");
			String myURL = casTicketsUrl + "/" + tgt;
			HttpsURLConnection hsu = (HttpsURLConnection) openConn(myURL);
			TrustModifier.relaxHostChecking(hsu);
			OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
			BufferedWriter bwr = new BufferedWriter(out);
			bwr.write(encodedServiceURL);
			bwr.flush();
			bwr.close();
			out.close();
			BufferedReader isr = new BufferedReader(new InputStreamReader(hsu.getInputStream()));
			String serviceTicket = IOUtils.toString(isr);
			isr.close();
			hsu.disconnect();
			return serviceTicket;
		} catch (MalformedURLException mfue) {

		} catch (UnsupportedEncodingException e) {
			logger.error("Error while encoding service URL: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Error while getting service ticket: " + e);
		}
		return null;
	}

	private String getTgt(String username, String password) {
		String tgt = null;
		try {
			String casTicketsUrl = casURL.toString() + "/v1/tickets";
			HttpsURLConnection hsu = (HttpsURLConnection) openConn(casTicketsUrl);
			hsu.setDoInput(true);
			hsu.setDoOutput(true);
			TrustModifier.relaxHostChecking(hsu);
			String s = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
			s += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

			OutputStreamWriter out = new OutputStreamWriter(hsu.getOutputStream());
			BufferedWriter bwr = new BufferedWriter(out);
			bwr.write(s);
			bwr.flush();
			bwr.close();
			out.close();
			tgt = hsu.getHeaderField("location");
			if (hsu.getResponseCode() == 201 && tgt != null) {
				closeConn(hsu);
				if (tgt.contains((casURL.toString() + "/v1/tickets/"))) {
					tgt = tgt.substring((casURL + "/v1/tickets/").length());
				}

			}
		} catch (Exception e) {
			logger.error("Error while getting TGT: [" + e.getClass().getName() + "] " + e.getMessage());
		}
		return tgt;
	}

	static URLConnection openConn(String urlk) throws MalformedURLException, IOException {

		URL url = new URL(urlk);
		HttpsURLConnection hsu = (HttpsURLConnection) url.openConnection();
		hsu.setDoInput(true);
		hsu.setDoOutput(true);
		hsu.setRequestMethod("POST");
		return hsu;

	}

	static void closeConn(HttpsURLConnection c) {
		c.disconnect();
	}

	private CASUserPrincipal getAuthenticatedUserWithProxyGrantingTicket(String proxyTicket, String clientIpAddress)
			throws AuthenticationException {
		try {
			AttributePrincipalWithProxyGrantingTicket attributePrincipalWithProxyGrantingTicket = getAttributePrincipalWithProxyGrantingTicket(
					proxyTicket);
			User u = getUserFromAttributes(
					attributePrincipalWithProxyGrantingTicket.getAttributePrincipal().getAttributes());

			if (u.getAllRoles() == null || u.getAllRoles().size() == 0 || u.getDirectRoles() == null
					|| u.getDirectRoles().size() == 0) {
				// TODO
				// deleteTicket(originalTGT);
			}
			return new CASUserPrincipal(u, attributePrincipalWithProxyGrantingTicket.getProxyGrantingTicket(),
					clientIpAddress);
		} catch (AuthenticationException e) {
			throw e;
		} catch (Throwable e) {
			throw new AuthenticationException("Error while getting User: " + e.getMessage());
		}
	}

	public CASUserPrincipal getCASUserPrincipal(String username, String password, String clientIpAddress)
			throws AuthenticationException {
		CASUserPrincipal cup = null;
		if (password.startsWith("ST-")) {
			try { // get using Proxy ticket
				cup = this.getAuthenticatedUserWithProxyGrantingTicket(password, clientIpAddress);
			} catch (Throwable e) {
				logger.warn("Could not authenticate with service ticket: " + password, e);
			}
		}
		if (cup == null && password.startsWith("TGT-")) {
			try { // get using PGT
				String proxyTicket = this.generateProxyTicket(password);
				cup = this.getAuthenticatedUserWithProxyGrantingTicket(proxyTicket, clientIpAddress);
			} catch (Throwable e) {
				logger.warn("Could not authenticate with service ticket: " + password, e);
			}
		}
		if (cup == null) {
			try {
				cup = this.getAuthenticatedUser(username, password, clientIpAddress);
			} catch (Throwable e) {
				logger.warn("Could not authenticate with service ticket: " + password, e);
			}
		}
		if (cup == null) {
			throw new AuthenticationException("Error while getting User");
		}
		return cup;
	}

	public CASUserPrincipal getCASUserPrincipalFromProxyGrantingTicket(String proxyGrantingTicket, String clientIpAddress)
			throws AuthenticationException {
		CASUserPrincipal cup = null;
		try {
			String proxyTicket = this.generateProxyTicket(proxyGrantingTicket);
			cup = this.getAuthenticatedUserWithProxyGrantingTicket(proxyTicket, clientIpAddress);
		} catch (Throwable e) {
			logger.warn("Error while authenticating with proxy granting ticket:" + e.getMessage(), e);
		}
		return cup;
	}

}
