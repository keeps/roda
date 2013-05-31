package pt.gov.dgarq.roda.common.convert.db.modules.msAccess.in;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.SimpleCell;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ForeignKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.PrimaryKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.msAccess.MsAccessHelper;
import pt.gov.dgarq.roda.common.convert.db.modules.odbc.in.ODBCImportModule;

/**
 * 
 * @author Luis Faria
 * 
 */
public class MsAccessImportModule extends ODBCImportModule {

	private final Logger logger = Logger.getLogger(MsAccessImportModule.class);

	/**
	 * Create a new Microsoft Access import module
	 * 
	 * @param msAccessFile
	 */
	public MsAccessImportModule(File msAccessFile) {
		super("Driver={Microsoft Access Driver (*.mdb)};DBQ="
				+ msAccessFile.getAbsolutePath(), new MsAccessHelper());
	}

	@Override
	public Connection getConnection() throws SQLException,
			ClassNotFoundException {
		if (connection == null) {
			logger.debug("Loading JDBC Driver " + driverClassName);
			Class.forName(driverClassName);
			logger.debug("Getting connection");
			connection = DriverManager
					.getConnection(connectionURL, "admin", "");
			logger.debug("Connected");
		}
		return connection;
	}

	@Override
	protected PrimaryKey getPrimaryKey(String tableName) throws SQLException,
			UnknownTypeException, ClassNotFoundException {
		String key_colname = null;

		// get the primary key information
		ResultSet rset = getMetadata().getIndexInfo(null, null, tableName,
				true, true);
		while (rset.next()) {
			String idx = rset.getString(6);
			if (idx != null) {
				// Note: index "PrimaryKey" is Access DB specific
				// other db server has diff. index syntax.
				if (idx.equalsIgnoreCase("PrimaryKey")) {
					key_colname = rset.getString(9);
				}
			}
		}

		return new PrimaryKey(Arrays.asList(new String[] { key_colname }));
	}

	@Override
	protected Statement getStatement() throws SQLException,
			ClassNotFoundException {
		if (statement == null) {
			statement = getConnection().createStatement();
		}
		return statement;
	}

	@Override
	protected List<ForeignKey> getForeignKeys(String tableName)
			throws SQLException, UnknownTypeException, ClassNotFoundException {
		List<ForeignKey> fKeys = new Vector<ForeignKey>();

		ResultSet foreignKeys = getStatement()
				.executeQuery(
						"SELECT  szRelationship, szReferencedObject, szColumn, szReferencedColumn FROM MSysRelationships WHERE szObject like '"
								+ tableName + "'");

		while (foreignKeys.next()) {
			// FK name
			String id = foreignKeys.getString(1);
			// if FK has no name, use foreign table name instead
			if (id == null) {
				id = foreignKeys.getString(2);
			}

			// local column
			String name = foreignKeys.getString(3);

			// foreign table
			String refTable = foreignKeys.getString(2);
			// foreign column
			String refColumn = foreignKeys.getString(4);
			fKeys.add(new ForeignKey(tableName + "." + id, name, refTable,
					refColumn));
		}

		return fKeys;
	}

	private final DateFormat accessDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	@Override
	protected Cell convertRawToCell(String tableName, String columnName,
			int columnIndex, int rowIndex, Type cellType, ResultSet rawData)
			throws SQLException, InvalidDataException, ClassNotFoundException,
			ModuleException {
		Cell cell;
		String id = tableName + "." + columnName + "." + rowIndex;
		if (cellType instanceof SimpleTypeDateTime) {
			String dateString = rawData.getString(columnName);
			if (dateString == null) {
				cell = new SimpleCell(null);
			} else {
				try {
					cell = new SimpleCell(id, DateParser
							.getIsoDateNoMillis(accessDateFormat
									.parse(dateString)));
				} catch (ParseException e) {
					throw new InvalidDataException(
							"Invalid date found in cell " + id + ": "
									+ dateString);
				}
			}
		} else {
			cell = super.convertRawToCell(tableName, columnName, columnIndex,
					rowIndex, cellType, rawData);
		}
		return cell;
	}

}
