package pt.gov.dgarq.roda.servlet.cas;

import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.servlet.UserPrincipal;

/**
 * FIXME this will be most certainly removed
 * */
@Deprecated
public class CASUserPrincipal extends UserPrincipal{
	private static final long serialVersionUID = 387759283684834603L;
	private String proxyGrantingTicket;
	private String clientIpAddress;
	private boolean guest;
	public CASUserPrincipal(User user, String proxyGrantingTicket, String clientIpAddress){
		super(user);
		this.proxyGrantingTicket = proxyGrantingTicket;
		this.clientIpAddress = clientIpAddress;
		this.guest=false;
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

	public boolean isGuest() {
		return guest;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}
	
	public boolean hasRole(String role){
		return true;
	}
	
	
	
	
}
