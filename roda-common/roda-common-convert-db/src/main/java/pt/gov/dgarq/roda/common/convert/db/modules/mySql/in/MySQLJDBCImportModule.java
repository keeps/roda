/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.mySql.in;

import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.in.JDBCImportModule;

/**
 * @author Luis Faria
 * 
 */
public class MySQLJDBCImportModule extends JDBCImportModule {

	/**
	 * MySQL JDBC import module constructor
	 * 
	 * @param hostname
	 *            the hostname of the MySQL server
	 * @param database
	 *            the name of the database to import from
	 * @param username
	 *            the name of the user to use in connection
	 * @param password
	 *            the password of the user to use in connection
	 */
	public MySQLJDBCImportModule(String hostname, String database,
			String username, String password) {
		super("com.mysql.jdbc.Driver", "jdbc:mysql://" + hostname + "/"
				+ database + "?" + "user=" + username + "&password=" + password);
	}

	/**
	 * MySQL JDBC import module constructor
	 * 
	 * @param hostname
	 *            the hostname of the MySQL server
	 * @param port
	 *            the port that the MySQL server is listening
	 * @param database
	 *            the name of the database to import from
	 * @param username
	 *            the name of the user to use in connection
	 * @param password
	 *            the password of the user to use in connection
	 */
	public MySQLJDBCImportModule(String hostname, int port, String database,
			String username, String password) {
		super("com.mysql.jdbc.Driver", "jdbc:mysql://" + hostname + ":" + port
				+ "/" + database + "?" + "user=" + username + "&password="
				+ password);
	}
}
