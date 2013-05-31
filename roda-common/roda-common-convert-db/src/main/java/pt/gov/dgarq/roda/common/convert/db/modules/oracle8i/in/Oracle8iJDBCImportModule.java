package pt.gov.dgarq.roda.common.convert.db.modules.oracle8i.in;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.in.JDBCImportModule;

/**
 * Microsoft SQL Server JDBC import module.
 * 
 * @author Luis Faria
 */
public class Oracle8iJDBCImportModule extends JDBCImportModule {

	private final Logger logger = Logger
			.getLogger(Oracle8iJDBCImportModule.class);

	/**
	 * Create a new Microsoft SQL Server import module using the default
	 * instance.
	 * 
	 * @param serverName
	 *            the name (host name) of the server
	 * @param database
	 *            the name of the database we'll be accessing
	 * @param username
	 *            the name of the user to use in the connection
	 * @param password
	 *            the password of the user to use in the connection
	 */
	public Oracle8iJDBCImportModule(String serverName, int port,
			String database, String username, String password) {
		super("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:" + username + "/"
				+ password + "@" + serverName + ":" + port + ":" + database);

		logger.info("jdbc:oracle:thin:" + username + "/" + password + "@"
				+ serverName + ":" + port + ":" + database);
	}

}
