/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.odbc.in;

import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;
import pt.gov.dgarq.roda.common.convert.db.modules.jdbc.in.JDBCImportModule;

/**
 * @author Luis Faria
 * 
 */
public class ODBCImportModule extends JDBCImportModule {

	/**
	 * @param source
	 */
	public ODBCImportModule(String source) {
		super("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:" + source);
	}

	public ODBCImportModule(String source, String username, String password) {
		super("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:" + source + ";UID="
				+ username + ";PWD=" + password);
	}

	/**
	 * @param source
	 * @param sqlHelper
	 */
	public ODBCImportModule(String source, SQLHelper sqlHelper) {
		super("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:" + source, sqlHelper);
	}
}
