package pt.gov.dgarq.roda.servlet;

import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * @author Rui Castro
 */
public class AuthenticationState {

	private String username;
	private String password;
	private User user;
	private long ttl;

	/**
	 * @param userName
	 * @param password
	 * @param user
	 * @param ttl
	 */
	public AuthenticationState(String userName, String password, User user,
			long ttl) {
		this.username = userName;
		this.password = password;
		this.user = user;
		this.ttl = ttl;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the userName
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUsername(String userName) {
		this.username = userName;
	}

	/**
	 * @return the ttl
	 */
	public long getTtl() {
		return ttl;
	}

	/**
	 * @param ttl
	 *            the ttl to set
	 */
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

}
