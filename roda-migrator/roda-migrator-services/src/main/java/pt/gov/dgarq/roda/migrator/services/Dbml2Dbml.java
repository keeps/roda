package pt.gov.dgarq.roda.migrator.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.convert.db.model.exception.InvalidDataException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.ModuleException;
import pt.gov.dgarq.roda.common.convert.db.model.exception.UnknownTypeException;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseHandler;
import pt.gov.dgarq.roda.common.convert.db.modules.DatabaseImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.in.DBMLImportModule;
import pt.gov.dgarq.roda.common.convert.db.modules.dbml.out.DBMLExportModule;
import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.common.RODAServiceException;
import pt.gov.dgarq.roda.core.data.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.migrator.common.ConverterException;
import pt.gov.dgarq.roda.migrator.common.InvalidRepresentationException;
import pt.gov.dgarq.roda.migrator.common.RepresentationAlreadyConvertedException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationSubtypeException;
import pt.gov.dgarq.roda.migrator.common.WrongRepresentationTypeException;
import pt.gov.dgarq.roda.migrator.common.data.ConversionResult;
import pt.gov.dgarq.roda.migrator.common.data.LocalRepresentationObject;
import pt.gov.dgarq.roda.util.TempDir;

/**
 * @author Luis Faria
 * 
 */
public class Dbml2Dbml extends AbstractSynchronousConverter {

	private static final Logger logger = Logger.getLogger(Dbml2Dbml.class);

	private static final String VERSION = "1.0";

	/**
	 * @throws RODAServiceException
	 */
	public Dbml2Dbml() throws RODAServiceException {
		super();
	}

	/**
	 * @throws ConverterException
	 * 
	 * @see SynchronousConverter#convert(RepresentationObject)
	 */
	public ConversionResult convert(RepresentationObject representation)
			throws RepresentationAlreadyConvertedException,
			InvalidRepresentationException, WrongRepresentationTypeException,
			WrongRepresentationSubtypeException, ConverterException {

		UUID uuid = UUID.randomUUID();
		File finalDirectory = new File(getCacheDirectory(), uuid.toString());
		String report = "";

		try {

			LocalRepresentationObject localRepresentation = downloadRepresentationToLocalDisk(representation);

			logger.trace("Representation downloaded " + localRepresentation);

			File tempDirectory = TempDir.createUniqueDirectory("convertedRep");

			// Create a new RepresentationObject that is a copy of source
			// RepresentationObject
			LocalRepresentationObject convertedRepresentation = new LocalRepresentationObject(
					tempDirectory, localRepresentation);

			logger.debug("Saving converted representation files to "
					+ tempDirectory);
			File dbmlFile = new File(URI.create(localRepresentation
					.getRootFile().getAccessURL()));

			DatabaseImportModule dbmlImportModule = new DBMLImportModule(
					dbmlFile.getParentFile(), dbmlFile.getName());
			DatabaseHandler dbmlExportModule = new DBMLExportModule(
					tempDirectory, dbmlFile.getName());

			dbmlImportModule.getDatabase(dbmlExportModule);

			moveToFinalDirectory(convertedRepresentation, finalDirectory);

			// Set the size of the new root file
			RepresentationFile rootFile = convertedRepresentation.getRootFile();
			File file = new File(finalDirectory, rootFile.getId());
			rootFile.setSize(file.length());

			EventPreservationObject eventPO = new EventPreservationObject();
			eventPO.setOutcome("success");
			eventPO.setOutcomeDetailNote("converter outcome details");
			eventPO.setOutcomeDetailExtension(report);
			logger.info("Event is " + eventPO);

			return new ConversionResult(convertedRepresentation, eventPO,
					getAgent());

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
