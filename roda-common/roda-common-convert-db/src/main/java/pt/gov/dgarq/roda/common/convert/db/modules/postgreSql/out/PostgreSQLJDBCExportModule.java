package pt.gov.dgarq.roda.common.convert.db.modules.postgreSql.out;

import java.sql.SQLException;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.out.JDBCExportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.postgreSql.PostgreSQLHelper;

/**
 * <p>
 * Module to export data to a PostgreSQL database management system via JDBC
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
public class PostgreSQLJDBCExportModule extends JDBCExportModule {

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
	public PostgreSQLJDBCExportModule(String hostname, String database,
			String username, String password, boolean encrypt) {
		super("org.postgresql.Driver", "jdbc:postgresql://" + hostname + "/"
				+ database + "?user=" + username + "&password=" + password
				+ (encrypt ? "&ssl=true" : ""), new PostgreSQLHelper());
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
	public PostgreSQLJDBCExportModule(String hostname, int port,
			String database, String username, String password, boolean encrypt) {
		super("org.postgresql.Driver", "jdbc:postgresql://" + hostname + ":"
				+ port + "/" + database + "?user=" + username + "&password="
				+ password + (encrypt ? "&ssl=true" : ""),
				new PostgreSQLHelper());
	}

	public void handleDataCloseTable(String tableId) throws ModuleException {
		try {
			getStatement().executeUpdate(
					((PostgreSQLHelper) getSqlHelper())
							.grantPermissionsSQL(currentTableStructure
									.getName()));
		} catch (SQLException e) {
			throw new ModuleException("Error granting permissions to public", e);
		}
		super.handleDataCloseTable(tableId);
	}

}
