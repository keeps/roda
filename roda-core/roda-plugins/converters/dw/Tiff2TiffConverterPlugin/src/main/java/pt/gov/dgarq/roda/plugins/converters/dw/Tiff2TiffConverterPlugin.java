package pt.gov.dgarq.roda.plugins.converters.dw;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.DownloaderException;
import pt.gov.dgarq.roda.core.data.RepresentationFile;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;
import pt.gov.dgarq.roda.plugins.converters.common.LocalRepresentationObject;
import pt.gov.dgarq.roda.plugins.converters.common.RepresentationConverterException;
import pt.gov.dgarq.roda.util.CommandException;
import pt.gov.dgarq.roda.util.CommandUtility;

/**
 * @author Rui Castro
 */
public class Tiff2TiffConverterPlugin extends AbstractRodaMigratorPlugin {
	private static Logger logger = Logger
			.getLogger(Tiff2TiffConverterPlugin.class);

	private final String name = "Converter/TIFF to TIFF";
	private final float version = 1.0f;
	private final String description = "Digitalized Work TIFF representations normalizer plugin. "
			+ "Convert DigitalizedWork representations with TIFF images to uncompressed TIFFs using RODA Migrator service DW2Tiff.";

	/**
	 * 
	 * @see Plugin#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @see Plugin#getVersion()
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * @see Plugin#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see AbstractRodaMigratorPlugin#isRepresentationConverted(RepresentationObject)
	 */
	@Override
	public boolean isRepresentationConverted(RepresentationObject object)
			throws RepresentationConverterException {

		// Download images and verify they are TIFF uncompressed

		try {

			boolean isConverted = true;

			LocalRepresentationObject localRObject = downloadRepresentationToLocalDisk(object);

			if (localRObject.getPartFiles() != null) {

				for (RepresentationFile rFile : localRObject.getPartFiles()) {

					if (getImageCompression(new File(URI.create(rFile
							.getAccessURL()))) != null) {
						isConverted = false;
						break;
					}
				}

			} else {
				isConverted = true;
			}

			// Cleanup resources
			FileUtils.deleteDirectory(localRObject.getDirectory());

			return isConverted;

		} catch (DownloaderException e) {
			throw new RepresentationConverterException(e.getMessage(), e);
		} catch (IOException e) {
			throw new RepresentationConverterException(e.getMessage(), e);
		} catch (CommandException e) {
			throw new RepresentationConverterException(e.getMessage(), e);
		}

	}

	@Override
	protected String getConfigurationFile() {
		return "tiff2tiff-converter.properties";
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#getRepresentationType ()
	 */
	@Override
	protected String getRepresentationType() {
		return RepresentationObject.DIGITALIZED_WORK;
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#isNormalization()
	 */
	@Override
	protected boolean isNormalization() {
		return true;
	}

	/**
	 * Get image compression or <code>null</code> if the image is not
	 * compressed.
	 * 
	 * @param image
	 *            the image file
	 * 
	 * @return the compression.
	 * 
	 * @throws CommandException
	 */
	private String getImageCompression(File image) throws CommandException {
		String compression = CommandUtility.execute(new String[] { "identify", //$NON-NLS-1$
				"-format", "%C", image.getAbsolutePath() }); //$NON-NLS-1$ //$NON-NLS-2$
		// remove trailing new line
		compression = compression.substring(0, compression.length() - 1);

		if ("None".equals(compression)) { //$NON-NLS-1$
			compression = null;
		}

		return compression;
	}

}
