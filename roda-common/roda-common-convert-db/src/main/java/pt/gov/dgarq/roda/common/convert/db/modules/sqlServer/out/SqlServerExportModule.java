/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.sqlServer.out;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.SimpleCell;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.out.JDBCExportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.sqlServer.SQLServerHelper;

/**
 * @author Luis Faria
 * 
 */
public class SqlServerExportModule extends JDBCExportModule {
	/**
	 * Create a new Microsoft SQL Server export module using the default
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
	 * @param integratedSecurity
	 *            true to use windows login, false to use SQL Server login
	 * @param encrypt
	 *            true to use encryption in the connection
	 */
	public SqlServerExportModule(String serverName, String database,
			String username, String password, boolean integratedSecurity,
			boolean encrypt) {
		super("com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://" + serverName + ";database=" + database
						+ ";integratedSecurity="
						+ (integratedSecurity ? "true" : "false") + ";encrypt="
						+ (encrypt ? "true" : "false"), new SQLServerHelper());

	}

	/**
	 * Create a new Microsoft SQL Server export module using the instance name.
	 * The constructor using the port number is preferred over this to avoid a
	 * round-trip to the server to discover the instance port number.
	 * 
	 * @param serverName
	 *            the name (host name) of the server
	 * @param instanceName
	 *            the name of the instance
	 * @param database
	 *            the name of the database we'll be accessing
	 * @param username
	 *            the name of the user to use in the connection
	 * @param password
	 *            the password of the user to use in the connection
	 * @param integratedSecurity
	 *            true to use windows login, false to use SQL Server login
	 * @param encrypt
	 *            true to use encryption in the connection
	 */
	public SqlServerExportModule(String serverName, String instanceName,
			String database, String username, String password,
			boolean integratedSecurity, boolean encrypt) {
		super("com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://" + serverName + "\\" + instanceName
						+ ";database=" + database + ";user=" + username
						+ ";password=" + password + ";integratedSecurity="
						+ (integratedSecurity ? "true" : "false") + ";encrypt="
						+ (encrypt ? "true" : "false"), new SQLServerHelper());

	}

	/**
	 * Create a new Microsoft SQL Server export module using the port number.
	 * 
	 * @param serverName
	 *            the name (host name) of the server
	 * @param portNumber
	 *            the port number of the server instance, default is 1433
	 * @param database
	 *            the name of the database we'll be accessing
	 * @param username
	 *            the name of the user to use in the connection
	 * @param password
	 *            the password of the user to use in the connection
	 * @param integratedSecurity
	 *            true to use windows login, false to use SQL Server login
	 * @param encrypt
	 *            true to use encryption in the connection
	 */
	public SqlServerExportModule(String serverName, int portNumber,
			String database, String username, String password,
			boolean integratedSecurity, boolean encrypt) {
		super("com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"jdbc:sqlserver://" + serverName + ":" + portNumber
						+ ";database=" + database + ";user=" + username
						+ ";password=" + password + ";integratedSecurity="
						+ (integratedSecurity ? "true" : "false") + ";encrypt="
						+ (encrypt ? "true" : "false"), new SQLServerHelper());

	}

	protected CleanResourcesInterface handleDataCell(PreparedStatement ps,
			int index, Cell cell, Type type) throws InvalidDataException,
			ModuleException {
		CleanResourcesInterface ret = new CleanResourcesInterface() {
			public void clean() {

			}
		};
		if (cell instanceof SimpleCell && type instanceof SimpleTypeDateTime) {
			SimpleCell simple = (SimpleCell) cell;
			String data = simple.getSimpledata();
			try {
				if (data != null) {
					ps.setString(index, data);
				} else {
					ps.setNull(index, Types.CHAR);
				}
			} catch (SQLException e) {
				throw new ModuleException("SQL error while handling cell "
						+ cell.getId(), e);
			}

		} else {
			super.handleDataCell(ps, index, cell, type);
		}
		return ret;
	}
}
