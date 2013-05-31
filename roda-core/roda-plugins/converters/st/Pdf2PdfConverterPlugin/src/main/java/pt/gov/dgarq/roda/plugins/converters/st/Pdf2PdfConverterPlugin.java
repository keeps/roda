package pt.gov.dgarq.roda.plugins.converters.st;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.core.plugins.Plugin;
import pt.gov.dgarq.roda.plugins.converters.common.AbstractRodaMigratorPlugin;
import pt.gov.dgarq.roda.plugins.converters.common.RepresentationConverterException;

/**
 * @author Rui Castro
 */
public class Pdf2PdfConverterPlugin extends AbstractRodaMigratorPlugin {
	private static Logger logger = Logger
			.getLogger(Pdf2PdfConverterPlugin.class);

	private final String name = "Converter/PDF to PDF/A";
	private final float version = 1.0f;
	private final String description = "StructuredText PDF representations normalizer plugin. "
			+ "Convert StructuredText representations from PDF to PDF using RODA Migrator service ST2Pdf.";

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

	@Override
	protected String getConfigurationFile() {
		return "pdf2pdf-converter.properties";
	}

	/**
	 * 
	 * @see AbstractRodaMigratorPlugin#getRepresentationType ()
	 */
	@Override
	protected String getRepresentationType() {
		return RepresentationObject.STRUCTURED_TEXT;
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
	 * @see AbstractRodaMigratorPlugin#isRepresentationConverted(RepresentationObject)
	 */
	@Override
	public boolean isRepresentationConverted(RepresentationObject object)
			throws RepresentationConverterException {
		return true;
	}

}
