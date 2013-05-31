package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.in.DBMLImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.mySql.out.PhpMyAdminExportModule;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.InvalidRepresentationException;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;

/**
 * @author Luis Faria
 * 
 */
public class Dbml2PhpMyAdmin extends AbstractSynchronousConverter {
	private static final Logger logger = Logger
			.getLogger(Dbml2PhpMyAdmin.class);

	private static final String VERSION = "1.0";

	private final String mySqlHost;
	private final int mySqlPort;
	private final String mySqlUser;
	private final String mySqlPass;
	private final String mySqlDatabase;
	private final String mySqlColumnInfo;

	/**
	 * @throws RODAServiceException
	 */
	public Dbml2PhpMyAdmin() throws RODAServiceException {
		super();

		mySqlHost = getConfiguration().getString("phpmyadmin.mysql.host");
		mySqlPort = Integer.valueOf(getConfiguration().getString(
				"phpmyadmin.mysql.port"));
		mySqlUser = getConfiguration().getString("phpmyadmin.mysql.user");
		mySqlPass = getConfiguration().getString("phpmyadmin.mysql.pass");
		mySqlDatabase = getConfiguration().getString(
				"phpmyadmin.mysql.database");
		mySqlColumnInfo = getConfiguration().getString(
				"phpmyadmin.mysql.column_info");
	}

	/**
	 * 
	 * 
	 * @throws ConverterException
	 * 
	 * @see SynchronousConverter#convert(RepresentationObject)
	 */
	public ConversionResult convert(RepresentationObject representation)
			throws RepresentationAlreadyConvertedException,
			InvalidRepresentationException, WrongRepresentationTypeException,
			WrongRepresentationSubtypeException, ConverterException {

		// UUID uuid = UUID.randomUUID();
		// File finalDirectory = new File(getCacheDirectory(), uuid.toString());

		try {

			LocalRepresentationObject localRepresentation = downloadRepresentationToLocalDisk(representation);

			logger.trace("Representation downloaded " + localRepresentation);

			File dbmlFile = new File(URI.create(localRepresentation
					.getRootFile().getAccessURL()));

			DatabaseImportModule dbmlImportModule = new DBMLImportModule(
					dbmlFile.getParentFile(), dbmlFile.getName());
			DatabaseHandler phpMyAdminExportModule = new PhpMyAdminExportModule(
					mySqlHost, mySqlPort, representation.getPid().replace(':',
							'_'), mySqlUser, mySqlPass, mySqlDatabase,
					mySqlColumnInfo);

			dbmlImportModule.getDatabase(phpMyAdminExportModule);
			// the export module connects directly to the database and does not
			// create any files on the file system, so we return null.
			return null;

		} catch (DownloaderException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (IOException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (ModuleException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			if (e.getModuleErrors() != null) {
				for (Entry<String, Throwable> error : e.getModuleErrors()
						.entrySet()) {
					logger.debug(error.getKey(), error.getValue());
				}
			}

			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (UnknownTypeException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		} catch (InvalidDataException e) {
			logger.debug("Exception downloading representation files - "
					+ e.getMessage(), e);
			throw new ConverterException(
					"Exception downloading representation files - "
							+ e.getMessage(), e);
		}

	}

	protected String getVersion() throws ConverterException {

		String version = getClass().getName() + "/" + VERSION + " - ";

		String convertDbVersion = "RODA-Common-Convert-DB";

		return version + convertDbVersion;

	}
}
