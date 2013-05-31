/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.dbml.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.transaction.util.FileHelper;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.common.FileFormat;
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
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.util.XmlEncodeUtility;

/**
 * @author Luis Faria
 * 
 */
public class DBMLExportModule implements DatabaseHandler {

	private final Logger logger = Logger.getLogger(DBMLExportModule.class);

	/**
	 * Default name for the DBML file
	 */
	public static final String DEFAULT_DBML_FILE_NAME = "DBML.xml";

	private static final String SCHEMA_VERSION = "0.2";

	private static final String ENCODING = "UTF-8";

	/**
	 * Interface to control binary files creation
	 * 
	 * @author Luis Faria
	 * 
	 */
	public interface DBMLBinaryCreate {
		/**
		 * Create the path to the binary cell to insert in the DBML file
		 * 
		 * @param bin
		 * @return the file name
		 */
		public String createBinaryPath(BinaryCell bin);

		/**
		 * Create the file where to export the binary
		 * 
		 * @param baseDir
		 *            the base directory
		 * @param bin
		 *            the binary cell
		 * @param path
		 *            the path given by this interface
		 * @return the file where the binary will be dumped to
		 */
		public File createBinaryFile(File baseDir, BinaryCell bin, String path);
	}

	/**
	 * The default binary creating interface
	 */
	public static DBMLBinaryCreate DEFAULT_BINARY_CREATE = new DBMLBinaryCreate() {

		public File createBinaryFile(File baseDir, BinaryCell bin, String path) {
			return new File(baseDir, path);
		}

		public String createBinaryPath(BinaryCell bin) {
			String filename = bin.getId();
			if (bin.getFormatHits() != null && bin.getFormatHits().size() > 0
					&& bin.getFormatHits().get(0) != null
					&& bin.getFormatHits().get(0).getExtensions() != null
					&& bin.getFormatHits().get(0).getExtensions().size() > 0) {
				filename += "."
						+ bin.getFormatHits().get(0).getExtensions().get(0);

			} else {
				filename += ".bin";
			}
			return filename;
		}

	};

	private final File baseDir;

	private final DBMLBinaryCreate dbmlBinaryCreate;

	private BufferedWriter dbmlOutput;

	private boolean dataOpened;

	/**
	 * Create a new DBML export module which will export the database to
	 * directory
	 * 
	 * @param baseDir
	 *            the base directory to export the DBML and files
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public DBMLExportModule(File baseDir) throws FileNotFoundException,
			UnsupportedEncodingException {
		this(baseDir, DEFAULT_DBML_FILE_NAME);
	}

	/**
	 * Create a new DBML export module which will export the database to
	 * directory
	 * 
	 * @param baseDir
	 *            the base directory to export the DBML and files
	 * @param dbmlFileName
	 *            the name of the DBML file
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public DBMLExportModule(File baseDir, String dbmlFileName)
			throws FileNotFoundException, UnsupportedEncodingException {
		this(baseDir, dbmlFileName, DEFAULT_BINARY_CREATE);
	}

	/**
	 * Create a new DBML export module which will export the database to
	 * directory
	 * 
	 * @param baseDir
	 *            the base directory to export the DBML and files
	 * @param dbmlFileName
	 *            the name of the DBML file
	 * @param dbmlBinaryCreate
	 *            the interface to use when creating binary cells
	 * 
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public DBMLExportModule(File baseDir, String dbmlFileName,
			DBMLBinaryCreate dbmlBinaryCreate) throws FileNotFoundException,
			UnsupportedEncodingException {
		this.baseDir = baseDir;
		if (!baseDir.exists()) {
			baseDir.mkdir();
		}
		if (!baseDir.isDirectory()) {
			baseDir.delete();
			baseDir.mkdir();
		}

		this.dbmlBinaryCreate = dbmlBinaryCreate;

		dbmlOutput = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(baseDir.getAbsolutePath() + "/"
						+ dbmlFileName)), ENCODING));

		dataOpened = false;

	}

	public void handleStructure(DatabaseStructure structure)
			throws ModuleException, UnknownTypeException {
		try {
			exportStructure(structure);
		} catch (UnsupportedEncodingException e) {
			throw new ModuleException("Error exporting structure", e);
		} catch (IOException e) {
			throw new ModuleException("Error exporting structure", e);
		}

	}

	public void handleDataOpenTable(String tableId) throws ModuleException {
		try {
			if (!dataOpened) {
				exportOpenData();
				dataOpened = true;
				logger.debug("Exporting data");
			}
			exportOpenTableData(tableId);
		} catch (IOException e) {
			throw new ModuleException("Error opening table " + tableId, e);
		}

	}

	public void handleDataCloseTable(String tableId) throws ModuleException {
		try {
			exportCloseTableData();
		} catch (IOException e) {
			throw new ModuleException("Error closing table " + tableId, e);
		}

	}

	public void handleDataRow(Row row) throws InvalidDataException,
			ModuleException {
		try {
			exportRowData(row);
		} catch (IOException e) {
			throw new ModuleException("Error exporting row " + row.getIndex(),
					e);
		}

	}

	public void initDatabase() throws ModuleException {
		try {
			print("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>\n");
		} catch (IOException e) {
			throw new ModuleException("Error writing file", e);
		}

	}

	public void finishDatabase() throws ModuleException {
		try {
			exportCloseData();
			print("</db>");
			finishPrinting();
		} catch (IOException e) {
			throw new ModuleException("Error writing file", e);
		}

	}

	/**
	 * Export the database structure to DBML
	 * 
	 * @param structure
	 *            the database structure
	 * @throws UnknownTypeException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void exportStructure(DatabaseStructure structure)
			throws UnknownTypeException, UnsupportedEncodingException,
			IOException {
		logger.debug("Exporting structure");
		print("<db name=\"" + encode(structure.getName()) + "\"");

		if (structure.getCreationDate() != null) {
			print(" creationDate=\"" + encode(structure.getCreationDate())
					+ "\"");
		}
		print(" exportDate=\"" + getCurrentDate() + "\"");
		if (structure.getProductName() != null) {
			print(" productName=\"" + encode(structure.getProductName()) + "\"");
		}
		if (structure.getProductVersion() != null) {
			print(" productVersion=\"" + encode(structure.getProductVersion())
					+ "\"");
		}
		if (structure.getDefaultTransactionIsolationLevel() != null) {
			print(" defaultTransactionIsolationLevel=\""
					+ structure.getDefaultTransactionIsolationLevel() + "\"");
		}
		if (structure.getExtraNameCharacters() != null) {
			print(" extraNameCharacters=\""
					+ encode(structure.getExtraNameCharacters()) + "\"");
		}
		if (structure.getStringFunctions() != null) {
			print(" stringFunctions=\""
					+ encode(structure.getStringFunctions()) + "\"");
		}
		if (structure.getSystemFunctions() != null) {
			print(" systemFunctions=\""
					+ encode(structure.getSystemFunctions()) + "\"");
		}
		if (structure.getTimeDateFunctions() != null) {
			print(" timeDateFunctions=\""
					+ encode(structure.getTimeDateFunctions()) + "\"");
		}
		if (structure.getUrl() != null) {
			print(" url=\"" + encode(structure.getUrl()) + "\"");
		}
		if (structure.getSupportsANSI92EntryLevelSQL() != null) {
			print(" supportsANSI92EntryLevelSQL=\""
					+ structure.getSupportsANSI92EntryLevelSQL() + "\"");
		}
		if (structure.getSupportsANSI92IntermediateSQL() != null) {
			print(" supportsANSI92IntermediateSQL=\""
					+ structure.getSupportsANSI92IntermediateSQL() + "\"");
		}
		if (structure.getSupportsANSI92FullSQL() != null) {
			print(" supportsANSI92FullSQL=\""
					+ structure.getSupportsANSI92FullSQL() + "\"");
		}
		if (structure.getSupportsCoreSQLGrammar() != null) {
			print(" supportsCoreSQLGrammar=\""
					+ structure.getSupportsCoreSQLGrammar() + "\"");
		}
		print(" schemaVersion=\"" + SCHEMA_VERSION + "\">\n");
		print("\t<structure>\n");
		for (TableStructure table : structure.getTables()) {
			exportTableStructure(table);
		}
		print("\t</structure>\n");
	}

	/**
	 * Export table structure to DBML
	 * 
	 * @param table
	 *            the table structure
	 * @throws UnknownTypeException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void exportTableStructure(TableStructure table)
			throws UnknownTypeException, UnsupportedEncodingException,
			IOException {
		print("\t\t<table id=\"" + encode(table.getId()) + "\" name=\""
				+ encode(table.getName()) + "\"");
		if (table.getDescription() != null) {
			print(" description=\"" + encode(table.getDescription()) + "\"");
		}
		print(">\n");

		print("\t\t\t<columns>\n");
		for (ColumnStructure column : table.getColumns()) {
			exportColumnStructure(column);
		}
		print("\t\t\t</columns>\n");

		print("\t\t\t<keys>\n");
		exportPrimaryKey(table.getPrimaryKey());

		for (ForeignKey fk : table.getForeignKeys()) {
			exportForeignKey(fk);
		}
		print("\t\t\t</keys>\n");

		print("\t\t</table>\n");
	}

	private void exportColumnStructure(ColumnStructure column)
			throws UnknownTypeException, UnsupportedEncodingException,
			IOException {
		print("\t\t\t<column id=\"" + encode(column.getId()) + "\" name=\""
				+ encode(column.getName()) + "\"");
		if (column.isNillable() != null) {
			print(" nillable=\"" + column.isNillable() + "\"");
		}
		if (column.getDescription() != null) {
			print(" description=\"" + encode(column.getDescription()) + "\"");
		}
		print(">\n");
		print("\t\t\t\t<type");
		if (column.getType().getOriginalTypeName() != null) {
			print(" originalTypeName=\""
					+ encode(column.getType().getOriginalTypeName()) + "\"");
		}
		if (column.getType().getDescription() != null) {
			print(" description=\"" + encode(column.getType().getDescription())
					+ "\"");
		}
		print(">\n");
		exportType(column.getType());
		print("\t\t\t\t</type>\n");
		print("\t\t\t</column>\n");

	}

	private void exportType(Type type) throws UnknownTypeException,
			UnsupportedEncodingException, IOException {
		print("\t\t\t\t\t");
		if (type instanceof SimpleTypeString) {
			SimpleTypeString stringType = (SimpleTypeString) type;
			print("<simpleTypeString");
			print(" length=\"" + stringType.getLength() + "\"");
			print(" variableLegth=\"" + stringType.isLengthVariable() + "\"");
			if (stringType.getCharset() != null) {
				print(" charSet=\"" + encode(stringType.getCharset()) + "\"");
			}
			print("/>\n");
		} else if (type instanceof SimpleTypeNumericExact) {
			SimpleTypeNumericExact numExactType = (SimpleTypeNumericExact) type;
			print("<simpleTypeNumericExact");
			if (numExactType.getPrecision() != null) {
				print(" precision=\"" + numExactType.getPrecision() + "\"");
			}
			if (numExactType.getScale() != null) {
				print(" scale=\"" + numExactType.getScale() + "\"");
			}
			print("/>\n");

		} else if (type instanceof SimpleTypeNumericApproximate) {
			SimpleTypeNumericApproximate numApproxType = (SimpleTypeNumericApproximate) type;
			print("<simpleTypeNumericApproximate");
			if (numApproxType.getPrecision() != null) {
				print(" precision=\"" + numApproxType.getPrecision() + "\"");
			}
			print("/>\n");

		} else if (type instanceof SimpleTypeBoolean) {
			print("<simpleTypeBoolean/>\n");

		} else if (type instanceof SimpleTypeEnumeration) {
			SimpleTypeEnumeration enumeration = (SimpleTypeEnumeration) type;
			print("<simpleTypeEnumeration>\n");
			for (String option : enumeration.getOptions()) {
				print("\t\t\t\t\t\t<simpleTypeEnumerationOption" + " name=\""
						+ encode(option) + "\"/>\n");
			}
			print("\t\t\t\t\t</simpleTypeEnumeration>\n");

		} else if (type instanceof SimpleTypeDateTime) {
			SimpleTypeDateTime dateTimeType = (SimpleTypeDateTime) type;
			print("<simpleTypeDateTime");
			print(" timeDefined=\"" + dateTimeType.getTimeDefined() + "\"");
			print(" timeZoneDefined=\"" + dateTimeType.getTimeZoneDefined()
					+ "\"");

			print("/>\n");

		} else if (type instanceof SimpleTypeInterval) {
			SimpleTypeInterval interval = (SimpleTypeInterval) type;
			print("<simpleTypeInterval");

			if (interval.getType() == SimpleTypeInterval.IntervalType.STARTDATE_ENDDATE) {
				print(" type=\"START_END\"");
			} else if (interval.getType() == SimpleTypeInterval.IntervalType.STARTDATE_DURATION) {
				print(" type=\"START_DURATION\"");
			} else if (interval.getType() == SimpleTypeInterval.IntervalType.DURATION_ENDDATE) {
				print(" type=\"DURATION_END\"");
			} else if (interval.getType() == SimpleTypeInterval.IntervalType.DURATION) {
				print(" type=\"DURATION\"");
			}

			print("/>\n");

		} else if (type instanceof SimpleTypeBinary) {
			SimpleTypeBinary binary = (SimpleTypeBinary) type;
			print("<simpleTypeBinary");
			if (binary.getFormatRegistryName() != null) {
				print(" formatRegistryName=\""
						+ encode(binary.getFormatRegistryName()) + "\"");
			}
			if (binary.getFormatRegistryKey() != null) {
				print(" formatRegistryKey=\""
						+ encode(binary.getFormatRegistryKey()) + "\"");
			}
			print("/>\n");
		} else if (type instanceof ComposedTypeArray) {
			ComposedTypeArray array = (ComposedTypeArray) type;
			print("<composedTypeArray>\n");
			exportType(array.getElementType());
			print("\t\t\t\t\t</composedTypeArray>\n");

		} else if (type instanceof ComposedTypeStructure) {
			ComposedTypeStructure struct = (ComposedTypeStructure) type;
			print("<composedTypeStructure>\n");
			for (Type elementType : struct.getElements()) {
				exportType(elementType);
			}
			print("\t\t\t\t\t</composedTypeStructure>\n");
		} else {
			throw new UnknownTypeException(type.toString());
		}

	}

	private void exportPrimaryKey(PrimaryKey primaryKey)
			throws UnsupportedEncodingException, IOException {
		if (primaryKey != null && primaryKey.getColumnNames().size() > 0) {
			print("\t\t\t<pkey type=\""
					+ (primaryKey.getColumnNames().size() == 1 ? "SIMPLE"
							: "COMPOSITE") + "\">\n");
			for (String column : primaryKey.getColumnNames()) {
				print("\t\t\t\t<field name=\"" + encode(column) + "\"/>\n");
			}
			print("\t\t\t</pkey>\n");
		}

	}

	private void exportForeignKey(ForeignKey fk)
			throws UnsupportedEncodingException, IOException {
		print("\t\t\t<fkey id=\"" + encode(fk.getId()) + "\" name=\""
				+ encode(fk.getName()) + "\" in=\"" + encode(fk.getRefTable())
				+ "\" ref=\"" + encode(fk.getRefColumn()) + "\"/>\n");

	}

	private void exportOpenData() throws IOException {
		print("\t<data>\n");
	}

	private void exportCloseData() throws IOException {
		print("\t</data>\n");
	}

	private void exportOpenTableData(String tableId) throws IOException {
		print("\t\t<tableData id=\"" + encode(tableId) + "\">\n");
	}

	private void exportCloseTableData() throws IOException {
		print("\t\t</tableData>\n");
	}

	private void exportRowData(Row row) throws IOException, ModuleException {
		print("\t\t\t<row id=\"" + row.getIndex() + "\">\n");
		for (Cell cell : row.getCells()) {
			print("\t\t\t\t<cell id=\"" + encode(cell.getId()) + "\">\n");
			exportCellData(cell);
			print("\t\t\t\t</cell>\n");
		}
		print("\t\t\t</row>\n");
	}

	private void exportCellData(Cell cell) throws IOException, ModuleException {
		String space = "\t\t\t\t\t";
		print(space);
		if (cell instanceof SimpleCell) {
			SimpleCell simple = (SimpleCell) cell;
			if (simple.getSimpledata() != null) {
				print("<s>");
				print(encode(simple.getSimpledata()));
				print("</s>\n");
			} else {
				print("<s xsi:nil=\"true\"/>\n");
			}

		} else if (cell instanceof ComposedCell) {
			ComposedCell composed = (ComposedCell) cell;
			if (composed.getComposedData() != null) {
				print("<c>\n");
				for (Cell subCell : composed.getComposedData()) {
					exportCellData(subCell);
				}
				print(space + "</c>\n");
			} else {
				print("<c xsi:nil=\"true\"/>\n");
			}
		} else if (cell instanceof BinaryCell) {
			BinaryCell bin = (BinaryCell) cell;
			if (bin.getInputstream() != null) {
				String path = dbmlBinaryCreate.createBinaryPath(bin);
				File binFile = dbmlBinaryCreate.createBinaryFile(baseDir, bin,
						path);
				FileHelper.copy(bin.getInputstream(), binFile);
				print("<b file=\"" + encode(path) + "\"");
				if (bin.getFormatHits().size() > 0) {
					FileFormat format = bin.getFormatHits().get(0);
					if (format.getMimetype() != null
							&& format.getMimetype().length() > 0) {
						print(" formatRegistryName=\"MIME\" formatRegistryKey=\""
								+ encode(format.getMimetype()) + "\"");
					} else if (format.getPuid() != null
							&& format.getPuid().length() > 0) {
						print(" formatRegistryName=\"PRONOM\" formatRegistryKey=\""
								+ encode(format.getPuid()) + "\"");
					}
				}
				print("/>\n");

				bin.cleanResources();
			} else {
				print("<b xsi:nil=\"true\"/>\n");
			}
		}
	}

	/**
	 * Get current date and time
	 * 
	 * @return the date in ISO 8601 format, with no milliseconds
	 */
	private String getCurrentDate() {
		Date date = new Date();
		return DateParser.getIsoDateNoMillis(date);
	}

	private void print(String s) throws IOException {
		dbmlOutput.write(s);
	}

	private void finishPrinting() throws IOException {
		dbmlOutput.flush();
		dbmlOutput.close();
	}

	/**
	 * @see XmlEncodeUtility#encode(String)
	 */
	private String encode(String s) {
		return s != null ? XmlEncodeUtility.encode(s) : null;
	}
}
