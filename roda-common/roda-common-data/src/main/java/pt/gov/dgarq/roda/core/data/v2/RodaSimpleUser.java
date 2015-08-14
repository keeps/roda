package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;

public class RodaSimpleUser implements Serializable {
	private static final long serialVersionUID = 6514790636010895870L;

	private String username;
	private String email;
	private boolean guest;

	public RodaSimpleUser() {
		super();
	}

	public RodaSimpleUser(String username, String email, boolean guest) {
		super();
		this.username = username;
		this.email = email;
		this.guest = guest;
	}

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

	@Override
	public String toString() {
		return "RodaSimpleUser [username=" + username + ", email=" + email + ", guest=" + guest + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (guest ? 1231 : 1237);
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RodaSimpleUser)) {
			return false;
		}
		RodaSimpleUser other = (RodaSimpleUser) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (guest != other.guest) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
