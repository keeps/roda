package pt.gov.dgarq.roda.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import pt.gov.dgarq.roda.core.data.FileFormat;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.CommandUtility;
import edu.harvard.hul.ois.fits.FitsOutput;
import edu.harvard.hul.ois.fits.exceptions.FitsException;
import edu.harvard.hul.ois.fits.identity.ExternalIdentifier;
import edu.harvard.hul.ois.fits.identity.FitsIdentity;

public class FITSUtility {
	static final private Logger logger = Logger.getLogger(FITSUtility.class);
	private static String fullProfile = "full";
	private static String identifyProfile = "fastIdentify";

	public static FileFormat execute(File f) throws FitsException {
		try {
			List<String> command = getCommand(identifyProfile);
			command.add(f.getAbsolutePath());
			logger.debug("Executing fits:"+command);
			String fitsOutput = CommandUtility.execute(command);
			fitsOutput = fitsOutput.substring(fitsOutput.indexOf("<?xml"));
			FitsOutput output = new FitsOutput(fitsOutput);

			FileFormat fileFormat = null;
			if (output.getIdentities().size() > 0) {
				for (FitsIdentity identity : output.getIdentities()) {
					fileFormat = new FileFormat();
					fileFormat.setExtensions(new String[] { FilenameUtils
							.getExtension(f.getPath()) });
					fileFormat
							.setFormatRegistryName("PRONOM http://www.nationalarchives.gov.uk/PRONOM");
					fileFormat.setMimetype(identity.getMimetype());
					fileFormat.setName(identity.getFormat());
					if (identity.getExternalIdentifiers() != null) {
						for (ExternalIdentifier externalIdentifier : identity
								.getExternalIdentifiers()) {
							if (externalIdentifier.getName().equalsIgnoreCase(
									"puid")) {
								fileFormat.setPuid(externalIdentifier
										.getValue());
							}
						}
					}
					if (identity.getFormatVersions() != null && identity.getFormatVersions().size()>0) {
						fileFormat.setVersion(identity.getFormatVersions()
								.get(0).getValue());
					}
					break;
				}
			}
			return fileFormat;
		} catch (CommandException e) {
			throw new FitsException("Error while executing FITS command",e);
		} catch (JDOMException e) {
			throw new FitsException("Error while parsing FITS output",e);
		} catch (IOException e) {
			throw new FitsException("Error while parsing FITS output",e);
		}
	}

	public static String inspect(File f) throws FitsException {
		try {
			List<String> command = getCommand(fullProfile);
			command.add(f.getAbsolutePath());
			String fitsOutput = CommandUtility.execute(command);
			fitsOutput = fitsOutput.substring(fitsOutput.indexOf("<?xml"));
			FitsOutput output = new FitsOutput(fitsOutput);
			return new XMLOutputter().outputString(output.getFitsXml());
		} catch (CommandException e) {
			throw new FitsException("Error while executing FITS command");
		} catch (JDOMException e) {
			throw new FitsException("Error while parsing FITS output");
		} catch (IOException e) {
			throw new FitsException("Error while parsing FITS output");
		}
	}

	private static List<String> getCommand(String profile) {
		File RODA_HOME = null;
		if (System.getProperty("roda.home") != null) {
			RODA_HOME = new File(System.getProperty("roda.home"));//$NON-NLS-1$
		} else if (System.getenv("RODA_HOME") != null) {
			RODA_HOME = new File(System.getenv("RODA_HOME")); //$NON-NLS-1$
		} else {
			RODA_HOME = new File("."); //$NON-NLS-1$
		}
		File FITS_DIRECTORY = new File(RODA_HOME, "fits");

		String osName = System.getProperty("os.name");
		List<String> command;
		if (osName.startsWith("Windows")) {
			if (profile == null) {
				command = new ArrayList<String>(Arrays.asList(
						FITS_DIRECTORY.getPath() + File.separator + "fits.bat",
						"-i"));
			} else {
				command = new ArrayList<String>(Arrays.asList(
						FITS_DIRECTORY.getPath() + File.separator + "fits.bat",
						"-p", profile, "-i"));
			}
		} else {
			if (profile == null) {
				command = new ArrayList<String>(Arrays.asList(
						FITS_DIRECTORY.getPath() + File.separator + "fits.sh",
						"-i"));
			} else {
				command = new ArrayList<String>(Arrays.asList(
						FITS_DIRECTORY.getPath() + File.separator + "fits.sh",
						"-p", profile, "-i"));
			}
		}
		return command;
	}
	

}
