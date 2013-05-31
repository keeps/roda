package pt.gov.dgarq.roda.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.util.StreamUtility;
import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.OutputHandler;

/**
 * @author Rui Castro
 */
public class JhoveUtility {

	private static Logger logger = Logger.getLogger(JhoveUtility.class);

	public static String inspect(File targetFile) throws JhoveUtilityException,
			FileNotFoundException {

		if (targetFile == null || !targetFile.isFile() || !targetFile.exists()) {
			logger.warn("target file '" + targetFile + "' cannot be found.");
			throw new FileNotFoundException("target file '" + targetFile
					+ "' cannot be found.");
		}

		Calendar calendar = Calendar.getInstance();

		App app = new App(JhoveUtility.class.getSimpleName(), "1.0", new int[] {
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH) },
				"Format Identification Utility", "");

		try {

			JhoveBase jhoveBase = new JhoveBase();

			File jhoveConfigFile = createJhoveConfigurationFile();
			// System.setProperty("edu.harvard.hul.ois.jhove.saxClass", );

			jhoveBase.init(jhoveConfigFile.getAbsolutePath(), null);

			File outputFile = File.createTempFile("jhove", "output");
			logger.debug("JHOVE output file " + outputFile);

			Module module = jhoveBase.getModule(null);
			OutputHandler aboutHandler = jhoveBase.getHandler(null);
			OutputHandler xmlHandler = jhoveBase.getHandler("XML");

			logger.debug("Calling JHOVE dispatch(...) on file " + targetFile);

			jhoveBase.dispatch(app, module, aboutHandler, xmlHandler,
					outputFile.getAbsolutePath(), new String[] { targetFile
							.getAbsolutePath() });

			logger.debug("JHOVE dispatch(...) finished processing the file");

			FileInputStream outputFileInputStream = new FileInputStream(
					outputFile);

			String output = StreamUtility
					.inputStreamToString(outputFileInputStream);

			logger.debug("JHOVE output read to string of size "
					+ output.length());

			outputFileInputStream.close();
			jhoveConfigFile.delete();
			outputFile.delete();
			

			// logger.debug("Fixing MIX namespace in JHOVE output");
			// output = fixMixNamespaceInJhoveOutput(output);
			// logger.debug("JHOVE output fixed. Returning...");

			return output;

		} catch (Exception e) {
			logger.warn("Error inspecting file '" + targetFile + "' - "
					+ e.getMessage(), e);
			throw new JhoveUtilityException("Error inspecting file '"
					+ targetFile + "' - " + e.getMessage(), e);
		}

	}

	/**
	 * The current version of Jhove (1.1) has a problem with the namespace of
	 * MIX. It uses namespace http://www.loc.gov/mix/ for schema
	 * http://www.loc.gov/mix/mix.xsd when http://www.loc.gov/mix/v20 should be
	 * used.
	 * 
	 * @param jhoveOutput
	 *            the jhove output.
	 * 
	 * @return a {@link String} with the Jhove output fixed.
	 */
	private static String fixMixNamespaceInJhoveOutput(String jhoveOutput) {
		String fixedOutput = jhoveOutput.replaceAll(
				"xmlns:mix=\"http://www.loc.gov/mix/\"",
				"xmlns:mix=\"http://www.loc.gov/mix/v20\"");

		fixedOutput = fixedOutput.replaceAll(
				"http://www.loc.gov/mix/ http://www.loc.gov/mix/mix.xsd",
				"http://www.loc.gov/mix/v20 http://www.loc.gov/mix/mix.xsd");

		return fixedOutput;
	}

	/**
	 * Copy the Jhove configuration file to a temporary file.
	 * 
	 * @return the {@link File} where the Jhove configuration was saved.
	 * 
	 * @throws IOException
	 */
	private static File createJhoveConfigurationFile() throws IOException {
		InputStream jhoveConfInputStream = JhoveUtility.class
				.getResourceAsStream("jhove.conf");
		File jhoveConfFile = File.createTempFile("jhove", "conf");
		FileOutputStream jhoveConfOutputStream = new FileOutputStream(
				jhoveConfFile);

		IOUtils.copy(jhoveConfInputStream, jhoveConfOutputStream);

		jhoveConfInputStream.close();
		jhoveConfOutputStream.close();

		logger.debug("JHOVE configuration file " + jhoveConfFile);

		return jhoveConfFile;
	}

}
