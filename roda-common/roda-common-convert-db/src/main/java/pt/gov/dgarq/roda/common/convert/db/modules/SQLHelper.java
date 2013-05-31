/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.Row;
import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ColumnStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.ForeignKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.PrimaryKey;
import pt.gov.dgarq.roda.common.convert.db.model.structure.TableStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.ComposedTypeArray;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.ComposedTypeStructure;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBinary;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeBoolean;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeDateTime;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeEnumeration;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeInterval;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericApproximate;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericExact;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;

/**
 * @author Luis Faria
 * 
 */
public class SQLHelper {

	/**
	 * SQL to get all rows from a table
	 * 
	 * @param tableName
	 *            the table name
	 * @return the SQL
	 */
	public String selectTableSQL(String tableName) {
		return "SELECT * FROM " + escapeTableName(tableName);
	}

	/**
	 * SQL to create the database
	 * 
	 * @param dbName
	 *            the database name
	 * @return the SQL
	 */
	public String createDatabaseSQL(String dbName) {
		return "CREATE DATABASE " + escapeDatabaseName(dbName);
	}

	/**
	 * SQL to create a table from table struture
	 * 
	 * @param table
	 *            the table structure
	 * @return the SQL
	 * @throws UnknownTypeException
	 */
	public String createTableSQL(TableStructure table)
			throws UnknownTypeException {
		return "CREATE TABLE "
				+ escapeTableName(table.getName())
				+ " ("
				+ createColumnsSQL(table.getColumns(), table.getPrimaryKey(),
						table.getForeignKeys()) + ")";
	}

	protected String createColumnsSQL(List<ColumnStructure> columns,
			PrimaryKey pkey, List<ForeignKey> fkeys)
			throws UnknownTypeException {
		String ret = "";
		int index = 0;
		for (ColumnStructure column : columns) {
			boolean isPkey = pkey != null
					&& pkey.getColumnNames().contains(column.getName());
			boolean isFkey = false;
			for (ForeignKey fkey : fkeys) {
				isFkey |= fkey.getName().equals(column.getName());
			}
			ret += (index > 0 ? ", " : "")
					+ createColumnSQL(column, isPkey, isFkey);
			index++;
		}
		return ret;
	}

	protected String createColumnSQL(ColumnStructure column,
			boolean isPrimaryKey, boolean isForeignKey)
			throws UnknownTypeException {
		return escapeColumnName(column.getName())
				+ " "
				+ createTypeSQL(column.getType(), isPrimaryKey, isForeignKey)
				+ (column.isNillable() == null || column.isNillable() ? " NULL"
						: " NOT NULL");
	}

	/**
	 * Convert a column type (from model) to the database type. This method
	 * should be overriden for each specific DBMS export module implementation
	 * to use the specific data types.
	 * 
	 * @param type
	 *            the column type
	 * @return the string representation of the database type
	 * @throws UnknownTypeException
	 */
	protected String createTypeSQL(Type type, boolean isPrimaryKey,
			boolean isForeignKey) throws UnknownTypeException {
		String ret;
		if (type instanceof SimpleTypeString) {
			SimpleTypeString string = (SimpleTypeString) type;
			if (string.isLengthVariable()) {
				ret = "varchar(" + string.getLength() + ")";
			} else {
				ret = "char(" + string.getLength() + ")";
			}
		} else if (type instanceof SimpleTypeNumericExact) {
			SimpleTypeNumericExact numericExact = (SimpleTypeNumericExact) type;
			ret = "numeric(" + numericExact.getPrecision() + ","
					+ numericExact.getScale() + ")";
		} else if (type instanceof SimpleTypeNumericApproximate) {
			SimpleTypeNumericApproximate numericApproximate = (SimpleTypeNumericApproximate) type;
			ret = "float(" + numericApproximate.getPrecision() + ")";
		} else if (type instanceof SimpleTypeBoolean) {
			ret = "boolean";
		} else if (type instanceof SimpleTypeDateTime) {
			throw new UnknownTypeException("DateTime type not yet supported");
		} else if (type instanceof SimpleTypeInterval) {
			throw new UnknownTypeException("Interval type not yet supported");
		} else if (type instanceof SimpleTypeEnumeration) {
			throw new UnknownTypeException("Enumeration type not yet supported");
		} else if (type instanceof SimpleTypeBinary) {
			throw new UnknownTypeException("Binary type not yet supported");
		} else if (type instanceof ComposedTypeArray) {
			throw new UnknownTypeException("Array type not yet supported");
		} else if (type instanceof ComposedTypeStructure) {
			throw new UnknownTypeException("Structure type not yet supported");
		} else {
			throw new UnknownTypeException(type.getClass().getName()
					+ " not yet supported");
		}
		return ret;
	}

	/**
	 * SQL to create the primary key, altering the already created table
	 * 
	 * @param tableName
	 *            the name of the table
	 * @param pkey
	 *            the primary key
	 * @return the SQL
	 */
	public String createPrimaryKeySQL(String tableName, PrimaryKey pkey) {
		String ret = null;
		if (pkey != null) {
			ret = "ALTER TABLE " + escapeTableName(tableName)
					+ " ADD PRIMARY KEY (";
			boolean comma = false;
			for (String field : pkey.getColumnNames()) {
				ret += (comma ? ", " : "") + escapeColumnName(field);
				comma = true;
			}
			ret += ")";
		}
		return ret;
	}

	/**
	 * SQL to create a foreign key (relation), altering the already created
	 * table
	 * 
	 * @param tableName
	 *            the name of the table
	 * @param fkey
	 *            the foreign key
	 * @return the SQL
	 */
	public String createForeignKeySQL(String tableName, ForeignKey fkey) {
		return "ALTER TABLE " + escapeTableName(tableName)
				+ " ADD FOREIGN KEY (" + escapeColumnName(fkey.getName())
				+ ") REFERENCES " + escapeTableName(fkey.getRefTable()) + "("
				+ escapeColumnName(fkey.getRefColumn()) + ")";
	}

	/**
	 * Interface of handlers that will transform a cell in SQL
	 * 
	 */
	public interface CellSQLHandler {
		/**
		 * Transform a cell in SQL
		 * 
		 * @param cell
		 *            the cell to transform
		 * @param column
		 *            the column to which this cell belongs to
		 * @return the SQL in a byte array. The byte array is to allow having
		 *         binary directly on the SQL.
		 * @throws InvalidDataException
		 * @throws ModuleException
		 */
		public byte[] createCellSQL(Cell cell, ColumnStructure column)
				throws InvalidDataException, ModuleException;
	}

	/**
	 * SQL to insert row on the table.
	 * 
	 * @param table
	 *            the table structure
	 * @param row
	 *            the row
	 * @param cellSQLHandler
	 *            handler that will transform the cell in SQL
	 * @return the prepared statement SQL
	 * @throws ModuleException
	 * @throws InvalidDataException
	 * 
	 */
	public byte[] createRowSQL(TableStructure table, Row row,
			CellSQLHandler cellSQLHandler) throws InvalidDataException,
			ModuleException {
		ByteArrayOutputStream sqlOut = new ByteArrayOutputStream();
		try {
			sqlOut.write(("INSERT INTO " + escapeTableName(table.getName()) + " VALUES (")
					.getBytes());
			int i = 0;
			Iterator<ColumnStructure> columnIt = table.getColumns().iterator();
			for (Cell cell : row.getCells()) {
				ColumnStructure column = columnIt.next();
				if (i++ > 0) {
					sqlOut.write(", ".getBytes());
				}
				sqlOut.write(cellSQLHandler.createCellSQL(cell, column));
			}
			sqlOut.write(")".getBytes());
		} catch (IOException e) {
			throw new ModuleException("Error creating row SQL", e);
		}
		return sqlOut.toByteArray();
	}

	/**
	 * Prepared SQL statement to insert row on the table.
	 * 
	 * @param table
	 *            the table structure
	 * @return the prepared SQL statement
	 * 
	 */
	public String createRowSQL(TableStructure table) {
		String ret = "INSERT INTO " + escapeTableName(table.getName()) + " VALUES (";
		for (int i = 0; i < table.getColumns().size(); i++) {
			if (i > 0) {
				ret += ", ";
			}
			ret += "?";
		}
		ret += ")";

		return ret;
	}

	protected String escapeDatabaseName(String database) {
		return database;
	}

	protected String escapeTableName(String table) {
		return table;
	}

	protected String escapeColumnName(String column) {
		return column;
	}

}
