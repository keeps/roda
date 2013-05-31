/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.modules.dbml.in;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import pt.gov.dgarq.roda.common.FileFormat;
import pt.gov.dgarq.roda.common.convert.db.model.data.BinaryCell;
import pt.gov.dgarq.roda.common.convert.db.model.data.Cell;
import pt.gov.dgarq.roda.common.convert.db.model.data.ComposedCell;
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
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeEnumeration;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeInterval;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericApproximate;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeNumericExact;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.SimpleTypeString;
import pt.gov.dgarq.roda.common.convert.db.model.structure.type.Type;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;

/**
 * @author Luis Faria
 * 
 */
public class DBMLImportModule implements DatabaseImportModule {

	/**
	 * The default DBML file name
	 */
	public static final String DBML_DEFAULT_FILE_NAME = "DBML.xml";

	private static final String SCHEMA_VERSION = "0.2";

	// private static final String ENCODING = "UTF-8";

	private final Logger logger = Logger.getLogger(DBMLImportModule.class);

	private SAXParser saxParser;

	private InputStream dbml;

	/**
	 * Interface to handle the binary inputstream lookup
	 * 
	 * @author Luis Faria
	 */
	public interface DBMLBinaryLookup {
		/**
		 * Lookup a binary inputstream
		 * 
		 * @param id
		 *            the binary id
		 * @return the binary inputstream
		 * @throws ModuleException
		 */
		public InputStream getBinary(String id) throws ModuleException;
	}

	private DBMLBinaryLookup binLookup;

	/**
	 * DBML import module constructor
	 * 
	 * @param dbml
	 *            the DBML file inputstream
	 * @param binLookup
	 *            the interface to lookup binaries referenced in the DBML
	 * @throws ModuleException
	 */
	public DBMLImportModule(InputStream dbml, DBMLBinaryLookup binLookup)
			throws ModuleException {
		init(dbml, binLookup);
	}

	/**
	 * DBML import modile constructor using a base directory
	 * 
	 * @param baseDir
	 *            the base directory, all files are inside this directory, the
	 *            DMBL file should have the default name
	 * @throws ModuleException
	 */
	public DBMLImportModule(final File baseDir) throws ModuleException {
		this(baseDir, DBML_DEFAULT_FILE_NAME);
	}

	/**
	 * DBML import modile constructor using a base directory and specifying the
	 * DBML file name
	 * 
	 * @param baseDir
	 *            the base directory, all files are inside this directory
	 * @param dbmlFileName
	 *            the DBML file name
	 * @throws ModuleException
	 */
	public DBMLImportModule(final File baseDir, final String dbmlFileName)
			throws ModuleException {
		if (baseDir.isDirectory()) {
			File[] dbmlFile = baseDir.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.equals(dbmlFileName);
				}

			});

			if (dbmlFile.length > 0) {
				try {
					InputStream dbmlStream = new FileInputStream(dbmlFile[0]);

					init(dbmlStream, new DBMLBinaryLookup() {

						public InputStream getBinary(final String id)
								throws ModuleException {
							File[] files = baseDir
									.listFiles(new FilenameFilter() {

										public boolean accept(File dir,
												String name) {
											return name.equals(id);
										}

									});
							try {
								return files.length > 0 ? new FileInputStream(
										files[0]) : null;
							} catch (FileNotFoundException e) {
								throw new ModuleException(
										"Could not find file", e);
							}
						}

					});

				} catch (FileNotFoundException e) {
					throw new ModuleException("Could not find file", e);
				}

			} else {
				throw new ModuleException(dbmlFileName
						+ " file was not found under "
						+ baseDir.getAbsolutePath());
			}

		} else {
			throw new ModuleException("Base dir is not a directory: "
					+ baseDir.getAbsolutePath());
		}
	}

	protected void init(InputStream dbml, DBMLBinaryLookup binLookup)
			throws ModuleException {
		this.dbml = dbml;
		this.binLookup = binLookup;

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		try {
			saxParser = saxParserFactory.newSAXParser();
		} catch (SAXException e) {
			throw new ModuleException("Error initializing SAX parser", e);
		} catch (ParserConfigurationException e) {
			throw new ModuleException("Error initializing SAX parser", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pt.gov.dgarq.roda.common.convert.db.Import.DatabaseImportModule#getDatabase
	 * ()
	 */
	public void getDatabase(DatabaseHandler handler)
			throws UnknownTypeException, ModuleException {
		DBMLSAXHandler dbmlSAXHandler = new DBMLSAXHandler(binLookup, handler);
		try {
			saxParser.parse(dbml, dbmlSAXHandler);
			if (dbmlSAXHandler.getErrors().size() > 0) {
				throw new ModuleException(dbmlSAXHandler.getErrors());
			}
		} catch (SAXException e) {
			throw new ModuleException("Error parsing DBML", e);
		} catch (IOException e) {
			throw new ModuleException("Error reading DBML", e);
		}
	}

	/**
	 * The SAX handler for DBML files
	 * 
	 * @author Luis Faria
	 */
	public class DBMLSAXHandler extends DefaultHandler {

		private final Set<String> types = new HashSet<String>();

		private DBMLBinaryLookup binLookup;

		private DatabaseHandler handler;

		private DatabaseStructure structure;

		private Map<String, Throwable> errors;

		// scope variables
		private TableStructure currentTableStructure;

		private ColumnStructure currentColumnStructure;

		private List<Type> currentType;

		private String currentTypeDescription;

		private String currentTypeOriginalName;

		private PrimaryKey currentPKey;

		private String currentTableDataId;

		private Row currentRow;

		private String currentCellId;

		private List<Cell> currentCell;

		private SimpleCell currentSimpleCell;

		private boolean currentCellIsNull;

		/**
		 * The DBML Sax handler constructor
		 * 
		 * @param binLookup
		 *            the interface to lookup binary inputstreams referenced by
		 *            the DBML
		 * @param handler
		 *            the database handler that should be called when parsing
		 *            the DBML
		 */
		public DBMLSAXHandler(DBMLBinaryLookup binLookup,
				DatabaseHandler handler) {
			this.binLookup = binLookup;
			this.handler = handler;
			structure = null;
			errors = new TreeMap<String, Throwable>();

			// scope variables
			currentTableStructure = null;
			currentColumnStructure = null;
			currentType = null;
			currentTypeDescription = null;
			currentTypeOriginalName = null;
			currentPKey = null;
			currentTableDataId = null;
			currentRow = null;
			currentCellId = null;
			currentCell = null;
			currentSimpleCell = null;
			currentCellIsNull = false;

			// types
			types.add("simpleTypeString");
			types.add("simpleTypeNumericExact");
			types.add("simpleTypeNumericApproximate");
			types.add("simpleTypeBoolean");
			types.add("simpleTypeEnumeration");
			types.add("simpleTypeEnumerationOption");
			types.add("simpleTypeDateTime");
			types.add("simpleTypeInterval");
			types.add("simpleTypeBinary");
			types.add("composedTypeArray");
			types.add("composedTypeStructure");
		}

		/**
		 * Get all the errors that occured while parsing the DBML file and
		 * sending to the database handler
		 * 
		 * @return A map of errors, where the key is the erros message and the
		 *         value is the exception or null if there was no exception
		 */
		public Map<String, Throwable> getErrors() {
			return errors;
		}

		public void startDocument() {
			try {
				handler.initDatabase();
			} catch (ModuleException e) {
				errors.put("Error in document start", e);
			}
		}

		public void endDocument() {
			try {
				handler.finishDatabase();
			} catch (ModuleException e) {
				errors.put("Error in document end", e);
			}
		}

		public void startElement(String uri, String localName, String qname,
				Attributes attr) {
			if (qname.equals("db")) {
				if (structure != null) {
					errors.put("unexpected element: <db>", null);
				} else {
					createStructure(attr);
				}
			} else if (qname.equals("structure")) {
				// nothing to do
			} else if (qname.equals("table")) {
				TableStructure table = createTableStructure(attr);
				structure.getTables().add(table);
				if (currentTableStructure != null) {
					errors.put("table not closed: "
							+ currentTableStructure.getId(), null);
				}
				// TODO check if current column and type are null
				currentTableStructure = table;
			} else if (qname.equals("columns")) {
				// nothing to do
			} else if (qname.equals("column")) {
				ColumnStructure column = createColumnStructure(attr);
				if (currentTableStructure != null) {
					currentTableStructure.getColumns().add(column);
					if (currentColumnStructure != null) {
						errors.put("column not closed: "
								+ currentColumnStructure.getId(), null);
					}
					currentColumnStructure = column;
				}
			} else if (qname.equals("type")) {
				if (currentType != null) {
					errors.put("type not closed: " + currentType, null);
				}
				currentType = new Vector<Type>();
				currentTypeDescription = attr.getValue("description");
				currentTypeOriginalName = attr.getValue("originalTypeName");
			} else if (types.contains(qname)) {
				Type type = createType(qname, attr);
				if (currentType.size() == 0) {
					type.setDescription(currentTypeDescription);
					type.setOriginalTypeName(currentTypeOriginalName);
				} else if (currentType.size() > 1) {
					Type tailType = currentType.get(currentType.size() - 1);
					if (tailType instanceof ComposedTypeArray) {
						((ComposedTypeArray) tailType).setElementType(type);
					} else if (tailType instanceof ComposedTypeStructure) {
						((ComposedTypeStructure) tailType).getElements().add(
								type);
					}
				}
				currentType.add(type);
			} else if (qname.equals("keys")) {
				// nothing to do
			} else if (qname.equals("pkey")) {
				PrimaryKey pkey = new PrimaryKey();
				if (currentTableStructure != null) {
					currentTableStructure.setPrimaryKey(pkey);
					if (currentPKey != null) {
						errors.put("pkey not closed", null);
					}
					currentPKey = pkey;
				} else {
					errors
							.put("pkey found outside table structure scope",
									null);
				}
			} else if (qname.equals("field")) {
				String fieldName = attr.getValue("name");
				if (currentPKey != null) {
					currentPKey.getColumnNames().add(fieldName);
				} else {
					errors.put("Field found outside pkey scope: " + fieldName,
							null);
				}
			} else if (qname.equals("fkey")) {
				ForeignKey fkey = createForeignKey(attr);
				if (currentTableStructure != null) {
					currentTableStructure.getForeignKeys().add(fkey);
				} else {
					errors
							.put("fkey found outside table structure scope",
									null);
				}
			} else if (qname.equals("data")) {
				// nothing to do
			} else if (qname.equals("tableData")) {
				String tableDataId = attr.getValue("id");
				logger.debug("importing data of table " + tableDataId);
				if (currentTableDataId != null) {
					errors.put("tableData " + tableDataId
							+ " opened without tableData " + currentTableDataId
							+ " being closed", null);
					try {
						handler.handleDataCloseTable(currentTableDataId);
					} catch (ModuleException e) {
						errors.put("Error handling close of table "
								+ currentTableDataId, e);
					}
				}
				try {
					handler.handleDataOpenTable(tableDataId);
					currentTableDataId = tableDataId;
				} catch (ModuleException e) {
					errors
							.put("Error handling open of table " + tableDataId,
									e);
				}

			} else if (qname.equals("row")) {
				Row row = new Row();
				row.setIndex(parseInteger(attr.getValue("id")).intValue());
				if (currentRow != null) {
					errors.put("Row not closed, index: "
							+ currentRow.getIndex(), null);
				}
				currentRow = row;
			} else if (qname.equals("cell")) {
				if (currentCell != null || currentCellId != null) {
					errors.put("Cell not closed: " + currentCellId, null);
				}
				currentCellId = attr.getValue("id");
				currentCell = new Vector<Cell>();
			} else if (qname.equals("s")) {
				if (currentCell != null && currentCellId != null) {
					if (currentSimpleCell != null) {
						errors.put("Simple cell not closed: " + currentCellId,
								null);
					}
					currentSimpleCell = new SimpleCell(currentCellId);
					currentCell.add(currentSimpleCell);
					currentCellIsNull = attr.getValue("xsi:nil") != null
							&& attr.getValue("xsi:nil").equals("true");

				} else {
					errors.put("Simple cell element with no cell id defined"
							+ " in scope", null);
				}
			} else if (qname.equals("c")) {
				if (currentCell != null && currentCellId != null) {
					currentCell.add(new ComposedCell(currentCellId));
				}
			} else if (qname.equals("b")) {
				if (currentCell != null && currentCellId != null) {
					BinaryCell b;
					String fileName = attr.getValue("file");
					String formatRegistryName = attr
							.getValue("formatRegistryName");
					String formatRegistryKey = attr
							.getValue("formatRegistryKey");
					List<FileFormat> fileFormats = new Vector<FileFormat>();
					if (formatRegistryName != null
							&& formatRegistryName.equals("MIME")) {
						fileFormats.add(new FileFormat(null, null, null,
								formatRegistryKey, null));
					} else if (formatRegistryName != null
							&& formatRegistryName.equals("PRONOM")) {
						fileFormats.add(new FileFormat(null, null,
								formatRegistryKey, null, null));
					}
					try {
						InputStream stream = binLookup.getBinary(fileName);
						FileItem fileItem = stream != null ? new FileItem(
								stream) : null;
						b = new BinaryCell(currentCellId, fileItem, fileFormats);
						currentCell.add(b);
					} catch (ModuleException e) {
						errors.put("Error looking up binary", e);
					}
				} else {
					errors.put("binary cell outside cell context", null);
				}

			} else {
				errors.put("Unrecognized element opened: " + qname, null);
			}
		}

		public void characters(char buf[], int offset, int len)
				throws SAXException {
			if (currentSimpleCell != null) {
				String append = currentSimpleCell.getSimpledata();
				if (append == null) {
					append = "";
				}
				// currentSimpleCell.setSimpledata(append
				// + unencode(new String(buf, offset, len)));
				currentSimpleCell.setSimpledata(append
						+ new String(buf, offset, len));
			}
		}

		public void ignorableWhitespace(char[] ch, int start, int length)
				throws SAXException {
			if (currentSimpleCell != null) {
				logger
						.warn("found ignorable whitespace inside a simple cell: '"
								+ new String(ch, start, length) + "'");
			}
		}

		public void endElement(String uri, String localName, String qname)
				throws SAXException {
			if (qname.equals("db")) {
				// nothing to do
			} else if (qname.equals("structure")) {
				try {
					handler.handleStructure(structure);
				} catch (ModuleException e) {
					errors.put("Error handling structure", e);

				} catch (UnknownTypeException e) {
					errors.put("Error handling structure", e);
				}
			} else if (qname.equals("table")) {
				currentTableStructure = null;
			} else if (qname.equals("columns")) {
				// nothing to do
			} else if (qname.equals("column")) {
				currentColumnStructure = null;
			} else if (qname.equals("type")) {
				currentType = null;
				currentTypeDescription = null;
				currentTypeOriginalName = null;
			} else if (types.contains(qname)) {
				if (currentType != null) {
					if (currentType.size() == 1
							&& currentColumnStructure != null) {
						currentColumnStructure.setType(currentType.get(0));
					}
					currentType.remove(currentType.size() - 1);
				} else {
					errors
							.put("Type closed with no type opened in scope",
									null);
				}
			} else if (qname.equals("keys")) {
				// nothing to do
			} else if (qname.equals("pkey")) {
				currentPKey = null;
			} else if (qname.equals("field")) {
				// nothing to do
			} else if (qname.equals("fkey")) {
				// nothing to do
			} else if (qname.equals("data")) {
				// nothing to do
			} else if (qname.equals("tableData")) {
				try {
					handler.handleDataCloseTable(currentTableDataId);
				} catch (ModuleException e) {
					errors.put("Error closing table", null);
				}
				currentTableDataId = null;
			} else if (qname.equals("row")) {
				if (currentRow != null) {
					try {
						handler.handleDataRow(currentRow);
					} catch (InvalidDataException e) {
						errors.put("Error handling row "
								+ currentRow.getIndex() + " of table "
								+ currentTableDataId, e);
					} catch (ModuleException e) {
						errors.put("Error handling row "
								+ currentRow.getIndex() + " of table "
								+ currentTableDataId, e);
					}
					currentRow = null;
				} else {
					errors.put("row closed without being opened", null);
				}

			} else if (qname.equals("cell")) {
				if (currentCell != null && currentCell.size() == 1) {
					if (currentRow != null) {
						currentRow.getCells().add(currentCell.get(0));
					} else {
						errors.put("Cell outside row context", null);
					}
				}
				currentCellId = null;
				currentCell = null;
			} else if (qname.equals("s")) {
				if (currentCell != null) {
					if (currentSimpleCell.getSimpledata() == null
							&& !currentCellIsNull) {
						currentSimpleCell.setSimpledata("");
					}
					if (currentCell.size() == 1) {
						if (currentCell.get(0) == currentSimpleCell) {
							currentSimpleCell = null;
						} else {
							errors.put("current cell inconsistent", null);
						}
					} else if (currentCell.size() > 1) {
						Cell parentCell = currentCell
								.get(currentCell.size() - 2);
						if (parentCell instanceof ComposedCell) {
							((ComposedCell) parentCell).getComposedData().add(
									currentSimpleCell);
						} else {
							errors.put("Non composed cell with subcells: "
									+ parentCell.getId(), null);
						}
					} else {
						errors.put("simple cell outside cell context", null);
					}
				} else {
					errors.put("simple cell outside cell context", null);
				}
			} else if (qname.equals("c")) {
				if (currentCell.size() == 1) {
					// do nothing
				} else if (currentCell.size() > 1) {
					Cell parentCell = currentCell.get(currentCell.size() - 2);
					Cell childCell = currentCell.remove(currentCell.size() - 1);
					if (parentCell instanceof ComposedCell) {
						((ComposedCell) parentCell).getComposedData().add(
								childCell);
					} else {
						errors.put("Non composed cell with subcells: "
								+ parentCell.getId(), null);
					}
				} else {
					errors.put("Composed cell outside cell context", null);
				}
			} else if (qname.equals("b")) {
				if (currentCell.size() == 1) {
					// do nothing
				} else if (currentCell.size() > 1) {
					Cell parentCell = currentCell.get(currentCell.size() - 2);
					Cell childCell = currentCell.remove(currentCell.size() - 1);
					if (parentCell instanceof ComposedCell) {
						((ComposedCell) parentCell).getComposedData().add(
								childCell);
					} else {
						errors.put("Non composed cell with subcells: "
								+ parentCell.getId(), null);
					}
				} else {
					errors.put("Binary cell outside cell context", null);
				}
			} else {
				errors.put("Unrecognized element closed: " + qname, null);
			}
		}

		private void createStructure(Attributes attr) {
			structure = new DatabaseStructure();
			structure.setName(attr.getValue("name"));
			structure.setCreationDate(attr.getValue("creationDate"));
			structure.setProductName(attr.getValue("productName"));
			structure.setProductVersion(attr.getValue("productVersion"));
			structure.setDefaultTransactionIsolationLevel(parseInteger(attr
					.getValue("defaultTransactionIsolationLevel")));
			structure.setExtraNameCharacters(attr
					.getValue("extraNameCharacters"));
			structure.setStringFunctions(attr.getValue("stringFunctions"));
			structure.setSystemFunctions(attr.getValue("systemFunctions"));
			structure.setTimeDateFunctions(attr.getValue("timeDateFunctions"));
			structure.setUrl(attr.getValue("url"));
			structure.setSupportsANSI92EntryLevelSQL(parseBoolean(attr
					.getValue("supportsANSI92EntryLevelSQL")));
			structure.setSupportsANSI92IntermediateSQL(parseBoolean(attr
					.getValue("supportsANSI92IntermediateSQL")));
			structure.setSupportsANSI92FullSQL(parseBoolean(attr
					.getValue("supportsANSI92FullSQL")));
			structure.setSupportsCoreSQLGrammar(parseBoolean(attr
					.getValue("supportsCoreSQLGrammar")));
			if (!attr.getValue("schemaVersion").equals(SCHEMA_VERSION)) {
				errors.put("Schema version is different from the supported "
						+ attr.getValue("schemaVersion") + "!="
						+ SCHEMA_VERSION, null);
			}
		}

		private TableStructure createTableStructure(Attributes attr) {
			TableStructure table = new TableStructure();
			table.setId(attr.getValue("id"));
			table.setName(attr.getValue("name"));
			table.setDescription(attr.getValue("description"));
			return table;
		}

		private ColumnStructure createColumnStructure(Attributes attr) {
			ColumnStructure column = new ColumnStructure();
			column.setId(attr.getValue("id"));
			column.setName(attr.getValue("name"));
			column.setNillable(parseBoolean(attr.getValue("nillable")));
			column.setDescription(attr.getValue("description"));
			return column;
		}

		private Type createType(String qname, Attributes attr) {
			Type type = null;
			if (qname.equals("simpleTypeString")) {
				type = new SimpleTypeString(parseInteger(attr
						.getValue("length")), parseBoolean(attr
						.getValue("variableLegth")), attr.getValue("charSet"));
			} else if (qname.equals("simpleTypeNumericExact")) {
				type = new SimpleTypeNumericExact(parseInteger(attr
						.getValue("precision")), parseInteger(attr
						.getValue("scale")));
			} else if (qname.equals("simpleTypeNumericApproximate")) {
				type = new SimpleTypeNumericApproximate(parseInteger(attr
						.getValue("precision")));
			} else if (qname.equals("simpleTypeBoolean")) {
				type = new SimpleTypeBoolean();
			} else if (qname.equals("simpleTypeEnumeration")) {
				type = new SimpleTypeEnumeration();
			} else if (qname.equals("simpleTypeEnumerationOption")) {
				String optionName = attr.getValue("name");
				if (currentType.size() > 0
						&& currentType.get(currentType.size() - 1) instanceof SimpleTypeEnumeration) {
					((SimpleTypeEnumeration) currentType
							.get(currentType.size() - 1)).getOptions().add(
							optionName);

				} else {
					errors.put("Enumeration option element must be inside an"
							+ " enumeration element", null);
				}
			} else if (qname.equals("simpleTypeDateTime")) {
				type = new SimpleTypeDateTime(parseBoolean(attr
						.getValue("timeDefined")), parseBoolean(attr
						.getValue("timeZoneDefined")));
			} else if (qname.equals("simpleTypeInterval")) {
				String intervalType = attr.getValue("type");
				if (intervalType.equals("START_END")) {
					type = new SimpleTypeInterval(
							SimpleTypeInterval.IntervalType.STARTDATE_ENDDATE);
				} else if (intervalType.equals("START_DURATION")) {
					type = new SimpleTypeInterval(
							SimpleTypeInterval.IntervalType.STARTDATE_DURATION);
				} else if (intervalType.equals("DURATION_END")) {
					type = new SimpleTypeInterval(
							SimpleTypeInterval.IntervalType.DURATION_ENDDATE);
				} else if (intervalType.equals("DURATION")) {
					type = new SimpleTypeInterval(
							SimpleTypeInterval.IntervalType.DURATION);
				} else {
					errors.put("Wrong interval type value: " + intervalType,
							null);
				}

			} else if (qname.equals("simpleTypeBinary")) {
				type = new SimpleTypeBinary(
						attr.getValue("formatRegistryName"), attr
								.getValue("formatRegistryKey"));
			} else if (qname.equals("composedTypeArray")) {
				type = new ComposedTypeArray();
			} else if (qname.equals("composedTypeStructure")) {
				type = new ComposedTypeStructure();
			} else {
				errors.put("Unrecognized type: " + qname, null);
			}
			return type;
		}

		private ForeignKey createForeignKey(Attributes attr) {
			return new ForeignKey(attr.getValue("id"), attr.getValue("name"),
					attr.getValue("in"), attr.getValue("ref"));
		}

		public void warning(SAXParseException spe) {
			logger.warn("Warning caught while parsing", spe);
		}

		public void fatalError(SAXParseException spe) throws SAXException {
			logger.fatal("Fatal error caught while parsing", spe);
			throw spe;
		}

		private Integer parseInteger(String value) {
			return value != null ? Integer.valueOf(value) : null;
		}

		private Boolean parseBoolean(String value) {
			return value != null ? Boolean.valueOf(value) : null;
		}
	}

}
