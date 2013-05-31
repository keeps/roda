/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.jdbc.in;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.common.FileFormat;
import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.common.convert.db.model.data.BinaryCell;
import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.FileItem;
import pt.gov.dgarq.roda.common.convert.db.model.data.Row;
import pt.gov.dgarq.roda.common.convert.db.model.data.SimpleCell;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ColumnStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.DatabaseStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ForeignKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.PrimaryKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.TableStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.ComposedTypeArray;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.ComposedTypeStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBinary;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBoolean;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericApproximate;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericExact;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;

/**
 * @author Luis Faria
 * 
 */
public class JDBCImportModule implements DatabaseImportModule {

	// if fetch size is zero, then the driver decides the best fetch size
	private static final int ROW_FETCH_BLOCK_SIZE = 0;

	private final Logger logger = Logger.getLogger(JDBCImportModule.class);

	protected final String driverClassName;

	protected final String connectionURL;

	protected Connection connection;

	protected Statement statement;

	protected DatabaseMetaData dbMetadata;

	protected DatabaseStructure dbStructure;

	protected SQLHelper sqlHelper;

	protected FormatUtility formatUtility;

	/**
	 * Create a new JDBC import module
	 * 
	 * @param driverClassName
	 *            the name of the the JDBC driver class
	 * @param connectionURL
	 *            the connection url to use in the connection
	 */
	public JDBCImportModule(String driverClassName, String connectionURL) {
		this(driverClassName, connectionURL, new SQLHelper());
	}

	protected JDBCImportModule(String driverClassName, String connectionURL,
			SQLHelper sqlHelper) {
		this.driverClassName = driverClassName;
		this.connectionURL = connectionURL;
		this.sqlHelper = sqlHelper;
		connection = null;
		dbMetadata = null;
		dbStructure = null;
	}

	/**
	 * Connect to the server using the properties defined in the constructor, or
	 * return the existing connection
	 * 
	 * @return the connection
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 *             the JDBC driver could not be found in classpath
	 */
	public Connection getConnection() throws SQLException,
			ClassNotFoundException {
		if (connection == null) {
			logger.debug("Loading JDBC Driver " + driverClassName);
			Class.forName(driverClassName);
			logger.debug("Getting connection");
			connection = DriverManager.getConnection(connectionURL);
			logger.debug("Connected");
		}
		return connection;
	}

	protected Statement getStatement() throws SQLException,
			ClassNotFoundException {
		if (statement == null) {
			statement = getConnection().createStatement(
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT);
		}
		return statement;
	}

	/**
	 * Get the database metadata
	 * 
	 * @return the database metadata
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public DatabaseMetaData getMetadata() throws SQLException,
			ClassNotFoundException {
		if (dbMetadata == null) {
			dbMetadata = getConnection().getMetaData();
		}
		return dbMetadata;
	}

	/**
	 * Close current connection
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
			dbMetadata = null;
			dbStructure = null;
		}
	}

	/**
	 * @return the database structure
	 * @throws SQLException
	 * @throws UnknownTypeException
	 *             the original data type is unknown
	 * @throws ClassNotFoundException
	 */
	protected DatabaseStructure getDatabaseStructure() throws SQLException,
			UnknownTypeException, ClassNotFoundException {
		if (dbStructure == null) {
			logger.debug("Importing structure");
			dbStructure = new DatabaseStructure();

			dbStructure.setName(getConnection().getCatalog());
			dbStructure.setProductName(getMetadata().getDatabaseProductName());
			dbStructure.setProductVersion(getMetadata()
					.getDatabaseProductVersion());

			List<TableStructure> tables = new Vector<TableStructure>();
			ResultSet rset = getMetadata().getTables(dbStructure.getName(),
					null, "%", new String[] { "TABLE" });
			while (rset.next()) {
				String tablename = rset.getString(3);
				tables.add(getTableStructure(tablename));
			}
			dbStructure.setTables(tables);
		}
		return dbStructure;
	}

	/**
	 * @param tableName
	 *            the name of the table
	 * @return the table structure
	 * @throws SQLException
	 * @throws UnknownTypeException
	 *             the original data type is unknown
	 * @throws ClassNotFoundException
	 */
	protected TableStructure getTableStructure(String tableName)
			throws SQLException, UnknownTypeException, ClassNotFoundException {

		TableStructure table = new TableStructure();
		table.setId(tableName);
		table.setName(tableName);

		List<ColumnStructure> columns = new Vector<ColumnStructure>();
		ResultSet rs = getMetadata().getColumns(null, null, tableName, "%");
		logger
				.debug(tableName
						+ "Structure: "
						+ "Column Name, Nullable, Data Type, Type Name, Column Size, Decimal Digits, Num Prec Radix, index, Remarks");
		while (rs.next()) {
			String columnName = rs.getString(4);
			String isNullable = rs.getString(18);
			int dataType = rs.getInt(5);
			String typeName = rs.getString(6);
			int columnSize = rs.getInt(7);
			int decimalDigits = rs.getInt(9);
			int numPrecRadix = rs.getInt(10);
			int index = rs.getInt(17);
			String remarks = rs.getString(12);

			logger.debug(tableName + "Column: " + columnName + ", "
					+ isNullable + ", " + dataType + ", " + typeName + ", "
					+ columnSize + ", " + decimalDigits + ", " + numPrecRadix
					+ ", " + index + ", " + remarks);

			Boolean nillable = Boolean.TRUE;
			if (isNullable != null && isNullable.equals("NO")) {
				nillable = Boolean.FALSE;
			}

			Type columnType = getType(dataType, typeName, columnSize,
					decimalDigits, numPrecRadix);

			ColumnStructure column = getColumnStructure(tableName, columnName,
					columnType, nillable, index, remarks);

			columns.add(column);
		}
		table.setColumns(columns);

		table.setPrimaryKey(getPrimaryKey(tableName));
		table.setForeignKeys(getForeignKeys(tableName));

		return table;
	}

	/**
	 * Create the column structure
	 * 
	 * @param tableName
	 *            the name of the table which the column belongs to
	 * @param columnName
	 *            the name of the column
	 * @param type
	 *            the type of the column
	 * @param nillable
	 *            is the column nillable
	 * @param index
	 *            the column index
	 * @param description
	 *            the column description
	 * @return the column structure
	 */
	protected ColumnStructure getColumnStructure(String tableName,
			String columnName, Type type, Boolean nillable, int index,
			String description) {
		ColumnStructure column = new ColumnStructure(tableName + "."
				+ columnName, columnName, type, nillable, description);
		column.setId(tableName + "." + columnName);
		column.setName(columnName);
		column.setType(type);
		column.setNillable(nillable);
		column.setDescription(description);
		return column;
	}

	/**
	 * Map the original type to the normalized type model
	 * 
	 * @param originalDataType
	 *            the name of the original data type
	 * @param originalMaxSize
	 *            the max size of the original data type
	 * @return the normalized type
	 * @throws UnknownTypeException
	 *             the original type is unknown and cannot be mapped
	 */
	protected Type getType(int dataType, String typeName, int columnSize,
			int decimalDigits, int numPrecRadix) throws UnknownTypeException {
		Type type;
		switch (dataType) {
		case Types.BIGINT:
			type = new SimpleTypeNumericExact(Integer.valueOf(columnSize),
					Integer.valueOf(decimalDigits));
			break;
		case Types.BINARY:
			type = new SimpleTypeBinary();
			break;
		case Types.BIT:
			type = new SimpleTypeBoolean();
			break;
		case Types.BLOB:
			type = new SimpleTypeBinary();
			break;
		case Types.BOOLEAN:
			type = new SimpleTypeBoolean();
			break;
		case Types.CHAR:
			type = new SimpleTypeString(Integer.valueOf(columnSize),
					Boolean.FALSE);
			break;
		case Types.CLOB:
			type = new SimpleTypeString(Integer.valueOf(columnSize),
					Boolean.TRUE);
			break;
		case Types.DATE:
			type = new SimpleTypeDateTime(Boolean.FALSE, Boolean.FALSE);
			break;
		case Types.DECIMAL:
			type = new SimpleTypeNumericExact(Integer.valueOf(columnSize),
					Integer.valueOf(decimalDigits));
			break;
		case Types.DOUBLE:
			type = new SimpleTypeNumericApproximate(Integer.valueOf(columnSize));
			break;
		case Types.FLOAT:
			type = new SimpleTypeNumericApproximate(Integer.valueOf(columnSize));
			break;
		case Types.INTEGER:
			type = new SimpleTypeNumericExact(Integer.valueOf(columnSize),
					Integer.valueOf(decimalDigits));
			break;
		case Types.LONGVARBINARY:
			type = new SimpleTypeBinary();
			break;
		case Types.LONGVARCHAR:
			type = new SimpleTypeString(Integer.valueOf(columnSize),
					Boolean.TRUE);
			break;
		case Types.NUMERIC:
			type = new SimpleTypeNumericApproximate(Integer.valueOf(columnSize));
			break;
		case Types.REAL:
			type = new SimpleTypeNumericApproximate(Integer.valueOf(columnSize));
			break;
		case Types.SMALLINT:
			type = new SimpleTypeNumericExact(Integer.valueOf(columnSize),
					Integer.valueOf(decimalDigits));
			break;
		case Types.TIME:
			type = new SimpleTypeDateTime(Boolean.TRUE, Boolean.FALSE);
			break;
		case Types.TIMESTAMP:
			type = new SimpleTypeDateTime(Boolean.TRUE, Boolean.FALSE);
			break;
		case Types.TINYINT:
			type = new SimpleTypeNumericExact(Integer.valueOf(columnSize),
					Integer.valueOf(decimalDigits));
			break;
		case Types.VARBINARY:
			type = new SimpleTypeBinary();
			break;
		case Types.VARCHAR:
			type = new SimpleTypeString(Integer.valueOf(columnSize),
					Boolean.TRUE);
			break;
		case Types.ARRAY:
			// TODO add array type convert support
			throw new UnknownTypeException("Array type not yet supported");

		case Types.STRUCT:
			// TODO add struct type convert support
			throw new UnknownTypeException("Struct type not yet supported");
		default:
			throw new UnknownTypeException("Unsuported JDBC type, code: "
					+ dataType);
		}
		type.setOriginalTypeName(typeName);
		return type;
	}

	/**
	 * Get the table primary key
	 * 
	 * @param tableName
	 *            the name of the table
	 * @return the primary key
	 * @throws SQLException
	 * @throws UnknownTypeException
	 * @throws ClassNotFoundException
	 */
	protected PrimaryKey getPrimaryKey(String tableName) throws SQLException,
			UnknownTypeException, ClassNotFoundException {
		List<String> pkColumns = new Vector<String>();

		ResultSet rs = getMetadata().getPrimaryKeys(
				getDatabaseStructure().getName(), null, tableName);
		while (rs.next()) {
			pkColumns.add(rs.getString(4));
		}
		return new PrimaryKey(pkColumns);
	}

	/**
	 * Get the table foreign keys
	 * 
	 * @param tableName
	 *            the name of the table
	 * @return the foreign keys
	 * @throws SQLException
	 * @throws UnknownTypeException
	 * @throws ClassNotFoundException
	 */
	protected List<ForeignKey> getForeignKeys(String tableName)
			throws SQLException, UnknownTypeException, ClassNotFoundException {
		List<ForeignKey> foreignKeys = new Vector<ForeignKey>();

		ResultSet rs = getMetadata().getImportedKeys(
				getDatabaseStructure().getName(), null, tableName);
		while (rs.next()) {
			String name = rs.getString(8);
			String refTable = rs.getString(3);
			String refColumn = rs.getString(4);
			ForeignKey fk = new ForeignKey(tableName + "." + name, name,
					refTable, refColumn);
			foreignKeys.add(fk);
		}
		return foreignKeys;
	}

	protected Row convertRawToRow(ResultSet rawData,
			TableStructure tableStructure) throws InvalidDataException,
			SQLException, ClassNotFoundException, ModuleException {
		Row row = null;
		if (isRowValid(rawData, tableStructure)) {
			List<Cell> cells = new ArrayList<Cell>(tableStructure.getColumns()
					.size());
			for (int i = 0; i < tableStructure.getColumns().size(); i++) {
				ColumnStructure colStruct = tableStructure.getColumns().get(i);
				cells.add(convertRawToCell(tableStructure.getName(), colStruct
						.getName(), i + 1, rawData.getRow(), colStruct
						.getType(), rawData));
			}
			row = new Row(rawData.getRow(), cells);
		}
		return row;
	}

	protected Cell convertRawToCell(String tableName, String columnName,
			int columnIndex, int rowIndex, Type cellType, ResultSet rawData)
			throws SQLException, InvalidDataException, ClassNotFoundException,
			ModuleException {
		Cell cell;
		String id = tableName + "." + columnName + "." + rowIndex;
		if (cellType instanceof ComposedTypeArray) {
			// Array array = rawData.getArray(index);
			// TODO convert array to cell
			throw new InvalidDataException(
					"Convert data of array type not yet supported");
		} else if (cellType instanceof ComposedTypeStructure) {
			// TODO get structure and convert to cell
			throw new InvalidDataException(
					"Convert data of struct type not yet supported");
		} else if (cellType instanceof SimpleTypeBoolean) {
			cell = new SimpleCell(id, rawData.getBoolean(columnName) ? "true"
					: "false");
		} else if (cellType instanceof SimpleTypeDateTime) {
			Date date = rawData.getDate(columnName);
			if (date != null) {
				String isoDate = DateParser.getIsoDate(date);
				cell = new SimpleCell(id, isoDate);
			} else {
				cell = new SimpleCell(id, null);
			}
		} else if (cellType instanceof SimpleTypeBinary) {
			InputStream binaryStream = rawData.getBinaryStream(columnName);
			FileItem fileItem = new FileItem(binaryStream);
			FileFormat fileFormat = FormatUtility.getFileFormat(fileItem
					.getFile());
			List<FileFormat> formats = new ArrayList<FileFormat>();
			formats.add(fileFormat);
			cell = new BinaryCell(id, fileItem, formats);
		} else {
			cell = new SimpleCell(id, rawData.getString(columnName));
		}
		return cell;
	}

	protected boolean isRowValid(ResultSet raw, TableStructure structure)
			throws InvalidDataException, SQLException {
		boolean ret;
		ResultSetMetaData metadata = raw.getMetaData();
		if (metadata.getColumnCount() == structure.getColumns().size()) {
			ret = true;
		} else {
			ret = false;
			throw new InvalidDataException("table: " + structure.getName()
					+ " row number: " + raw.getRow()
					+ " error: different column number from structure "
					+ metadata.getColumnCount() + "!="
					+ structure.getColumns().size());
		}
		return ret;
	}

	protected ResultSet getTableRawData(String tableName) throws SQLException,
			ClassNotFoundException {
		ResultSet set = getStatement().executeQuery(
				sqlHelper.selectTableSQL(tableName));
		set.setFetchSize(ROW_FETCH_BLOCK_SIZE);
		return set;
	}

	public void getDatabase(DatabaseHandler handler) throws ModuleException,
			UnknownTypeException, InvalidDataException {
		try {
			logger.debug("initializing database");
			handler.initDatabase();
			logger.debug("getting database structure");
			handler.handleStructure(getDatabaseStructure());
			for (TableStructure table : getDatabaseStructure().getTables()) {
				logger.debug("getting data of table " + table.getId());
				handler.handleDataOpenTable(table.getId());
				ResultSet tableRawData = getTableRawData(table.getName());
				while (tableRawData.next()) {
					handler.handleDataRow(convertRawToRow(tableRawData, table));
				}
				handler.handleDataCloseTable(table.getId());
			}
			logger.debug("finishing database");
			handler.finishDatabase();
		} catch (SQLException e) {
			throw new ModuleException("SQL error while conecting", e);
		} catch (ClassNotFoundException e) {
			throw new ModuleException("JDBC driver class could not be found", e);
		}
	}

}
