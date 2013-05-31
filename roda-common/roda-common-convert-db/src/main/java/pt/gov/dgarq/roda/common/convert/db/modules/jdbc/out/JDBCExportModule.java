/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.jdbc.out;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.common.convert.db.model.data.BinaryCell;
import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.ComposedCell;
import pt.gov.dgarq.roda.common.convert.db.model.data.Row;
import pt.gov.dgarq.roda.common.convert.db.model.data.SimpleCell;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ColumnStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.DatabaseStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ForeignKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.TableStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBinary;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBoolean;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericApproximate;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericExact;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.common.convert.db.modules.SQLHelper;

/**
 * @author Luis Faria
 * 
 */
public class JDBCExportModule implements DatabaseHandler {

	protected static int BATCH_SIZE = 100;

	private final Logger logger = Logger.getLogger(JDBCExportModule.class);

	protected final String driverClassName;

	protected final String connectionURL;

	protected Connection connection;

	protected Statement statement;

	protected DatabaseStructure databaseStructure;

	protected TableStructure currentTableStructure;

	protected SQLHelper sqlHelper;

	protected int batch_index;

	protected PreparedStatement currentRowInsertStatement;

	/**
	 * Generic JDBC export module constructor
	 * 
	 * @param driverClassName
	 *            the name of the JDBC driver class
	 * @param connectionURL
	 *            the URL to use in connection
	 */
	public JDBCExportModule(String driverClassName, String connectionURL) {
		this(driverClassName, connectionURL, new SQLHelper());
	}

	/**
	 * Generic JDBC export module constructor with SQLHelper definition
	 * 
	 * @param driverClassName
	 *            the name of the JDBC driver class
	 * @param connectionURL
	 *            the URL to use in connection
	 * @param sqlHelper
	 *            the SQLHelper instance to use
	 */
	public JDBCExportModule(String driverClassName, String connectionURL,
			SQLHelper sqlHelper) {
		this.driverClassName = driverClassName;
		this.connectionURL = connectionURL;
		this.sqlHelper = sqlHelper;
		connection = null;
		statement = null;
		databaseStructure = null;
		currentTableStructure = null;
		batch_index = 0;
		currentRowInsertStatement = null;
	}

	/**
	 * Connect to the server using the properties defined in the constructor, or
	 * return the existing connection
	 * 
	 * @return the connection
	 * 
	 * @throws ModuleException
	 *             This exception can be thrown if the JDBC driver class is not
	 *             found or an SQL error occurs while connecting
	 */
	public Connection getConnection() throws ModuleException {
		if (connection == null) {
			try {
				logger.debug("Loading JDBC Driver " + driverClassName);
				Class.forName(driverClassName);
				logger.debug("Getting connection");
				connection = DriverManager.getConnection(connectionURL);
				connection.setAutoCommit(true);
				logger.debug("Connected");
			} catch (ClassNotFoundException e) {
				throw new ModuleException(
						"JDBC driver class could not be found", e);
			} catch (SQLException e) {
				throw new ModuleException("SQL error creating connection", e);
			}

		}
		return connection;
	}

	protected Statement getStatement() throws ModuleException {
		if (statement == null && getConnection() != null) {
			try {
				statement = getConnection().createStatement();
			} catch (SQLException e) {
				throw new ModuleException("SQL error creating statement", e);
			}
		}
		return statement;
	}

	public void initDatabase() throws ModuleException {

	}

	public void handleStructure(DatabaseStructure structure)
			throws ModuleException, UnknownTypeException {
		this.databaseStructure = structure;
		createDatabase(structure.getName());
		int[] batchResult = null;
		if (getStatement() != null) {
			try {
				logger.debug("Handling database structure");
				for (TableStructure table : structure.getTables()) {
					logger.debug("Adding to batch creation of table "
							+ table.getName());
					logger.trace("sql: " + sqlHelper.createTableSQL(table));
					getStatement().addBatch(sqlHelper.createTableSQL(table));
					String pkeySQL = sqlHelper.createPrimaryKeySQL(table
							.getName(), table.getPrimaryKey());
					if (pkeySQL != null) {
						logger.trace("sql: " + pkeySQL);
						getStatement().addBatch(pkeySQL);
					}
				}
				logger.debug("Executing table creation batch");
				batchResult = getStatement().executeBatch();
				getStatement().clearBatch();

			} catch (SQLException e) {
				if (batchResult != null) {
					for (int i = 0; i < batchResult.length; i++) {
						int result = batchResult[i];
						if (result == Statement.EXECUTE_FAILED) {
							logger.error("Batch failed at index " + i);
						}
					}
				}
				SQLException ei = e;
				do {
					if (ei != null) {
						logger
								.error(
										"Error creating structure (next exception)",
										ei);
					}
					ei = ei.getNextException();
				} while (ei != null);
				throw new ModuleException("Error creating structure", e);

			}
		}
	}

	/**
	 * Override this method to create the database
	 * 
	 * @param dbName
	 *            the database name
	 * @throws ModuleException
	 */
	protected void createDatabase(String dbName) throws ModuleException {
		// nothing will be done by default
	}

	public void handleDataOpenTable(String tableId) throws ModuleException {
		if (databaseStructure != null) {
			for (TableStructure table : databaseStructure.getTables()) {
				if (table.getId().equals(tableId)) {
					currentTableStructure = table;
					try {
						currentRowInsertStatement = getConnection()
								.prepareStatement(sqlHelper.createRowSQL(table));
					} catch (SQLException e) {
						throw new ModuleException("Error creating table "
								+ tableId + " prepared statement", e);
					}
				}
			}
			if (currentTableStructure == null) {
				throw new ModuleException("Could not find table id '" + tableId
						+ "' in database structure");
			}
		} else if (databaseStructure != null) {
			throw new ModuleException(
					"Cannot open table before database structure is created");
		}
	}

	public void handleDataCloseTable(String tableId) throws ModuleException {
		currentTableStructure = null;
		if (batch_index > 0) {
			try {
				currentRowInsertStatement.executeBatch();
			} catch (SQLException e) {
				throw new ModuleException("Error executing insert batch", e);
			}
			batch_index = 0;
			currentRowInsertStatement = null;
		}
	}

	public void handleDataRow(Row row) throws InvalidDataException,
			ModuleException {
		if (currentTableStructure != null && currentRowInsertStatement != null) {
			Iterator<ColumnStructure> columnIterator = currentTableStructure
					.getColumns().iterator();
			List<CleanResourcesInterface> cleanResourcesList = new ArrayList<CleanResourcesInterface>();
			int index = 1;
			for (Cell cell : row.getCells()) {
				ColumnStructure column = columnIterator.next();
				CleanResourcesInterface cleanResources = handleDataCell(
						currentRowInsertStatement, index, cell, column
								.getType());
				cleanResourcesList.add(cleanResources);
				index++;
			}
			try {
				currentRowInsertStatement.addBatch();
				if (++batch_index > BATCH_SIZE) {
					currentRowInsertStatement.executeBatch();
					currentRowInsertStatement.clearBatch();
					batch_index = 0;
				}
			} catch (SQLException e) {
				throw new ModuleException("Error executing insert batch", e);
			} finally {
				for (CleanResourcesInterface clean : cleanResourcesList) {
					clean.clean();
				}
			}

		} else if (databaseStructure != null) {
			throw new ModuleException(
					"Cannot handle data row before a table is open and insert statement created");
		}

	}

	public interface CleanResourcesInterface {
		public void clean();
	}

	protected CleanResourcesInterface handleDataCell(PreparedStatement ps,
			int index, Cell cell, Type type) throws InvalidDataException,
			ModuleException {
		CleanResourcesInterface ret = new CleanResourcesInterface() {
			public void clean() {

			}
		};
		try {
			if (cell instanceof SimpleCell) {
				SimpleCell simple = (SimpleCell) cell;
				String data = simple.getSimpledata();
				if (type instanceof SimpleTypeString) {
					if (data != null) {
						ps.setString(index, data);

					} else {
						ps.setNull(index, Types.VARCHAR);
					}
				} else if (type instanceof SimpleTypeNumericExact) {
					if (data != null) {
						ps.setInt(index, Integer.valueOf(data));
					} else {
						ps.setNull(index, Types.INTEGER);
					}
				} else if (type instanceof SimpleTypeNumericApproximate) {
					if (data != null) {
						ps.setFloat(index, Float.valueOf(data));
					} else {
						ps.setNull(index, Types.FLOAT);
					}

				} else if (type instanceof SimpleTypeDateTime) {
					// SimpleTypeDateTime dateTime = (SimpleTypeDateTime) type;
					if (data != null) {
						Date date = DateParser.parse(data);
						java.sql.Date sqlDate = new java.sql.Date(date
								.getTime());
						ps.setDate(index, sqlDate);
					} else {
						ps.setNull(index, Types.DATE);
					}

				} else if (type instanceof SimpleTypeBoolean) {
					if (data != null) {
						ps.setBoolean(index, Boolean.valueOf(data));
					} else {
						ps.setNull(index, Types.BOOLEAN);
					}

				} else {
					throw new InvalidDataException(
							type.getClass().getSimpleName()
									+ " not applicable to simple cell or not yet supported");
				}
			} else if (cell instanceof BinaryCell) {
				final BinaryCell bin = (BinaryCell) cell;
				if (!(type instanceof SimpleTypeBinary)) {
					logger.error("Binary cell found when column type is "
							+ type.getClass().getSimpleName());
				}
				ps.setBinaryStream(index, bin.getInputstream(), (int) bin
						.getLength());

				ret = new CleanResourcesInterface() {

					public void clean() {
						bin.cleanResources();
					}

				};

			} else if (cell instanceof ComposedCell) {
				// ComposedCell comp = (ComposedCell) cell;
				// TODO export composed data
				throw new ModuleException("Composed data not yet supported");
			} else {
				throw new ModuleException("Unsuported cell type "
						+ cell.getClass().getName());
			}
		} catch (SQLException e) {
			throw new ModuleException("SQL error while handling cell "
					+ cell.getId(), e);
		} catch (InvalidDateException e) {
			throw new InvalidDataException("Error handling cell "
					+ cell.getId() + ":" + e.getMessage());
		}
		return ret;
	}

	public void finishDatabase() throws ModuleException {
		if (databaseStructure != null) {
			handleForeignKeys();
			commit();
		}
	}

	protected void handleForeignKeys() throws ModuleException {
		logger.debug("Creating foreign keys");
		try {
			for (TableStructure table : databaseStructure.getTables()) {
				for (ForeignKey fkey : table.getForeignKeys()) {
					String fkeySQL = sqlHelper.createForeignKeySQL(table
							.getName(), fkey);
					getStatement().addBatch(fkeySQL);
				}
			}
			getStatement().executeBatch();
			getStatement().clearBatch();
		} catch (SQLException e) {
			throw new ModuleException("Error creating foreign keys", e);
		}
	}

	protected void commit() throws ModuleException {
		// logger.debug("Commiting");
		// try {
		// getConnection().commit();
		// } catch (SQLException e) {
		// throw new ModuleException("Error while commiting", e);
		// }
	}

	/**
	 * Get the SQLHelper used by this instance
	 * 
	 * @return the SQLHelper
	 */
	public SQLHelper getSqlHelper() {
		return sqlHelper;
	}
}
