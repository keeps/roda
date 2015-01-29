package pt.gov.dgarq.roda.servlet.cas;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.servlet.UserPrincipal;

public class CASUserPrincipal extends UserPrincipal{
	private static final long serialVersionUID = 387759283684834603L;
	private String proxyGrantingTicket;
	
	public CASUserPrincipal(User user, String proxyGrantingTicket){
		super(user);
		this.proxyGrantingTicket = proxyGrantingTicket;
	}

	public String getProxyGrantingTicket() {
		return proxyGrantingTicket;
	}

	public void setProxyGrantingTicket(String proxyGrantingTicket) {
		this.proxyGrantingTicket = proxyGrantingTicket;
	}

	
	
	
}
