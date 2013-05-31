package pt.gov.dgarq.roda.servlet;

import pt.gov.dgarq.roda.core.data.User;

/**
 * This is a {@link UserPrincipal} with extra attributes like the user
 * provided password.
 * 
 * @author Rui Castro
 */
public class ExtendedUserPrincipal extends UserPrincipal {
	private static final long serialVersionUID = 2163580201575276372L;

	private String password = null;

	/**
	 * Constructs a new {@link ExtendedUserPrincipal}.
	 * 
	 * @param user
	 *            the {@link User} represented by this
	 *            {@link ExtendedUserPrincipal}.
	 * @param password
	 *            the user password.
	 */
	public ExtendedUserPrincipal(User user, String password) {
		super(user);
		setPassword(password);
	}

	/**
	 * @see UserPrincipal#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + super.toString()
				+ ", password=*****" + ")";
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

}
