package pt.gov.dgarq.roda.servlet.cas;

import org.jasig.cas.client.authentication.AttributePrincipal;

public class AttributePrincipalWithProxyGrantingTicket {
	AttributePrincipal attributePrincipal;
	String proxyGrantingTicket;
	
	
	
	public AttributePrincipalWithProxyGrantingTicket(
			AttributePrincipal attributePrincipal, String proxyGrantingTicket) {
		super();
		this.attributePrincipal = attributePrincipal;
		this.proxyGrantingTicket = proxyGrantingTicket;
	}
	
	public AttributePrincipal getAttributePrincipal() {
		return attributePrincipal;
	}
	public void setAttributePrincipal(AttributePrincipal attributePrincipal) {
		this.attributePrincipal = attributePrincipal;
	}
	public String getProxyGrantingTicket() {
		return proxyGrantingTicket;
	}
	public void setProxyGrantingTicket(String proxyGrantingTicket) {
		this.proxyGrantingTicket = proxyGrantingTicket;
	}
	
	
}
