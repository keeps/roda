package pt.gov.dgarq.roda.core.data.v2;

public class RodaSimpleUser {
	private String username;
	private String email;
	private boolean guest;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean isGuest() {
		return guest;
	}
	public void setGuest(boolean guest) {
		this.guest = guest;
	}
	
	
}
