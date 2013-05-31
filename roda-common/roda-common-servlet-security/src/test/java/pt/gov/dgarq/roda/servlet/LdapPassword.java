package pt.gov.dgarq.roda.servlet;

/**
 * @author Rohan Pinto <rohan@rohanpinto.com>
 * @author Rui Castro
 * 
 * @see http://www.ldapguru.net/modules/newbb/viewtopic.php?topic_id=1479&forum=6
 */
import pt.gov.dgarq.roda.util.PasswordHandler;

/**
 * @author Rui Castro
 */
public class LdapPassword {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 0) {
			System.err.println("Syntax: java LdapPassword");
			System.err.println("\t -generate <algorithm> <password> [<salt>]");
			System.err.println("\t -verify <digest> <password>");
			System.exit(1);
		}

		PasswordHandler handler = PasswordHandler.getInstance();

		if (args[0].equalsIgnoreCase("-generate")) {
			if (args.length < 3) {
				System.err
						.println("Use: -generate <algorithm> <password> [<salt>]");
				System.exit(1);
			}
			String algorithm = args[1];
			String password = args[2];
			String salt = null;
			if (args.length > 3) {
				salt = args[3];
			}

			try {
				System.out.println("Digest: "
						+ handler.generateDigest(password, salt, algorithm));
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		} else if (args[0].equalsIgnoreCase("-verify")) {
			if (args.length < 3) {
				System.err.println("Use: -verify <digest> <password>");
				System.exit(1);
			}
			String digest = args[1];
			String password = args[2];

			try {
				if (handler.verify(digest, password)) {
					System.out.println("Password matches");
				} else {
					System.out.println("Password does not match");
				}
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		} else {
			System.err.println("Unrecognied option: " + args[0]);
			System.exit(1);
		}
	}

}
