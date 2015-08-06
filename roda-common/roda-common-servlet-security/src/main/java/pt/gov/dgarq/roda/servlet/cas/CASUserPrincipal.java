package pt.gov.dgarq.roda.servlet.cas;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.servlet.UserPrincipal;

public class CASUserPrincipal extends UserPrincipal{
	private static final long serialVersionUID = 387759283684834603L;
	private String proxyGrantingTicket;
	private String clientIpAddress;
	
	public CASUserPrincipal(User user, String proxyGrantingTicket, String clientIpAddress){
		super(user);
		this.proxyGrantingTicket = proxyGrantingTicket;
		this.clientIpAddress = clientIpAddress;
	}

	public String getProxyGrantingTicket() {
		return proxyGrantingTicket;
	}

	public void setProxyGrantingTicket(String proxyGrantingTicket) {
		this.proxyGrantingTicket = proxyGrantingTicket;
	}

	public String getClientIpAddress() {
		return clientIpAddress;
	}

	public void setClientIpAddress(String clientIpAddress) {
		this.clientIpAddress = clientIpAddress;
	}

	
	
	
}
