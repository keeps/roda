/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.postgreSql.in;

import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.in.JDBCImportModule;

/**
 * <p>
 * Module to import data from a PostgreSQL database management system via JDBC
 * driver. The postgresql-8.3-603.jdbc3.jar driver supports PostgreSQL version
 * 7.4 to 8.3.
 * </p>
 * 
 * <p>
 * To use this module, the PostgreSQL server must be configured:
 * </p>
 * <ol>
 * <li>Server must be configured to accept TCP/IP connections. This can be done
 * by setting <code>listen_addresses = 'localhost'</code> (or
 * <code>tcpip_socket = true</code> in older versions) in the postgresql.conf
 * file.</li>
 * <li>The client authentication setup in the pg_hba.conf file may need to be
 * configured, adding a line like
 * <code>host all all 127.0.0.1 255.0.0.0 trust</code>. The JDBC driver
 * supports the trust, ident, password, md5, and crypt authentication methods.
 * </li>
 * </ol>
 * 
 * @author Luis Faria
 * 
 */
public class PostgreSQLJDBCImportModule extends JDBCImportModule {

	/**
	 * Create a new PostgreSQL JDBC import module
	 * 
	 * @param hostname
	 *            the name of the PostgreSQL server host (e.g. localhost)
	 * @param database
	 *            the name of the database to connect to
	 * @param username
	 *            the name of the user to use in connection
	 * @param password
	 *            the password of the user to use in connection
	 * @param encrypt
	 *            encrypt connection
	 */
	public PostgreSQLJDBCImportModule(String hostname, String database,
			String username, String password, boolean encrypt) {
		super("org.postgresql.Driver", "jdbc:postgresql://" + hostname + "/"
				+ database + "?user=" + username + "&password=" + password
				+ (encrypt ? "&ssl=true" : ""));
	}

	/**
	 * Create a new PostgreSQL JDBC import module
	 * 
	 * @param hostname
	 *            the name of the PostgreSQL server host (e.g. localhost)
	 * @param port
	 *            the port of where the PostgreSQL server is listening, default
	 *            is 5432
	 * @param database
	 *            the name of the database to connect to
	 * @param username
	 *            the name of the user to use in connection
	 * @param password
	 *            the password of the user to use in connection
	 * @param encrypt
	 *            encrypt connection
	 */
	public PostgreSQLJDBCImportModule(String hostname, int port,
			String database, String username, String password, boolean encrypt) {
		super("org.postgresql.Driver", "jdbc:postgresql://" + hostname + ":"
				+ port + "/" + database + "?user=" + username + "&password="
				+ password + (encrypt ? "&ssl=true" : ""));
	}
}
