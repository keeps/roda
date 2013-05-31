/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.mySql.out;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.out.JDBCExportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.mySql.MySQLHelper;

/**
 * @author Luis Faria
 * 
 */
public class MySQLJDBCExportModule extends JDBCExportModule {

	protected static final String MYSQL_ADMIN_DATABASE = "mysql";

	private final Logger logger = Logger.getLogger(MySQLJDBCExportModule.class);

	private final String hostname;

	private final int port;

	private final String username;

	private final String password;

	private final Map<String, Connection> connections;

	/**
	 * MySQL JDBC export module constructor
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
	public MySQLJDBCExportModule(String hostname, String database,
			String username, String password) {
		super("com.mysql.jdbc.Driver",
				"jdbc:mysql://" + hostname + "/" + database + "?" + "user="
						+ username + "&password=" + password, new MySQLHelper());
		this.hostname = hostname;
		this.port = -1;
		this.username = username;
		this.password = password;
		this.connections = new HashMap<String, Connection>();

	}

	/**
	 * MySQL JDBC export module constructor
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
	public MySQLJDBCExportModule(String hostname, int port, String database,
			String username, String password) {
		super("com.mysql.jdbc.Driver", "jdbc:mysql://" + hostname + ":" + port
				+ "/" + database + "?" + "user=" + username + "&password="
				+ password, new MySQLHelper());
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
		this.connections = new HashMap<String, Connection>();
	}

	/**
	 * Get a conection to a database. This connection can be used to create the
	 * database
	 * 
	 * @param databaseName
	 *            the name of the database to connect
	 * 
	 * @return the JDBC connection
	 * @throws ModuleException
	 */
	public Connection getConnection(String databaseName) throws ModuleException {
		Connection connection;
		if (!connections.containsKey(databaseName)) {
			String connectionURL = "jdbc:mysql://" + hostname
					+ (port >= 0 ? ":" + port : "") + "/" + databaseName + "?"
					+ "user=" + username + "&password=" + password;
			try {
				logger.debug("Loading JDBC Driver " + driverClassName);
				Class.forName(driverClassName);
				logger.debug("Getting admin connection");
				connection = DriverManager.getConnection(connectionURL);
				connection.setAutoCommit(true);
				logger.debug("Connected");
				connections.put(databaseName, connection);
			} catch (ClassNotFoundException e) {
				throw new ModuleException(
						"JDBC driver class could not be found", e);
			} catch (SQLException e) {
				throw new ModuleException("SQL error creating connection", e);
			}

		} else {
			connection = connections.get(databaseName);
		}
		return connection;
	}

}
